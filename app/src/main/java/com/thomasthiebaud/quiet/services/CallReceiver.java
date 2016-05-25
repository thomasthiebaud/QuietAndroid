package com.thomasthiebaud.quiet.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.thomasthiebaud.quiet.R;
import com.thomasthiebaud.quiet.contract.DatabaseContract;
import com.thomasthiebaud.quiet.model.Content;
import com.thomasthiebaud.quiet.model.Message;
import com.thomasthiebaud.quiet.utils.AuthCallback;
import com.thomasthiebaud.quiet.utils.Authentication;
import com.thomasthiebaud.quiet.model.Body;
import com.thomasthiebaud.quiet.app.DetailsActivity;
import com.thomasthiebaud.quiet.contract.IntentContract;
import com.thomasthiebaud.quiet.contract.NotificationContract;
import com.thomasthiebaud.quiet.utils.Widget;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by thomasthiebaud on 5/3/16.
 */
public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = CallReceiver.class.getSimpleName();
    private Context context;
    private boolean wasLastCallOffHooked = false;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Authentication.initialize(context);
        this.context = context;

        NotificationManager mNotifyManager;
        String number = intent.getStringExtra(IntentContract.PHONE_NUMBER);

        switch (intent.getAction()) {
            case IntentContract.PHONE_STATE:
                this.handlePhoneStateChanged();
                break;
            case IntentContract.AD_ACTION:
                mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotifyManager.cancel(NotificationContract.REPORTED_NOTIFICATION_ID);
                this.reportPhone(number, true);
                break;
            case IntentContract.OK_ACTION:
                mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotifyManager.cancel(NotificationContract.REPORTED_NOTIFICATION_ID);
                Widget.safe(context);
                break;
            case IntentContract.SCAM_ACTION:
                mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotifyManager.cancel(NotificationContract.REPORTED_NOTIFICATION_ID);
                this.reportPhone(number, false);
                break;
        }
    }

    private void handlePhoneStateChanged() {
        TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, final String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                NotificationManager notificationManager;
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        Log.d(TAG, "RINGING");
                        handlePhoneRinging(incomingNumber);
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        Log.d(TAG, "OFFHOOK");
                        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(NotificationContract.INCOMING_CALL_NOTIFICATION_ID);
                        wasLastCallOffHooked = true;
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        Log.d(TAG, "IDLE");
                        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(NotificationContract.INCOMING_CALL_NOTIFICATION_ID);

                        Widget.idle(context);

                        if(wasLastCallOffHooked) {
                            createReportNotification(incomingNumber);
                            wasLastCallOffHooked = false;
                        }
                        break;
                }
            }
        },PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void handlePhoneRinging(final String incomingNumber) {
        if(this.numberMatchContact(incomingNumber))
            return;

        Authentication.getInstance().silentSignIn(new AuthCallback() {
            @Override
            public void onSuccess(String idToken) {
                Call<Message> results = HttpService.getInstance().getQuietApi().checkPhoneNumber(idToken, incomingNumber);
                results.enqueue(new Callback<Message>() {
                    @Override
                    public void onResponse(Call<Message> call, final Response<Message> response) {
                        if(response.code() == 200) {
                            final Content content = response.body().getContent();
                            savePhoneIntoDatabase(content);

                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    createIncomingCallNotification(content);
                                }
                            }, 500);
                        }
                    }

                    @Override
                    public void onFailure(Call<Message> call, Throwable t) {
                        Log.e(TAG, t.toString());
                        Content content = readPhoneFromDatabase(incomingNumber);

                        if(content != null)
                            createIncomingCallNotification(content);
                    }
                });
            }

            @Override
            public void onError(int code) {

            }
        });
    }

    private void reportPhone(final String number, final boolean isAd) {
        String idToken = Authentication.getIdToken(context);
        final Body body = new Body()
                .add("idToken", idToken)
                .add("number", number);

        if(isAd)
            body.add("ad", true).add("scam", false);
        else
            body.add("ad", false).add("scam", true);

        Authentication.getInstance().silentSignIn(new AuthCallback() {
            @Override
            public void onSuccess(String idToken) {
                Call<Message> results = HttpService.getInstance().getQuietApi().reportPhoneNumber(body);
                results.enqueue(new Callback<Message>() {
                    @Override
                    public void onResponse(Call<Message> call, Response<Message> response) {
                        Log.e(TAG, response.body().getMessage());
                    }

                    @Override
                    public void onFailure(Call<Message> call, Throwable t) {
                        Log.e(TAG, t.toString());
                    }
                });
            }

            @Override
            public void onError(int code) {

            }
        });
    }

    public void createIncomingCallNotification(final Content content) {
        String number = content.getNumber();
        int ad = content.getAd();
        int scam = content.getScam();
        int score = content.getScore();

        Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra(IntentContract.PHONE_NUMBER, number);
        intent.putExtra(IntentContract.SCORE, score);
        intent.putExtra(IntentContract.AD, ad);
        intent.putExtra(IntentContract.SCAM, scam);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Resources res = context.getResources();
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle().addLine(res.getQuantityString(R.plurals.report_score, score, score));
        if(ad > 0)
            inboxStyle.addLine(res.getQuantityString(R.plurals.report_ad, ad, ad));
        if(scam > 0)
            inboxStyle.addLine(res.getQuantityString(R.plurals.report_scam, scam, scam));

        int icon;
        String contentText;
        if(score <= 0) {
            icon = R.drawable.safe;
            contentText = context.getString(R.string.safe);
        }
        else if(score <= 3 && scam == 0) {
            icon = R.drawable.reported;
            contentText = context.getString(R.string.reported);
        }
        else {
            icon = R.drawable.dangerous;
            contentText = context.getString(R.string.dangerous);
        }

        Widget.ringing(context, number, icon, contentText);

        Notification notification =  new NotificationCompat.Builder(context)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), icon))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.call_from) + number)
                .setContentText(contentText)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pIntent)
                .setStyle(inboxStyle)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NotificationContract.INCOMING_CALL_NOTIFICATION_ID, notification);
    }

    public void createReportNotification(final String incomingNumber) {
        //ad intent
        Intent adReceive = new Intent();
        adReceive.setAction(IntentContract.AD_ACTION);
        adReceive.putExtra(IntentContract.PHONE_NUMBER, incomingNumber);
        PendingIntent pendingIntentAd = PendingIntent.getBroadcast(context, 12345, adReceive, PendingIntent.FLAG_UPDATE_CURRENT);

        //ok intent
        Intent okReceive = new Intent();
        okReceive.setAction(IntentContract.OK_ACTION);
        okReceive.putExtra(IntentContract.PHONE_NUMBER, incomingNumber);
        PendingIntent pendingIntentOk = PendingIntent.getBroadcast(context, 12345, okReceive, PendingIntent.FLAG_UPDATE_CURRENT);

        //scam intent
        Intent scamReceive = new Intent();
        scamReceive.setAction(IntentContract.SCAM_ACTION);
        scamReceive.putExtra(IntentContract.PHONE_NUMBER, incomingNumber);
        PendingIntent pendingIntentScam = PendingIntent.getBroadcast(context, 12345, scamReceive, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.call_feedback))
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .addAction(R.drawable.reported_bw, context.getString(R.string.ad), pendingIntentAd)
                .addAction(R.drawable.safe_bw, context.getString(R.string.ok), pendingIntentOk)
                .addAction(R.drawable.dangerous_bw, context.getString(R.string.scam), pendingIntentScam)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NotificationContract.REPORTED_NOTIFICATION_ID, notification);
    }

    private boolean numberMatchContact(final String number) {
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String[] mPhoneNumberProjection = { ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME };
        Cursor cur = context.getContentResolver().query(lookupUri,mPhoneNumberProjection, null, null, null);
        try {
            if (cur.moveToFirst()) {
                return true;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return false;
    }

    private void savePhoneIntoDatabase(final Content content) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Phone.COLUMN_NUMBER, content.getNumber());
        values.put(DatabaseContract.Phone.COLUMN_SCORE, content.getScore());
        values.put(DatabaseContract.Phone.COLUMN_SCAM, content.getScam());
        values.put(DatabaseContract.Phone.COLUMN_AD, content.getAd());

        context.getContentResolver().insert(
                DatabaseContract.PHONE_CONTENT_URI.buildUpon().appendPath(content.getNumber()).build(),
                values
        );
    }

    private Content readPhoneFromDatabase(final String incomingNumber) {
        Content content = null;
        Cursor cursor = context.getContentResolver().query(
                DatabaseContract.PHONE_CONTENT_URI.buildUpon().appendPath(incomingNumber).build(),
                null,
                DatabaseContract.Phone.COLUMN_NUMBER + "= ?",
                new String[]{incomingNumber},
                null
        );

        try {
            if (cursor.moveToFirst()) {
                content = new Content();
                content.setNumber(cursor.getString(DatabaseContract.Phone.INDEX_NUMBER));
                content.setScore(cursor.getInt(DatabaseContract.Phone.INDEX_SCORE));
                content.setAd(cursor.getInt(DatabaseContract.Phone.INDEX_AD));
                content.setScam(cursor.getInt(DatabaseContract.Phone.INDEX_SCAM));
            } else {
                Log.e(TAG, "Number not cached into the local database");
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return content;
    }
}
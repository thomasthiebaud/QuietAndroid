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
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.thomasthiebaud.quiet.R;
import com.thomasthiebaud.quiet.contract.DatabaseContract;
import com.thomasthiebaud.quiet.model.Content;
import com.thomasthiebaud.quiet.model.Message;
import com.thomasthiebaud.quiet.utils.Body;
import com.thomasthiebaud.quiet.app.DetailsActivity;
import com.thomasthiebaud.quiet.app.DisplayActivity;
import com.thomasthiebaud.quiet.utils.Utils;
import com.thomasthiebaud.quiet.contract.IntentContract;
import com.thomasthiebaud.quiet.contract.NotificationContract;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by thomasthiebaud on 5/3/16.
 */
public class PhoneReceiver extends BroadcastReceiver {
    private static final String TAG = PhoneReceiver.class.getSimpleName();
    private Context context;
    private boolean wasLastCallOffHooked = false;

    @Override
    public void onReceive(final Context context, Intent intent) {
        this.context = context;
        TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, final String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);

                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    Log.d(TAG, "RINGING");
                    phoneRinging(context, incomingNumber);
                }

                if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
                    Log.d(TAG, "OFFHOOK");
                    NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotifyManager.cancel(NotificationContract.INCOMING_CALL_NOTIFICATION_ID);
                    wasLastCallOffHooked = true;
                }

                if (TelephonyManager.CALL_STATE_IDLE == state) {
                    Log.d(TAG, "IDLE");
                    if(wasLastCallOffHooked) {
                        createReportNotification(incomingNumber);
                        wasLastCallOffHooked = false;
                    }
                }
            }
        },PhoneStateListener.LISTEN_CALL_STATE);

        String action = intent.getAction();
        String number = intent.getStringExtra("number");
        if("Ad".equals(action)) {
            Toast.makeText(context, "COLUMN_AD", Toast.LENGTH_SHORT).show();
            NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotifyManager.cancel(NotificationContract.REPORTED_NOTIFICATION_ID);
            this.reportPhone(number, true);
        } else if ("Ok".equals(action)) {
            NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotifyManager.cancel(NotificationContract.REPORTED_NOTIFICATION_ID);
        } else if ("Scam".equals(action)) {
            Toast.makeText(context, "COLUMN_SCAM", Toast.LENGTH_SHORT).show();
            NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotifyManager.cancel(NotificationContract.REPORTED_NOTIFICATION_ID);
            this.reportPhone(number, false);
        }
    }

    private void reportPhone(String number, boolean isAd) {
        String idToken = Utils.getIdToken(context);
        Body body = new Body()
                .add("idToken", idToken)
                .add("number", number);

        if(isAd)
            body.add("ad", true).add("scam", false);
        else
            body.add("ad", false).add("scam", true);

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

    private void phoneRinging(final Context context, final String incomingNumber) {
        if(this.contactExists(context, incomingNumber))
            return;

        String idToken = Utils.getIdToken(context);

        Call<Message> results = HttpService.getInstance().getQuietApi().checkPhoneNumber(idToken, incomingNumber);
        results.enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, final Response<Message> response) {
                if(response.code() == 200) {
                    final Content content = response.body().getContent();

                    ContentValues values = new ContentValues();
                    values.put(DatabaseContract.Phone.COLUMN_NUMBER, content.getNumber());
                    values.put(DatabaseContract.Phone.COLUMN_SCORE, content.getScore());
                    values.put(DatabaseContract.Phone.COLUMN_SCAM, content.getScam());
                    values.put(DatabaseContract.Phone.COLUMN_AD, content.getAd());

                    context.getContentResolver().insert(
                            DatabaseContract.PHONE_CONTENT_URI.buildUpon().appendPath(incomingNumber).build(),
                            values
                    );

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
                Cursor cursor = context.getContentResolver().query(
                        DatabaseContract.PHONE_CONTENT_URI.buildUpon().appendPath(incomingNumber).build(),
                        null,
                        DatabaseContract.Phone.COLUMN_NUMBER + "= ?",
                        new String[]{incomingNumber},
                        null
                );

                try {
                    if (cursor.moveToFirst()) {
                        Content content = new Content();
                        content.setNumber(cursor.getString(DatabaseContract.Phone.INDEX_NUMBER));
                        content.setScore(cursor.getInt(DatabaseContract.Phone.INDEX_SCORE));
                        content.setAd(cursor.getInt(DatabaseContract.Phone.INDEX_AD));
                        content.setScam(cursor.getInt(DatabaseContract.Phone.INDEX_SCAM));
                        createIncomingCallNotification(content);
                    } else {
                        Log.e(TAG, "Not cached");
                    }
                } finally {
                    if (cursor != null)
                        cursor.close();
                }
            }
        });
    }

    private boolean contactExists(Context context, String number) {
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

    public void createReportNotification(String incomingNumber) {
        Intent intent = new Intent(context, DisplayActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        //Yes intent
        Intent adReceive = new Intent();
        adReceive.setAction("Ad");
        adReceive.putExtra("number", incomingNumber);
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(context, 12345, adReceive, PendingIntent.FLAG_UPDATE_CURRENT);

        //Maybe intent
        Intent okReceive = new Intent();
        okReceive.setAction("Ok");
        okReceive.putExtra("number", incomingNumber);
        PendingIntent pendingIntentMaybe = PendingIntent.getBroadcast(context, 12345, okReceive, PendingIntent.FLAG_UPDATE_CURRENT);

        //No intent
        Intent scamReceive = new Intent();
        scamReceive.setAction("Scam");
        scamReceive.putExtra("number", incomingNumber);
        PendingIntent pendingIntentNo = PendingIntent.getBroadcast(context, 12345, scamReceive, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.safe)
                        .setContentTitle("How was your last call ?")
                        .setContentText("This help us improving the application")
                        .setPriority(Notification.PRIORITY_MAX)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentIntent(pIntent)
                        .addAction(R.drawable.reported, "Ad", pendingIntentYes)
                        .addAction(R.drawable.safe, "Ok", pendingIntentMaybe)
                        .addAction(R.drawable.dangerous, "Scam", pendingIntentNo);

        Notification noti = builder.build();

        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyManager.notify(NotificationContract.REPORTED_NOTIFICATION_ID, noti);
    }

    public void createIncomingCallNotification(Content content) {
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
            contentText = "Safe";
        }
        else if(score <= 3) {
            icon = R.drawable.reported;
            contentText = "Reported";
        }
        else {
            icon = R.drawable.dangerous;
            contentText = "Dangerous";
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(icon)
                        .setContentTitle("Call from : " + number)
                        .setContentText(contentText)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentIntent(pIntent)
                        .setStyle(inboxStyle);

        Notification noti = builder.build();

        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyManager.notify(NotificationContract.INCOMING_CALL_NOTIFICATION_ID, noti);
    }
}
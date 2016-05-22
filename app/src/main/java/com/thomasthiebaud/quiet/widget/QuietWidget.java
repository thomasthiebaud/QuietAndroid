package com.thomasthiebaud.quiet.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.thomasthiebaud.quiet.R;
import com.thomasthiebaud.quiet.app.DetailsActivity;
import com.thomasthiebaud.quiet.contract.IntentContract;
import com.thomasthiebaud.quiet.utils.Utils;

/**
 * Created by thomasthiebaud on 5/21/16.
 */
public class QuietWidget extends AppWidgetProvider {
    public static final String TAG = QuietWidget.class.getSimpleName();

    private String status;
    private int icon;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for(int id : appWidgetIds) {
            appWidgetManager.updateAppWidget(id, getRemoteViews(context));
        }
    }

    private RemoteViews getRemoteViews(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        if(Utils.isRunning(context)) {
            if(status == null || status.isEmpty() || icon == 0) {
                remoteViews.setTextViewText(R.id.status, context.getString(R.string.quiet_is_running));
                remoteViews.setImageViewResource(R.id.icon, R.drawable.running);
            } else {
                remoteViews.setTextViewText(R.id.status, this.status);
                remoteViews.setImageViewResource(R.id.icon, this.icon);
            }
        } else {
            remoteViews.setTextViewText(R.id.status, context.getString(R.string.quiet_is_not_running));
            remoteViews.setImageViewResource(R.id.icon, R.drawable.dangerous);
        }

        String number = Utils.getNumber(context);
        if(number != null && !number.isEmpty()) {
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra(IntentContract.PHONE_NUMBER, number);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
        } else {
            remoteViews.setOnClickPendingIntent(R.id.widget_layout, null);
        }
        return remoteViews;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);
        switch (intent.getAction()) {
            case IntentContract.WIDGET_ACTION:
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
                ComponentName thisWidget = new ComponentName(context, QuietWidget.class);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
                if (appWidgetIds != null && appWidgetIds.length > 0) {
                    this.icon = intent.getIntExtra(IntentContract.ICON, R.drawable.running);
                    this.status = intent.getStringExtra(IntentContract.STATUS);
                    onUpdate(context, appWidgetManager, appWidgetIds);
                }

                break;
        }
    }
}

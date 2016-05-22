package com.thomasthiebaud.quiet.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.thomasthiebaud.quiet.R;
import com.thomasthiebaud.quiet.contract.IntentContract;

/**
 * Created by thomasthiebaud on 5/21/16.
 */
public class QuietWidget extends AppWidgetProvider {
    public static final String TAG = QuietWidget.class.getSimpleName();

    private String status = "Quiet is running";
    private int icon = R.drawable.running;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for(int id : appWidgetIds) {
            appWidgetManager.updateAppWidget(id, getRemoteViews(context));
        }
    }

    private RemoteViews getRemoteViews(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        remoteViews.setTextViewText(R.id.status, this.status);
        remoteViews.setImageViewResource(R.id.icon, this.icon);
        return remoteViews;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);
        switch (intent.getAction()) {
            case "com.thomasthiebaud.quiet.UPDATE_WIDGET_STATE":
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

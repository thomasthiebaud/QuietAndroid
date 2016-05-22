package com.thomasthiebaud.quiet.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.thomasthiebaud.quiet.R;
import com.thomasthiebaud.quiet.contract.IntentContract;

public final class DisplayActivity extends AppCompatActivity {
    private static final String TAG = DisplayActivity.class.getSimpleName();
    private Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        tracker = application.getDefaultTracker();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView titleView = (TextView) findViewById(R.id.title);
        TextView descriptionView = (TextView) findViewById(R.id.description);

        Intent intent = getIntent();
        if(intent.hasExtra(IntentContract.TITLE))
            titleView.setText(intent.getStringExtra(IntentContract.TITLE));

        if(intent.hasExtra(IntentContract.DESCRIPTION))
            descriptionView.setText(intent.getStringExtra(IntentContract.DESCRIPTION));
    }

    @Override
    public void onResume() {
        super.onResume();
        tracker.setScreenName("Display : " + DisplayActivity.class.getName());
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public static void displayError(Context context, String description) {
        Intent intent = new Intent(context, DisplayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(IntentContract.TITLE, "Error");
        intent.putExtra(IntentContract.DESCRIPTION, description);
        context.startActivity(intent);
    }

    public static void displaySuccess(Context context, String description) {
        Intent intent = new Intent(context, DisplayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(IntentContract.TITLE, "Success");
        intent.putExtra(IntentContract.DESCRIPTION, description);
        context.startActivity(intent);
    }
}

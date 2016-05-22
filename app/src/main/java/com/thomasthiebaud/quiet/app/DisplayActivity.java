package com.thomasthiebaud.quiet.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
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

        TextView titleView = (TextView) findViewById(R.id.title);
        TextView descriptionView = (TextView) findViewById(R.id.description);
        ImageView statusIcon = (ImageView) findViewById(R.id.status_icon);

        Intent intent = getIntent();
        if(intent.hasExtra(IntentContract.TITLE)) {
            String title = intent.getStringExtra(IntentContract.TITLE);
            titleView.setText(title);
            toolbar.setTitle(title);
        }

        if(intent.hasExtra(IntentContract.DESCRIPTION))
            descriptionView.setText(intent.getStringExtra(IntentContract.DESCRIPTION));

        if(intent.hasExtra(IntentContract.ICON))
            statusIcon.setImageResource(intent.getIntExtra(IntentContract.ICON, R.drawable.safe));
    }

    @Override
    public void onResume() {
        super.onResume();
        tracker.setScreenName("Display : " + DisplayActivity.class.getName());
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public static void displayError(Context context, String title, String description) {
        Intent intent = new Intent(context, DisplayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(IntentContract.ICON, R.drawable.error);
        intent.putExtra(IntentContract.TITLE, title);
        intent.putExtra(IntentContract.DESCRIPTION, description);
        context.startActivity(intent);
    }

    public static void displaySuccess(Context context, String title, String description) {
        Intent intent = new Intent(context, DisplayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(IntentContract.ICON, R.drawable.success);
        intent.putExtra(IntentContract.TITLE, title);
        intent.putExtra(IntentContract.DESCRIPTION, description);
        context.startActivity(intent);
    }
}

package com.thomasthiebaud.quiet.app;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.thomasthiebaud.quiet.R;
import com.thomasthiebaud.quiet.contracts.IntentContract;

public final class DisplayActivity extends AppCompatActivity {
    private static final String TAG = DisplayActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

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

package com.thomasthiebaud.quiet.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.thomasthiebaud.quiet.R;
import com.thomasthiebaud.quiet.contract.DatabaseContract;
import com.thomasthiebaud.quiet.contract.IntentContract;
import com.thomasthiebaud.quiet.contract.LoaderContract;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = DetailsActivity.class.getSimpleName();
    private Tracker tracker;
    private String number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        tracker = application.getDefaultTracker();

        Intent intent = getIntent();
        this.number = intent.getStringExtra(IntentContract.PHONE_NUMBER);

        getSupportLoaderManager().initLoader(LoaderContract.PHONE_LOADER, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        tracker.setScreenName("Details : " + DetailsActivity.class.getName());
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.e(TAG, "Load");
        CursorLoader cursorLoader = null;
        switch (id) {
            case LoaderContract.PHONE_LOADER:
                cursorLoader = new CursorLoader(
                        this,
                        DatabaseContract.PHONE_CONTENT_URI.buildUpon().appendPath(this.number).build(),
                        null,
                        DatabaseContract.Phone.COLUMN_NUMBER + "= ?",
                        new String[]{this.number},
                        null
                );
                break;
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LoaderContract.PHONE_LOADER:
                if(data != null && data.moveToFirst()) {
                    String number = data.getString(DatabaseContract.Phone.INDEX_NUMBER);
                    int score = data.getInt(DatabaseContract.Phone.INDEX_SCORE);
                    int scam = data.getInt(DatabaseContract.Phone.INDEX_SCAM);
                    int ad = data.getInt(DatabaseContract.Phone.INDEX_AD);
                    int scamPercent = scam * 100 / score;
                    int adPercent = ad * 100 / score;

                    TextView numberView = (TextView) findViewById(R.id.number);
                    TextView scoreView = (TextView) findViewById(R.id.score);
                    ArcProgress adView = (ArcProgress) findViewById(R.id.ad);
                    ArcProgress scamView = (ArcProgress) findViewById(R.id.scam);

                    numberView.setText(number);
                    scoreView.setText(score + "");
                    scamView.setProgress(scamPercent);
                    adView.setProgress(adPercent);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}

package com.thomasthiebaud.quiet.app;

import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.thomasthiebaud.quiet.R;
import com.thomasthiebaud.quiet.contract.DatabaseContract;
import com.thomasthiebaud.quiet.contract.IntentContract;
import com.thomasthiebaud.quiet.contract.LoaderContract;
import com.thomasthiebaud.quiet.utils.Quiet;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by thomasthiebaud on 5/29/16.
 * This code is base on https://github.com/saulmm/CoordinatorBehaviorExample
 */
public class DetailsActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = DetailsActivity.class.getSimpleName();
    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;

    private Tracker tracker;
    private String number;
    private String status;
    private int icon;

    private LinearLayout titleContainer;
    private TextView titleView;
    private AppBarLayout appBarLayout;
    private CircleImageView circleImageView;
    private TextView numberView;
    private TextView scoreView;
    private ArcProgress adView;
    private ArcProgress scamView;
    private TextView satusView;

    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        tracker = application.getDefaultTracker();

        Intent intent = getIntent();
        this.number = intent.getStringExtra(IntentContract.PHONE_NUMBER);
        this.status = intent.getStringExtra(IntentContract.STATUS);
        this.icon = intent.getIntExtra(IntentContract.ICON, R.drawable.safe);

        titleView = (TextView) findViewById(R.id.main_textview_title);
        titleContainer = (LinearLayout) findViewById(R.id.main_linearlayout_title);
        appBarLayout = (AppBarLayout) findViewById(R.id.main_appbar);
        circleImageView = (CircleImageView) findViewById(R.id.circleimageview);
        numberView = (TextView) findViewById(R.id.number);
        scoreView = (TextView) findViewById(R.id.score);
        adView = (ArcProgress) findViewById(R.id.ad);
        scamView = (ArcProgress) findViewById(R.id.scam);
        satusView = (TextView) findViewById(R.id.status);
        
        satusView.setText(status);
        titleView.setText(number);
        circleImageView.setImageResource(icon);

        Quiet.removeNumber(getApplicationContext());

        getSupportLoaderManager().initLoader(LoaderContract.PHONE_LOADER, null, this);

        appBarLayout.addOnOffsetChangedListener(this);
        startAlphaAnimation(titleView, 0, View.INVISIBLE);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    @Override
    public void onResume() {
        super.onResume();
        tracker.setScreenName("Details : " + DetailsActivity.class.getName());
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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

                    numberView.setText(number);
                    scoreView.setText(score + "");
                    scamView.setProgress(scamPercent);
                    adView.setProgress(adPercent);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        loader.reset();
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {
            if(!mIsTheTitleVisible) {
                startAlphaAnimation(titleView, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }
        } else {
            if (mIsTheTitleVisible) {
                startAlphaAnimation(titleView, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if(mIsTheTitleContainerVisible) {
                startAlphaAnimation(titleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }
        } else {
            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(titleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    public static void startAlphaAnimation (View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }
}
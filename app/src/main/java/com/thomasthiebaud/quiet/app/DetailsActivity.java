package com.thomasthiebaud.quiet.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.thomasthiebaud.quiet.R;
import com.thomasthiebaud.quiet.contracts.IntentContract;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        String number = intent.getStringExtra(IntentContract.PHONE_NUMBER);
        int score = intent.getIntExtra(IntentContract.SCORE, 0);
        int scam = intent.getIntExtra(IntentContract.SCAM, 0);
        int ad = intent.getIntExtra(IntentContract.AD, 0);

        TextView numberView = (TextView) findViewById(R.id.number);
        TextView scoreView = (TextView) findViewById(R.id.score);
        DonutProgress scamView = (DonutProgress) findViewById(R.id.scam);
        DonutProgress adView = (DonutProgress) findViewById(R.id.ad);

        numberView.setText(number);
        scoreView.setText(score + "");
        scamView.setMax(score);
        scamView.setProgress(scam);
        adView.setMax(score);
        adView.setProgress(ad);
    }
}

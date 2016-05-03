package com.thomasthiebaud.quiet.app.success;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.thomasthiebaud.quiet.R;

public class SuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView titleView = (TextView) findViewById(R.id.title);
        TextView descriptionView = (TextView) findViewById(R.id.description);

        Intent intent = getIntent();
        if(intent.hasExtra("title"))
            titleView.setText(intent.getStringExtra("title"));

        if(intent.hasExtra("description"))
            descriptionView.setText(intent.getStringExtra("description"));
    }
}

package com.thomasthiebaud.quiet.contract;

import android.net.Uri;

/**
 * Created by thomasthiebaud on 5/19/16.
 */
public interface DatabaseContract {

    String AUTHORITY = "com.thomasthiebaud.quiet";
    Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    String PHONE_PATH = "phone";
    Uri PHONE_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PHONE_PATH).build();

    interface Phone {
        String TABLE_NAME = "phone";
        String COLUMN_NUMBER = "number";
        String COLUMN_SCAM = "scam";
        String COLUMN_AD = "ad";
        String COLUMN_SCORE = "score";

        int INDEX_NUMBER = 0;
        int INDEX_SCORE = 1;
        int INDEX_AD = 2;
        int INDEX_SCAM = 3;
    }
}

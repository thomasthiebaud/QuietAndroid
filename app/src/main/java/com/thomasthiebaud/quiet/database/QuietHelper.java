package com.thomasthiebaud.quiet.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.thomasthiebaud.quiet.contract.DatabaseContract;

/**
 * Created by thomasthiebaud on 5/19/16.
 */
public class QuietHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "quiet.db";
    final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + DatabaseContract.Phone.TABLE_NAME + " (" +
            DatabaseContract.Phone.COLUMN_NUMBER + " VARCHAR(20) NOT NULL, " +
            DatabaseContract.Phone.COLUMN_SCORE + " INTEGER NOT NULL, " +
            DatabaseContract.Phone.COLUMN_AD + " INTEGER NOT NULL, " +
            DatabaseContract.Phone.COLUMN_SCAM + " INTEGER NOT NULL, " +
            "UNIQUE(" + DatabaseContract.Phone.COLUMN_NUMBER + ")" +
            " );";


    public QuietHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Phone.TABLE_NAME);
        onCreate(database);
    }
}

package com.thomasthiebaud.quiet.database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.thomasthiebaud.quiet.contract.DatabaseContract;

/**
 * Created by thomasthiebaud on 5/19/16.
 */
public class QuietProvider extends ContentProvider {
    private SQLiteOpenHelper openHelper;
    
    private static final UriMatcher uriMatcher = buildUriMatcher();
    private static final int PHONE_NUMBER = 100;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DatabaseContract.AUTHORITY;

        matcher.addURI(authority, DatabaseContract.PHONE_PATH + "/*", PHONE_NUMBER);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        openHelper = new QuietHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final int match = uriMatcher.match(uri);
        final Cursor cursor;

        switch (match) {
            case PHONE_NUMBER:
                cursor = openHelper.getReadableDatabase().query(
                        DatabaseContract.Phone.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case PHONE_NUMBER: return ContentResolver.CURSOR_ITEM_BASE_TYPE;

            default: throw new UnsupportedOperationException("Unknown Uri" + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = uriMatcher.match(uri);

        switch (match) {
            case PHONE_NUMBER:
                openHelper.getWritableDatabase().insertWithOnConflict(
                        DatabaseContract.Phone.TABLE_NAME,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                );
                break;
            default: throw new UnsupportedOperationException("Unknown Uri" + uri);
        }

        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}

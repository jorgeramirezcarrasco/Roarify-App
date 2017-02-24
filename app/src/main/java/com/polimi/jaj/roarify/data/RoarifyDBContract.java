package com.polimi.jaj.roarify.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Alberto on 1/1/17.
 */

public class RoarifyDBContract {
    public static final String DATABASE_NAME = "Roarify.db";
    public static final int DATABASE_VERSION = 1;

    public static final String FLOAT_TYPE = " float";
    public static final String TEXT_TYPE = " text";

    public static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_FAVORITES_TABLE =
            "CREATE TABLE IF NOT EXISTS "+FavoriteEntry.TABLE_NAME+" ("+
                    FavoriteEntry.COLUMN_NAME_MESSAGE_ID+TEXT_TYPE+" PRIMARY KEY"+COMMA_SEP+
                    FavoriteEntry.COLUMN_NAME_USER_ID+TEXT_TYPE+COMMA_SEP+
                    FavoriteEntry.COLUMN_NAME_USER_NAME+TEXT_TYPE+COMMA_SEP+
                    FavoriteEntry.COLUMN_NAME_MESSAGE+TEXT_TYPE+COMMA_SEP+
                    FavoriteEntry.COLUMN_NAME_TIME+TEXT_TYPE+COMMA_SEP+
                    FavoriteEntry.COLUMN_NAME_LATITUDE+FLOAT_TYPE+COMMA_SEP+
                    FavoriteEntry.COLUMN_NAME_LONGITUDE+FLOAT_TYPE+" )";


    public static SQLiteDatabase getWritableDatabase(Context context){
        return new RoarifyDBHelper(context).getWritableDatabase();
    }

    public static SQLiteDatabase getReadableDatabase(Context context){
        return new RoarifyDBHelper(context).getReadableDatabase();
    }

    public static class FavoriteEntry implements BaseColumns {

        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_NAME_MESSAGE_ID = "message_id";
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_NAME_USER_NAME = "user_name";
        public static final String COLUMN_NAME_MESSAGE = "message";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_LATITUDE = "loc_lat";
        public static final String COLUMN_NAME_LONGITUDE = "loc_lon";
    }

    public static class RoarifyDBHelper extends SQLiteOpenHelper {

        public RoarifyDBHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_FAVORITES_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}

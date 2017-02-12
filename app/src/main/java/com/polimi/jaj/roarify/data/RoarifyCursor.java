package com.polimi.jaj.roarify.data;

import android.database.Cursor;
import android.database.CursorWrapper;

/**
 * Created by Alberto on 1/1/17.
 */

public class RoarifyCursor extends CursorWrapper{

    public RoarifyCursor(Cursor cursor){
        super(cursor);
    }

    public String getMessageId(){
        return getString(
                getColumnIndex(RoarifyDBContract.FavoriteEntry.
                        COLUMN_NAME_MESSAGE_ID));
    }

    public String getUserId(){
        return getString(
                getColumnIndex(RoarifyDBContract.FavoriteEntry.
                        COLUMN_NAME_USER_ID));
    }

    public String getUserName(){
        return getString(
                getColumnIndex(RoarifyDBContract.FavoriteEntry.
                        COLUMN_NAME_USER_NAME));
    }

    public String getMessage(){
        return getString(
                getColumnIndex(RoarifyDBContract.FavoriteEntry.
                        COLUMN_NAME_MESSAGE));
    }

    public String getTime(){
        return getString(
                getColumnIndex(RoarifyDBContract.FavoriteEntry.
                        COLUMN_NAME_TIME));
    }

    public Double getLat(){
        return getDouble(
                getColumnIndex(RoarifyDBContract.FavoriteEntry.
                        COLUMN_NAME_LATITUDE));
    }

    public Double getLon(){
        return getDouble(
                getColumnIndex(RoarifyDBContract.FavoriteEntry.
                        COLUMN_NAME_LONGITUDE));
    }

}
package com.polimi.jaj.roarify.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.polimi.jaj.roarify.model.Message;

import static com.polimi.jaj.roarify.data.RoarifyDBContract.*;

/**
 * Created by Alberto on 1/1/17.
 */

public class RoarifySQLiteRepository {

    private SQLiteDatabase db;

    public RoarifySQLiteRepository(Context context){
        db = getWritableDatabase(context);
    }

    public void add(Message favorite){
        db.execSQL("INSERT OR REPLACE INTO "+FavoriteEntry.TABLE_NAME+" ("+
                        FavoriteEntry.COLUMN_NAME_MESSAGE_ID+COMMA_SEP+
                        FavoriteEntry.COLUMN_NAME_USER_ID+COMMA_SEP+
                        FavoriteEntry.COLUMN_NAME_USER_NAME+COMMA_SEP+
                        FavoriteEntry.COLUMN_NAME_MESSAGE+COMMA_SEP+
                        FavoriteEntry.COLUMN_NAME_TIME+COMMA_SEP+
                        FavoriteEntry.COLUMN_NAME_LATITUDE+COMMA_SEP+
                        FavoriteEntry.COLUMN_NAME_LONGITUDE+ ") "+
                        "VALUES(?"+COMMA_SEP+"?"+COMMA_SEP+"?"+COMMA_SEP+"?"+COMMA_SEP+"?"+COMMA_SEP+"?"+COMMA_SEP+"?)",
                new Object[]{favorite.getMessageId(),favorite.getUserId(), favorite.getUserName(),
                        favorite.getText(), favorite.getTime(), favorite.getLatitude(),
                        favorite.getLongitude(),});
    }

    public void delete(Message favorite){
        db.execSQL("DELETE FROM "+FavoriteEntry.TABLE_NAME+ " WHERE " +
                        FavoriteEntry.COLUMN_NAME_MESSAGE_ID + " = ?",
                new Object[]{favorite.getMessageId()});
    }

    public void delete(String messageID){
        db.execSQL("DELETE FROM "+FavoriteEntry.TABLE_NAME+ " WHERE " +
                        FavoriteEntry.COLUMN_NAME_MESSAGE_ID + " = ?",
                new Object[]{messageID});
    }

    public void deleteAll(){
        db.execSQL("DELETE FROM "+FavoriteEntry.TABLE_NAME);
    }

    public RoarifyCursor findById(String id){
        return new RoarifyCursor(db.rawQuery("SELECT * FROM "+FavoriteEntry.TABLE_NAME+" WHERE "+
                FavoriteEntry.COLUMN_NAME_MESSAGE_ID + " = ?", new String[]{id}));
    }

    public RoarifyCursor findAll(){
        return new RoarifyCursor(db.rawQuery("SELECT * FROM "+FavoriteEntry.TABLE_NAME, null));
    }

}

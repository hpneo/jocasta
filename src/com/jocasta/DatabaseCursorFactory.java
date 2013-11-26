package com.jocasta;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;

public class DatabaseCursorFactory implements CursorFactory {
    private boolean debugEnabled;
    
    public DatabaseCursorFactory() {
        this.debugEnabled = false;
    }
    
    public DatabaseCursorFactory(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery, String editTable, SQLiteQuery query) {
        if (this.debugEnabled) {
            Log.d("JOCASTA_SQL", query.toString());
        }
        
        return new SQLiteCursor(db, masterQuery, editTable, query);
    }

}

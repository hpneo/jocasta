package com.jocasta;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class Database {
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase database;
    private Context context;

    public Database(Context context) {
        this.databaseHelper = new DatabaseHelper(context);
        this.context = context;
    }

    public <T extends Model> List<T> getModels() {
        return this.databaseHelper.getModels(this.context);
    }

    public synchronized SQLiteDatabase getDB() {
        if (this.database == null) {
            this.database = this.databaseHelper.getWritableDatabase();
        }

        return this.database;
    }
}

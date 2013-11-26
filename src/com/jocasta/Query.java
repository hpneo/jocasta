package com.jocasta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Query<T extends Model> {
    private SQLiteDatabase db;
    private Class<T> modelClass;
    private String tableName;
    private HashSet<String> conditions = new HashSet<String>();
    private List<Object> values = new ArrayList<Object>();

    public Query(Class<T> type, SQLiteDatabase db, String tableName, Entry<String, Object> condition) {
        this.db = db;
        this.modelClass = type;
        this.tableName = tableName;
        if (condition != null) {
            this.conditions.add(conditionToString(condition));
        }
    }

    public Query<T> where(Entry<String, Object> conditions) {
        this.conditions.add(conditionToString(conditions));

        return this;
    }

    public Cursor toCursor() {
        String[] columns = null;
        String selection = getSelection();
        String[] selectionArgs = getSelectionArgs();
        String groupBy = null;
        String having = null;
        String orderBy = null;
        String limit = null;
        
        Log.i("Cursor:selection", selection + "");
        Log.i("Cursor:selectionArgs", selectionArgs.length + "");

        return db.query(this.tableName, columns, selection, selectionArgs,
                groupBy, having, orderBy, limit);
    }

    @SuppressWarnings({ "unchecked", "hiding" })
    public <T extends Model> List<T> toList() {
        Cursor cursor = toCursor();

        T result = null;
        List<T> results = new ArrayList<T>();

        try {
            while (cursor.moveToNext()) {
                result = (T) this.modelClass.newInstance();
                result.inflate(cursor);
                
                results.add(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }

        return results;
    }

    private String conditionToString(Entry<String, Object> condition) {
        this.values.add(String.valueOf(condition.getValue()));
        return condition.getKey() + " = ?";
    }

    private String getSelection() {
        String[] selection = this.conditions.toArray(new String[0]);
        String selectionAsString = "";

        for (int i = 0; i < selection.length; i++) {
            if (i < selection.length - 1) {
                selectionAsString = selection[i] + " AND ";
            } else {
                selectionAsString = selection[i];
            }
        }

        return selectionAsString;
    }

    private String[] getSelectionArgs() {
        String[] selectionArgs = this.values.toArray(new String[0]);

        return selectionArgs;
    }
}
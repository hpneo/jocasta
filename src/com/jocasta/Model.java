package com.jocasta;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.NameValuePair;

import com.jocasta.annotations.Ignore;
import com.jocasta.async.RestAsyncTask;
import com.jocasta.async.RestClient;
import com.jocasta.callbacks.*;
import com.jocasta.utils.Inflector;
import com.jocasta.utils.QueryCondition;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Model {
    
    public Long id = null;
    @Ignore
    public static String BASE_URL = null;
    @Ignore
    protected static String resourceURL = null;
    @Ignore
    protected static HashMap<String, String> URLS = new HashMap<String, String>();
    
    public Model(Context context) {
    }

    public Model() {
    }
    
    public static String getBaseUrl() {
        return BASE_URL;
    }
    
    public static String getResourceUrl(String action) {
        return BASE_URL + resourceURL + URLS.get(action);
    }

    public static <T extends Model> T find(Class<T> type, int id) {
        List<T> results = where(type, new QueryCondition<String, Object>("id", id)).toList();
        
        return results.get(0);
    }
    
    public static <T extends Model> List<T> all(Class<T> type) {
        List<T> results = where(type, null).toList();
        
        return results;
    }
    
    public static <T extends Model> boolean isEmpty(Class<T> type) {
        List<T> results = where(type, null).toList();
        
        return results.isEmpty();
    }
    
    public void save() {
        Log.i("MODEL_SAVE", "SAVE");
        save(null, null);
    }

    public void save(SuccessCallback successCallback) {
        Log.i("MODEL_SAVE", "SAVE");
        save(successCallback, null);
    }

    public void save(SuccessCallback successCallback, FailCallback failCallback) {
        Log.i("MODEL_SAVE", "SAVE");
        SQLiteDatabase db = getApplication().getDatabase().getDB();
        List<Field> fields = getTableFields();
        ContentValues values = new ContentValues(fields.size());
        
        for (Field field : fields) {
            field.setAccessible(true);
            Class<?> columnType = field.getType();
            String columnName = QueryCondition.getColumnName(field.getName());
            
            try {
                Object columnValue = field.get(this);
                
                if (!columnName.equalsIgnoreCase("id")) {
                    if (columnType.equals(Short.class) || columnType.equals(short.class)) {
                        values.put(columnName, (Short) columnValue);
                    }
                    else if (columnType.equals(Integer.class) || columnType.equals(int.class)) {
                        values.put(columnName, (Integer) columnValue);
                    }
                    else if (columnType.equals(Long.class) || columnType.equals(long.class)) {
                        values.put(columnName, (Long) columnValue);
                    }
                    else if (columnType.equals(Float.class) || columnType.equals(float.class)) {
                        values.put(columnName, (Float) columnValue);
                    }
                    else if (columnType.equals(Double.class) || columnType.equals(double.class)) {
                        values.put(columnName, (Double) columnValue);
                    }
                    else if (columnType.equals(Boolean.class) || columnType.equals(boolean.class)) {
                        values.put(columnName, (Boolean) columnValue);
                    }
                    else if (Date.class.equals(columnType)) {
                        values.put(columnName, ((Date) field.get(this)).getTime());
                    }
                    else if (Calendar.class.equals(columnType)) {
                        values.put(columnName, ((Calendar) field.get(this)).getTimeInMillis());
                    }
                    else{
                        values.put(columnName, String.valueOf(columnValue));
                    }
                }
            } catch (Exception e) {
                Log.e("JOCASTA_MODEL_ERROR", e.getMessage());
            }
        }
        
        if (isNewRecord()) {
            Log.i("JOCASTA_IS_NEW_RECORD", "IS_NEW_RECORD");
            id = db.insert(getTableName(), null, values);
            
            if (this.id == -1) {
                if (failCallback != null) {
                    failCallback.run(null);
                }
            }
            else {
                if (successCallback != null) {
                    successCallback.run(this);
                }
            }
        }
        else {
            Log.i("JOCASTA_ISNT_NEW_RECORD", "ISNT_NEW_RECORD");
            int records = db.update(getTableName(), values, "id = ?", new String[]{ String.valueOf(id) });
            
            if (records == 0) {
                if (failCallback != null) {
                    failCallback.run(null);
                }
            }
            else {
                if (successCallback != null) {
                    successCallback.run(this);
                }
            }
        }
    }
    
    public void delete() {
        delete(null, null);
    }

    public void delete(SuccessCallback successCallback) {
        delete(successCallback, null);
    }

    public void delete(SuccessCallback successCallback, FailCallback failCallback) {
        SQLiteDatabase db = getApplication().getDatabase().getDB();
        
        int rowsDeleted = db.delete(getTableName(), "id = ?", new String[]{ String.valueOf(id) });
        
        if (rowsDeleted == 1) {
            if (successCallback != null) {
                successCallback.run(this);
            }
        }
        else {
            if (failCallback != null) {
                failCallback.run(null);
            }
        }
    }

    public static <T extends Model> void fetch(Class<T> type, AsyncSuccessCallback successCallback, AsyncFailCallback failCallback) {
        String url = null;
        
        try {
            url = type.getMethod("getResourceUrl", String.class).invoke(type, "index").toString();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        if (url != null && !url.equals(null)) {
            get(url, null, successCallback, failCallback);
        }
    }
    
    public static <T extends Model> void clear(Class<T> type) {
        SQLiteDatabase db = getApplication().getDatabase().getDB();
        
        db.delete(getTableName(type), null, null);
    }

    public void sync(AsyncSuccessCallback successCallback, AsyncFailCallback failCallback) {
        if (resourceURL != null && !resourceURL.equals("")) {
            if (this.isNewRecord()) {
                post(resourceURL, new ArrayList<NameValuePair>(), successCallback, failCallback);
            }
            else {
                put(resourceURL, new ArrayList<NameValuePair>(), successCallback, failCallback);
            }
        }
    }

    public static void get(String url, ArrayList<NameValuePair> params, AsyncSuccessCallback successCallback, AsyncFailCallback failCallback) {
        RestAsyncTask asyncTask = new RestAsyncTask(RestClient.RequestMethod.GET);
        asyncTask.getClient().addAllParams(params);
        asyncTask.setSuccessCallback(successCallback);
        asyncTask.setFailCallback(failCallback);
        asyncTask.execute(url);
    }

    public static void post(String url, ArrayList<NameValuePair> params, AsyncSuccessCallback successCallback, AsyncFailCallback failCallback) {
        RestAsyncTask asyncTask = new RestAsyncTask(RestClient.RequestMethod.POST);
        asyncTask.getClient().addAllParams(params);
        asyncTask.execute(url);
    }

    public static void put(String url, ArrayList<NameValuePair> params, AsyncSuccessCallback successCallback, AsyncFailCallback failCallback) {
        RestAsyncTask asyncTask = new RestAsyncTask(RestClient.RequestMethod.PUT);
        asyncTask.getClient().addAllParams(params);
        asyncTask.execute(url);
    }
    
    public static <T extends Model> Query<T> where(Class<T> type, Entry<String, Object> conditions) {
        SQLiteDatabase db = getApplication().getDatabase().getDB();

        return new Query<T>(type, db, getTableName(type), conditions);
    }
    
    @SuppressWarnings("rawtypes")
    public void inflate(Cursor cursor) {
        List<Field> fields = getTableFields(getClass());
        
        for (Field field : fields) {
            field.setAccessible(true);
            
            try {
                Class fieldType = field.getType();
                String columnName = QueryCondition.getColumnName(field.getName());
                Integer columnIndex = cursor.getColumnIndex(columnName);
                
                Log.i("MODEL_INFLATE", columnName);
                
                if (columnName.equalsIgnoreCase("id")) {
                    field.set(this, cursor.getLong(columnIndex));
                }
                else if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
                    field.set(this, cursor.getLong(columnIndex));
                }
                else if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
                    field.set(this, cursor.getInt(columnIndex));
                }
                else if (fieldType.equals(short.class) || fieldType.equals(Short.class)) {
                    field.set(this, cursor.getShort(columnIndex));
                }
                else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
                    field.set(this, cursor.getFloat(columnIndex));
                }
                else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
                    field.set(this, cursor.getDouble(columnIndex));
                }
                else if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
                    field.set(this, cursor.getString(columnIndex).equals("1"));
                }
                else if (fieldType.equals(String.class)) {
                    String value = cursor.getString(columnIndex);
                    
                    if (value != null && value.equalsIgnoreCase("null")) {
                        value = null;
                    }
                    
                    field.set(this, value);
                }
                else if (fieldType.equals(Date.class)) {
                    long timestamp = cursor.getLong(columnIndex);
                    
                    field.set(this, new Date(timestamp));
                }
                else if (fieldType.equals(Calendar.class)) {
                    long timestamp = cursor.getLong(columnIndex);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(timestamp);
                    
                    field.set(this, calendar);
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    public static Application getApplication() {
        return Application.getApplication();
    }

    public String getTableName() {
        return getTableName(getClass());
    }

    public static String getTableName(Class<?> type) {
        return Inflector.tableize(type);
    }
    
    public List<Field> getTableFields() {
        return getTableFields(getClass());
    }
    
    public static List<Field> getTableFields(Class<?> type) {
        List<Field> fields = new ArrayList<Field>();
        Collections.addAll(fields, type.getDeclaredFields());
        
        for (Field field : type.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Ignore.class) && !fields.contains(field)) {
                fields.add(field);
            }
        }
        
        return fields;
    }
    
    public boolean isNewRecord() {
        return this.id == null;
    }
}

package com.jocasta;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.jocasta.utils.QueryCondition;

import dalvik.system.DexFile;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private Context context;
    
    public DatabaseHelper(Context context) {
        super(context, DatabaseConfig.getDatabaseName(context), null, DatabaseConfig.getDatabaseVersion(context));
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.i("JOCASTA_DB_HELPER", "onCreate");
        
        createDatabase(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.i("JOCASTA_DB_HELPER", "onUpgrade");
        
        if (newVersion > oldVersion) {
            upgradeDatabase(database);
        }
    }
    
    private <T extends Model> void createDatabase(SQLiteDatabase database) {
        List<T> models = getModels(this.context);
        
        for (T model : models) {
            dropTable(model, database);
            createTable(model, database);
        }
    }
    
    private <T extends Model> void upgradeDatabase(SQLiteDatabase database) {
        List<T> models = getModels(this.context);
        
        for (T model : models) {
            dropTable(model, database);
            createTable(model, database);
        }
    }
    
    private <T extends Model> void dropTable(T model, SQLiteDatabase database) {
        String sql = "DROP TABLE IF EXISTS " + model.getTableName();
        
        try {
            Log.i("JOCASTA_DB_HELPER:dropTable", sql);
            database.execSQL(sql);
        } catch (Exception e) {
            Log.i("JOCASTA_DB_HELPER_ERROR:dropTable", e.getMessage());
        }
    }
    
    private <T extends Model> void createTable(T model, SQLiteDatabase database) {
        List<Field> fields = model.getTableFields();
        
        StringBuilder builder = new StringBuilder("CREATE TABLE ").append(model.getTableName()).append(" ( id INTEGER PRIMARY KEY AUTOINCREMENT ");
        
        for (Field column : fields) {
            column.setAccessible(true);
            String columnName = QueryCondition.getColumnName(column.getName());
            String columnType = QueryCondition.getColumnType(column.getType());
            
            if (columnType != null) {
                if (columnName.equalsIgnoreCase("id")) {
                    continue;
                }
                
                builder.append(", ").append(columnName).append(" ").append(columnType);
            }
        }
        
        builder.append(" ) ");
        
        String sql = builder.toString();
        
        Log.i("JOCASTA_DB_HELPER:createTable", sql);
        
        if (!sql.equals("")) {
            database.execSQL(sql);
        }
    }
    
    public <T extends Model> List<T> getModels(Context context) {
        List<T> models = new ArrayList<T>();
        
        try {
            String fileName = this.context.getPackageResourcePath();
            DexFile dexFile = new DexFile(fileName);
            Enumeration<?> classes = dexFile.entries();

//            Log.i("JOCASTA_DB_HELPER:getModels:fileName", fileName);
//            Log.i("JOCASTA_DB_HELPER:getModels:getModelPackageName", DatabaseConfig.getModelPackageName(context));
            
            while (classes.hasMoreElements()) {
                String className = (String) classes.nextElement();
                
                if (className.startsWith(DatabaseConfig.getModelPackageName(context))) {
                    Log.i("JOCASTA_DB_HELPER:getModels:className", className);
                    T modelClass = getModel(className, context);
                    
                    if (modelClass != null) {
                        models.add(modelClass);
                    }
                }
            }
        } catch (IOException e) {
            Log.d("JOCASTA_DB_HELPER:getModels:IOException", e.getMessage());
        }
        
        return models;
    }
    
    @SuppressWarnings("unchecked")
    private <T extends Model> T getModel(String className, Context context) {
        Class<?> modelClass = null;
        
        try {
            modelClass = Class.forName(className, true, context.getClass().getClassLoader());
            Log.i("JOCASTA_DB_HELPER:getModel", modelClass + "");
        } catch (Exception e) {
            Log.e("JOCASTA_DB_HELPER:getModel", e.getMessage());
        }
        
        if (modelClass != null) {
            try {
                Log.i("JOCASTA", ((T) modelClass.newInstance()) + "");
                
                Constructor<T> constr = (Constructor<T>) modelClass.getDeclaredConstructor();
                constr.setAccessible(true);
                
                return constr.newInstance();
            } catch (IllegalArgumentException e) {
                Log.e("JOCASTA_DB_HELPER:getModel:IllegalArgumentException", e.getMessage());
            } catch (InstantiationException e) {
                Log.e("JOCASTA_DB_HELPER:getModel:InstantiationException", e.getMessage());
            } catch (IllegalAccessException e) {
                Log.e("JOCASTA_DB_HELPER:getModel:IllegalAccessException", e.getMessage());
            } catch (InvocationTargetException e) {
                Log.e("JOCASTA_DB_HELPER:getModel:InvocationTargetException", e.getMessage());
            } catch (NoSuchMethodException e) {
                Log.e("JOCASTA_DB_HELPER:getModel:NoSuchMethodException", e.getMessage());
                // e.printStackTrace();
            }
        }
        
        return null;
    }

}

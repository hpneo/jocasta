package com.jocasta;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class DatabaseConfig {
    public static String getDatabaseName(Context context) {
        String databaseName = getMetadataString(context, "DATABASE");
        
        return databaseName;
    }
    
    public static String getModelPackageName(Context context) {
        String databaseName = getMetadataString(context, "MODELS_PACKAGE");
        
        return databaseName;
    }
    
    public static Integer getDatabaseVersion(Context context) {
        Integer databaseName = getMetadataInt(context, "VERSION");
        
        return databaseName;
    }
    
    public static String getMetadataString(Context context, String name) {
        String value = null;
        PackageManager pm = context.getPackageManager();
        
        try {
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), 128);
            value = ai.metaData.getString(name);
        } catch (NameNotFoundException e) {
            Log.d("JOCASTA_DB_CONFIG", e.getMessage());
        }
        
        return value;
    }
    
    public static Integer getMetadataInt(Context context, String name) {
        Integer value = null;
        PackageManager pm = context.getPackageManager();
        
        try {
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), 128);
            value = ai.metaData.getInt(name);
        } catch (NameNotFoundException e) {
            Log.d("JOCASTA_DB_CONFIG", e.getMessage());
        }
        
        return value;
    }
}

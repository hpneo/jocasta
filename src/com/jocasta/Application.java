package com.jocasta;

public class Application extends android.app.Application {
    private Database database;
    private static Application application;
    
    public Application() {
        super();
    }
    
    public void onCreate() {
        super.onCreate();
        
        Application.application = this;
        this.database = new Database(this);
    }
    
    public void onTerminate() {
        if (this.database != null ) {
            this.database.getDB().close();
        }
        
        super.onTerminate();
    }
    
    public static Application getApplication() {
        return application;
    }
    
    public Database getDatabase() {
        return this.database;
    }
}

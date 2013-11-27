package com.jocasta.async;

import com.jocasta.callbacks.AsyncFailCallback;
import com.jocasta.callbacks.AsyncSuccessCallback;

import android.os.AsyncTask;
import android.util.Log;

public class RestAsyncTask extends AsyncTask<String, Void, String> {
    private AsyncSuccessCallback successCallback;
    private AsyncFailCallback failCallback;
    private RestClient.RequestMethod method;
    private RestClient client;
    
    public RestAsyncTask(RestClient.RequestMethod method) {
        this.method = method;
        this.client = new RestClient();
    }

    @Override
    protected String doInBackground(String... urls) {
        Log.i("RestAsyncTask.doInBackground", urls[0]);
        try {
            return loadFromNetwork(urls[0]);
        } catch (Exception e) {
            Log.i("doInBackground.Exception", e.getMessage() + "");
            e.printStackTrace();
            if (this.failCallback != null) {
                this.failCallback.run(e);
            }
            
            return null;
        }
    }
    
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        
        Log.i("RestAsyncTask.onPostExecute", result + "");
        
        if (!result.equals(null)) {
            if (this.successCallback != null) {
                this.successCallback.run(result);
            }
        }
    }
    
    private String loadFromNetwork(String url) throws Exception {
        this.client.setURL(url);
        this.client.execute(this.method);

        Log.i("RestAsyncTask", this.client.getURL() + "");
        Log.i("RestAsyncTask.client.getResponse", this.client.getResponse() + "");
        
        return client.getResponse();
    }

    public RestClient getClient() {
        return client;
    }

    public void setClient(RestClient client) {
        this.client = client;
    }

    public void setSuccessCallback(AsyncSuccessCallback successCallback) {
        this.successCallback = successCallback;
    }

    public void setFailCallback(AsyncFailCallback failCallback) {
        this.failCallback = failCallback;
    }

}

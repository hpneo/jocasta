package com.jocasta.async;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.util.Log;

public class RestClient {

    public enum RequestMethod {
        GET, POST, PUT
    }

    private ArrayList<NameValuePair> params;
    private ArrayList<NameValuePair> headers;
    private ArrayList<String> errors;

    private String url;

    private int responseCode;
    private String message;

    private String response;

    public RestClient() {
        params = new ArrayList<NameValuePair>();
        headers = new ArrayList<NameValuePair>();
    }

    public RestClient(String url) {
        this.url = url;
        params = new ArrayList<NameValuePair>();
        headers = new ArrayList<NameValuePair>();
    }

    public void addParam(String name, String value) {
        params.add(new BasicNameValuePair(name, value));
    }

    public void addAllParams(ArrayList<NameValuePair> params) {
        if (params != null) {
            params.addAll(params);
        }
    }

    public void addHeader(String name, String value) {
        headers.add(new BasicNameValuePair(name, value));
    }

    public void execute(RequestMethod method) throws Exception {
        switch (method) {
            case GET: {
                // add parameters
                String combinedParams = "";
    
                if (!params.isEmpty()) {
                    combinedParams += "?";
                    for (NameValuePair p : params) {
                        String paramString = p.getName() + "="
                                + URLEncoder.encode(p.getValue(), "UTF-8");
                        if (combinedParams.length() > 1) {
                            combinedParams += "&" + paramString;
                        } else {
                            combinedParams += paramString;
                        }
                    }
                }
    
                Log.i("SUPER_URL", url + combinedParams);
    
                HttpGet request = new HttpGet(url + combinedParams);
    
                // add headers
                for (NameValuePair h : headers) {
                    request.addHeader(h.getName(), h.getValue());
                }
    
                executeRequest(request, url);
                break;
            }
            case POST: {
                HttpPost request = new HttpPost(url);
    
                // add headers
                for (NameValuePair h : headers) {
                    request.addHeader(h.getName(), h.getValue());
                }
    
                if (!params.isEmpty()) {
                    request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                }
    
                executeRequest(request, url);
                break;
            }
            case PUT: {
                HttpPut request = new HttpPut(url);
    
                for (NameValuePair h : headers) {
                    request.addHeader(h.getName(), h.getValue());
                }
    
                if (!params.isEmpty()) {
                    request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                }
    
                executeRequest(request, url);
                break;
            }
        }
    }

    private void executeRequest(HttpUriRequest request, String url) {
        HttpClient client = new DefaultHttpClient();

        HttpResponse httpResponse;

        try {
            httpResponse = client.execute(request);
            responseCode = httpResponse.getStatusLine().getStatusCode();
            message = httpResponse.getStatusLine().getReasonPhrase();

            HttpEntity entity = httpResponse.getEntity();

            if (entity != null) {
                InputStream instream = entity.getContent();
                response = convertStreamToString(instream);

                // Closing the input stream will trigger connection release
                instream.close();
            }

        } catch (ClientProtocolException e) {
            client.getConnectionManager().shutdown();
            this.errors.add(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            client.getConnectionManager().shutdown();
            this.errors.add(e.getMessage());
            e.printStackTrace();
        }
    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    public void setURL(String url) {
        this.url = url;
    }

    public String getURL() {
        Iterator<NameValuePair> iterator = this.params.iterator();

        String stringParams = "";

        while (iterator.hasNext()) {
            NameValuePair pair = iterator.next();
            stringParams += pair.getName() + "=" + pair.getValue() + "&";
        }

        return this.url + "?" + stringParams;
    }

    public String getResponse() {
        return response;
    }

    public String getErrorMessage() {
        return message;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public ArrayList<String> getErrors() {
        return errors;
    }
}
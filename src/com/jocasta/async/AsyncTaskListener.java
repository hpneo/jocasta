package com.jocasta.async;

public interface AsyncTaskListener {
    void onTaskCompleted(Object result);
    void onTaskError(Object result);
    void onTaskCancelled(Object result);
}

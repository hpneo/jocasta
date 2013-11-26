package com.jocasta.callbacks;

import com.jocasta.Model;

public interface SuccessCallback {
    <T extends Model> void run(T model);
}

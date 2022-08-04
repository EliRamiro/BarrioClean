package com.aply.barrioclean.utils.Culqi;

import org.json.JSONObject;

public interface TokenCallback {

    void onSuccess(JSONObject token);

    void onError(Exception error);

}

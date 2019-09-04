package com.tryfit.common.train;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by Miha-ha on 02.07.16.
 */
public class ErrorResponse implements IResponse {
    public String error;

    ErrorResponse(JSONArray header) {
        Decode(header);
    }

    ErrorResponse(String error) {
        this.error = error;
    }

    public void Decode(JSONArray header) {
        if (header.length() < 2) {
            error = "Header is too small";
            return;
        }
        try {
            error = header.getString(1);
        }catch (JSONException e) {
            Log.e("Train.ErrorResponse", "Decode error:"+e.getMessage());
        }
    }

    @Override
    public ArrayList Args() {
        return new ArrayList();
    }

    @Override
    public String Error() {
        return error;
    }
}

package com.tryfit.common.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;

/**
 * Created by alexeyreznik on 28/09/2017.
 */

public class QRCodeParser {

    public static String parseClientId(Context context, String qrCode) {
        String clientId = null;
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            String jwtSecret = bundle.getString("JWTSecret");
            if (jwtSecret != null && !jwtSecret.isEmpty()) {
                Jwt jwt = Jwts.parser().setSigningKey(jwtSecret.getBytes("UTF-8")).parse(qrCode);
                clientId = new JSONObject(jwt.getBody().toString()).getString("cid");
            }
        } catch (PackageManager.NameNotFoundException | JSONException | UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        return clientId;
    }
}

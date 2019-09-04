package com.tryfit.common.parsers;

import com.tryfit.common.db.models.Measures;
import com.tryfit.common.db.models.Scan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alexeyreznik on 19/06/2017.
 */

public class ScanParser {

    public static Scan parseScan(JSONObject json) {
        Scan result = null;
        try {
            String scanId = json.getString("id");
            String clientID = json.getString("clientID");

            result = new Scan();

            result.setScannerID(json.getString("scannerID"));
//            result.setCreated(json.getInt("created"));
//            result.setUpdated(json.getInt("updated"));
            result.setLeftMeasures(parseMeasure(json.getJSONArray("leftMeasures")));
            result.setRightMeasures(parseMeasure(json.getJSONArray("rightMeasures")));
//            result.setWeight(json.getInt("weight"));
//            result.setDeleted(json.getBoolean("deleted"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

//    public static Double[] parseMeasure(JSONArray json) {
//        Double[] result = null;
//        if (json.length() == 10) {
//            result = new Double[10];
//            try {
//                result[0] = json.getDouble(0);
//                result[1] = json.getDouble(1);
//                result[2] = json.getDouble(2);
//                result[3] = json.getDouble(3);
//                result[4] = json.getDouble(4);
//                result[5] = json.getDouble(5);
//                result[6] = json.getDouble(6);
//                result[7] = json.getDouble(7);
//                result[8] = json.getDouble(8);
//                result[9] = json.getDouble(9);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        return result;
//    }

    public static Measures parseMeasure(JSONArray json) {
        Measures result = null;
        if (json.length() == 10) {
            try {
                result = new Measures(
                        json.getDouble(0),
                        json.getDouble(1),
                        json.getDouble(2),
                        json.getDouble(3),
                        json.getDouble(4),
                        json.getDouble(5),
                        json.getDouble(6),
                        json.getDouble(7),
                        json.getDouble(8),
                        json.getDouble(9)
                );
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
}

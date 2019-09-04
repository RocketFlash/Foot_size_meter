package com.tryfit.common.rest;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.JsonDataException;
import com.tryfit.common.db.models.Measures;

/**
 * Created by alexeyreznik on 24/08/2017.
 */

public class MeasuresAdapter {

    @FromJson
    Measures fromJson(double[] measures) {
        if (measures.length != 10) {
            throw new JsonDataException("Incorrect measures array size: " + measures.length);
        }
        return new Measures(
                measures[0],
                measures[1],
                measures[2],
                measures[3],
                measures[4],
                measures[5],
                measures[6],
                measures[7],
                measures[8],
                measures[9]);
    }
}

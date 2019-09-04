package com.tryfit.common.db.models;

import org.json.JSONArray;
import org.json.JSONException;

import io.realm.RealmObject;

/**
 * Created by alexeyreznik on 24/08/2017.
 */

public class Measures extends RealmObject {
    private double stickLength;
    private double ballWidth;
    private double heelWidth;
    private double toeHeight;
    private double ballGirth;
    private double instepGirth;
    private double heelGirth;
    private double ankleGirth;
    private double calfGirth;
    private double calfHeight;

    public Measures() {

    }

    public Measures(double stickLength, double ballWidth, double heelWidth, double toeHeight, double ballGirth,
                    double instepGirth, double heelGirth, double ankleGirth, double calfGirth, double calfHeight) {
        this.stickLength = stickLength;
        this.ballWidth = ballWidth;
        this.heelWidth = heelWidth;
        this.toeHeight = toeHeight;
        this.ballGirth = ballGirth;
        this.instepGirth = instepGirth;
        this.heelGirth = heelGirth;
        this.ankleGirth = ankleGirth;
        this.calfGirth = calfGirth;
        this.calfHeight = calfHeight;
    }

    public Measures(double[] measures) {
        this.stickLength = measures[0];
        this.ballWidth = measures[1];
        this.heelWidth = measures[2];
        this.toeHeight = measures[3];
        this.ballGirth = measures[4];
        this.instepGirth = measures[5];
        this.heelGirth = measures[6];
        this.ankleGirth = measures[7];
        this.calfGirth = measures[8];
        this.calfHeight = measures[9];
    }

    public Double[] toArray() {
        Double[] result = new Double[10];
        result[0] = stickLength;
        result[1] = ballWidth;
        result[2] = heelWidth;
        result[3] = toeHeight;
        result[4] = ballGirth;
        result[5] = instepGirth;
        result[6] = heelGirth;
        result[7] = ankleGirth;
        result[8] = calfGirth;
        result[9] = calfHeight;
        return result;
    }

    public JSONArray toJsonArray() {
        JSONArray result = new JSONArray();
        try {
            result.put(stickLength);
            result.put(ballWidth);
            result.put(heelWidth);
            result.put(toeHeight);
            result.put(ballGirth);
            result.put(instepGirth);
            result.put(heelGirth);
            result.put(ankleGirth);
            result.put(calfGirth);
            result.put(calfHeight);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("stickLength " + stickLength + "\n");
        sb.append("ballWidth " + ballWidth + "\n");
        sb.append("heelWidth " + heelWidth + "\n");
        sb.append("toeHeight " + toeHeight + "\n");
        sb.append("ballGirth " + ballGirth + "\n");
        sb.append("instepGirth " + instepGirth + "\n");
        sb.append("heelGirth " + heelGirth + "\n");
        sb.append("ankleGirth " + ankleGirth + "\n");
        sb.append("calfGirth " + calfGirth + "\n");
        sb.append("calfHeight " + calfHeight + "\n");
        return sb.toString();
    }

    public double getStickLength() {
        return stickLength;
    }

    public void setStickLength(double stickLength) {
        this.stickLength = stickLength;
    }

    public double getBallWidth() {
        return ballWidth;
    }

    public void setBallWidth(double ballWidth) {
        this.ballWidth = ballWidth;
    }

    public double getHeelWidth() {
        return heelWidth;
    }

    public void setHeelWidth(double heelWidth) {
        this.heelWidth = heelWidth;
    }

    public double getToeHeight() {
        return toeHeight;
    }

    public void setToeHeight(double toeHeight) {
        this.toeHeight = toeHeight;
    }

    public double getBallGirth() {
        return ballGirth;
    }

    public void setBallGirth(double ballGirth) {
        this.ballGirth = ballGirth;
    }

    public double getInstepGirth() {
        return instepGirth;
    }

    public void setInstepGirth(double instepGirth) {
        this.instepGirth = instepGirth;
    }

    public double getHeelGirth() {
        return heelGirth;
    }

    public void setHeelGirth(double heelGirth) {
        this.heelGirth = heelGirth;
    }

    public double getAnkleGirth() {
        return ankleGirth;
    }

    public void setAnkleGirth(double ankleGirth) {
        this.ankleGirth = ankleGirth;
    }

    public double getCalfGirth() {
        return calfGirth;
    }

    public void setCalfGirth(double calfGirth) {
        this.calfGirth = calfGirth;
    }

    public double getCalfHeight() {
        return calfHeight;
    }

    public void setCalfHeight(double calfHeight) {
        this.calfHeight = calfHeight;
    }
}

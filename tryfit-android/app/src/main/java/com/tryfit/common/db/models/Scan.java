package com.tryfit.common.db.models;

import android.os.Parcelable;

import io.realm.RealmObject;

/**
 * Created by alexeyreznik on 19/06/2017.
 */

public class Scan extends RealmObject {

    private String clientID;
    private String scannerID;
    private int created;
    private int updated;
    private Measures leftMeasures;
    private Measures rightMeasures;
    private int weight;
    private boolean deleted;
    private int duration;

    public Scan() {

    }

    @Override
    public String toString() {
        return "Scan{" +
                ", clientID='" + clientID + '\'' +
                ", scannerID='" + scannerID + '\'' +
                ", created=" + created +
                ", updated=" + updated +
                ", leftMeasures=" + leftMeasures +
                ", rightMeasures=" + rightMeasures +
                ", weight=" + weight +
                ", deleted=" + deleted +
                ", duration=" + duration +
                '}';
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getScannerID() {
        return scannerID;
    }

    public void setScannerID(String scannerID) {
        this.scannerID = scannerID;
    }

    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }

    public Measures getLeftMeasures() {
        return leftMeasures;
    }

    public void setLeftMeasures(Measures leftMeasures) {
        this.leftMeasures = leftMeasures;
    }

    public Measures getRightMeasures() {
        return rightMeasures;
    }

    public void setRightMeasures(Measures rightMeasures) {
        this.rightMeasures = rightMeasures;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}

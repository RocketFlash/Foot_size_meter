package com.tryfit.common.db.models;

import android.support.annotation.NonNull;

/**
 * Created by alexeyreznik on 14/09/2017.
 */

public class Size implements Comparable<Size> {
    private float value;
    private float fitrate;
    private float fitrateABS;
    private boolean available;

    public Size(float value, float fitrate, boolean available) {
        this.value = value;
        this.fitrate = fitrate;
        this.available = available;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getFitrate() {
        return fitrate;
    }

    public void setFitrate(float fitrate) {
        this.fitrate = fitrate;
    }

    public float getFitrateAbs() {
        return fitrateABS;
    }

    public void setFitrateAbs(float fitrateAbs) {
        this.fitrateABS = fitrateAbs;
    }

    public boolean getAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public int compareTo(@NonNull Size size) {
        if (this.value > size.getValue()) {
            return 1;
        } else if (this.value < size.getValue()) {
            return -1;
        } else {
            return 0;
        }
    }
}

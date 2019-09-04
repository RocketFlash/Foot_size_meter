package com.tryfit.common.train;

/**
 * Created by alexeyreznik on 28/08/2017.
 */

public interface OnTrainConnectedListener {
    public void onTrainConnected();
    public void onError(String message);
}

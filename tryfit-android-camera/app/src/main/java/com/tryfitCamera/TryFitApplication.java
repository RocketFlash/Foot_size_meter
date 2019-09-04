package com.tryfitCamera;

import android.app.Application;

import com.tryfitCamera.train.Train;

/**
 * Created by alexeyreznik on 19/06/2017.
 */

public class TryFitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initTrain();
    }

    public void initTrain() {
        Train train = Train.getInstance();
        train.url = "wss://test.try.fit:10443/train";
        train.Open(false);
    }
}

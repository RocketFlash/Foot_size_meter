package com.tryfit;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.Picasso;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by alexeyreznik on 19/06/2017.
 */

public class TryFitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //Init Realm Database
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

        //Init crashlytics
        Fabric.with(this, new Crashlytics());
    }
}

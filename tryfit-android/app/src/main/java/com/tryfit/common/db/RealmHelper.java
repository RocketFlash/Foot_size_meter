package com.tryfit.common.db;

import com.tryfit.common.db.models.Client;
import com.tryfit.common.db.models.FITModel;
import com.tryfit.common.db.models.Measures;
import com.tryfit.common.db.models.Mesh;
import com.tryfit.common.db.models.Scan;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by alexeyreznik on 24/08/2017.
 */

public class RealmHelper {

    public static final String KEY_LEFT_FOOT_MODEL = "left";
    public static final String KEY_RIGHT_FOOT_MODEL = "right";
    public static final String KEY_2D_SCAN_LEFT = "2d_scan_left";
    public static final String KEY_2D_SCAN_RIGHT = "2d_scan_right";

    public static Client getCurrentClient(Realm realm) {
        return realm.where(Client.class).findFirst();
    }

    public static void saveCurrentClient(Realm realm, Client client) {
        realm.beginTransaction();
        realm.copyToRealm(client);
        realm.commitTransaction();
    }

    public static void deleteCurrentClient(Realm realm) {
        realm.beginTransaction();
        RealmResults<Client> clients = realm.where(Client.class).findAll();
        clients.deleteAllFromRealm();
        RealmResults<Scan> scans = realm.where(Scan.class).findAll();
        scans.deleteAllFromRealm();
        RealmResults<Measures> measures = realm.where(Measures.class).findAll();
        measures.deleteAllFromRealm();
        RealmResults<FITModel> fitModels = realm.where(FITModel.class).findAll();
        fitModels.deleteAllFromRealm();
        RealmResults<Mesh> meshes = realm.where(Mesh.class).findAll();
        meshes.deleteAllFromRealm();
        realm.commitTransaction();
    }

    public static void updateCurrentClient(Realm realm, Client client) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(client);
        realm.commitTransaction();
    }

    public static void saveFootModel(Realm realm, FITModel model, String id) {
        model.setId(id);
        realm.beginTransaction();
        realm.copyToRealm(model);
        realm.commitTransaction();
    }

    public static FITModel getFootModel(Realm realm, String id) {
        return realm.where(FITModel.class).equalTo("id", id).findFirst();
    }

    public static void saveScan2D(Realm realm, String foot, float stickLength, float ballWidth) {
        Client client = getCurrentClient(realm);
        realm.beginTransaction();
        Measures measures = realm.createObject(Measures.class);
        measures.setStickLength(stickLength);
        measures.setBallWidth(ballWidth);
        Scan scan2D = client.getScan2D();
        if (scan2D == null) {
            scan2D = realm.createObject(Scan.class);
            client.setScan2D(scan2D);
        }
        if (foot != null && foot.equals("left")) {
            scan2D.setLeftMeasures(measures);
        } else {
            scan2D.setRightMeasures(measures);
        }
        realm.commitTransaction();
    }
}

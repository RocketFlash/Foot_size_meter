package com.tryfit.common.db.models;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexeyreznik on 24/07/2017.
 */

public class FITModel extends RealmObject {

    @PrimaryKey
    private String id;
    private RealmList<Mesh> meshes;

    public FITModel() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Mesh> getMeshes() {
        return meshes;
    }

    public void setMeshes(Mesh[] meshes) {
        this.meshes = new RealmList<Mesh>(meshes);
    }

    @Override
    public String toString() {
        return "FITModel{" +
                "meshes=" + meshes +
                '}';
    }
}

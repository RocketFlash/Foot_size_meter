package com.tryfit.common.db.models;

/**
 * Created by alexeyreznik on 12/09/2017.
 */

public class Group {
    public static final String GROUP_NAME_ALL = "All";
    public static final String GROUP_ID_ALL = "";

    private String id;
    private String name;

    public Group(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

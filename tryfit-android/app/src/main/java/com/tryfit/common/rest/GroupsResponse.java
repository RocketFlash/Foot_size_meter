package com.tryfit.common.rest;

import com.tryfit.common.db.models.Group;

import java.util.List;

/**
 * Created by alexeyreznik on 28/09/2017.
 */

public class GroupsResponse {

    private GroupsData data;

    public GroupsData getData() {
        return data;
    }

    public static class GroupsData {
        List<Group> groups;

        public List<Group> getGroups() {
            return groups;
        }
    }
}

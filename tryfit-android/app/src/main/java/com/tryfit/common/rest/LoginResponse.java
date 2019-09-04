package com.tryfit.common.rest;

import com.tryfit.common.db.models.Client;

/**
 * Created by alexeyreznik on 18/08/2017.
 */

public class LoginResponse {
    private String access_token;
    private Client client;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "access_token='" + access_token + '\'' +
                ", client=" + client +
                '}';
    }
}

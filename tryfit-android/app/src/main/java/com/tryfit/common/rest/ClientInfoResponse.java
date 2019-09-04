package com.tryfit.common.rest;

import com.tryfit.common.db.models.Client;

/**
 * Created by alexeyreznik on 28/09/2017.
 */

public class ClientInfoResponse {
    private ClientData data;

    public ClientData getData() {
        return data;
    }

    public static class ClientData {
        private Client client;

        public Client getClient() {
            return client;
        }
    }
}

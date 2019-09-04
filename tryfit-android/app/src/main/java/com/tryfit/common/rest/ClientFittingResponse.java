package com.tryfit.common.rest;

import com.tryfit.common.db.models.FittingItem;

import java.util.List;

/**
 * Created by alexeyreznik on 28/09/2017.
 */

public class ClientFittingResponse {
    private ClientFittingData data;

    public ClientFittingData getData() {
        return data;
    }

    public static class ClientFittingData {
        private ClientFitting clientFitting;

        public ClientFitting getClientFitting() {
            return clientFitting;
        }

        public static class ClientFitting {
            private int total;
            private List<FittingItem> items;

            public int getTotal() {
                return total;
            }

            public List<FittingItem> getItems() {
                return items;
            }
        }
    }
}

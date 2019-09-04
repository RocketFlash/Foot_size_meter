package com.tryfit.common.train;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by alexeyreznik on 25/08/2017.
 */

public class TrainHelper {
    private static final String TAG = TrainHelper.class.getSimpleName();

    public static final String TRAIN_DEMO_SERVER_URL = "wss://demo.try.fit/train";
    public static final String TRAIN_TEST_SERVER_URL = "wss://test.try.fit:10443/train";
    public static final String TRAIN_SCANNER_URL = "wss://scanner.try.fit:10443/train";

    private static TrainHelper instance = null;
    private Train mTrainSession;

    private TrainHelper() {
    }

    public static TrainHelper getInstance() {
        if (instance == null) {
            instance = new TrainHelper();
        }
        return instance;
    }

    synchronized public void initTrainSession(String url, @Nullable String accessToken, final OnTrainConnectedListener listener) {
        mTrainSession = Train.getInstance();
        mTrainSession.url = url;
        mTrainSession.token = accessToken;
        mTrainSession.Open(false, new OnTrainConnectedListener() {
            @Override
            public void onTrainConnected() {
                listener.onTrainConnected();
            }

            @Override
            public void onError(String message) {
                listener.onError(message);
            }
        });
    }

    public void closeTrainSession() {
        if (mTrainSession != null) {
            mTrainSession.Close();
            mTrainSession = null;
        }
    }

    public boolean isTrainSessionOpen() {
        if (mTrainSession != null) {
            return mTrainSession.isOpen();
        } else {
            return false;
        }
    }

    public void getModels(String clientId, ICallback callback) {
        if (mTrainSession != null) {
            final ArrayList inputArgs = new ArrayList();
            inputArgs.add(clientId);
            inputArgs.add(0); //Speed
            mTrainSession.Call("admin.clients.model", inputArgs, null, callback);
        } else {
            Log.e(TAG, "trainSession is null");
        }
    }

    public void getModelsFromScanner(String clientId, ICallback callback) {
        if (mTrainSession != null) {
            final ArrayList inputArgs = new ArrayList();
            inputArgs.add(clientId);
            inputArgs.add(0); //Speed
            mTrainSession.Call("scanner.points.get", inputArgs, null, callback);
        } else {
            Log.e(TAG, "trainSession is null");
        }
    }

    public void createClient(JSONObject clientObject, ICallback callback) {
        if (mTrainSession != null) {
            final ArrayList inputArgs = new ArrayList();
            inputArgs.add(clientObject);
            mTrainSession.Call("scanner.clients.create", inputArgs, null, callback);
        } else {
            Log.e(TAG, "trainSession is null");
        }
    }

    public void startScan(String clientId, ICallback callback) {
        if (mTrainSession != null) {
            final ArrayList inputArgs = new ArrayList();
            inputArgs.add("");
            if (Objects.equals(mTrainSession.url, TRAIN_TEST_SERVER_URL)) {
                clientId = "f842b85e-f725-42cf-86aa-076075d9bfb6";
            }
            inputArgs.add(clientId);
            inputArgs.add(0); //Speed
            mTrainSession.Call("scanner.action.start", inputArgs, null, callback);
        } else {
            Log.e(TAG, "trainSession is null");
        }
    }

    public void getClientInfo(String clientId, ICallback callback) {
        if (mTrainSession != null) {
            String query = "{\n" +
                    "client(id:\"" + clientId + "\"){\n" +//client
                    "name,\n" +
                    "contractorID,\n" +
                    "scan {\n" +//scan
                    "id,\n" +
                    "clientID,\n" +
                    "scannerID,\n" +
                    "created,\n" +
                    "updated,\n" +
                    "leftMeasures,\n" +
                    "rightMeasures,\n" +
                    "weight,\n" +
                    "deleted\n" +
                    "}\n" +//scan
                    "}\n" +//client
                    "}\n";

            final ArrayList inputArgs = new ArrayList();
            inputArgs.add(query);
            mTrainSession.Call("api.graphql", inputArgs, null, callback);
        } else {
            Log.e(TAG, "trainSession is null");
        }
    }

    public void getClientFitting(String clientId, String groupId, int start, int limit, ICallback callback) {
        if (mTrainSession != null) {
            String query = "query($filter: [FilterInput], $sort: [SortInput]) {\n" +
                    "  clientFitting(\n" +
                    "    filter: $filter,\n" +
                    "    sort: $sort,\n" +
                    "    start: " + start + ",\n" +
                    "    limit: " + limit + ",\n" +
                    "    clientID: \"" + clientId + "\"\n" +
                    "  ) {\n" +
                    "    total,\n" +
                    "    items {\n" +
                    "    product {\n" +
                    "      id, code, name, pictures, price, sex, groupID, sizes {\n" +
                    "        value, fitrate, fitrateABS, available }\n" +
                    "      },\n" +
                    "    size {\n" +
                    "      value, fitrate, fitrateABS, available}\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";

            String queryParams = "{\n" +
                    "  \"filter\": [{\"field\": \"product.groupID\", \"query\": \"" + groupId + "\"}]," +
                    "  \"sort\": [{\"field\": \"size.fitrateAbs\", \"dir\": \"desc\"}]\n" +
                    "}";

            final ArrayList inputArgs = new ArrayList();
            inputArgs.add(query);
            inputArgs.add(queryParams);
            mTrainSession.Call("api.graphql", inputArgs, null, callback);
        } else {
            Log.e(TAG, "trainSession is null");
        }
    }

    public void getGroups(String contractorId, ICallback callback) {
        if (mTrainSession != null) {
            String query = "query{groups(contractorID:\"" + contractorId + "\") {\n" +
                    "  id,\n" +
                    "  name\n" +
                    "}}";

            final ArrayList inputArgs = new ArrayList();
            inputArgs.add(query);
            mTrainSession.Call("api.graphql", inputArgs, null, callback);
        } else {
            Log.e(TAG, "trainSession is null");
        }
    }

    public void loginInPlugin(String clientID, String sid, ICallback callback) {
        if (mTrainSession != null) {
            final ArrayList inputArgs = new ArrayList();
            inputArgs.add(clientID);
            inputArgs.add(sid);
            mTrainSession.Call("api.clients.loginInPlugin", inputArgs, null, callback);
        } else {
            Log.e(TAG, "trainSession is null");
        }
    }
}

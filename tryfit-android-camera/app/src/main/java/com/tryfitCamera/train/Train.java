package com.tryfitCamera.train;

import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLContext;

/**
 * Created by Miha-ha on 27.06.16.
 */
public class Train {
    private static Train ourInstance = new Train();
    public String sid;
    public String url;
    private WebSocket socket;
    private int requestID = 0;
    private Map<Integer, ICallback> handlers = new HashMap<>();

    private Train() {
        this.sid = "" + UUID.randomUUID();
    }

    public static Train getInstance() {
        return ourInstance;
    }

    private int nextID() {
        return requestID++;
    }

    public void Call(String event, ArrayList args, byte[][] data, ICallback callback) {
        Log.d("Train", String.format("Call %s with args: %s", event, args));

//        callback.onResult(args, "no errors");
        //test
//        int id = nextID();
//        handlers.put(id, callback);
//        ICallback cb = handlers.get(id);
//        cb.onResult(args, "test error");

        if (socket.getState() == WebSocketState.OPEN) {
            int id = nextID();
            handlers.put(id, callback);

            RPCRequest req = new RPCRequest(id, event, args, data);
            byte[] binary = req.Encode();
//            Log.i("Train", "Binary: "+ Arrays.toString(binary));
            socket.sendBinary(binary);
        } else {
            Log.e("Train", "Connection state:" + socket.getState());
        }
    }

    private void Decode(ByteBuffer bytes) {
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        byte cmd = bytes.get();
        Log.i("Train", "cmd:" + cmd);
        int headerSize = bytes.getInt();
        Log.i("Train", "headerSize:" + headerSize);
        byte[] headerData = new byte[headerSize];
        bytes.get(headerData);
        JSONArray header;
        int id;
        try {
            header = new JSONArray(new String(headerData));
//            Log.i("Train", "header:" + a);
            id = header.getInt(0);
        } catch (JSONException e) {
            Log.e("Train", "Decode header error:" + e.getMessage());
            return;
        }

        IResponse response;

        switch (cmd) {
            case 0://error
                response = new ErrorResponse(header);
                break;
            case 1://ok
                response = new OKResponse();
                break;
            case 11://rpc
                response = new RPCResponse(header, bytes);
                break;
            default:
                response = new ErrorResponse("Handler for command not found: " + cmd);
                break;
        }

        ICallback callback = handlers.get(id);
        callback.onResult(response.Args(), response.Error());

    }

    public void Open(boolean force) {
        Log.i("Train", "Opening...");
        if (force && socket != null) {
            socket.disconnect();
            socket = null;

        }

        URI uri = URI.create(this.url);
        Log.i("Train", "Uri: " + uri);

        if (socket == null) {

            WebSocketFactory factory = new WebSocketFactory();

            factory.setConnectionTimeout(5000);

            try {
                // Create a custom SSL context.
                SSLContext context = NaiveSSLContext.getInstance("TLS");

                // Set the custom SSL context.
                factory.setSSLContext(context);
                factory.setVerifyHostname(false);

                socket = factory.createSocket(uri)
                        .setPingInterval(60 * 1000)
                        .addListener(new WebSocketAdapter() {
                            @Override
                            public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                                super.onConnected(websocket, headers);

                                Log.i("Train", "Connected with sid: " + sid);
                            }

                            @Override
                            public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                                super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);

                                Log.e("Train", "Disconnected");
                                Reopen(1000);
                            }

                            @Override
                            public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                                super.onCloseFrame(websocket, frame);

                                Log.e("Train", "Close");
                                Reopen(1000);
                            }

                            @Override
                            public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                                super.onError(websocket, cause);

                                Log.e("Train", "Error:" + cause.getLocalizedMessage());
//                        Reopen(1000);
                            }

                            @Override
                            public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
                                super.onConnectError(websocket, exception);

                                Log.e("Train", "Connect error:" + exception.getLocalizedMessage());
                                Reopen(1000);
                            }

                            @Override
                            public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {
                                super.onUnexpectedError(websocket, cause);

                                Log.e("Train", "Unexpected error:" + cause.getLocalizedMessage());
                                Reopen(1000);

                            }

                            @Override
                            public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
                                super.onBinaryMessage(websocket, binary);

                                Decode(ByteBuffer.wrap(binary));
                            }
                        })
                        .connectAsynchronously();

            } catch (Exception e) {
                Log.e("Train", "Create WebSocket error:" + e.getLocalizedMessage());
            }

        }
    }

    public void Reopen(int delay) {
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        Open(true);
                    }
                },
                delay
        );
    }

}

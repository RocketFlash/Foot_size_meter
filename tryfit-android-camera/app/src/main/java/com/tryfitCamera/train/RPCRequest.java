package com.tryfitCamera.train;

import android.util.Log;

import org.json.JSONArray;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * Created by Miha-ha on 30.07.16.
 */
public class RPCRequest {
    private int id;
    private String event;
    private ArrayList args;
    private byte[][] data;

    RPCRequest(int id, String event, ArrayList args, byte[][] data) {

        this.id = id;
        this.event = event;
        this.args = args == null ? new ArrayList() : args;
        this.data = data == null ? new byte[][]{} : data;
    }

    public byte[] Encode() {

        int totalSize = 5; //reserve for cmd-1 and headerSize-4
        JSONArray sizes = new JSONArray();


        for (int i = 0; i < data.length; i++) {
            sizes.put(data[i].length);
            totalSize += data[i].length;
        }

        JSONArray header = new JSONArray();
        header.put(id);
        header.put(event);

        JSONArray argsArray = new JSONArray();
        for (int i = 0; i < args.size(); i++) {
            argsArray.put(args.get(i));
        }
        header.put(argsArray);
        header.put(sizes);

        byte[] headerBytes = header.toString().getBytes();
        totalSize += headerBytes.length;

        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) 10); //cmd rpc
        Log.i("Train", String.format("header length: %d, total: %d", (short) headerBytes.length, totalSize));
        buffer.putInt(headerBytes.length);
        buffer.put(headerBytes);
//
        for (int i = 0; i < data.length; i++) {
            buffer.put(data[i]);
        }

        Log.i("Train", String.format("Encode done"));

        return buffer.array();
    }
}

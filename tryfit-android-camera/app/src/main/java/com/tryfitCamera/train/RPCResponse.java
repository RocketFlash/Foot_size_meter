package com.tryfitCamera.train;

import org.json.JSONArray;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by Miha-ha on 02.07.16.
 */
public class RPCResponse implements IResponse {
    private ArrayList args = new ArrayList();
    private String error;

    RPCResponse(JSONArray header, ByteBuffer buffer) {
        Decode(header, buffer);
    }

    public void Decode(JSONArray header, ByteBuffer buffer) {
        //TODO: check header length

        try {
            JSONArray argsJson = header.getJSONArray(1);
            for (int i = 0; i < argsJson.length(); i++) {
                args.add(argsJson.get(i));
            }

            /*
                let sizes = p.json[2];
                for (let size of sizes) {
                    let buffer = new ArrayBuffer(size);
                    let a = new Uint8Array(buffer);
                    a.set(new Uint8Array(data, offset, size));
                    args.push(buffer)
                    offset += size;
                }
             */
//            val sizes = header.getJSONArray(2)
//            var offset = 0
//            for (i in 0..(sizes.length() - 1)) {
//                val size = sizes.getInt(i)
//                val data = ByteArray(size)
//                buffer.get(data, offset, size)
//                args.add(data)
//                offset += size
//            }

            JSONArray sizesJson = header.getJSONArray(2);

            for (int i = 0; i < sizesJson.length(); i++) {
                int size = sizesJson.getInt(i);
                byte[] data = new byte[size];
//                Log.i("Response", "position:"+buffer.position());
                buffer.get(data, 0, size);

                args.add(data);
            }

        } catch (Exception e) {
//            e.printStackTrace();
            error = String.format("RPCResponse.Decode error: %s", e.getMessage());
        }
    }

    @Override
    public ArrayList Args() {
        return args;
    }

    @Override
    public String Error() {
        return error;
    }
}

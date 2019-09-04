package com.tryfit.common.parsers;

import android.content.Context;
import android.util.Log;

import com.tryfit.R;
import com.tryfit.common.db.models.FITModel;
import com.tryfit.common.db.models.Mesh;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by alexeyreznik on 24/07/2017.
 */

public class FITParser {

    private static final String TAG = FITParser.class.getSimpleName();

    public static void test(Context context) {

        InputStream is = context.getResources().openRawResource(R.raw.left);
        FITModel leftModel = parse(is);
        Log.d(TAG, "Left foot: \n" + leftModel);

        is = context.getResources().openRawResource(R.raw.right);
        FITModel rightModel = parse(is);
        Log.d(TAG, "Right foot: \n" + rightModel);
    }

    public static FITModel parse(InputStream is) {
        try {
            int size = is.available();
            return parse(is, size);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static FITModel parse(InputStream is, int size) {
        FITModel model = new FITModel();
        try {
            Log.d(TAG, "size: " + size);
            byte[] data = new byte[size];
            DataInputStream dis = new DataInputStream(is);

            dis.readFully(data, 0, size);

            Log.d(TAG, "data: " + data.length);

            byte[] headerSizeData = Arrays.copyOfRange(data, 0, 4);
            int headerSize = Integer.parseInt(new String(headerSizeData), 16);
            Log.d(TAG, "headerSize: " + headerSize);

            byte[] headerData = Arrays.copyOfRange(data, 0, headerSize);
            String header = new String(headerData);
            Log.d(TAG, "header: \n" + header + " length: " + headerData.length);

            byte[] vboData = Arrays.copyOfRange(data, headerSize, size);
            Log.d(TAG, "body: " + vboData.length);

            int nmesh = 0;
            int meshIdx = 0;
            Mesh[] meshes = null;

            String[] lines = header.split("\n");
            for (String line : lines) {
                if (line.startsWith("nmesh")) {
                    nmesh = Integer.valueOf(line.split("\\s+")[1]);
                    meshes = new Mesh[nmesh];
                }
                if (line.startsWith("mesh")) {
                    String[] tokens = line.split("\\s+");
                    if (tokens.length >= 4) {
                        String name = tokens[1];
                        String attr = tokens[2];
                        int count = Integer.valueOf(tokens[3]);
                        int trisCount = Integer.valueOf(tokens[4]);

                        Log.d(TAG, "count: " + count + " trisCount: " + trisCount);

                        Mesh mesh = new Mesh();
                        mesh.setName(name);
                        mesh.setNumOfIndices(trisCount * 3);
                        mesh.setVboData(vboData);

                        if (attr.contains("v")) {
                            mesh.setNumOfVertices(count);
                        }
                        if (attr.contains("n")) {
                            mesh.setNumOfNormals(count);
                        }
                        if (attr.contains("t")) {
                            mesh.setNumOfUvs(count);
                        }
                        if (attr.contains("c")) {
                            mesh.setNumOfColors(count);
                        }

                        if (meshes != null) {
                            meshes[meshIdx] = mesh;
                        }
                    }
                } else if (line.startsWith("vbo_offset")) {
                    String[] offsets = line.substring(11).split("\\s+");
                    int index = 0;

                    if (meshes != null && meshes.length > 0) {
                        Mesh mesh = meshes[meshIdx];

                        if (mesh.getNumOfVertices() > 0) {
                            mesh.setVerticesOffset(Integer.valueOf(offsets[index]));
                            index++;
                        }

                        if (mesh.getNumOfNormals() > 0) {
                            mesh.setNormalsOffset(Integer.valueOf(offsets[index]));
                            index++;
                        }

                        if (mesh.getNumOfUvs() > 0) {
                            mesh.setUvsOffset(Integer.valueOf(offsets[index]));
                            index++;
                        }

                        if (mesh.getNumOfColors() > 0) {
                            mesh.setColorsOffset(Integer.valueOf(offsets[index]));
                            index++;
                        }

                        if (mesh.getNumOfIndices() > 0) {
                            mesh.setIndicesOffset(Integer.valueOf(offsets[index]));
                            index++;
                        }
                    }
                    meshIdx++;
                }
            }

            model.setMeshes(meshes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return model;
    }
}

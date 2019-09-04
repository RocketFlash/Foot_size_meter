package com.tryfit.common.db.models;

import java.util.Arrays;

import io.realm.RealmObject;

/**
 * Created by alexeyreznik on 24/08/2017.
 */

public class Mesh extends RealmObject {
    private String mName;
    private int mNumOfVertices;
    private int mNumOfNormals;
    private int mNumOfIndices;
    private int mNumOfUvs;
    private int mNumOfColors;
    private int mVerticesOffset;
    private int mNormalsOffset;
    private int mUvsOffset;
    private int mColorsOffset;
    private int mIndicesOffset;
    private byte[] mVboData;

    public void setName(String name) {
        this.mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setNumOfVertices(int numOfVertices) {
        this.mNumOfVertices = numOfVertices;
    }

    public void setNumOfNormals(int numOfNormals) {
        this.mNumOfNormals = numOfNormals;
    }

    public void setNumOfIndices(int numOfIndices) {
        this.mNumOfIndices = numOfIndices;
    }

    public void setNumOfUvs(int numOfUvs) {
        this.mNumOfUvs = numOfUvs;
    }

    public void setNumOfColors(int numOfColors) {
        this.mNumOfColors = numOfColors;
    }

    public int getNumOfVertices() {
        return mNumOfVertices;
    }

    public void setVerticesOffset(int verticesOffset) {
        this.mVerticesOffset = verticesOffset;
    }

    public int getNumOfNormals() {
        return mNumOfNormals;
    }

    public void setNormalsOffset(int normalsOffset) {
        this.mNormalsOffset = normalsOffset;
    }

    public int getNumOfUvs() {
        return mNumOfUvs;
    }

    public void setUvsOffset(int uvsOffset) {
        this.mUvsOffset = uvsOffset;
    }

    public int getNumOfColors() {
        return mNumOfColors;
    }

    public void setColorsOffset(int colorsOffset) {
        this.mColorsOffset = colorsOffset;
    }

    public int getNumOfIndices() {
        return mNumOfIndices;
    }

    public void setIndicesOffset(int indicesOffset) {
        this.mIndicesOffset = indicesOffset;
    }

    public int getVerticesOffset() {
        return mVerticesOffset;
    }

    public Integer getNormalsOffset() {
        return mNormalsOffset;
    }

    public int getUvsOffset() {
        return mUvsOffset;
    }

    public int getColorsOffset() {
        return mColorsOffset;
    }

    public Integer getIndicesOffset() {
        return mIndicesOffset;
    }

    public void setVboData(byte[] vboData) {
        this.mVboData = vboData;
    }

    public byte[] getVboData() {
        return mVboData;
    }

    @Override
    public String toString() {
        return "Mesh{" +
                "mName='" + mName + '\'' +
                ", mNumOfVertices=" + mNumOfVertices +
                ", mNumOfNormals=" + mNumOfNormals +
                ", mNumOfIndices=" + mNumOfIndices +
                ", mNumOfUvs=" + mNumOfUvs +
                ", mNumOfColors=" + mNumOfColors +
                ", mVerticesOffset=" + mVerticesOffset +
                ", mNormalsOffset=" + mNormalsOffset +
                ", mUvsOffset=" + mUvsOffset +
                ", mColorsOffset=" + mColorsOffset +
                ", mIndicesOffset=" + mIndicesOffset +
                ", mVboData=" + Arrays.toString(mVboData) +
                '}';
    }
}
package com.tryfit.scans.opengl;

/**
 * Created by alexeyreznik on 11/09/2017.
 */

public class Mesh {

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

    public static Mesh fromRealmMesh(com.tryfit.common.db.models.Mesh realmMesh) {
        Mesh mesh = new Mesh();
        mesh.mName = realmMesh.getName();
        mesh.mNumOfVertices = realmMesh.getNumOfVertices();
        mesh.mNumOfNormals = realmMesh.getNumOfNormals();
        mesh.mNumOfIndices = realmMesh.getNumOfIndices();
        mesh.mNumOfUvs = realmMesh.getNumOfUvs();
        mesh.mNumOfColors = realmMesh.getNumOfColors();
        mesh.mVerticesOffset = realmMesh.getVerticesOffset();
        mesh.mNormalsOffset = realmMesh.getNormalsOffset();
        mesh.mUvsOffset = realmMesh.getUvsOffset();
        mesh.mColorsOffset = realmMesh.getColorsOffset();
        mesh.mIndicesOffset = realmMesh.getIndicesOffset();
        mesh.mVboData = realmMesh.getVboData();
        return mesh;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public int getNumOfVertices() {
        return mNumOfVertices;
    }

    public void setNumOfVertices(int mNumOfVertices) {
        this.mNumOfVertices = mNumOfVertices;
    }

    public int getNumOfNormals() {
        return mNumOfNormals;
    }

    public void setNumOfNormals(int mNumOfNormals) {
        this.mNumOfNormals = mNumOfNormals;
    }

    public int getNumOfIndices() {
        return mNumOfIndices;
    }

    public void setNumOfIndices(int mNumOfIndices) {
        this.mNumOfIndices = mNumOfIndices;
    }

    public int getNumOfUvs() {
        return mNumOfUvs;
    }

    public void setNumOfUvs(int mNumOfUvs) {
        this.mNumOfUvs = mNumOfUvs;
    }

    public int getNumOfColors() {
        return mNumOfColors;
    }

    public void setNumOfColors(int mNumOfColors) {
        this.mNumOfColors = mNumOfColors;
    }

    public int getVerticesOffset() {
        return mVerticesOffset;
    }

    public void setVerticesOffset(int mVerticesOffset) {
        this.mVerticesOffset = mVerticesOffset;
    }

    public int getNormalsOffset() {
        return mNormalsOffset;
    }

    public void setNormalsOffset(int mNormalsOffset) {
        this.mNormalsOffset = mNormalsOffset;
    }

    public int getUvsOffset() {
        return mUvsOffset;
    }

    public void setUvsOffset(int mUvsOffset) {
        this.mUvsOffset = mUvsOffset;
    }

    public int getColorsOffset() {
        return mColorsOffset;
    }

    public void setColorsOffset(int mColorsOffset) {
        this.mColorsOffset = mColorsOffset;
    }

    public int getIndicesOffset() {
        return mIndicesOffset;
    }

    public void setIndicesOffset(int mIndicesOffset) {
        this.mIndicesOffset = mIndicesOffset;
    }

    public byte[] getVboData() {
        return mVboData;
    }

    public void setVboData(byte[] mVboData) {
        this.mVboData = mVboData;
    }
}

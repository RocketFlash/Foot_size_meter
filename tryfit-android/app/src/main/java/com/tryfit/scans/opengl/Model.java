package com.tryfit.scans.opengl;

import android.opengl.GLES20;
import android.opengl.Matrix;

/**
 * Created by alexeyreznik on 10/08/2017.
 */

public class Model {

    private int mVertexShaderHandle;
    private int mFragmentShaderHandle;
    private int mProgramHandle;

    private int mLightPosHandle;
    private int mColorHandle;
    private int mPointSizeHandle;

    // matrix for the cube space
    float[] mModelMatrix = new float[16];

    // model-view-projection matrix
    private float[] mMVPMatrix = new float[16];

    private String mVertexShaderSrc, mFragmentShaderSrc;

    private int mAColor;
    private int mAPosition;
    private int mANormal;
    private int mATextureCoord;
    private int mUMVPMatrix;
    private int mUTexture;

    private int indicesCount;

    public void init(ModelBuffers modelBuffers, String vertexShader, String fragmentShader) {
//        indicesCount = modelBuffers.indicesCount;

        mVertexShaderSrc = vertexShader;
        mFragmentShaderSrc = fragmentShader;

        // compile the vertex shader
        mVertexShaderHandle = GLESUtil.loadShader(GLES20.GL_VERTEX_SHADER, mVertexShaderSrc);

        // compile the fragment shader
        mFragmentShaderHandle = GLESUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShaderSrc);

        // create an OpenGL program, attach both shaders to it and link it
        mProgramHandle = GLESUtil.createProgram(mVertexShaderHandle, mFragmentShaderHandle);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);

        // retrieve vertex attributes location
        mAPosition = GLES20.glGetAttribLocation(mProgramHandle, "a_position");
//		mAColor = GLES20.glGetAttribLocation(mProgramHandle, "a_color");
        mANormal = GLES20.glGetAttribLocation(mProgramHandle, "a_normal");
//		mATextureCoord = GLES20.glGetAttribLocation(mProgramHandle, "a_texture_coord");

        mLightPosHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_LightPos");
        mColorHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_color");
        mPointSizeHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_pointSize");

        // no constant attributes so enable arrays for all attributes
        GLES20.glEnableVertexAttribArray(mAPosition);
//		GLES20.glEnableVertexAttribArray(mAColor);
        GLES20.glEnableVertexAttribArray(mANormal);
//		GLES20.glEnableVertexAttribArray(mATextureCoord);

        // retrieve uniforms locations
        mUMVPMatrix = GLES20.glGetUniformLocation(mProgramHandle, "u_mvp_matrix");

//		mUTexture = GLES20.glGetUniformLocation(mProgramHandle, "s_texture");

        // now it's time to tell the attributes where the vertex data is,
        // for each attribute first the associated buffer is binded (glBindBuffer)
        // and then  the attribute location is specified (glVertexAttribPointer)
        // with data format.
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, modelBuffers.mBuffers.get(0));
//        GLES20.glVertexAttribPointer(mAPosition, 3, GLES20.GL_FLOAT, false, 0, 0);

//		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, cubeBuffers.mBuffers.get(1));
//		GLES20.glVertexAttribPointer(mAColor, 4, GLES20.GL_FLOAT, false, 0, 0);

//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, modelBuffers.mBuffers.get(2));
//        GLES20.glVertexAttribPointer(mANormal, 3, GLES20.GL_FLOAT, false, 0, 0);

//		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, cubeBuffers.mBuffers.get(3));
//		GLES20.glVertexAttribPointer(mATextureCoord, 3, GLES20.GL_FLOAT, false, 0, 0);

//        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, modelBuffers.mBuffers.get(4));

        // same thing for the texture
//		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//		GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, cubeBuffers.mTextures.get(0));
//
//		// here GL_TEXTURE0 is used so the uniform must be set to 0
//		GLES20.glUniform1i(mUTexture, 0);

//        bindBuffers(modelBuffers);
    }

    public void bindBuffers(ModelBuffers modelBuffers) {
        indicesCount = modelBuffers.indicesCount;

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, modelBuffers.mBuffers.get(0));
        GLES20.glVertexAttribPointer(mAPosition, 3, GLES20.GL_FLOAT, false, 0, 0);

//		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, cubeBuffers.mBuffers.get(1));
//		GLES20.glVertexAttribPointer(mAColor, 4, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, modelBuffers.mBuffers.get(2));
        GLES20.glVertexAttribPointer(mANormal, 3, GLES20.GL_FLOAT, false, 0, 0);

//		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, cubeBuffers.mBuffers.get(3));
//		GLES20.glVertexAttribPointer(mATextureCoord, 3, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, modelBuffers.mBuffers.get(4));

        // same thing for the texture
//		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//		GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, cubeBuffers.mTextures.get(0));
//
//		// here GL_TEXTURE0 is used so the uniform must be set to 0
//		GLES20.glUniform1i(mUTexture, 0);
    }

    public void draw(float[] viewProjectionMatrix, float[] lightPosInEyeSpace) {

        // use the mProgramHandle for which everything has been set up in init
        GLES20.glUseProgram(mProgramHandle);

        // the only operation that need to be continuously done here
        Matrix.multiplyMM(mMVPMatrix, 0, viewProjectionMatrix, 0, mModelMatrix, 0);
        GLES20.glUniformMatrix4fv(mUMVPMatrix, 1, false, mMVPMatrix, 0);

        GLES20.glUniform3f(mLightPosHandle, lightPosInEyeSpace[0], lightPosInEyeSpace[1], lightPosInEyeSpace[2]);

        GLES20.glEnable(GLES20.GL_POLYGON_OFFSET_FILL);
        GLES20.glPolygonOffset(3.0f, 3.0f);

        GLES20.glUniform4f(mColorHandle, 1f, 1f, 1f, 1.0f);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indicesCount, GLES20.GL_UNSIGNED_INT, 0);

        GLES20.glDisable(GLES20.GL_POLYGON_OFFSET_FILL);
    }
}

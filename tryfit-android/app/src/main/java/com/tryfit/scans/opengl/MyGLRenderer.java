package com.tryfit.scans.opengl;


import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * Created by alexeyreznik on 18/07/2017.
 */

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = MyGLRenderer.class.getSimpleName();

    private final float SCALE = 0.003f;
    private final Mesh mLeftMesh;
    private final Mesh mRightMesh;

    private String mVertexShaderSrc =
            "attribute vec4 a_position;"
                    + "attribute vec3 a_normal;"
                    + "varying vec3 v_normal;"
                    + "uniform mat4 u_mvp_matrix;"
                    + "varying vec3 v_Position;"
                    + "varying vec3 v_Normal;"
                    + "varying float u_pointSize;"
                    + "void main()"
                    + "{"
                    + "v_Position = vec3(u_mvp_matrix * a_position);"
                    + "v_Normal = vec3(u_mvp_matrix * vec4(a_normal, 0.0));"
                    + "gl_Position = u_mvp_matrix * a_position;"
                    + "v_normal = a_normal;"
                    + "gl_PointSize = u_pointSize;"
                    + "}";

    private String mFragmentShaderSrc =
            "#ifdef GL_FRAGMENT_PRECISION_HIGH \n"
                    + "precision highp float; \n"
                    + "#else \n"
                    + "precision mediump float; \n"
                    + "#endif \n"
                    + "uniform vec3 u_LightPos; \n"
                    + "varying vec3 v_Position; \n"
                    + "varying vec3 v_Normal; \n"
                    + "uniform vec4 u_color;\n"
                    + "void main() \n" + "{ \n"
                    + "float distance = length(u_LightPos - v_Position); \n"
                    + "vec3 lightVector = normalize(u_LightPos - v_Position); \n"
                    + "float diffuse = max(dot(v_Normal, lightVector), 0.0);"
                    + "diffuse = diffuse * (1.0 / distance); \n"
                    + "diffuse = diffuse + 0.2; \n"
                    + "gl_FragColor = u_color; \n"
                    + "} \n";

    // camera matrix
    private float[] mViewMatrix = new float[16];
    // projection matrix
    private float[] mProjectionMatrix = new float[16];

    private final float[] mLightPosInModelSpace = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
    private final float[] mLightPosInWorldSpace = new float[4];
    private final float[] mLightPosInEyeSpace = new float[4];

    private float[] mLightModelMatrix = new float[16];

    private ModelBuffers leftFootBuffers;
    private ModelBuffers rightFootBuffers;
    private ModelBuffersWireframe leftFootBuffersWireframe;
    private ModelBuffersWireframe rightFootBuffersWireframe;
    private Model leftFoot;
    private Model rightFoot;
    private ModelWireframe leftFootWireframe;
    private ModelWireframe rightFootWireframe;

    private volatile float mDeltaX;
    private volatile float mDeltaY = 50f;
    private float mScaleFactor = 1.0f;
    private long mTimeManualRotationStarted;

    private volatile boolean mManualRotationStarted = false;

    /**
     * Store the accumulated rotation.
     */
    private final float[] mAccumulatedXRotation = new float[16];

    /**
     * Store the current rotation.
     */
    private final float[] mCurrentXRotation = new float[16];

    /**
     * A temporary matrix.
     */
    private float[] mTemporaryMatrix = new float[16];

    public MyGLRenderer(Mesh leftMesh, Mesh rightMesh) {
        this.mLeftMesh = leftMesh;
        this.mRightMesh = rightMesh;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * android.opengl.GLSurfaceView.Renderer#onSurfaceCreated(javax.microedition
     * .khronos.opengles.GL10, javax.microedition.khronos.egl.EGLConfig)
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d("Render", "onSurfaceCreated");
        // Set the color buffer clear color.
        GLES20.glClearColor(0.968f, 0.976f, 0.980f, 1.0f);
//        GLES20.glClearColor(1f, 1f, 1f, 0f);
//        GLES20.glClearColor(0f, 0f, 0f, 0f);

        // Enable the depth buffer so that OpenGL take into account if an object occlude
        // another and not just draw objects over each others according to draw functions call order.
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LESS);

        // Initialize the accumulated rotation matrix
        Matrix.setIdentityM(mAccumulatedXRotation, 0);

        // Load all vertex data (positions, texture coordinates, normals
        // and textures) in video ram.

        initBuffers();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * android.opengl.GLSurfaceView.Renderer#onSurfaceChanged(javax.microedition
     * .khronos.opengles.GL10, int, int)
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d("Render", "onSurfaceChanged");
        GLES20.glViewport(0, 0, width, height);

        float aspectRatio = (float) width / (float) height;
        // set the projection as a classic perspective projection
        Matrix.perspectiveM(mProjectionMatrix, 0, 90.0f, aspectRatio, 0.1f, 1000f);
    }

    private void initBuffers() {
        Log.d("Render", "initBuffers");

        if (mLeftMesh != null) {
            leftFootBuffers = new ModelBuffers();
            leftFootBuffers.init(mLeftMesh);

            leftFoot = new Model();
            leftFoot.init(leftFootBuffers, mVertexShaderSrc, mFragmentShaderSrc);

            leftFootBuffersWireframe = new ModelBuffersWireframe();
            leftFootBuffersWireframe.init(mLeftMesh);

            leftFootWireframe = new ModelWireframe();
            leftFootWireframe.init(leftFootBuffersWireframe, mVertexShaderSrc, mFragmentShaderSrc);
        }
        if (mRightMesh != null) {
            rightFootBuffers = new ModelBuffers();
            rightFootBuffers.init(mRightMesh);

            rightFoot = new Model();
            rightFoot.init(rightFootBuffers, mVertexShaderSrc, mFragmentShaderSrc);

            rightFootBuffersWireframe = new ModelBuffersWireframe();
            rightFootBuffersWireframe.init(mRightMesh);

            rightFootWireframe = new ModelWireframe();
            rightFootWireframe.init(rightFootBuffersWireframe, mVertexShaderSrc, mFragmentShaderSrc);
        }
    }

    public void startManualRotation() {
        if (!mManualRotationStarted) {
            mManualRotationStarted = true;
        }
    }

    public void startAutomaticRotation() {
        if (mManualRotationStarted) {
            mManualRotationStarted = false;
        }
    }

    public float getDeltaX() {
        return mDeltaX;
    }

    public void setDeltaX(float deltaX) {
        this.mDeltaX = deltaX;
        mTimeManualRotationStarted = System.currentTimeMillis();
    }

    public float getDeltaY() {
        return mDeltaY;
    }

    public void setDeltaY(float deltaY) {
        if (deltaY < 0f) {
            mDeltaY = 0f;
        } else if (deltaY > 100f) {
            mDeltaY = 100f;
        } else {
            mDeltaY = deltaY;
        }
        mTimeManualRotationStarted = System.currentTimeMillis();
    }

    public float getScaleFactor() {
        return mScaleFactor;
    }

    public void setScaleFactor(float mScaleFactor) {
        this.mScaleFactor = mScaleFactor;
    }

    // how long the animation has been running
    private long elapsedTime = 0;
    // when it started
    private long startTime = 0;
    // view-projection matrix
    private float[] vp = new float[16];

    /*
     * (non-Javadoc)
     *
     * @see
     * android.opengl.GLSurfaceView.Renderer#onDrawFrame(javax.microedition
     * .khronos.opengles.GL10)
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        // clear the color and depth buffer since both of them are used
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT
                | GLES20.GL_DEPTH_BUFFER_BIT);

        long now = System.currentTimeMillis();

        // startTime and elapsedTime set the start of the animation at 0 rather
        // than using System.currentTimeMillis() directly.
        if (startTime > 0)
            elapsedTime = now - startTime;
        else
            startTime = now;


        long timeSinceLastManualRotation = now - mTimeManualRotationStarted;
        if (timeSinceLastManualRotation > 4000L) {
            startAutomaticRotation();
        }


        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -1.0f);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);

        float eyeY = getDeltaY() / 100f;

        Matrix.setLookAtM(mViewMatrix, 0, 1.0f, eyeY, 0f, 0f, 0.3f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(vp, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // complete rotation every 4 seconds
//        mRotationAngle = 0.09F * (float) (elapsedTime % 4000L);

        if (!mManualRotationStarted) {
            mDeltaX = 1F;
        }

        //Prepare rotation
        Matrix.setIdentityM(mCurrentXRotation, 0);
        Matrix.rotateM(mCurrentXRotation, 0, mDeltaX, 0.0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mTemporaryMatrix, 0, mCurrentXRotation, 0, mAccumulatedXRotation, 0);
        System.arraycopy(mTemporaryMatrix, 0, mAccumulatedXRotation, 0, 16);
        mDeltaX = 0.0f;

        //Draw left foot mesh
        if (leftFoot != null) {
            Matrix.setIdentityM(leftFoot.mModelMatrix, 0);
            Matrix.multiplyMM(mTemporaryMatrix, 0, leftFoot.mModelMatrix, 0, mAccumulatedXRotation, 0);
            System.arraycopy(mTemporaryMatrix, 0, leftFoot.mModelMatrix, 0, 16);

            Matrix.scaleM(leftFoot.mModelMatrix, 0, SCALE * mScaleFactor, SCALE * mScaleFactor, SCALE * mScaleFactor);

            leftFoot.bindBuffers(leftFootBuffers);
            leftFoot.draw(vp, mLightPosInEyeSpace);

        }

        //Draw left foot wireframe
        if (leftFootWireframe != null) {
            Matrix.setIdentityM(leftFootWireframe.mModelMatrix, 0);
            Matrix.multiplyMM(mTemporaryMatrix, 0, leftFootWireframe.mModelMatrix, 0, mAccumulatedXRotation, 0);
            System.arraycopy(mTemporaryMatrix, 0, leftFootWireframe.mModelMatrix, 0, 16);
            Matrix.scaleM(leftFootWireframe.mModelMatrix, 0, SCALE * mScaleFactor, SCALE * mScaleFactor, SCALE * mScaleFactor);

            //Draw left foot
            leftFootWireframe.bindBuffers(leftFootBuffersWireframe);
            leftFootWireframe.draw(vp, mLightPosInEyeSpace);

        }

        //Draw right foot mesh
        if (rightFoot != null) {
            Matrix.setIdentityM(rightFoot.mModelMatrix, 0);
            Matrix.multiplyMM(mTemporaryMatrix, 0, rightFoot.mModelMatrix, 0, mAccumulatedXRotation, 0);
            System.arraycopy(mTemporaryMatrix, 0, rightFoot.mModelMatrix, 0, 16);
            Matrix.scaleM(rightFoot.mModelMatrix, 0, SCALE * mScaleFactor, SCALE * mScaleFactor, SCALE * mScaleFactor);

            rightFoot.bindBuffers(rightFootBuffers);
            rightFoot.draw(vp, mLightPosInEyeSpace);
        }

        //Draw right foot wireframe
        if (rightFootWireframe != null) {
            Matrix.setIdentityM(rightFootWireframe.mModelMatrix, 0);
            Matrix.multiplyMM(mTemporaryMatrix, 0, rightFootWireframe.mModelMatrix, 0, mAccumulatedXRotation, 0);
            System.arraycopy(mTemporaryMatrix, 0, rightFootWireframe.mModelMatrix, 0, 16);
            Matrix.scaleM(rightFootWireframe.mModelMatrix, 0, SCALE * mScaleFactor, SCALE * mScaleFactor, SCALE * mScaleFactor);

            rightFootWireframe.bindBuffers(rightFootBuffersWireframe);
            rightFootWireframe.draw(vp, mLightPosInEyeSpace);
        }
    }

    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("", glOperation + ": glError " + error);
        }
    }

}

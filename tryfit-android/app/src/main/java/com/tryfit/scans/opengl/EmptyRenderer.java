package com.tryfit.scans.opengl;


import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * Created by alexeyreznik on 18/07/2017.
 */

public class EmptyRenderer implements GLSurfaceView.Renderer {

    public EmptyRenderer() {
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
        // Set the color buffer clear color.
        GLES20.glClearColor(0.968f, 0.976f, 0.980f, 1.0f);

        // Enable the depth buffer so that OpenGL take into account if an object occlude
        // another and not just draw objects over each others according to draw functions call order.
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LESS);
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
        GLES20.glViewport(0, 0, width, height);

    }

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
    }
}

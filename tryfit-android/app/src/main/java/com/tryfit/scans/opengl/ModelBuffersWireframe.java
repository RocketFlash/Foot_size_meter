package com.tryfit.scans.opengl;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by alexeyreznik on 12/09/2017.
 */

public class ModelBuffersWireframe {
    private static final int BYTES_PER_FLOAT = 4;
    private static final int BYTES_PER_INT = 4;
    private static final int COMPONENTS_PER_VECTOR = 3;

    IntBuffer mBuffers = IntBuffer.allocate(5);
    int indicesCount;

    public void init(Mesh mesh) {

        FloatBuffer bufferVertices;
        FloatBuffer bufferNormals;
        IntBuffer bufferIndices;

        int verticesLength = mesh.getNumOfVertices() * COMPONENTS_PER_VECTOR * BYTES_PER_FLOAT;
        bufferVertices = ByteBuffer.allocateDirect(verticesLength).order(ByteOrder.nativeOrder()).asFloatBuffer();
        bufferVertices.put(
                ByteBuffer.wrap(mesh.getVboData(), mesh.getVerticesOffset(), verticesLength)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer()
        ).position(0);


        int normalsLength = mesh.getNumOfNormals() * COMPONENTS_PER_VECTOR * BYTES_PER_FLOAT;
        bufferNormals = ByteBuffer.allocateDirect(normalsLength).order(ByteOrder.nativeOrder()).asFloatBuffer();
        bufferNormals.put(
                ByteBuffer.wrap(mesh.getVboData(), mesh.getNormalsOffset(), normalsLength)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer()
        ).position(0);

        int indicesLength = mesh.getNumOfIndices() * BYTES_PER_INT;
        IntBuffer indicesNew = ByteBuffer.wrap(mesh.getVboData(), mesh.getIndicesOffset(), indicesLength)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer();

        bufferIndices = ByteBuffer.allocateDirect(indicesLength*2).order(ByteOrder.nativeOrder()).asIntBuffer();
        for (int i = 0; i < mesh.getNumOfIndices()/3; i++) {
            int a = indicesNew.get();
            int b = indicesNew.get();
            int c = indicesNew.get();

            bufferIndices.put(a);
            bufferIndices.put(b);
            bufferIndices.put(b);
            bufferIndices.put(c);
            bufferIndices.put(c);
            bufferIndices.put(a);
        }
        bufferIndices.position(0);
        indicesCount = bufferIndices.capacity();


        // initialize vertex byte buffer for per vertex color
//        bufferColors = ByteBuffer.allocateDirect(mCubeColors.length * 4)
//        		.order(ByteOrder.nativeOrder())
//        		.asFloatBuffer();
//        bufferColors.put(mCubeColors);
//        bufferColors.position(0);

        // initialize vertex byte buffer for per vertex texture coords
//		bufferTextureCoords = ByteBuffer.allocateDirect(mCubeTextureCoords.length * 4)
//				.order(ByteOrder.nativeOrder()).asFloatBuffer();
//		bufferTextureCoords.put(mCubeTextureCoords);
//		bufferTextureCoords.position(0);

        // Generate the 4 vertex buffers
        GLES20.glGenBuffers(5, mBuffers);

        // load the positions
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers.get(0));
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bufferVertices.capacity() * BYTES_PER_FLOAT,
                bufferVertices, GLES20.GL_STATIC_DRAW);

        // load the colors
//		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers.get(1));
//		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bufferColors.capacity() * 4,
//				bufferColors, GLES20.GL_STATIC_DRAW);

        // load the normals
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers.get(2));
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bufferNormals.capacity() * BYTES_PER_FLOAT,
                bufferNormals, GLES20.GL_STATIC_DRAW);

        // load the texture coordinates
//		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers.get(3));
//		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bufferTextureCoords.capacity() * 4,
//				bufferTextureCoords, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mBuffers.get(4));
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufferIndices.capacity() * BYTES_PER_INT,
                bufferIndices, GLES20.GL_STATIC_DRAW);

        // TEXTURES

//		// Retrieve a Bitmap containing our texture
//		Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.droid);
//
//		// Generates one texture buffer and binds to it
//		GLES20.glGenTextures(1, mTextures);
//		// After binding all texture calls will effect the texture found at mTextures.get(0)
//		GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, mTextures.get(0));
//
//		// Here GLUtils.texImage2D is used since the texture is contained in a Bitmap
//		// If the texture was in a Buffer (i.e ByteBuffer) then GLES20.glTexImage2D could be used
//
//		// Load the cube face - Positive X
//		GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GLES20.GL_RGBA, bm,
//				GLES20.GL_UNSIGNED_BYTE, 0);
//
//		// Load the cube face - Negative X
//		GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,  0, GLES20.GL_RGBA, bm,
//				GLES20.GL_UNSIGNED_BYTE, 0);
//
//		// Load the cube face - Positive Y
//		GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GLES20.GL_RGBA, bm,
//				GLES20.GL_UNSIGNED_BYTE, 0);
//
//		// Load the cube face - Negative Y
//		GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GLES20.GL_RGBA, bm,
//				GLES20.GL_UNSIGNED_BYTE, 0);
//
//		// Load the cube face - Positive Z
//		GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GLES20.GL_RGBA, bm,
//				GLES20.GL_UNSIGNED_BYTE, 0);
//
//		// Load the cube face - Negative Z
//		GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GLES20.GL_RGBA, bm,
//				GLES20.GL_UNSIGNED_BYTE, 0);
//
//		// Generate a mipmap for the 6 sides so 6 mipmaps
//		GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_CUBE_MAP);
//
//		// Set the filtering mode
//		GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER,
//				// For the emulator use linear filtering since trilinear isn't supported (at least not on my machine)
//				// on devices GLES20.GL_LINEAR_MIPMAP_LINEAR should be supported.
//				// With really simple textures there isn't much difference anyway.
//				GLES20.GL_LINEAR/*GLES20.GL_LINEAR_MIPMAP_LINEAR*/);
//
//		GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER,
//				GLES20.GL_NEAREST);
//		GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_S,
//				GLES20.GL_CLAMP_TO_EDGE);
//		GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_T,
//				GLES20.GL_CLAMP_TO_EDGE);
//
//		// the pixel data is saved by GLUtils.texImage2D so this is safe
//		// http://androidxref.com/source/xref/frameworks/base/core/jni/android/opengl/util.cpp#util_texImage2D for the curious
//		bm.recycle();
//
//		// Now everything needed is in video ram.
//		// At this point all that is really needed are the buffers index stored in mBuffers and mTextures,
//		// everything else can be freed to retrieve memory space.
    }
}

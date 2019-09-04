package com.tryfit.tryfitlib;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.media.Image;
import android.util.Log;

import com.tryfit.views.CanvasView;

import java.nio.ByteBuffer;

/**
 * Created by alexeyreznik on 05/06/2017.
 */

public class TryFitLibHelper {

    static {
        System.loadLibrary("tryfit-lib");
    }

    private static final String TAG = TryFitLibHelper.class.getSimpleName();

    private static TryFitLibHelper instance = null;

    private TryFitLibHelper() {

    }

    public static TryFitLibHelper getInstance() {
        if (instance == null) {
            instance = new TryFitLibHelper();
        }
        return instance;
    }

    public TryFitLibResult processTopPicture(Image mImage) {

        if (mImage.getFormat() != ImageFormat.YUV_420_888) {
            throw new IllegalArgumentException("src must have format YUV_420_888.");
        }

        Image.Plane[] planes = mImage.getPlanes();
        if (planes[1].getPixelStride() != 1 && planes[1].getPixelStride() != 2) {
            throw new IllegalArgumentException(
                    "src chroma plane must have a pixel stride of 1 or 2: got "
                            + planes[1].getPixelStride());
        }

        Log.d(TAG, "mImage: " + mImage.getWidth() + " x " + mImage.getHeight());
        ByteBuffer yuv = planes[0].getBuffer();
        int[] rgba = new int[mImage.getWidth() * mImage.getHeight()];

        long start = System.currentTimeMillis();
        TryFitLibResult tryFitLibResult = processTopPicture(mImage.getWidth(), mImage.getHeight(), CanvasView.HELPER_RECT_SIZE, yuv, rgba);
        Log.d(TAG, "Processing done. Time elapsed: " + (System.currentTimeMillis() - start) + " ms");
        Log.d(TAG, "TryFitLibResult type: " + tryFitLibResult.getType() + " result: " + tryFitLibResult.getResult());

        final Bitmap bmp = Bitmap.createBitmap(mImage.getHeight(), mImage.getWidth(), Bitmap.Config.ARGB_8888);
        bmp.setPixels(rgba, 0/* offset */, mImage.getHeight() /* stride */, 0, 0, mImage.getHeight(), mImage.getWidth());
        Log.d(TAG, "Bitmap: " + bmp.getWidth() + " x " + bmp.getHeight() + " bytes: " + bmp.getByteCount());

        tryFitLibResult.setProcessed(bmp);
        return tryFitLibResult;
    }

    public static native TryFitLibResult processTopPicture(int width, int height, double roiSize, ByteBuffer yuv, int[] rgba);
}

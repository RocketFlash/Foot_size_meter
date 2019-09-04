package com.tryfitCamera.rawCamera;

/**
 * Created by Rauf Yagfarov on 25/09/2017.
 */

//import java.io.ByteArrayOutputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.ArrayList;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.os.Handler;
//import android.os.Looper;
//import android.provider.MediaStore;
//import android.util.Log;
//import android.view.SurfaceHolder;
//import android.view.View;
//import android.view.animation.AlphaAnimation;
//import android.widget.ImageView;
//
//import android.graphics.Bitmap;
//import android.graphics.ImageFormat;
//import android.hardware.Camera;
//import android.hardware.Camera.Parameters;
//
//import com.tryfitCamera.camera.PaperDetection2Activity;
//import com.tryfitCamera.camera.ResultActivity;
//import com.tryfitCamera.camera.tryfitlib.TryFitLibResult;
//import com.tryfitCamera.train.ICallback;
//import com.tryfitCamera.train.Train;
//
//import org.opencv.android.Utils;
//import org.opencv.core.CvType;
//import org.opencv.core.Mat;
//
//import static com.tryfitCamera.camera.PaperDetection2Activity.scaleDown;

//public class CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback
//{
//    private Camera mCamera = null;
//    private ImageView MyCameraPreview = null;
//    private static final String TAG = "OCVSample::Activity";
//    private Bitmap bitmap = null;
//    private int[] pixels = null;
//    private byte[] FrameData = null;
//    private int imageFormat;
//    private int PreviewSizeWidth;
//    private int PreviewSizeHeight;
//    private boolean bProcessing;
//    private boolean isFlashOn;
//    private Parameters params;
//    private Context context;
//
//    static
//    {
//        System.loadLibrary("tryfit-lib");
//    }
//
//
//    private static native int[] processSimplePictureAndroid2(int PreviewSizeWidth, int PreviewSizeHeight, byte[] FrameData);
//
//    private static native TryFitLibResult findFootOnSimplePictureAndroid2(long inputMatAddr, long outputMatAddr);
//
//    public CameraPreview(Context context,int PreviewlayoutWidth, int PreviewlayoutHeight, ImageView CameraPreview){
//        this.context = context;
//        this.isFlashOn = false;
//        this.PreviewSizeWidth = PreviewlayoutWidth;
//        this.PreviewSizeHeight = PreviewlayoutHeight;
//        this.MyCameraPreview = CameraPreview;
//        this.bitmap = Bitmap.createBitmap(PreviewSizeWidth, PreviewSizeHeight, Bitmap.Config.ARGB_8888);
//        this.pixels = new int[PreviewSizeWidth * PreviewSizeHeight];
//    }
//
//    @Override
//    public void onPreviewFrame(byte[] arg0, Camera arg1){
//        // At preview mode, the frame data will push to here.
//        if (imageFormat == ImageFormat.NV21){
//                FrameData = arg0;
////        if (isMagic) {
////            if (mThreadPoolExecutor.getQueue().isEmpty() && !isExecuted) {
////                new FastProcessImageTask(mrmr.clone()).executeOnExecutor(mThreadPoolExecutor);
////            }
////        }
//////        if (count % checkEachN == 0 && !isExecuted) {
//////            new FastProcessImageTask(mrmr.clone()).executeOnExecutor(mThreadPoolExecutor);
//////        }
////
////        if ((isSnapshot) && !isExecuted) {
////
////            isSnapshot = false;
////            isExecuted = true;
////            onceFound = false;
////            counter2 = 0;
////            count = 0;
////            counterSuccesses = 0;
////            counterAll = 0;
////
////            setPointsNewValues(new int[0]);
////
////            runOnUiThread(new Runnable() {
////                final Mat mrmrCopy2 = mrmr.clone();
////                @Override
////                public void run() {
//////                    drawView.clearCanvas();
//////                    drawView.invalidate();
////                    new ProcessImageTask(activityCurr, mrmrCopy2).execute();
////                    drawView.clearCanvas();
////                    drawView.invalidate();
////                }
////            });
////        }
////
////
//////        int[] annPoints = {0,0,mrmr.cols(),0,mrmr.cols(),mrmr.rows(),0,mrmr.rows()};
//////        drawView.update(annPoints);
////
//////        if (currPoints.length > 0 && !isExecuted) {
//////            ArrayList<Point> pointsOrdered = new ArrayList<Point>();
//////            for (int i = 0; i < 7; i += 2) {
//////                pointsOrdered.add(new Point(currPoints[i], currPoints[i + 1]));
//////            }
//////            MatOfPoint sourceMat = new MatOfPoint();
//////            sourceMat.fromList(pointsOrdered);
//////            ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//////            contours.add(sourceMat);
//////            Imgproc.drawContours(mrmr, contours, 0, new Scalar(0, 0, 255), 5);
//////        }
////
////        if (onceFound) {
////            percentage = (int) (100 * (double) counterSuccesses / (double) counterAll);
////        } else {
////            percentage = 0;
////        }
////
////        if (!isExecuted) {
////            runOnUiThread(new Runnable() {
////                @Override
////                public void run() {
////                    textView.setText(String.format("Detection: %s %% ", String.valueOf(percentage)));
//////                    drawView.invalidate();
////                }
////            });
////        } else {
////            onceFound = false;
////            counter2 = 0;
////            count = 0;
////            counterSuccesses = 0;
////            counterAll = 0;
////        }
////
////        count++;
//
//
//        }
//    }
//
//    public void onPause()
//    {
//        mCamera.stopPreview();
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
//    {
//        Parameters parameters;
//
//        parameters = mCamera.getParameters();
//        // Set the camera preview size
//        parameters.setPreviewSize(PreviewSizeWidth, PreviewSizeHeight);
//
//        imageFormat = parameters.getPreviewFormat();
//
//        mCamera.setParameters(parameters);
//
//        mCamera.startPreview();
//    }
//
//    @Override
//    public void surfaceCreated(SurfaceHolder arg0)
//    {
//        mCamera = Camera.open();
//        try
//        {
//            this.params = mCamera.getParameters();
//            if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
//                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//            }
//            mCamera.setParameters(params);
//
//            // If did not set the SurfaceHolder, the preview area will be black.
//            mCamera.setPreviewDisplay(arg0);
//            mCamera.setPreviewCallback(this);
//        }
//        catch (IOException e)
//        {
//            mCamera.release();
//            mCamera = null;
//        }
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder arg0)
//    {
//        mCamera.setPreviewCallback(null);
//        mCamera.stopPreview();
//        mCamera.release();
//        mCamera = null;
//    }
//
//
//    public void turnOnFlash() {
//        if (!isFlashOn) {
//            if (mCamera == null || params == null) {
//                return;
//            }
//
//            params = mCamera.getParameters();
//            params.setFlashMode(Parameters.FLASH_MODE_TORCH);
//            mCamera.setParameters(params);
//            isFlashOn = true;
//
//        }
//
//    }
//
//    public void turnOffFlash() {
//        if (isFlashOn) {
//            if (mCamera == null || params == null) {
//                return;
//            }
//
//            params = mCamera.getParameters();
//            params.setFlashMode(Parameters.FLASH_MODE_OFF);
//            mCamera.setParameters(params);
//            isFlashOn = false;
//        }
//    }
//
//
//    public int[] processSimplePicture(byte[] Frame) {
//        int[] pts = processSimplePictureAndroid2(PreviewSizeWidth,PreviewSizeWidth,Frame);
//        return pts;
//    }
//
//    public TryFitLibResult findFootOnSimplePicture(Mat inputMat, Mat outputMat) {
//
//        TryFitLibResult result = findFootOnSimplePictureAndroid2(inputMat.getNativeObjAddr(), outputMat.getNativeObjAddr());
//
//        Bitmap input = Bitmap.createBitmap((int) inputMat.size().width, (int) inputMat.size().height, Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(inputMat, input);
//        result.setOriginalBitmap(input);
//
//        if (result.getResult() == TryFitLibResult.TRY_FIT_LIB_RESULT_RESULT_POSITIVE) {
//            Bitmap output = Bitmap.createBitmap((int) outputMat.size().width, (int) outputMat.size().height, Bitmap.Config.ARGB_8888);
//            Utils.matToBitmap(outputMat, output);
//            result.setProcessedBitmap(output);
//        } else {
//            result.setProcessedBitmap(null);
//        }
//
//        inputMat.release();
//        outputMat.release();
//
//        return result;
//    }
//
//    private void sendImage(final String path, final Bitmap mImage, boolean clearBitmap) {
//        if (mImage != null) {
//            Log.d(TAG, "path: " + path);
//            Log.d(TAG, "bitmap: " + mImage.getWidth() + " " + mImage.getHeight());
//            new UploadImageTask(path, mImage, clearBitmap).execute();
//        } else {
//            Log.e(TAG, "sendImage: Bitmap is null");
//        }
//    }
//
//    private void sendText(final String path, String text) {
//        if (text != null) {
//            byte[][] byteArray = new byte[1][];
//            byteArray[0] = text.getBytes();
//
//            Log.d(TAG, "path: " + path);
//            Log.d(TAG, "byteArray: " + byteArray[0].length);
//
//            new UploadFileTask(path, byteArray).execute();
//        } else {
//            Log.d(TAG, "sendText: text is null");
//        }
//    }
//
//
//    private class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {
//        private final Uri mUri;
//
//        public LoadImageTask(Uri uri) {
//            this.mUri = uri;
//        }
//
//        @Override
//        protected Bitmap doInBackground(Void... voids) {
//            Bitmap bitmap = null;
//            try {
//                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), mUri);
//                bitmap = scaleDown(bitmap, 1280, true);
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//            return bitmap;
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap bitmap) {
//            super.onPostExecute(bitmap);
//            isExecuted = true;
//
//            if (bitmap != null) {
//                new ProcessImageTask(PaperDetection2Activity.this, bitmap).execute();
//            }
//        }
//    }
//
//    public class ProcessImageTask extends AsyncTask<Void, Void, TryFitLibResult> {
//        private final Mat mImage;
//        private final int wi;
//        private final int he;
//        private final Activity mActivity;
//
//
//        public ProcessImageTask(Activity context, Mat image) {
//            this.mImage = image;
//            this.wi = image.cols();
//            this.he = image.rows();
//            this.mActivity = context;
//        }
//
//        public ProcessImageTask(Activity context, Bitmap bitmap) {
//
//            Mat newMat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);
//            Utils.bitmapToMat(bitmap, newMat);
//            bitmap.recycle();
//            this.mImage = newMat;
//            this.wi = newMat.cols();
//            this.he = newMat.rows();
//            this.mActivity = context;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            inAnimation = new AlphaAnimation(0f, 1f);
//            inAnimation.setDuration(1000);
//            progressBarHolder.setAnimation(inAnimation);
//            progressBarHolder.setVisibility(View.VISIBLE);
//            selectImageButton.setVisibility(View.INVISIBLE);
//            imageButton.setVisibility(View.INVISIBLE);
//            setFlash.setVisibility(View.INVISIBLE);
//            setMagic.setVisibility(View.INVISIBLE);
//            drawView.setVisibility(View.INVISIBLE);
//            mAttitudeIndicator.setVisibility(View.INVISIBLE);
//            drawView.clearCanvas();
//            drawView.invalidate();
//            textView.setVisibility(View.INVISIBLE);
//        }
//
//        @Override
//        protected TryFitLibResult doInBackground(Void... voids) {
//            return findFootOnSimplePicture(mImage, new Mat());
//        }
//
//
//        @Override
//        protected void onPostExecute(final TryFitLibResult result) {
//            super.onPostExecute(result);
////            mImage.release();
//            setPointsNewValues(new int[0]);
//            setPrevPointsNewValues(new int[0]);
//
//            onceFound = false;
//            counter2 = 0;
//            count = 0;
//            counterSuccesses = 0;
//            counterAll = 0;
//
//
//            if (result.getResult() == TryFitLibResult.TRY_FIT_LIB_RESULT_RESULT_POSITIVE) {
//
//                footParams = new int[2];
//                footParams[0] = result.getStickLength();
//                footParams[1] = result.getBallWidth();
//
//                Log.d(TAG, "ProcessImageTask.onPostExecute result: " + result.getResult()
//                        + " length: " + result.getStickLength()
//                        + " width: " + result.getBallWidth()
//                        + " processed: " + ((result.getProcessedBitmap() != null)
//                        ? (result.getProcessedBitmap().getWidth() + " x " + result.getProcessedBitmap().getHeight())
//                        : "null"));
//
////                if (result.getStickLength() > 0) {
////                    Toast toast = Toast.makeText(getApplicationContext(),
////                            "Length: " + String.valueOf(result.getStickLength() + " Width: " + String.valueOf(result.getBallWidth())), Toast.LENGTH_LONG);
////                    toast.show();
////                }
//
//
//                final String timeStamp = String.valueOf(System.currentTimeMillis());
//                String path = "/android/";
//                if (result.getResult() == TryFitLibResult.TRY_FIT_LIB_RESULT_RESULT_POSITIVE) {
//                    path += "successes/";
//                } else {
//                    path += "failures/";
//                }
//
//                path += "top/";
//
//                String originalPath = path + "ver06_" + timeStamp + "_original.jpg";
//                String processedName = path + "ver06_" + timeStamp + "_processed.jpg";
//                final String finalPath = path;
//
//
//                sendImage(originalPath, result.getOriginalBitmap(), true);
//                sendImage(processedName, result.getProcessedBitmap(), false);
//
//
//                String infoPath = finalPath + "ver06_" + timeStamp + "_info.txt";
//                String info = "StickLength: " + result.getStickLength()
//                        + " BallWidth: " + result.getBallWidth();
//
//                sendText(infoPath, info);
//
//
//                //Write file
//                String filename = "bitmap.png";
//                new SaveFileTask(mActivity, info, filename, result.getProcessedBitmap()).execute();
//            } else {
//
//                outAnimation = new AlphaAnimation(1f, 0f);
//                outAnimation.setDuration(1000);
//                progressBarHolder.setAnimation(outAnimation);
//                progressBarHolder.setVisibility(View.GONE);
//                drawView.setVisibility(View.VISIBLE);
//                selectImageButton.setVisibility(View.VISIBLE);
//                imageButton.setVisibility(View.VISIBLE);
//                setFlash.setVisibility(View.VISIBLE);
//                setMagic.setVisibility(View.VISIBLE);
//                textView.setVisibility(View.VISIBLE);
//                mAttitudeIndicator.setVisibility(View.VISIBLE);
//
//
//                Toast toast = Toast.makeText(getApplicationContext(),
//                        "There is no foot ", Toast.LENGTH_LONG);
//                toast.show();
//                isExecuted = false;
//            }
//            drawView.clearCanvas();
//            drawView.invalidate();
//
//        }
//    }
//
//    private class UploadImageTask extends AsyncTask<Void, Void, Void> {
//        private final String mPath;
//        private final Bitmap mBitmap;
//        private final boolean mClearBitmap;
//
//        public UploadImageTask(String path, Bitmap bitmap, boolean clearBitmap) {
//            this.mPath = path;
//            this.mBitmap = bitmap;
//            this.mClearBitmap = clearBitmap;
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//
//            byte[][] byteArray = new byte[1][];
//            byteArray[0] = stream.toByteArray();
//            final ArrayList inputArgs = new ArrayList();
//            inputArgs.add(mPath);
//            Train.getInstance().Call("scanner.upload", inputArgs, byteArray, new ICallback() {
//                @Override
//                public void onResult(ArrayList args, String error) {
//                    if (error != null && !error.isEmpty()) {
//                        Log.e("Train", "scanner.upload " + error);
//                    } else {
//                        Log.d("Train", "scanner.upload Success");
//                    }
//                }
//            });
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            if (mClearBitmap) {
//                mBitmap.recycle();
//            }
//        }
//    }
//
//    private class UploadFileTask extends AsyncTask<Void, Void, Void> {
//        private final String mPath;
//        private final byte[][] mData;
//
//        public UploadFileTask(final String path, byte[][] data) {
//            this.mPath = path;
//            this.mData = data;
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            final ArrayList inputArgs = new ArrayList();
//            inputArgs.add(mPath);
//            Train.getInstance().Call("scanner.upload", inputArgs, mData, new ICallback() {
//                @Override
//                public void onResult(ArrayList args, String error) {
//                    if (error != null && !error.isEmpty()) {
//                        Log.e("Train", "scanner.upload " + error);
//                    } else {
//                        Log.d("Train", "scanner.upload Success");
//                    }
//                }
//            });
//            return null;
//        }
//    }
//
//    private class SaveFileTask extends AsyncTask<Void, Void, Boolean> {
//        private final String filename;
//        private final Bitmap bmp;
//        private final Activity currActivity;
//        private final String processingResult;
//
//        public SaveFileTask(Activity activity, String processingResult, final String path, Bitmap bmp) {
//            this.filename = path;
//            this.bmp = bmp;
//            this.currActivity = activity;
//            this.processingResult = processingResult;
//        }
//
//        @Override
//        protected Boolean doInBackground(Void... voids) {
//
//            try {
//                FileOutputStream stream = currActivity.openFileOutput(filename, Context.MODE_PRIVATE);
//                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
//
//                //Cleanup
//                stream.close();
//                bmp.recycle();
//                return Boolean.TRUE;
//            } catch (Exception e) {
//                e.printStackTrace();
//                return Boolean.FALSE;
//            }
//
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//
//            outAnimation = new AlphaAnimation(1f, 0f);
//            outAnimation.setDuration(1000);
//            progressBarHolder.setAnimation(outAnimation);
//            progressBarHolder.setVisibility(View.GONE);
//
//            selectImageButton.setVisibility(View.VISIBLE);
//            setFlash.setVisibility(View.VISIBLE);
//            setMagic.setVisibility(View.VISIBLE);
//            drawView.setVisibility(View.VISIBLE);
//            imageButton.setVisibility(View.VISIBLE);
//            textView.setVisibility(View.VISIBLE);
//            mAttitudeIndicator.setVisibility(View.VISIBLE);
//
//            if (result.booleanValue()) {
//                Intent in1 = new Intent(currActivity.getBaseContext(), ResultActivity.class);
//                in1.putExtra("image", filename);
//                in1.putExtra("processingResult", processingResult);
//                startActivity(in1);
//            }
//            isExecuted = false;
//        }
//    }
//
//    private class FastProcessImageTask extends AsyncTask<Void, Void, int[]> {
//        private final Mat image;
//        private final int imageWidth;
//        private final int imageHeight;
//
//        public FastProcessImageTask(Mat image) {
//            this.image = image;
//            this.imageWidth = image.width();
//            this.imageHeight = image.height();
//        }
//
//        @Override
//        protected int[] doInBackground(Void... voids) {
//
//            int[] prevResult = getPointsNewValues();
//            int[] result = processSimplePicture(image);
//
//            if (onceFound) counterAll++;
//            int err = 0;
//            if (result.length > 0) {
//                if (!onceFound) {
//                    counterAll++;
//                    onceFound = true;
//                }
//                counterSuccesses++;
//            }
//            if (result.length == prevResult.length && result.length > 0) {
//                for (int i = 0; i < result.length; i += 2) {
//                    err += abs(sqrt((result[i] - prevResult[i])
//                            * (result[i] - prevResult[i])
//                            + (result[i + 1] - prevResult[i + 1])
//                            * (result[i + 1] - prevResult[i + 1])));
//                }
//                if (err < 100) {
//                    counter2++;
//                } else {
//                    counter2 = 0;
//                }
//
//            } else {
//                counter2 = 0;
//            }
//
//            return result;
//
//        }
//
//        @Override
//        protected void onPostExecute(int[] result) {
//            setPointsNewValues(result);
////            mThreadPoolExecutor.getQueue().clear();
//            int[] resizedCurrPoints = new int[result.length];
//            double xCoef = ((double) screenWidth) / imageWidth;
//            double yCoef = ((double) screenHeight) / imageHeight;
//            for (int ppt = 0; ppt < result.length; ppt += 2) {
//                resizedCurrPoints[ppt] = (int) (xCoef * result[ppt]);
//                resizedCurrPoints[ppt + 1] = (int) (yCoef * result[ppt + 1]);
//            }
//
//            drawView.updateCoordinates(resizedCurrPoints, counter2);
////            drawView.postInvalidate();
////            if(counter2==1){
////
////            }
//
//            if (drawView.getIsFinished()) {
//                isSnapshot = false;
//                isExecuted = true;
//                onceFound = false;
//                counter2 = 0;
//                count = 0;
//                counterSuccesses = 0;
//                counterAll = 0;
//                drawView.setIsFinished(false);
//                drawView.clearCanvas();
//                drawView.updateCoordinates(new int[0], 0);
//
//                setPointsNewValues(new int[0]);
//                Mat imageCopy = image.clone();
//                new ProcessImageTask(activityCurr, imageCopy).execute();
//                image.release();
////                imageCopy.release();
//            }
//
//        }
//    }
//}
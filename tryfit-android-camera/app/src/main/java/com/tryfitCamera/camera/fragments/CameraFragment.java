package com.tryfitCamera.camera.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.tryfitCamera.camera.ResultActivity;
import com.tryfitCamera.camera.tryfitlib.TryFitLibResult;
import com.tryfitCamera.train.ICallback;
import com.tryfitCamera.train.Train;
import com.tryfitCamera.tryfit.R;
import com.tryfitCamera.utilities.AttitudeIndicator;
import com.tryfitCamera.utilities.DrawView;
import com.tryfitCamera.utilities.Orientation;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;

import static android.app.Activity.RESULT_OK;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;


public class CameraFragment extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2, Orientation.Listener {


    // Sets the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 20000;
    // Sets the Time Unit to Milliseconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.MILLISECONDS;
    private static final String TAG = "OCVSample::Activity";
    private static final int REQUEST_CAMERA_STATE = 10001;
    private static final int PICK_IMAGE_REQUEST = 0;
    private static final String READ_CAMERA_PERMISSION = Manifest.permission.CAMERA;
    /*
     * Gets the number of available cores
     * (not always the same as the maximum number of cores)
     */
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    static {
        System.loadLibrary("tryfit-lib");
    }

    public final int checkEachN = 7;
    volatile public ThreadPoolExecutor mThreadPoolExecutor;
    volatile public int count = 0;
    volatile public int counter2 = 0;
    volatile public boolean isExecuted = false;
    volatile public boolean onceFound = false;
    volatile public int counterAll = 0;
    volatile public int counterSuccesses = 0;
    volatile public int percentage = 0;
    @BindView(R.id.progressBarHolder3)
    FrameLayout progressBarHolder;
    @BindView(R.id.processed_imageview3)
    ImageView imageView;
    @BindView(R.id.text_view_id3)
    TextView textView;
    @BindView(R.id.action_take_picture3)
    ImageButton imageButton;
    @BindView(R.id.action_select_image3)
    ImageButton selectImageButton;
    @BindView(R.id.action_flash_button3)
    ImageButton setFlash;
    @BindView(R.id.action_magic_button3)
    ImageButton setMagic;
    @BindView(R.id.drawViewId3)
    DrawView drawView;
    @BindView(R.id.indicator_attitude3)
    AttitudeIndicator mAttitudeIndicator;
    @BindView(R.id.tutorial1_activity_java_surface_view3)
    JavaCameraView mOpenCvCameraView;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    volatile int[] points = new int[0];
    volatile int[] pointsPrev = new int[0];
    volatile int[] currPoints = new int[0];
    volatile int[] footParams = new int[0];
    private Activity activityCurr;
    private boolean isSnapshot;
    private boolean needUpdate;
    private boolean isMagic;
    private int screenWidth;
    private int screenHeight;
    private Orientation mOrientation;
    private CameraManager mCameraManager;
    private String mCameraId;
    private Boolean isTorchOn;
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getActivity()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public CameraFragment() {
    }

    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        return fragment;
    }

    private static native int[] processSimplePictureAndroid(long inputMatAddr);

    private static native TryFitLibResult findFootOnSimplePictureAndroid(long inputMatAddr, long outputMatAddr);

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camera, container, false);
        ButterKnife.bind(this, rootView);
        mOrientation = new Orientation(getActivity());

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();


        isSnapshot = false;
        needUpdate = false;
        isTorchOn = false;
        isMagic = true;
        activityCurr = getActivity();
        progressBarHolder.getChildAt(1).setRotation(270.0f);
//        progressBarHolder.setRotation(270.0f);

        textView.setTextColor(Color.BLUE);
        textView.setTextSize(12);


        Boolean isFlashAvailable = getActivity().getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!isFlashAvailable) {

            AlertDialog alert = new AlertDialog.Builder(getActivity())
                    .create();
            alert.setTitle("Error !!");
            alert.setMessage("Your device doesn't support flash light!");
            alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // closing the application
                    System.exit(0);
                }
            });
            alert.show();
        }

        mCameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


        setMagic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMagic) {
                    isMagic = !isMagic;
                    setMagic.setImageResource(R.drawable.ic_border_clear_white_36dp);
                } else {
                    isMagic = !isMagic;
                    setMagic.setImageResource(R.drawable.ic_border_outer_white_36dp);
                }
            }
        });


        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSnapshot = true;
            }
        });
        imageView.setVisibility(View.GONE);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageView.getVisibility() == View.INVISIBLE) {
                    imageView.setVisibility(View.VISIBLE);
                } else if (imageView.getVisibility() == View.VISIBLE) {
                    imageView.setVisibility(View.INVISIBLE);
                }
            }
        });

        mOpenCvCameraView.setMaxFrameSize(1280, 720);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        setFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTorchOn) {
                    mOpenCvCameraView.turnOnTheFlash();
                    isTorchOn = !isTorchOn;
                    setFlash.setImageResource(R.drawable.ic_flash_on_white_36dp);
                } else {
                    mOpenCvCameraView.turnOffTheFlash();
                    isTorchOn = !isTorchOn;
                    setFlash.setImageResource(R.drawable.ic_flash_off_white_36dp);
                }
            }
        });

        mThreadPoolExecutor = new ThreadPoolExecutor(
                1,   // Initial pool size
                1,   // Max pool size
                KEEP_ALIVE_TIME,       // Time idle thread waits before terminating
                KEEP_ALIVE_TIME_UNIT,  // Sets the Time Unit for KEEP_ALIVE_TIME
                new LinkedBlockingDeque<Runnable>());  // Work Queue

        if (!isPermissionGranted(READ_CAMERA_PERMISSION)) {
            requestPermission(READ_CAMERA_PERMISSION, REQUEST_CAMERA_STATE);
        }
        return rootView;
    }

    public synchronized int[] getPrevPointsNewValues() {
        return this.pointsPrev;
    }

    public synchronized void setPrevPointsNewValues(int[] points) {
        this.pointsPrev = points;
    }

    public synchronized int[] getPointsNewValues() {
        return this.points;
    }

    public synchronized void setPointsNewValues(int[] points) {
        this.points = points;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        Fabric.with(getActivity(), new Crashlytics());
    }



    public void selectImage() {
        isExecuted = true;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onStop() {
        super.onStop();
        mOrientation.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        mOrientation.startListening(this);
    }

    private boolean isPermissionGranted(String permission) {
        int permissionCheck = ActivityCompat.checkSelfPermission(getActivity(), permission);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_STATE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Access approved", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Access denied", Toast.LENGTH_LONG).show();
                showPermissionDialog(getActivity());
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);
    }

    private void showPermissionDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String title = getResources().getString(R.string.app_name);
        builder.setTitle(title);
        builder.setMessage(title + " requires access to camera");

        String positiveText = "Settings";
        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openAppSettings();
            }
        });

        String negativeText = "Exit";
        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getActivity().finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openAppSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
        startActivityForResult(intent, REQUEST_CAMERA_STATE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA_STATE) {
            if (!isPermissionGranted(READ_CAMERA_PERMISSION)) {
                requestPermission(READ_CAMERA_PERMISSION, REQUEST_CAMERA_STATE);
            }
        }
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            isExecuted = true;
            //Update UI for processing
            new LoadImageTask(uri).execute();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, getActivity(), mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }


    public int[] processSimplePicture(Mat image) {
        Mat imageClone = image.clone();
        int[] pts = processSimplePictureAndroid(imageClone.getNativeObjAddr());
        imageClone.release();
        return pts;
    }

    public TryFitLibResult findFootOnSimplePicture(Mat inputMat, Mat outputMat) {

        TryFitLibResult result = findFootOnSimplePictureAndroid(inputMat.getNativeObjAddr(), outputMat.getNativeObjAddr());

        Bitmap input = Bitmap.createBitmap((int) inputMat.size().width, (int) inputMat.size().height, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(inputMat, input);
        result.setOriginalBitmap(input);

        if (result.getResult() == TryFitLibResult.TRY_FIT_LIB_RESULT_RESULT_POSITIVE) {
            Bitmap output = Bitmap.createBitmap((int) outputMat.size().width, (int) outputMat.size().height, Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(outputMat, output);
            result.setProcessedBitmap(output);
        } else {
            result.setProcessedBitmap(null);
        }

        inputMat.release();
        outputMat.release();

        return result;
    }

    private void sendImage(final String path, final Bitmap mImage, boolean clearBitmap) {
        if (mImage != null) {
            Log.d(TAG, "path: " + path);
            Log.d(TAG, "bitmap: " + mImage.getWidth() + " " + mImage.getHeight());
            new UploadImageTask(path, mImage, clearBitmap).execute();
        } else {
            Log.e(TAG, "sendImage: Bitmap is null");
        }
    }

    private void sendText(final String path, String text) {
        if (text != null) {
            byte[][] byteArray = new byte[1][];
            byteArray[0] = text.getBytes();

            Log.d(TAG, "path: " + path);
            Log.d(TAG, "byteArray: " + byteArray[0].length);

            new UploadFileTask(path, byteArray).execute();
        } else {
            Log.d(TAG, "sendText: text is null");
        }
    }

    @Override
    public void onOrientationChanged(float pitch, float roll) {
        mAttitudeIndicator.setAttitude(pitch, roll);
    }

    private class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {
        private final Uri mUri;

        public LoadImageTask(Uri uri) {
            this.mUri = uri;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mUri);
                bitmap = scaleDown(bitmap, 1280, true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            isExecuted = true;

            if (bitmap != null) {
                new ProcessImageTask(getActivity(), bitmap).execute();
            }
        }
    }

    public class ProcessImageTask extends AsyncTask<Void, Void, TryFitLibResult> {
        private final Mat mImage;
        private final int wi;
        private final int he;
        private final Activity mActivity;


        public ProcessImageTask(Activity context, Mat image) {
            this.mImage = image;
            this.wi = image.cols();
            this.he = image.rows();
            this.mActivity = context;
        }

        public ProcessImageTask(Activity context, Bitmap bitmap) {

            Mat newMat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);
            Utils.bitmapToMat(bitmap, newMat);
            bitmap.recycle();
            this.mImage = newMat;
            this.wi = newMat.cols();
            this.he = newMat.rows();
            this.mActivity = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(1000);
            progressBarHolder.setAnimation(inAnimation);
            progressBarHolder.setVisibility(View.VISIBLE);
            selectImageButton.setVisibility(View.INVISIBLE);
            imageButton.setVisibility(View.INVISIBLE);
            setFlash.setVisibility(View.INVISIBLE);
            setMagic.setVisibility(View.INVISIBLE);
            drawView.setVisibility(View.INVISIBLE);
            mAttitudeIndicator.setVisibility(View.INVISIBLE);
            drawView.clearCanvas();
            drawView.invalidate();
            textView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected TryFitLibResult doInBackground(Void... voids) {
            return findFootOnSimplePicture(mImage, new Mat());
        }

        @Override
        protected void onPostExecute(final TryFitLibResult result) {
            super.onPostExecute(result);
//            mImage.release();
            setPointsNewValues(new int[0]);
            setPrevPointsNewValues(new int[0]);

            onceFound = false;
            counter2 = 0;
            count = 0;
            counterSuccesses = 0;
            counterAll = 0;


            if (result.getResult() == TryFitLibResult.TRY_FIT_LIB_RESULT_RESULT_POSITIVE) {

                footParams = new int[2];
                footParams[0] = result.getStickLength();
                footParams[1] = result.getBallWidth();

                Log.d(TAG, "ProcessImageTask.onPostExecute result: " + result.getResult()
                        + " length: " + result.getStickLength()
                        + " width: " + result.getBallWidth()
                        + " processed: " + ((result.getProcessedBitmap() != null)
                        ? (result.getProcessedBitmap().getWidth() + " x " + result.getProcessedBitmap().getHeight())
                        : "null"));

//                if (result.getStickLength() > 0) {
//                    Toast toast = Toast.makeText(getApplicationContext(),
//                            "Length: " + String.valueOf(result.getStickLength() + " Width: " + String.valueOf(result.getBallWidth())), Toast.LENGTH_LONG);
//                    toast.show();
//                }


                final String timeStamp = String.valueOf(System.currentTimeMillis());
                String path = "/android/";
                if (result.getResult() == TryFitLibResult.TRY_FIT_LIB_RESULT_RESULT_POSITIVE) {
                    path += "successes/";
                } else {
                    path += "failures/";
                }

                path += "top/";

                String originalPath = path + "ver06_" + timeStamp + "_original.jpg";
                String processedName = path + "ver06_" + timeStamp + "_processed.jpg";
                final String finalPath = path;


                sendImage(originalPath, result.getOriginalBitmap(), true);
                sendImage(processedName, result.getProcessedBitmap(), false);


                String infoPath = finalPath + "ver06_" + timeStamp + "_info.txt";
                String info = "StickLength: " + result.getStickLength()
                        + " BallWidth: " + result.getBallWidth();

                sendText(infoPath, info);


                //Write file
                String filename = "bitmap.png";
                new SaveFileTask(mActivity, info, filename, result.getProcessedBitmap()).execute();
            } else {

                outAnimation = new AlphaAnimation(1f, 0f);
                outAnimation.setDuration(1000);
                progressBarHolder.setAnimation(outAnimation);
                progressBarHolder.setVisibility(View.GONE);
                drawView.setVisibility(View.VISIBLE);
                selectImageButton.setVisibility(View.VISIBLE);
                imageButton.setVisibility(View.VISIBLE);
                setFlash.setVisibility(View.VISIBLE);
                setMagic.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                mAttitudeIndicator.setVisibility(View.VISIBLE);


                Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                        "There is no foot ", Toast.LENGTH_LONG);
                toast.show();
                isExecuted = false;
            }
            drawView.clearCanvas();
            drawView.invalidate();

        }
    }

    private class UploadImageTask extends AsyncTask<Void, Void, Void> {
        private final String mPath;
        private final Bitmap mBitmap;
        private final boolean mClearBitmap;

        public UploadImageTask(String path, Bitmap bitmap, boolean clearBitmap) {
            this.mPath = path;
            this.mBitmap = bitmap;
            this.mClearBitmap = clearBitmap;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

            byte[][] byteArray = new byte[1][];
            byteArray[0] = stream.toByteArray();
            final ArrayList inputArgs = new ArrayList();
            inputArgs.add(mPath);
            Train.getInstance().Call("scanner.upload", inputArgs, byteArray, new ICallback() {
                @Override
                public void onResult(ArrayList args, String error) {
                    if (error != null && !error.isEmpty()) {
                        Log.e("Train", "scanner.upload " + error);
                    } else {
                        Log.d("Train", "scanner.upload Success");
                    }
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (mClearBitmap) {
                mBitmap.recycle();
            }
        }
    }

    private class UploadFileTask extends AsyncTask<Void, Void, Void> {
        private final String mPath;
        private final byte[][] mData;

        public UploadFileTask(final String path, byte[][] data) {
            this.mPath = path;
            this.mData = data;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final ArrayList inputArgs = new ArrayList();
            inputArgs.add(mPath);
            Train.getInstance().Call("scanner.upload", inputArgs, mData, new ICallback() {
                @Override
                public void onResult(ArrayList args, String error) {
                    if (error != null && !error.isEmpty()) {
                        Log.e("Train", "scanner.upload " + error);
                    } else {
                        Log.d("Train", "scanner.upload Success");
                    }
                }
            });
            return null;
        }
    }

    private class SaveFileTask extends AsyncTask<Void, Void, Boolean> {
        private final String filename;
        private final Bitmap bmp;
        private final Activity currActivity;
        private final String processingResult;

        public SaveFileTask(Activity activity, String processingResult, final String path, Bitmap bmp) {
            this.filename = path;
            this.bmp = bmp;
            this.currActivity = activity;
            this.processingResult = processingResult;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            try {
                FileOutputStream stream = currActivity.openFileOutput(filename, Context.MODE_PRIVATE);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

                //Cleanup
                stream.close();
                bmp.recycle();
                return Boolean.TRUE;
            } catch (Exception e) {
                e.printStackTrace();
                return Boolean.FALSE;
            }

        }

        @Override
        protected void onPostExecute(Boolean result) {

            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(1000);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(View.GONE);

            selectImageButton.setVisibility(View.VISIBLE);
            setFlash.setVisibility(View.VISIBLE);
            setMagic.setVisibility(View.VISIBLE);
            drawView.setVisibility(View.VISIBLE);
            imageButton.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
            mAttitudeIndicator.setVisibility(View.VISIBLE);

            if (result.booleanValue()) {
                Intent in1 = new Intent(currActivity.getBaseContext(), ResultActivity.class);
                in1.putExtra("image", filename);
                in1.putExtra("processingResult", processingResult);
                startActivity(in1);
            }
            isExecuted = false;
        }
    }

    private class FastProcessImageTask extends AsyncTask<Void, Void, int[]> {
        private final Mat image;
        private final int imageWidth;
        private final int imageHeight;

        public FastProcessImageTask(Mat image) {
            this.image = image;
            this.imageWidth = image.width();
            this.imageHeight = image.height();
        }

        @Override
        protected int[] doInBackground(Void... voids) {

            int[] prevResult = getPointsNewValues();
            int[] result = processSimplePicture(image);

            if (onceFound) counterAll++;
            int err = 0;
            if (result.length > 0) {
                if (!onceFound) {
                    counterAll++;
                    onceFound = true;
                }
                counterSuccesses++;
            }
            if (result.length == prevResult.length && result.length > 0) {
                for (int i = 0; i < result.length; i += 2) {
                    err += abs(sqrt((result[i] - prevResult[i])
                            * (result[i] - prevResult[i])
                            + (result[i + 1] - prevResult[i + 1])
                            * (result[i + 1] - prevResult[i + 1])));
                }
                if (err < 100) {
                    counter2++;
                } else {
                    counter2 = 0;
                }

            } else {
                counter2 = 0;
            }

            return result;

        }

        @Override
        protected void onPostExecute(int[] result) {
            setPointsNewValues(result);
//            mThreadPoolExecutor.getQueue().clear();
            int[] resizedCurrPoints = new int[result.length];
            double xCoef = ((double) screenWidth) / imageWidth;
            double yCoef = ((double) screenHeight) / imageHeight;
            for (int ppt = 0; ppt < result.length; ppt += 2) {
                resizedCurrPoints[ppt] = (int) (xCoef * result[ppt]);
                resizedCurrPoints[ppt + 1] = (int) (yCoef * result[ppt + 1]);
            }

            drawView.updateCoordinates(resizedCurrPoints, counter2);
//            drawView.postInvalidate();
//            if(counter2==1){
//
//            }

            if (drawView.getIsFinished()) {
                isSnapshot = false;
                isExecuted = true;
                onceFound = false;
                counter2 = 0;
                count = 0;
                counterSuccesses = 0;
                counterAll = 0;
                drawView.setIsFinished(false);
                drawView.clearCanvas();
                drawView.updateCoordinates(new int[0], 0);

                setPointsNewValues(new int[0]);
                Mat imageCopy = image.clone();
                new ProcessImageTask(activityCurr, imageCopy).execute();
                image.release();
//                imageCopy.release();
            }

        }
    }
}

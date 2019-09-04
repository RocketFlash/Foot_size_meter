package com.tryfit.camera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.tryfit.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.fabric.sdk.android.Fabric;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;


public class PaperDetectionActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, Orientation.Listener {
    // Sets the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 20000;
    // Sets the Time Unit to Milliseconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.MILLISECONDS;
    private static final String TAG = "OCVSample::Activity";
    public static final String EXTRA_FOOT = "foot";
    public static final String EXTRA_STICK_LENGTH = "stickLength";
    public static final String EXTRA_BALL_WIDTH = "ballWidth";
    private static final int REQUEST_CAMERA_STATE = 200;
    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int GET_FOOT_PARAMS_RESULT = 999;
    private static final String READ_CAMERA_PERMISSION = Manifest.permission.CAMERA;
    /*
     * Gets the number of available cores
     * (not always the same as the maximum number of cores)
     */
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    static {
        System.loadLibrary("tryfit-lib");
    }

    volatile public ThreadPoolExecutor mThreadPoolExecutor;
    volatile public int count = 0;
    volatile public int counter2 = 0;
    volatile public boolean isExecuted = false;
    volatile public boolean onceFound = false;
    volatile public int counterAll = 0;
    volatile public int counterSuccesses = 0;
    volatile public int percentage = 0;
    @BindView(R.id.action_select_image)
    ImageButton mSelectImage;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    DrawView drawView;
    volatile int[] points = new int[0];
    volatile int[] pointsPrev = new int[0];
    volatile int[] currPoints = new int[0];
    volatile int[] footParams = new int[0];
    private Activity activityCurr;
    private boolean isSnapshot = false;
    private boolean needUpdate = false;
    private boolean isMagic = false;
    private boolean gyroExists = false;
    private int screenWidth;
    private int screenHeight;
    private FrameLayout progressBarHolder;
    private ImageView imageView;
    private TextView textView;
    private ImageButton imageButton;
    private ImageButton selectImageButton;
    private ImageButton setFlash;
    private ImageButton helpButton;
    private Orientation mOrientation;
    private AttitudeIndicator mAttitudeIndicator;
    private CameraManager mCameraManager;
    private String mCameraId;
    private Boolean isTorchOn = false;
    private Boolean firstTime = null;
    private myJavaCameraView mOpenCvCameraView;


    private String mFoot;

    private BaseLoaderCallback mLoaderCallback;

    public PaperDetectionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
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

        String foot = getIntent().getStringExtra(EXTRA_FOOT);
        if (foot == null) foot = "left";
        mFoot = foot;

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRotationAnimation();

        if (isFirstTime()) {
            Intent reg = new Intent(PaperDetectionActivity.this, InstructionsActivity.class);
            startActivity(reg);
        }

        setContentView(R.layout.activity_paper_detection);

        mOrientation = new Orientation(this);
        mOpenCvCameraView = (myJavaCameraView) findViewById(R.id.tutorial1_activity_java_surface_view);
        mAttitudeIndicator = (AttitudeIndicator) findViewById(R.id.indicator_attitude);
        selectImageButton = (ImageButton) findViewById(R.id.action_select_image);
        progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);
        imageButton = (ImageButton) findViewById(R.id.action_take_picture);
        helpButton = (ImageButton) findViewById(R.id.action_help);
        setFlash = (ImageButton) findViewById(R.id.action_flash_button);
        imageView = (ImageView) findViewById(R.id.processed_imageview);
        textView = (TextView) findViewById(R.id.text_view_id);


        if (!isPermissionGranted(READ_CAMERA_PERMISSION)) {
            requestPermission(READ_CAMERA_PERMISSION, REQUEST_CAMERA_STATE);
        }
        onCreateIfPermissionGranted();


        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

    }


    private boolean isFirstTime() {
        if (firstTime == null) {
            SharedPreferences mPreferences = this.getSharedPreferences("first_time", Context.MODE_PRIVATE);
            firstTime = mPreferences.getBoolean("firstTime", true);
            if (firstTime) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean("firstTime", false);
                editor.commit();
            }
        }
        return firstTime;
    }

    private void onCreateIfPermissionGranted() {

        PackageManager packageManager = getPackageManager();
        gyroExists = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
        if (!gyroExists) {
            mAttitudeIndicator.setVisibility(View.INVISIBLE);
        }

        mLoaderCallback = new BaseLoaderCallback(this) {
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

        Display display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();

        drawView = (DrawView) findViewById(R.id.drawViewId);

        isSnapshot = false;
        needUpdate = false;
        isTorchOn = false;
        isMagic = true;
        activityCurr = this;

        progressBarHolder.getChildAt(1).setRotation(270.0f);
//        progressBarHolder.setRotation(270.0f);

        textView.setTextColor(Color.BLUE);
        textView.setTextSize(12);
        textView.setVisibility(View.INVISIBLE);


        Boolean isFlashAvailable = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!isFlashAvailable) {
            setFlash.setVisibility(View.INVISIBLE);
        }

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg = new Intent(PaperDetectionActivity.this, InstructionsActivity.class);
                startActivity(reg);
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
                    setFlash.setImageResource(R.drawable.ic_flash_on);
                } else {
                    mOpenCvCameraView.turnOffTheFlash();
                    isTorchOn = !isTorchOn;
                    setFlash.setImageResource(R.drawable.ic_flash_off);
                }
            }
        });

        mThreadPoolExecutor = new ThreadPoolExecutor(
                1,   // Initial pool size
                1,   // Max pool size
                KEEP_ALIVE_TIME,       // Time idle thread waits before terminating
                KEEP_ALIVE_TIME_UNIT,  // Sets the Time Unit for KEEP_ALIVE_TIME
                new LinkedBlockingDeque<Runnable>());  // Work Queue


        Fabric.with(this, new Crashlytics());
    }


    private void setRotationAnimation() {
        int rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_CROSSFADE;
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.rotationAnimation = rotationAnimation;
        win.setAttributes(winParams);
    }


    public void selectImage() {
        isExecuted = true;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        if (isTorchOn) {
//            mOpenCvCameraView.turnOffTheFlash();
//        }
        mOrientation.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mOrientation.startListening(this);
    }

    private boolean isPermissionGranted(String permission) {
        int permissionCheck = ActivityCompat.checkSelfPermission(this, permission);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_STATE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(PaperDetectionActivity.this, "Access approved", Toast.LENGTH_LONG).show();
                onCreateIfPermissionGranted();
            } else {
                Toast.makeText(PaperDetectionActivity.this, "Access denied", Toast.LENGTH_LONG).show();
                showPermissionDialog(PaperDetectionActivity.this);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
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
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openAppSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_CAMERA_STATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_FOOT_PARAMS_RESULT && resultCode == RESULT_OK && data != null) {

            float flength = data.getFloatExtra(EXTRA_STICK_LENGTH, 0);
            float fwidth = data.getFloatExtra(EXTRA_BALL_WIDTH, 0);

            Intent dt = new Intent();
            dt.putExtra(EXTRA_FOOT, mFoot);
            dt.putExtra(EXTRA_STICK_LENGTH, flength);
            dt.putExtra(EXTRA_BALL_WIDTH, fwidth);
            setResult(RESULT_OK, dt);
            finish();
        }
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
//        if (isTorchOn) {
//            mOpenCvCameraView.turnOffTheFlash();
//        }
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_back, R.anim.slide_out_back);
    }

    @Override
    public void onResume() {
        super.onResume();

//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//            if (!isPermissionGranted(READ_CAMERA_PERMISSION)) {
//                requestPermission(READ_CAMERA_PERMISSION, REQUEST_CAMERA_STATE);
//            }
//        }

//        if (isTorchOn) {
//            mOpenCvCameraView.turnOnTheFlash();
//        }
        if (isPermissionGranted(READ_CAMERA_PERMISSION)) {
            if (!OpenCVLoader.initDebug()) {
                Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
            } else {
                Log.d(TAG, "OpenCV library found inside package. Using it!");
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }
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

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        final Mat mrmr = inputFrame.rgba();
        if (isMagic && mAttitudeIndicator.getIsGood()) {
            if (mThreadPoolExecutor.getQueue().isEmpty() && !isExecuted) {
                new FastProcessImageTask(mrmr.clone()).executeOnExecutor(mThreadPoolExecutor);
            }
        } else {
//            if(counter2>0){
            drawView.clearCanvas();
            counter2 = 0;
            count = 0;
//            }
        }
//        if (count % checkEachN == 0 && !isExecuted) {
//            new FastProcessImageTask(mrmr.clone()).executeOnExecutor(mThreadPoolExecutor);
//        }

        if ((isSnapshot) && !isExecuted) {

            isSnapshot = false;
            isExecuted = true;
            onceFound = false;
            counter2 = 0;
            count = 0;
            counterSuccesses = 0;
            counterAll = 0;

            setPointsNewValues(new int[0]);

            runOnUiThread(new Runnable() {
                final Mat mrmrCopy2 = mrmr.clone();

                @Override
                public void run() {
//                    drawView.clearCanvas();
//                    drawView.invalidate();
                    new ProcessImageTask(activityCurr, mrmrCopy2).execute();
                    drawView.clearCanvas();
                    drawView.invalidate();
                }
            });
        }


        if (onceFound) {
            percentage = (int) (100 * (double) counterSuccesses / (double) counterAll);
        } else {
            percentage = 0;
        }

        if (!isExecuted) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText(String.format("Detection: %s %% ", String.valueOf(percentage)));
                }
            });
        } else {
            onceFound = false;
            counter2 = 0;
            count = 0;
            counterSuccesses = 0;
            counterAll = 0;
        }

        count++;
        return mrmr;
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

    @Override
    public void onOrientationChanged(float pitch, float roll) {
        if (gyroExists) {
            mAttitudeIndicator.setAttitude(pitch, roll);
        }
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
                bitmap = MediaStore.Images.Media.getBitmap(PaperDetectionActivity.this.getContentResolver(), mUri);
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
                new ProcessImageTask(PaperDetectionActivity.this, bitmap).execute();
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
            helpButton.setVisibility(View.INVISIBLE);
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
            mImage.release();
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

                String filename = "bitmap.png";
                new SaveFileTask(mActivity, result, filename).execute();


            } else {

                outAnimation = new AlphaAnimation(1f, 0f);
                outAnimation.setDuration(1000);
                progressBarHolder.setAnimation(outAnimation);
                progressBarHolder.setVisibility(View.GONE);
                drawView.setVisibility(View.VISIBLE);
                selectImageButton.setVisibility(View.VISIBLE);
                imageButton.setVisibility(View.VISIBLE);
                setFlash.setVisibility(View.VISIBLE);
                helpButton.setVisibility(View.VISIBLE);
//                textView.setVisibility(View.VISIBLE);
                mAttitudeIndicator.setVisibility(View.VISIBLE);


                Toast toast = Toast.makeText(getApplicationContext(),
                        "There is no foot ", Toast.LENGTH_LONG);
                toast.show();
                isExecuted = false;
            }
            drawView.clearCanvas();
            drawView.invalidate();

        }
    }

    private class SaveFileTask extends AsyncTask<Void, Void, Boolean> {
        private final String filename;
        private final Bitmap bmp;
        private final Activity currActivity;
        private final TryFitLibResult result;

        public SaveFileTask(Activity activity, TryFitLibResult result, final String path) {
            this.filename = path;
            this.bmp = result.getProcessedBitmap();
            this.currActivity = activity;
            this.result = result;
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
        protected void onPostExecute(Boolean isOk) {

            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(1000);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(View.GONE);

            selectImageButton.setVisibility(View.VISIBLE);
            setFlash.setVisibility(View.VISIBLE);
            helpButton.setVisibility(View.VISIBLE);
            drawView.setVisibility(View.VISIBLE);
            imageButton.setVisibility(View.VISIBLE);
//            textView.setVisibility(View.VISIBLE);
            mAttitudeIndicator.setVisibility(View.VISIBLE);

            if (isOk) {
                Intent in1 = new Intent(PaperDetectionActivity.this, ResultActivity.class);
                in1.putExtra("image", filename);
                in1.putExtra("processingResult", this.result.getFootParamsAsString());
                startActivityForResult(in1, GET_FOOT_PARAMS_RESULT);
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
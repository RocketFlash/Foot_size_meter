package com.tryfitCamera.camera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.support.design.widget.Snackbar;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.tryfitCamera.camera.tryfitlib.TryFitLibResult;
import com.tryfitCamera.train.ICallback;
import com.tryfitCamera.train.Train;
import com.tryfitCamera.tryfit.R;
import com.tryfitCamera.utilities.AttitudeIndicator;
import com.tryfitCamera.utilities.DrawView;
import com.tryfitCamera.utilities.Orientation;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.fabric.sdk.android.Fabric;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;


public class PaperDetectionActivity extends Activity implements CvCameraViewListener2, Orientation.Listener {
    // Sets the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 20000;
    // Sets the Time Unit to Milliseconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.MILLISECONDS;
    private static final String TAG = "OCVSample::Activity";
    private static final int REQUEST_CAMERA_STATE = 200;
    private  static final int PERMISSIONS_MULTIPLE_REQUEST = 123;
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
    private int screenWidth;
    private int screenHeight;
    private FrameLayout progressBarHolder;
    private ImageView imageView;
    private TextView textView;
    private ImageButton imageButton;
    private ImageButton selectImageButton;
    private ImageButton setFlash;
    private ImageButton setMagic;
    private Orientation mOrientation;
    private AttitudeIndicator mAttitudeIndicator;
    private CameraManager mCameraManager;
    private String mCameraId;
    private Boolean isTorchOn = false;
    private JavaCameraView mOpenCvCameraView;
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;
    private boolean orientationIsOk = false;

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

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRotationAnimation();

        setContentView(R.layout.activity_paper_detection);

        mOrientation = new Orientation(this);
        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.tutorial1_activity_java_surface_view);
        mAttitudeIndicator = (AttitudeIndicator) findViewById(R.id.indicator_attitude);
        selectImageButton = (ImageButton) findViewById(R.id.action_select_image);
        progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);
        imageButton = (ImageButton) findViewById(R.id.action_take_picture);
        setMagic = (ImageButton) findViewById(R.id.action_magic_button);
        setFlash = (ImageButton) findViewById(R.id.action_flash_button);
        imageView = (ImageView) findViewById(R.id.processed_imageview);
        textView = (TextView) findViewById(R.id.text_view_id);


        checkPermission();


        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

//        permissionsDelegate = new PermissionsDelegate(this);


    }


    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat
                    .checkSelfPermission(this,
                            Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale
                        (this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale
                                (this, Manifest.permission.CAMERA)) {

                    Snackbar.make(this.findViewById(android.R.id.content),
                            "Please Grant Permissions to upload profile photo",
                            Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(
                                                new String[]{Manifest.permission
                                                        .READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                                                PERMISSIONS_MULTIPLE_REQUEST);
                                    }
                                }
                            }).show();
                } else {
                    requestPermissions(
                            new String[]{Manifest.permission
                                    .READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                            PERMISSIONS_MULTIPLE_REQUEST);
                }
            } else {
                onCreateIfPermissionGranted();
            }
        }
    }

    private void onCreateIfPermissionGranted() {

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

            AlertDialog alert = new AlertDialog.Builder(PaperDetectionActivity.this)
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
            return;
        }

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
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


        Fabric.with(this, new Crashlytics());
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(String filePath,
                                                     int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
//        options.inScaled = true;
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
//        options.inSampleSize = 1;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }


    private void setRotationAnimation() {
        int rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_CROSSFADE;
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.rotationAnimation = rotationAnimation;
        win.setAttributes(winParams);
    }

    public void turnOnFlashLight() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.setTorchMode(mCameraId, true);
                setFlash.setImageResource(R.drawable.ic_flash_on_white_36dp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void turnOffFlashLight() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.setTorchMode(mCameraId, false);
                setFlash.setImageResource(R.drawable.ic_flash_off_white_36dp);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (isTorchOn) {
            turnOffFlashLight();
        }
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

        switch (requestCode) {
            case PERMISSIONS_MULTIPLE_REQUEST:
                if (grantResults.length > 0) {
                    boolean cameraPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean readExternalFile = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(cameraPermission && readExternalFile)
                    {
                        onCreateIfPermissionGranted();

                    } else {
                        Snackbar.make(this.findViewById(android.R.id.content),
                                "Please Grant Permissions to upload profile photo",
                                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(
                                                    new String[]{Manifest.permission
                                                            .READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                                                    PERMISSIONS_MULTIPLE_REQUEST);
                                        }
                                    }
                                }).show();
                    }
                }
                break;
        }

//        if (requestCode == REQUEST_CAMERA_STATE) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(PaperDetectionActivity.this, "Access approved", Toast.LENGTH_LONG).show();
//                onCreateIfPermissionGranted();
//            } else {
//                Toast.makeText(PaperDetectionActivity.this, "Access denied", Toast.LENGTH_LONG).show();
//                showPermissionDialog(PaperDetectionActivity.this);
//            }
//        } else {
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
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
        if (isTorchOn) {
            turnOffFlashLight();
        }
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

        if (isTorchOn) {
            turnOnFlashLight();
        }
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

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

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


//        int[] annPoints = {0,0,mrmr.cols(),0,mrmr.cols(),mrmr.rows(),0,mrmr.rows()};
//        drawView.update(annPoints);

//        if (currPoints.length > 0 && !isExecuted) {
//            ArrayList<Point> pointsOrdered = new ArrayList<Point>();
//            for (int i = 0; i < 7; i += 2) {
//                pointsOrdered.add(new Point(currPoints[i], currPoints[i + 1]));
//            }
//            MatOfPoint sourceMat = new MatOfPoint();
//            sourceMat.fromList(pointsOrdered);
//            ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//            contours.add(sourceMat);
//            Imgproc.drawContours(mrmr, contours, 0, new Scalar(0, 0, 255), 5);
//        }

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
//                    drawView.invalidate();
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
            bitmap = decodeSampledBitmapFromFile(getRealPathFromURI(mUri),1280,720);
//                bitmap = MediaStore.Images.Media.getBitmap(PaperDetectionActivity.this.getContentResolver(), mUri);
//                bitmap = scaleDown(bitmap, 1280, true);

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
//            textView.setVisibility(View.VISIBLE);
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
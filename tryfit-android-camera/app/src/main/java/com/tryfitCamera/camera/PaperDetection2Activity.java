package com.tryfitCamera.camera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.Camera;
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
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
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
import io.fabric.sdk.android.Fabric;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;


public class PaperDetection2Activity extends Activity implements Orientation.Listener {


    private CameraPreview camPreview;
    private ImageView MyCameraPreview = null;
    private FrameLayout mainLayout;

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

    public final int checkEachN = 25;
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
    private boolean isSnapshot;
    private boolean needUpdate;
    private boolean isMagic;
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
    private Boolean isTorchOn;


    private static native int[] processSimplePictureAndroid2(int PreviewSizeWidth, int PreviewSizeHeight, byte[] FrameData,int[] pixels);

    private static native TryFitLibResult findFootOnSimplePictureAndroid2(int PreviewSizeWidth, int PreviewSizeHeight, byte[] FrameDataIn,int[] pixels,int[] pixels2);

    public PaperDetection2Activity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

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

        setContentView(R.layout.activity_paper_detection2);

        mOrientation = new Orientation(this);
        mAttitudeIndicator = (AttitudeIndicator) findViewById(R.id.indicator_attitude2);

        Display display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();


        MyCameraPreview = new ImageView(this);

        SurfaceView camView = new SurfaceView(this);
        SurfaceHolder camHolder = camView.getHolder();
        camPreview = new CameraPreview(this, screenWidth, screenHeight, MyCameraPreview);

        camHolder.addCallback(camPreview);
        camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mainLayout = (FrameLayout) findViewById(R.id.frameLayout2);
        mainLayout.addView(camView, new ViewGroup.LayoutParams(screenWidth, screenHeight));
        mainLayout.addView(MyCameraPreview, new ViewGroup.LayoutParams(screenWidth, screenHeight));

        drawView = (DrawView) findViewById(R.id.drawViewId2);

        isSnapshot = false;
        needUpdate = false;
        isTorchOn = false;
        isMagic = true;
        activityCurr = this;
        progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder2);
        progressBarHolder.getChildAt(1).setRotation(270.0f);
//        progressBarHolder.setRotation(270.0f);
        textView = (TextView) findViewById(R.id.text_view_id2);
        textView.setTextColor(Color.BLUE);
        textView.setTextSize(12);


        Boolean isFlashAvailable = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!isFlashAvailable) {

            AlertDialog alert = new AlertDialog.Builder(PaperDetection2Activity.this)
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


        setMagic = (ImageButton) findViewById(R.id.action_magic_button2);
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

        selectImageButton = (ImageButton) findViewById(R.id.action_select_image2);
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        imageButton = (ImageButton) findViewById(R.id.action_take_picture2);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSnapshot = true;
            }
        });
        imageView = (ImageView) findViewById(R.id.processed_imageview2);
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

        setFlash = (ImageButton) findViewById(R.id.action_flash_button2);
        setFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTorchOn) {
                    camPreview.turnOnFlash();
                    isTorchOn = !isTorchOn;
                    setFlash.setImageResource(R.drawable.ic_flash_on_white_36dp);
                } else {
                    camPreview.turnOffFlash();
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

        Fabric.with(this, new Crashlytics());
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
                Toast.makeText(PaperDetection2Activity.this, "Access approved", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(PaperDetection2Activity.this, "Access denied", Toast.LENGTH_LONG).show();
                showPermissionDialog(PaperDetection2Activity.this);
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
        if (requestCode == REQUEST_CAMERA_STATE) {
            if (!isPermissionGranted(READ_CAMERA_PERMISSION)) {
                requestPermission(READ_CAMERA_PERMISSION, REQUEST_CAMERA_STATE);
            }
        }
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            isExecuted = true;
            //Update UI for processing
//            new LoadImageTask(uri).execute();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if ( camPreview != null)
            camPreview.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onOrientationChanged(float pitch, float roll) {
        mAttitudeIndicator.setAttitude(pitch, roll);
    }


    public int[] processSimplePicture(byte[] Frame) {
        int[] pts = processSimplePictureAndroid2(screenWidth,screenHeight,Frame,new int[screenWidth * screenHeight]);
        return pts;
    }

    public TryFitLibResult findFootOnSimplePicture(byte[] Frame,int[] pixels,int[] pixels2) {

        TryFitLibResult result = findFootOnSimplePictureAndroid2(screenWidth,screenHeight,Frame, pixels,pixels2);
        Bitmap input = BitmapFactory.decodeByteArray(Frame, 0, Frame.length);
        result.setOriginalBitmap(input);

        if (result.getResult() == TryFitLibResult.TRY_FIT_LIB_RESULT_RESULT_POSITIVE) {
            Bitmap output = Bitmap.createBitmap(screenHeight, screenWidth, Bitmap.Config.ARGB_8888);
            Log.i(TAG, "Image size = " + String.valueOf(output.getWidth()) + " " + String.valueOf(output.getHeight()) );
            output.setPixels(pixels2, 0, screenHeight, 0, 0, screenHeight,screenWidth);
            result.setProcessedBitmap(output);
        } else {
            result.setProcessedBitmap(null);
        }

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


    private class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {
        private final Uri mUri;

        public LoadImageTask(Uri uri) {
            this.mUri = uri;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(PaperDetection2Activity.this.getContentResolver(), mUri);
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
                new ProcessImageTask(PaperDetection2Activity.this, bitmap).execute();
            }
        }
    }

    public class ProcessImageTask extends AsyncTask<Void, Void, TryFitLibResult> {
        private final byte[] mImage;
        private final Activity mActivity;


        public ProcessImageTask(Activity context, byte[] image) {
            this.mImage = image;
            this.mActivity = context;
        }

        public ProcessImageTask(Activity context, Bitmap bitmap) {

            Mat newMat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);
            Utils.bitmapToMat(bitmap, newMat);
            bitmap.recycle();

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

            this.mImage = stream.toByteArray();
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
            return findFootOnSimplePicture(mImage, new int[screenWidth * screenHeight],new int[screenWidth * screenHeight]);
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
        private final byte[] image;

        public FastProcessImageTask(byte[] image) {
            this.image = image;
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
//            double xCoef = ((double) screenWidth) / imageWidth;
//            double yCoef = ((double) screenHeight) / imageHeight;
            for (int ppt = 0; ppt < result.length; ppt += 2) {
                resizedCurrPoints[ppt] = (int) ( result[ppt]);
                resizedCurrPoints[ppt + 1] = (int) ( result[ppt + 1]);
            }

            drawView.updateCoordinates(resizedCurrPoints, counter2);


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
                new ProcessImageTask(activityCurr, image.clone()).execute();


            }

        }
    }

    public class CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback {
        private Camera mCamera = null;
        private ImageView MyCameraPreview = null;
        private static final String TAG = "OCVSample::Activity";
        private Bitmap bitmap = null;
        private int[] pixels = null;
        private byte[] FrameData = null;
        private int imageFormat;
        private int PreviewSizeWidth;
        private int PreviewSizeHeight;
        private boolean bProcessing;
        private boolean isFlashOn;
        private Camera.Parameters params;
        private Context context;


        public CameraPreview(Context context, int PreviewlayoutWidth, int PreviewlayoutHeight, ImageView CameraPreview) {
            this.context = context;
            this.isFlashOn = false;
            this.PreviewSizeWidth = PreviewlayoutWidth;
            this.PreviewSizeHeight = PreviewlayoutHeight;
            this.MyCameraPreview = CameraPreview;
            this.bitmap = Bitmap.createBitmap(PreviewSizeWidth, PreviewSizeHeight, Bitmap.Config.ARGB_8888);
            this.pixels = new int[PreviewSizeWidth * PreviewSizeHeight];
        }

        public Bitmap getBitmap(){
            return this.bitmap;
        }

        public void setMyCameraPreviewImageBitmap(){
            MyCameraPreview.setImageBitmap(bitmap);
        }

        @Override
        public void onPreviewFrame(byte[] arg0, Camera arg1) {
            // At preview mode, the frame data will push to here.
            if (imageFormat == ImageFormat.NV21) {
                FrameData = arg0;

            if (isMagic) {
//                Log.i(TAG, "Image size = " + String.valueOf(FrameData.length));
                if (mThreadPoolExecutor.getQueue().isEmpty() && !isExecuted) {
                    new FastProcessImageTask(FrameData.clone()).executeOnExecutor(mThreadPoolExecutor);
                }
            }

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
                final byte[] mrmrCopy2 = FrameData.clone();
                @Override
                public void run() {
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


            }
        }

        void onPause() {
            mCamera.stopPreview();
        }

        @Override
        public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
            Camera.Parameters parameters;

            parameters = mCamera.getParameters();
            // Set the camera preview size
            parameters.setPreviewSize(PreviewSizeWidth, PreviewSizeHeight);

            imageFormat = parameters.getPreviewFormat();

            mCamera.setParameters(parameters);

            mCamera.startPreview();
        }

        @Override
        public void surfaceCreated(SurfaceHolder arg0) {
            mCamera = Camera.open();
            try {
                this.params = mCamera.getParameters();
                if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }
                mCamera.setParameters(params);

                // If did not set the SurfaceHolder, the preview area will be black.
                mCamera.setPreviewDisplay(arg0);
                mCamera.setPreviewCallback(this);
            } catch (IOException e) {
                mCamera.release();
                mCamera = null;
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder arg0) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }


        void turnOnFlash() {
            if (!isFlashOn) {
                if (mCamera == null || params == null) {
                    return;
                }

                params = mCamera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(params);
                isFlashOn = true;

            }

        }

        void turnOffFlash() {
            if (isFlashOn) {
                if (mCamera == null || params == null) {
                    return;
                }

                params = mCamera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(params);
                isFlashOn = false;
            }
        }

    }
//
}
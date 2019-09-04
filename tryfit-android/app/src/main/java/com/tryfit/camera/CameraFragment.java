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
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
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
import com.tryfit.R;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;

import static android.app.Activity.RESULT_OK;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;


public class CameraFragment extends Fragment implements Orientation.Listener {


    @BindView(R.id.progressBarHolder)
    FrameLayout progressBarHolder;
    @BindView(R.id.action_take_picture)
    ImageButton imageButton;
    @BindView(R.id.action_select_image)
    ImageButton selectImageButton;
    @BindView(R.id.action_flash_button)
    ImageButton setFlash;
    @BindView(R.id.drawViewId)
    DrawView drawView;
    @BindView(R.id.indicator_attitude)
    AttitudeIndicator mAttitudeIndicator;
    @BindView(R.id.action_help)
    ImageButton helpButton;
    @BindView(R.id.processed_imageview)
    ImageView imageView;
    @BindView(R.id.camPreview1)
    ImageView MyCameraPreview;
    @BindView(R.id.camView1)
    SurfaceView camView;

    private CameraPreview camPreview;
    private FrameLayout mainLayout;

    // Sets the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 20500;
    // Sets the Time Unit to Milliseconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.MILLISECONDS;
    private static final String TAG = "CameraFragment";
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
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    volatile int[] points = new int[0];
    volatile int[] pointsPrev = new int[0];
    volatile int[] currPoints = new int[0];
    volatile int[] footParams = new int[0];
    private Activity activityCurr;
    private boolean isSnapshot = false;
    private boolean needUpdate = false;
    private boolean isMagic = false;
    private boolean gyroExists = false;
    private Boolean isFlashAvailable = false;
    private int screenWidth;
    private int screenHeight;
    private Orientation mOrientation;
    private Boolean isTorchOn = false;
    private boolean orientationIsOk = false;

    private String mFoot;


    public CameraFragment() {
    }

    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getActivity().overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camera, container, false);
        ButterKnife.bind(this, rootView);

        String foot = getActivity().getIntent().getStringExtra(EXTRA_FOOT);
        if (foot == null) foot = "left";
        mFoot = foot;


        mOrientation = new Orientation(getActivity());

        if (!isPermissionGranted(READ_CAMERA_PERMISSION)) {
            requestPermission(READ_CAMERA_PERMISSION, REQUEST_CAMERA_STATE);
        }else {
            onCreateIfPermissionGranted();
        }

        return rootView;
    }

    private void onCreateIfPermissionGranted() {


        PackageManager packageManager = getActivity().getPackageManager();
        gyroExists = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
        if (!gyroExists) {
            mAttitudeIndicator.setVisibility(View.INVISIBLE);
        }


        Display display = getActivity().getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();

        SurfaceHolder camHolder = camView.getHolder();
        camPreview = new CameraPreview(getActivity(), MyCameraPreview);

        camHolder.addCallback(camPreview);
        camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        isSnapshot = false;
        needUpdate = false;
        isTorchOn = false;
        isMagic = true;
        activityCurr = getActivity();

        isFlashAvailable = getActivity().getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (isFlashAvailable) {
            setFlash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isTorchOn) {
                        camPreview.turnOnFlash();
                        isTorchOn = !isTorchOn;
                        setFlash.setImageResource(R.drawable.ic_flash_on);
                    } else {
                        camPreview.turnOffFlash();
                        isTorchOn = !isTorchOn;
                        setFlash.setImageResource(R.drawable.ic_flash_off);
                    }
                }
            });
        } else {
            setFlash.setVisibility(View.INVISIBLE);
        }


        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TryFitCameraActivity) getActivity()).setFirstItemInActivity();
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


        mThreadPoolExecutor = new ThreadPoolExecutor(
                1,   // Initial pool size
                1,   // Max pool size
                KEEP_ALIVE_TIME,       // Time idle thread waits before terminating
                KEEP_ALIVE_TIME_UNIT,  // Sets the Time Unit for KEEP_ALIVE_TIME
                new LinkedBlockingDeque<Runnable>());  // Work Queue


        Fabric.with(getActivity(), new Crashlytics());
    }

    private static native int[] processSimplePictureAndroid2(int PreviewSizeWidth, int PreviewSizeHeight, byte[] FrameData, int[] pixels);

    private static native TryFitLibResult findFootOnSimplePictureAndroid2(int PreviewSizeWidth, int PreviewSizeHeight, byte[] FrameDataIn, int[] pixels2, boolean isFromGallery);

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
                onCreateIfPermissionGranted();
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
//        if ( camPreview != null) {
//            camView.getHolder().removeCallback(camPreview);
//            camPreview.onPause();
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        if ( camPreview != null) {
//            camView.getHolder().addCallback(camPreview);
//            camPreview.onResume();
//        }
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public int[] processSimplePicture(byte[] Frame) {
        int[] pts = processSimplePictureAndroid2(screenHeight, screenWidth, Frame, new int[screenWidth * screenHeight]);
        return pts;
    }

    public TryFitLibResult findFootOnSimplePicture(byte[] Frame, int width, int height, int[] pixels2, boolean isFromGallery) {

        TryFitLibResult result = findFootOnSimplePictureAndroid2(width, height, Frame, pixels2,isFromGallery);
        Bitmap input = BitmapFactory.decodeByteArray(Frame, 0, Frame.length);
        result.setOriginalBitmap(input);

        if (result.getResult() == TryFitLibResult.TRY_FIT_LIB_RESULT_RESULT_POSITIVE) {
            Bitmap output;
            if(isFromGallery) {
                output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Log.i(TAG, "Image size = " + String.valueOf(output.getWidth()) + " " + String.valueOf(output.getHeight()));
                output.setPixels(pixels2, 0, width, 0, 0, width, height);
            }else{
                output = Bitmap.createBitmap(height,width, Bitmap.Config.ARGB_8888);
                Log.i(TAG, "Image size = " + String.valueOf(output.getWidth()) + " " + String.valueOf(output.getHeight()));
                output.setPixels(pixels2, 0, height, 0, 0, height, width);
            }
            result.setProcessedBitmap(output);
        } else {
            result.setProcessedBitmap(null);
        }

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
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mUri);
                bitmap = scaleDown(bitmap, 1920, true);
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
                new ProcessImageTask(getActivity(), bitmap,true).execute();
            }
        }
    }

    public class ProcessImageTask extends AsyncTask<Void, Void, TryFitLibResult> {
        private final byte[] mImage;
        private final Activity mActivity;
        private boolean isFromGallery;
        private int bitmapWidth;
        private int bitmapHeight;


        public ProcessImageTask(Activity context, byte[] image,boolean isFromGallery) {
            this.mImage = image;
            this.mActivity = context;
            this.isFromGallery = isFromGallery;
            this.bitmapWidth = 0;
            this.bitmapHeight = 0;
        }

        public ProcessImageTask(Activity context, Bitmap bitmap,boolean isFromGallery) {

            this.isFromGallery = isFromGallery;
            Log.i(TAG,"I am in the beginning of ProcessImageTask creation");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            bitmapHeight = bitmap.getHeight();
            bitmapWidth =  bitmap.getWidth();
            this.mImage = stream.toByteArray();
            this.mActivity = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG,"I am in the beginning of onPreExecute in ProcessImageTask");
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
            Log.i(TAG,"I am in the end of onPreExecute in ProcessImageTask");
        }

        @Override
        protected TryFitLibResult doInBackground(Void... voids) {
            Log.i(TAG,"I am in the beginning of doInBackground in ProcessImageTask");
            if(isFromGallery) {
                return findFootOnSimplePicture(mImage,bitmapWidth,bitmapHeight,new int[bitmapHeight*bitmapWidth], isFromGallery);
            }else{
                return findFootOnSimplePicture(mImage, screenHeight, screenWidth, new int[screenWidth * screenHeight], isFromGallery);
            }
        }


        @Override
        protected void onPostExecute(final TryFitLibResult result) {
            super.onPostExecute(result);
//            mImage.release();
            setPointsNewValues(new int[0]);
            setPrevPointsNewValues(new int[0]);

            Log.i(TAG,"I am in the beginning of onPostExecute in ProcessImageTask");

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
//
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
                mAttitudeIndicator.setVisibility(View.VISIBLE);
                isExecuted = false;

                Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                        "There is no foot ", Toast.LENGTH_LONG);
                toast.show();

            }

            Log.i(TAG,"I am in the end of onPostExecute in ProcessImageTask");

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
                bmp.compress(Bitmap.CompressFormat.JPEG, 30, stream);

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
            mAttitudeIndicator.setVisibility(View.VISIBLE);

            if (isOk) {

                ResultFragment resultFragment = (ResultFragment)getActivity()
                        .getSupportFragmentManager()
                        .findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + 5);

                String filename = "bitmap.png";
                resultFragment.updateData(filename,footParams[0],footParams[1]);
                ((TryFitCameraActivity) getActivity()).setNextItemInActivity();

//                String tag = "android:switcher:" + R.id.view_pager + ":" + 5;
//                ResultFragment f = (ResultFragment) getActivity().getSupportFragmentManager().findFragmentByTag(tag);
//                f.setImage(filename);
//                f.setValues(result.getStickLength(),result.getBallWidth());

//                Intent in1 = new Intent(getActivity(), ResultActivity.class);
//                in1.putExtra("image", filename);
//                in1.putExtra("processingResult", this.result.getFootParamsAsString());
//                startActivityForResult(in1, GET_FOOT_PARAMS_RESULT);
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

            long startTime = System.currentTimeMillis();
            int[] prevResult = getPointsNewValues();
            Log.i(TAG,"I am before processSimplePicture");
            int[] result = processSimplePicture(image);

            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;

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

            Log.i(TAG,"Elapsed time = " + String.valueOf(elapsedTime));

            return result;

        }

        @Override
        protected void onPostExecute(int[] result) {

            long startTime = System.currentTimeMillis();
            Log.i(TAG,"I in the beggining of onPostExecute");

            setPointsNewValues(result);

            drawView.updateCoordinates(result, counter2);

            Log.i(TAG,"I in the end of onPostExecute");

            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            Log.i(TAG,"Elapsed time postExecute= " + String.valueOf(elapsedTime));

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
                new ProcessImageTask(activityCurr, image.clone(), false).execute();
            }

        }
    }

    public class CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback {
        private Camera mCamera = null;
        private ImageView MyCameraPreview = null;
        private static final String TAG = "OCVSample::Activity";
//        private Bitmap bitmap = null;
        private int[] pixels = null;
        private byte[] FrameData = null;
        private int imageFormat;
        private int PreviewSizeWidth;
        private int PreviewSizeHeight;
        private boolean bProcessing;
        private boolean isFlashOn;
        private Camera.Parameters params;
        private Context context;


        public CameraPreview(Context context, ImageView CameraPreview) {
            this.context = context;
            this.isFlashOn = false;
            this.MyCameraPreview = CameraPreview;
        }

//        public Bitmap getBitmap() {
//            return this.bitmap;
//        }

//        public void setMyCameraPreviewImageBitmap() {
//            MyCameraPreview.setImageBitmap(bitmap);
//        }

        @Override
        public void onPreviewFrame(byte[] arg0, Camera arg1) {
            // At preview mode, the frame data will push to here.
            if (imageFormat == ImageFormat.NV21) {
                FrameData = arg0;

                if (mAttitudeIndicator.getIsGood()) {
                    if (mThreadPoolExecutor.getQueue().isEmpty() && !isExecuted) {
                        new FastProcessImageTask(FrameData.clone()).executeOnExecutor(mThreadPoolExecutor);
                    }
                } else {
                    drawView.clearCanvas();
                    counter2 = 0;
                    count = 0;
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

                    getActivity().runOnUiThread(new Runnable() {
                        final byte[] mrmrCopy2 = FrameData.clone();

                        @Override
                        public void run() {
                            new ProcessImageTask(activityCurr, mrmrCopy2,false).execute();
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

                if (isExecuted) {
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

        void onResume() {
            mCamera.startPreview();
        }

//        boolean isCameraOpen(){
//            return mCamera
//        }


        @Override
        public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
            Camera.Parameters parameters;

            parameters = mCamera.getParameters();
            // Set the camera preview size

            Log.d(TAG,"Size CURRRR: " + String.valueOf(PreviewSizeWidth) + "  "+ String.valueOf(PreviewSizeHeight));
            parameters.setPreviewSize(PreviewSizeWidth,PreviewSizeHeight);

            imageFormat = parameters.getPreviewFormat();

            mCamera.setParameters(parameters);

            mCamera.startPreview();
        }

        @Override
        public void surfaceCreated(SurfaceHolder arg0) {
            mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);

            try {
                this.params = mCamera.getParameters();
                List<Camera.Size> allSizes = params.getSupportedPreviewSizes();
                Camera.Size size = allSizes.get(0); // get top size

                for (int i = 0; i < allSizes.size(); i++) {
                    if (allSizes.get(i).width > size.width)
                        size = allSizes.get(i);
                }
                PreviewSizeHeight = size.height;
                PreviewSizeWidth = size.width;
                screenWidth = PreviewSizeHeight;
                screenHeight = PreviewSizeWidth;
//                this.bitmap = Bitmap.createBitmap(PreviewSizeHeight,PreviewSizeWidth, Bitmap.Config.ARGB_8888);
                this.pixels = new int[PreviewSizeWidth * PreviewSizeHeight];
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
}

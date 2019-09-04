package com.tryfit.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.tryfit.R;

import java.io.FileInputStream;

public class ResultActivity extends AppCompatActivity {

    private ImageView imageView;
    //    private TextView textView;
    private Button buttonBad;
    private Button buttonOK;
    private LinearLayout textRersult;
    private TextView stickLengthResult;
    private TextView ballWidthResult;
    private float stickLength;
    private float ballWidth;

    public static final String EXTRA_FOOT = "foot";
    public static final String EXTRA_STICK_LENGTH = "stickLength";
    public static final String EXTRA_BALL_WIDTH = "ballWidth";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_result);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        imageView = (ImageView) findViewById(R.id.processed_imageview_result);
        textRersult = findViewById(R.id.foot_parameters_result);
//        textView = (TextView) findViewById(R.id.text_view_result_id);
        stickLengthResult = findViewById(R.id.stick_length_result);
        ballWidthResult = findViewById(R.id.ball_width_result);
        buttonBad = (Button) findViewById(R.id.buttonBad);
        buttonOK = (Button) findViewById(R.id.buttonOk);
        imageView.setVisibility(View.GONE);

        stickLength = 0;
        ballWidth = 0;

        buttonBad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent();
                data.putExtra(EXTRA_STICK_LENGTH, stickLength);
                data.putExtra(EXTRA_BALL_WIDTH, ballWidth);
                setResult(RESULT_OK, data);
                finish();
            }
        });


        Bitmap bmp = null;
        String filename = getIntent().getStringExtra("image");
        String processingResult = getIntent().getStringExtra("processingResult");
        try {
            FileInputStream is = this.openFileInput(filename);
            bmp = BitmapFactory.decodeStream(is);
            is.close();
            imageView.setImageBitmap(bmp);
//            imageView.setImageResource(R.drawable.ic_photo_library_24dp);
            imageView.setVisibility(View.VISIBLE);
//            textView.setText(processingResult);
            String[] values = processingResult.split(" ");
            stickLength = Float.parseFloat(values[0]);
            ballWidth = Float.parseFloat(values[1]);
            stickLengthResult.setText(String.valueOf(stickLength) + " mm");
            ballWidthResult.setText(String.valueOf(ballWidth) + " mm");

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_back, R.anim.slide_out_back);
    }
}

package com.tryfitCamera.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoView;
import com.tryfitCamera.tryfit.R;

import java.io.FileInputStream;

public class ResultActivity extends AppCompatActivity {

    private PhotoView imageView;
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_result);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        imageView = (PhotoView) findViewById(R.id.processed_imageview_result);
        textView = (TextView) findViewById(R.id.text_view_result_id);
        imageView.setVisibility(View.GONE);


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
            textView.setText(processingResult);
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

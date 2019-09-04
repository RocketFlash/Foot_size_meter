package com.tryfit.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.tryfit.MainActivity;
import com.tryfit.R;
import com.tryfit.common.db.RealmHelper;
import com.tryfit.common.db.models.Client;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private static final long ANIMATION_DURATION = 2000;
    private static final long ANIMATION_DELAY = 1000;

    @BindView(R.id.logo)
    ImageView mLogo;

    interface OnAnimationFinishedListener {
        void onAnimationFinished();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        //Get Client object from DB
        Realm realm = Realm.getDefaultInstance();
        final Client client = RealmHelper.getCurrentClient(realm);
        final String clientId = (client != null) ? client.getId() : null;
        realm.close();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                animateView(mLogo, new OnAnimationFinishedListener() {
                    @Override
                    public void onAnimationFinished() {
                        if (clientId == null) {
                            Log.d(TAG, "Client is null");
                            //New user -> Sign In screen
                            Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        } else {
                            Log.d(TAG, "Client ID: " + clientId);
                            //Existing user -> MainActivity
                            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            if (clientId.equals("demo_user")) {
                                intent.putExtra("demo_user", true);
                            }
                            startActivity(intent);
                        }
                    }
                });
            }
        }, ANIMATION_DELAY);
    }

    public void animateView(final View v, final OnAnimationFinishedListener listener) {
        Animation scaleDown = new ScaleAnimation(
                1.0f, 0.5f, // Start and end values for the X axis scaling
                1.0f, 0.5f, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
        scaleDown.setFillAfter(true); // Needed to keep the result of the animation
        scaleDown.setDuration(ANIMATION_DURATION / 2);
        scaleDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation scaleUp = new ScaleAnimation(
                        0.5f, 1.0f, // Start and end values for the X axis scaling
                        0.5f, 1.0f, // Start and end values for the Y axis scaling
                        Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                        Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
                scaleUp.setFillAfter(true); // Needed to keep the result of the animation
                scaleUp.setDuration(ANIMATION_DURATION / 2);
                scaleUp.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        listener.onAnimationFinished();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                v.startAnimation(scaleUp);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(scaleDown);
    }
}

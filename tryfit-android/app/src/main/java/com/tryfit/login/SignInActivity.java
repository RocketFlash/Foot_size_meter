package com.tryfit.login;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.tryfit.MainActivity;
import com.tryfit.R;
import com.tryfit.camera.PaperDetectionActivity;
import com.tryfit.common.db.models.Measures;
import com.tryfit.common.db.models.Scan;
import com.tryfit.common.utils.SharedPrefsHelper;

import java.io.UnsupportedEncodingException;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;

public class SignInActivity extends AppCompatActivity implements SignInDialog.SignInDialogActionsListener {

    private static final int REQUEST_CODE_SCAN_QR = 0;
    private static final int REQUEST_CODE_SCAN_USING_CAMERA = 1;
    private static final String TAG = SignInActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.action_sign_in)
    void signIn() {
        showSignInDialog();
    }

    private void showSignInDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("sign_in_dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        SignInDialog signInDialog = SignInDialog.newInstance();
        signInDialog.show(ft, "sign_in_dialog");
    }

    @OnClick(R.id.action_scan_using_camera)
    void scanUsingCamera() {
        Intent intent = new Intent(this, PaperDetectionActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN_USING_CAMERA);
    }

    @Override
    public void onScanQrAction() {
        Intent intent = new Intent(this, ScanQRActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN_QR);
    }

    @Override
    public void onSignedInAction() {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SCAN_QR:
                if (data != null) {
                    String qrCode = data.getStringExtra("SCAN_RESULT");
                    Log.d(TAG, "qrCode: " + qrCode);
                    int startIdx = qrCode.indexOf("t=");
                    String jwtToken = qrCode.substring(startIdx + 2, qrCode.length());
                    Log.d(TAG, "jwtToken: " + jwtToken);
                    if (!jwtToken.isEmpty()) {
                        try {
                            ApplicationInfo ai = getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
                            Bundle bundle = ai.metaData;
                            String jwtSecret = bundle.getString("JWTSecret");
                            if (jwtSecret != null && !jwtSecret.isEmpty()) {
                                Jwt jwt = Jwts.parser().setSigningKey(jwtSecret.getBytes("UTF-8")).parse(jwtToken);
                                Log.d(TAG, "body: " + jwt.getBody());

                                SharedPrefsHelper.putString(this, SharedPrefsHelper.SP_ACCESS_TOKEN, jwtToken);
                                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Log.e(TAG, "jwtSecret is null");
                            }
                        } catch (SignatureException | UnsupportedEncodingException | PackageManager.NameNotFoundException | MalformedJwtException ex) {
                            ex.printStackTrace();
                            Toast.makeText(this, "Failed to retrieve JWT token", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            case REQUEST_CODE_SCAN_USING_CAMERA:
                if (data != null) {
                    float stickLength = data.getFloatExtra(PaperDetectionActivity.EXTRA_STICK_LENGTH, 0f);
                    float ballWidth = data.getFloatExtra(PaperDetectionActivity.EXTRA_BALL_WIDTH, 0f);
                    String foot = data.getStringExtra(PaperDetectionActivity.EXTRA_FOOT);
                    Scan scan2d = new Scan();
                    Measures measures = new Measures();
                    measures.setStickLength(stickLength);
                    measures.setBallWidth(ballWidth);
                    if (foot != null && foot.equals("left")) {
                        scan2d.setLeftMeasures(measures);
                    } else {
                        scan2d.setRightMeasures(measures);
                    }
                    showSignUpDialog(scan2d);
                }
                break;
        }
    }

    private void showSignUpDialog(Scan scan) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("sign_up_dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        SignUpDialog signInDialog = SignUpDialog.newInstance();
        signInDialog.show(ft, "sign_up_dialog");
    }
}

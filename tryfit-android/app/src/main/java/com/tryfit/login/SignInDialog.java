package com.tryfit.login;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tryfit.R;
import com.tryfit.common.rest.LoginResponse;
import com.tryfit.common.rest.TryFitWebServiceProvider;
import com.tryfit.common.utils.SharedPrefsHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by alexeyreznik on 15/08/2017.
 */

public class SignInDialog extends DialogFragment {

    private static final String TAG = SignInDialog.class.getSimpleName();

    @BindView(R.id.phone_code)
    AppCompatEditText code;
    @BindView(R.id.phone_number)
    AppCompatEditText number;
    @BindView(R.id.country)
    Spinner country;
    @BindView(R.id.phone_panel)
    LinearLayout phonePanel;
    @BindView(R.id.email)
    AppCompatEditText email;
    @BindView(R.id.password)
    AppCompatEditText password;
    @BindView(R.id.email_panel)
    LinearLayout emailPanel;
    @BindView(R.id.sign_in_by_email)
    TextView signInByEmail;
    @BindView(R.id.sign_in_by_phone)
    TextView signInByPhone;
    @BindView(R.id.sign_in_by_qr)
    ImageButton signInByQr;
    @BindView(R.id.loading)
    ProgressBar loading;

    private SignInDialogActionsListener mActionListener;

    public interface SignInDialogActionsListener {
        void onSignedInAction();

        void onScanQrAction();
    }

    public static SignInDialog newInstance() {
        return new SignInDialog();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sign_in_dialog, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String[] countries = getResources().getStringArray(R.array.countries);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, countries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        country.setAdapter(adapter);

        final String[] codes = getResources().getStringArray(R.array.country_codes);
        country.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                code.setText(codes[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof SignInDialogActionsListener)) {
            Log.e(TAG, "Activity has not implemented SignInDialogActionsListener");
        } else {
            mActionListener = (SignInDialogActionsListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        int width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
        getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @OnClick(R.id.sign_in_by_email)
    void signInByEmail() {
        resetViews();
        phonePanel.setVisibility(View.GONE);
        emailPanel.setVisibility(View.VISIBLE);
        signInByEmail.setVisibility(View.GONE);
        signInByPhone.setVisibility(View.VISIBLE);
        email.requestFocus();
    }

    @OnClick(R.id.sign_in_by_phone)
    void signInByPhone() {
        resetViews();
        emailPanel.setVisibility(View.GONE);
        phonePanel.setVisibility(View.VISIBLE);
        signInByPhone.setVisibility(View.GONE);
        signInByEmail.setVisibility(View.VISIBLE);
        number.requestFocus();
    }

    @OnClick(R.id.action_next)
    void signIn() {
        if (validateFields()) {
            hideKeyboard();
            loading.setVisibility(View.VISIBLE);
            TryFitWebServiceProvider.getInstance().login(email.getText().toString(), password.getText().toString())
                    .enqueue(new Callback<LoginResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                            Log.d(TAG, "onResponse. Status: " + response.code());

                            if (response.code() == 200) {
                                LoginResponse body = response.body();
                                if (body != null) {
                                    //Save access token
                                    String accessToken = body.getAccess_token();
                                    SharedPrefsHelper.putString(getActivity(), SharedPrefsHelper.SP_ACCESS_TOKEN, accessToken);
                                    dismiss();

                                    if (mActionListener != null) {
                                        mActionListener.onSignedInAction();
                                    } else {
                                        Log.e(TAG, "mActionListener is null");
                                    }
                                }

                            } else {
                                password.setError(getString(R.string.error_incorrect_password));
                                password.requestFocus();
                            }

                            loading.setVisibility(View.GONE);
                        }

                        @Override
                        public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                            Log.e(TAG, t.toString());
                            Toast.makeText(getActivity(), "Failed to Sign In. Please check your network connection",
                                    Toast.LENGTH_SHORT).show();
                            loading.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private void hideKeyboard() {
        InputMethodManager mgr = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mgr != null) {
            mgr.hideSoftInputFromWindow(password.getWindowToken(), 0);
        }
    }

    private boolean validateFields() {

        //Check if email is empty
        if (emailPanel.getVisibility() == View.VISIBLE && email.getText().toString().isEmpty()) {
            email.setError(getResources().getString(R.string.error_field_required));
            email.requestFocus();
            return false;
        }

        //Check if email contains @
        if (emailPanel.getVisibility() == View.VISIBLE && !email.getText().toString().contains("@")) {
            email.setError(getResources().getString(R.string.error_incorrect_email));
            email.requestFocus();
            return false;
        }

        //check if password is empty
        if (emailPanel.getVisibility() == View.VISIBLE && password.getText().toString().isEmpty()) {
            password.setError(getResources().getString(R.string.error_field_required));
            password.requestFocus();
            return false;
        }

        //Check if phone number is empty
        if (phonePanel.getVisibility() == View.VISIBLE && number.getText().toString().isEmpty()) {
            number.setError(getResources().getString(R.string.error_field_required));
            number.requestFocus();
            return false;
        }

        //Check if phone number contains only digits
        if (phonePanel.getVisibility() == View.VISIBLE && !number.getText().toString().matches("[0-9]+")) {
            number.setError(getResources().getString(R.string.error_incorrect_number));
            number.requestFocus();
            return false;
        }

        return true;
    }

    private void resetViews() {
        country.setSelection(0);
        number.setText("");
        number.setError(null);
        email.setText("");
        email.setError(null);
        password.setText("");
        password.setError(null);
    }

    @OnClick(R.id.sign_in_by_qr)
    public void signInByQr() {
        dismiss();
        mActionListener.onScanQrAction();
    }
}

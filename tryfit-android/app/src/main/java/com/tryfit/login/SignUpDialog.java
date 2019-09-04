package com.tryfit.login;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tryfit.R;
import com.tryfit.common.db.models.Scan;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by alexeyreznik on 15/08/2017.
 */

public class SignUpDialog extends DialogFragment {

    private static final String TAG = SignUpDialog.class.getSimpleName();
    private static final String ARG_SCAN = "scan";

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
    @BindView(R.id.repeat_password)
    AppCompatEditText repeatPassword;
    @BindView(R.id.email_panel)
    LinearLayout emailPanel;
    @BindView(R.id.sign_up_by_email)
    TextView signUpByEmail;
    @BindView(R.id.sign_up_by_phone)
    TextView signUpByPhone;
    @BindView(R.id.first_name)
    TextView firstName;
    @BindView(R.id.last_name)
    TextView lastName;
    @BindView(R.id.loading)
    ProgressBar loading;

    public interface SignUpDialogActionsListener {
        void onBackAction();
        void onSignedUpAction();
    }

    public static SignUpDialog newInstance() {
        SignUpDialog f = new SignUpDialog();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sign_up_dialog, container, false);
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
    public void onResume() {
        super.onResume();
        int width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
        getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @OnClick(R.id.sign_up_by_email)
    void signInByEmail() {
        resetViews();
        phonePanel.setVisibility(View.GONE);
        emailPanel.setVisibility(View.VISIBLE);
        signUpByEmail.setVisibility(View.GONE);
        signUpByPhone.setVisibility(View.VISIBLE);
        email.requestFocus();
    }

    @OnClick(R.id.sign_up_by_phone)
    void signInByPhone() {
        resetViews();
        emailPanel.setVisibility(View.GONE);
        phonePanel.setVisibility(View.VISIBLE);
        signUpByPhone.setVisibility(View.GONE);
        signUpByEmail.setVisibility(View.VISIBLE);
        number.requestFocus();
    }

    @OnClick(R.id.action_finish)
    void signUp() {
        Toast.makeText(getContext(), "TBD", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.action_back)
    void back() {

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

        //check if repeat password is empty
        if (emailPanel.getVisibility() == View.VISIBLE && repeatPassword.getText().toString().isEmpty()) {
            repeatPassword.setError(getResources().getString(R.string.error_field_required));
            repeatPassword.requestFocus();
            return false;
        }

        //check if both passwords match
        if (emailPanel.getVisibility() == View.VISIBLE &&
                (!repeatPassword.getText().toString().equals(password.getText().toString()))) {
            repeatPassword.setError(getResources().getString(R.string.error_passwords_dont_match));
            repeatPassword.requestFocus();
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

        //Check if first name is empty
        if (firstName.getText().toString().isEmpty()) {
            firstName.setError(getResources().getString(R.string.error_field_required));
            firstName.requestFocus();
            return false;
        }

        //Check if last name is empty
        if (lastName.getText().toString().isEmpty()) {
            lastName.setError(getResources().getString(R.string.error_field_required));
            lastName.requestFocus();
            return false;
        }

        return true;
    }

    private void resetViews() {
        country.setSelection(0);
        number.setText("");
        email.setText("");
        password.setText("");
        repeatPassword.setText("");
        firstName.setText("");
        lastName.setText("");
    }
}

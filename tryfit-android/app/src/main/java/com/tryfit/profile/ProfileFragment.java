package com.tryfit.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tryfit.MainActivity;
import com.tryfit.R;
import com.tryfit.fittings.FittingItemsRepository;
import com.tryfit.common.OnFragmentInteractionListener;
import com.tryfit.common.db.RealmHelper;
import com.tryfit.common.db.models.Client;
import com.tryfit.login.ScanQRActivity;
import com.tryfit.login.SignInActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

public class ProfileFragment extends Fragment {

    public static final int REQUEST_CODE_SCAN_QR_TRYFIT_PROFILE = 0;
    @BindView(R.id.name)
    TextView mName;
    @BindView(R.id.email)
    TextView mEmail;

    private OnFragmentInteractionListener mListener;

    public ProfileFragment() {
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Init client info
        updateClientInfo();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                signOut();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateClientInfo() {
        Realm realm = Realm.getDefaultInstance();
        final Client client = RealmHelper.getCurrentClient(realm);
        if (client != null) {
            String name = client.getName() + " " + client.getSurname();
            mName.setText(name);
            if (client.getEmail() != null) mEmail.setText(client.getEmail());
        } else {
            mName.setText(getString(R.string.unknown));
        }
        realm.close();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.profile));
        }

        ((MainActivity) getActivity()).selectTab(MainActivity.TABS.Profile);
    }

    void signOut() {

        //Clear DB
        Realm realm = Realm.getDefaultInstance();
        RealmHelper.deleteCurrentClient(realm);
        realm.close();

        //Clear repositories
        FittingItemsRepository.getInstance().getItems().clear();
        FittingItemsRepository.getInstance().getGroups().clear();

        Intent intent = new Intent(getActivity(), SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}

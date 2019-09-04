package com.tryfit.fittings;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tryfit.MainActivity;
import com.tryfit.R;
import com.tryfit.common.OnFragmentInteractionListener;
import com.tryfit.common.db.RealmHelper;
import com.tryfit.common.db.models.Client;
import com.tryfit.common.db.models.FittingItem;
import com.tryfit.common.db.models.Group;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class FittingsFragment extends Fragment implements IFittingsView {

    private static final String TAG = FittingsFragment.class.getSimpleName();

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.categories_view)
    GroupsView groupsView;
    @BindView(R.id.fitrate_range_indicator)
    ImageView fitrateRangeIndicator;
    @BindView(R.id.fitrate_range_text)
    TextView fitrateRangeText;
    @BindView(R.id.srl)
    SwipeRefreshLayout srl;
    @BindView(R.id.fitrate_range_panel)
    LinearLayout fitrateRangePanel;

    private OnFragmentInteractionListener mListener;
    private FittingsRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private LayoutManagerType mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
    private String mClientId;
    private String mContractorId;

    private boolean mLoadingMore;
    private FittingsPresenter mFittingsPresenter;

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    public FittingsFragment() {
    }

    public static FittingsFragment newInstance() {
        return new FittingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mFittingsPresenter = new FittingsPresenter(getActivity());

        Realm realm = Realm.getDefaultInstance();
        Client client = RealmHelper.getCurrentClient(realm);
        mClientId = client.getId();
        mContractorId = client.getContractorID();
        realm.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fittings, container, false);
        rootView.setTag(TAG);

        ButterKnife.bind(this, rootView);

        int adapterLayoutId;
        if (mCurrentLayoutManagerType == LayoutManagerType.GRID_LAYOUT_MANAGER) {
            adapterLayoutId = R.layout.fittings_recycler_view_item_layout_grid;
            mLayoutManager = new GridLayoutManager(getActivity(), 2);
        } else {
            adapterLayoutId = R.layout.fittings_recycler_view_item_layout_list;
            mLayoutManager = new LinearLayoutManager(getActivity());
        }
        mAdapter = new FittingsRecyclerViewAdapter(getActivity(), adapterLayoutId, new OnRecyclerItemClickListener() {
            @Override
            public void onRecyclerItemClicked(int position) {
                mListener.onFragmentInteraction(R.id.recycler_view, position);
            }
        });
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addOnScrollListener(new MOnScrollListener());
        groupsView.setOnGroupSelectedListener(new GroupsView.OnGroupSelectedListener() {
            @Override
            public void onGroupSelected(String groupId) {
                mFittingsPresenter.selectGroup(groupId, mClientId);
            }
        });
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mFittingsPresenter.loadGroups(mClientId, mContractorId);
                mFittingsPresenter.loadItems(mClientId);
            }
        });
        return rootView;
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
        if (actionBar != null) actionBar.setTitle(getString(R.string.fittings));
        ((MainActivity) getActivity()).selectTab(MainActivity.TABS.Fittings);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFittingsPresenter.attachView(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mFittingsPresenter.detachView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fittings, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu_grid:
                showAsGrid();
                break;
            case R.id.action_menu_list:
                showAsList();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mFittingsPresenter.getItems().isEmpty()) {
            mFittingsPresenter.loadItems(mClientId);
        } else {
            mAdapter.addItems(mFittingsPresenter.getItems());
        }
        if (mFittingsPresenter.getGroups().isEmpty()) {
            mFittingsPresenter.loadGroups(mClientId, mContractorId);
        } else {
            groupsView.setGroups(mFittingsPresenter.getGroups(), mFittingsPresenter.getSelectedGroup());
        }
    }

    @Override
    public void setGroups(List<Group> groups) {
        groupsView.clear();
        groupsView.setGroups(groups, mFittingsPresenter.getSelectedGroup());
    }

    @Override
    public void setItems(List<FittingItem> items) {
        srl.setRefreshing(false);
        mAdapter.setItems(items);
    }

    @Override
    public void addItems(List<FittingItem> items) {
        mLoadingMore = false;
        mAdapter.addItems(items);
    }

    @Override
    public void displayError(String error) {
        srl.setRefreshing(false);
        Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showProgress(boolean progress) {
        srl.setRefreshing(progress);
    }

    public void showAsList() {
        if (mCurrentLayoutManagerType != LayoutManagerType.LINEAR_LAYOUT_MANAGER) {
            mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
            mLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(mLayoutManager);
            mAdapter = new FittingsRecyclerViewAdapter(getActivity(), R.layout.fittings_recycler_view_item_layout_list, new OnRecyclerItemClickListener() {
                @Override
                public void onRecyclerItemClicked(int position) {
                    mListener.onFragmentInteraction(R.id.recycler_view, position);
                }
            });
            recyclerView.setAdapter(mAdapter);
            mFittingsPresenter.loadItems(mClientId);
        }
    }

    public void showAsGrid() {
        if (mCurrentLayoutManagerType != LayoutManagerType.GRID_LAYOUT_MANAGER) {
            mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
            mLayoutManager = new GridLayoutManager(getActivity(), 2);
            recyclerView.setLayoutManager(mLayoutManager);
            mAdapter = new FittingsRecyclerViewAdapter(getActivity(), R.layout.fittings_recycler_view_item_layout_grid, new OnRecyclerItemClickListener() {
                @Override
                public void onRecyclerItemClicked(int position) {
                    mListener.onFragmentInteraction(R.id.recycler_view, position);
                }
            });
            recyclerView.setAdapter(mAdapter);
            mFittingsPresenter.loadItems(mClientId);
        }
    }

    private void updateFitrateRange() {
        int firstVisibleItemPosition = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
        if (firstVisibleItemPosition >= 0) {
            double fitrate = mAdapter.getItems().get(firstVisibleItemPosition).getSize().getFitrateAbs();
            if (fitrate >= 8.5) {
                fitrateRangeIndicator.setColorFilter(getResources().getColor(R.color.colorFitRateBest));
                fitrateRangeText.setText(String.format("%s %s", getString(R.string.fitrate_range_best), getString(R.string.fitrate_range_best_text)));
            } else if (fitrate < 8.5 && fitrate >= 7.5) {
                fitrateRangeIndicator.setColorFilter(getResources().getColor(R.color.colorFitRateGood));
                fitrateRangeText.setText(String.format("%s %s", getString(R.string.fitrate_range_good), getString(R.string.fitrate_range_good_text)));
            } else if (fitrate < 7.5 && fitrate >= 6.5) {
                fitrateRangeIndicator.setColorFilter(getResources().getColor(R.color.colorFitRateAverage));
                fitrateRangeText.setText(String.format("%s %s", getString(R.string.fitrate_range_average), getString(R.string.fitrate_range_average_text)));
            } else {
                fitrateRangeIndicator.setColorFilter(getResources().getColor(R.color.colorFitRateBad));
                fitrateRangeText.setText(String.format("%s %s", getString(R.string.fitrate_range_bad), getString(R.string.fitrate_range_bad_text)));
            }
            if (fitrateRangePanel.getVisibility() != View.VISIBLE)
                fitrateRangePanel.setVisibility(View.VISIBLE);
        } else {
            fitrateRangePanel.setVisibility(View.GONE);
        }
    }

    private class MOnScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(RecyclerView recyclerView,
                               int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            updateFitrateRange();

            int totalItemCount = mLayoutManager.getItemCount();
            int lastVisibleItem = ((LinearLayoutManager) mLayoutManager)
                    .findLastVisibleItemPosition();
            int lastAvailableItemsOffset;

            if (mCurrentLayoutManagerType == LayoutManagerType.GRID_LAYOUT_MANAGER) {
                lastAvailableItemsOffset = 6;
            } else {
                lastAvailableItemsOffset = 3;
            }

            if (!mLoadingMore
                    && lastVisibleItem + lastAvailableItemsOffset >= totalItemCount) {
                mLoadingMore = true;
                mFittingsPresenter.loadMoreItems(mClientId);
            }
        }
    }
}

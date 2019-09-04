package com.tryfit.fittings;

import android.app.Activity;
import android.content.Context;

import com.tryfit.common.db.models.FittingItem;
import com.tryfit.common.db.models.Group;

import java.util.List;

/**
 * Created by alexeyreznik on 19/09/2017.
 */

public class FittingsPresenter implements IFittingsPresenter {

    private static final String TAG = FittingsPresenter.class.getSimpleName();

    private FittingItemsRepository mFittingItemsRepository;
    private IFittingsView mView;
    private Context mContext;
    private String mSelectedGroupId = Group.GROUP_ID_ALL;

    public FittingsPresenter(Context context) {
        this.mContext = context;
        this.mFittingItemsRepository = FittingItemsRepository.getInstance();
    }

    @Override
    public void attachView(IFittingsView view) {
        this.mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public List<FittingItem> getItems() {
        return mFittingItemsRepository.getItems();
    }

    @Override
    public List<Group> getGroups() {
        return mFittingItemsRepository.getGroups();
    }

    @Override
    public void loadItems(String clientId) {
        if (mView != null) mView.showProgress(true);
        mFittingItemsRepository.loadItems(clientId, mSelectedGroupId, new FittingItemsRepository.OnProductsLoadedListener() {
            @Override
            public void onItemsLoaded() {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mView != null) {
                            mView.showProgress(false);
                            mView.setItems(mFittingItemsRepository.getItems());
                        }
                    }
                });
            }

            @Override
            public void onError() {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mView != null) {
                            mView.showProgress(false);
                            mView.displayError("Failed to load products");
                        }
                    }
                });
            }
        });
    }

    @Override
    public void loadMoreItems(String clientId) {
        mFittingItemsRepository.loadMoreItems(clientId, mSelectedGroupId, new FittingItemsRepository.OnProductsLoadedListener() {
            @Override
            public void onItemsLoaded() {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mView != null) {
                            mView.showProgress(false);
                            mView.addItems(mFittingItemsRepository.getItems());
                        }
                    }
                });
            }

            @Override
            public void onError() {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mView != null) {
                            mView.displayError("Failed to load more products");
                        }
                    }
                });
            }
        });
    }

    @Override
    public void loadGroups(String clientId, String contractorId) {
        mFittingItemsRepository.loadGroups(contractorId, new FittingItemsRepository.OnGroupsLoadedListener() {
            @Override
            public void onGroupsLoaded() {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mView != null) {
                            mView.setGroups(mFittingItemsRepository.getGroups());
                        }
                    }
                });
            }

            @Override
            public void onError() {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mView != null) {
                            mView.displayError("Failed to load groups");
                        }
                    }
                });
            }
        });
    }

    @Override
    public void selectGroup(String groupId, String clientId) {
        mSelectedGroupId = groupId;
        loadItems(clientId);
    }

    @Override
    public String getSelectedGroup() {
        return mSelectedGroupId;
    }
}

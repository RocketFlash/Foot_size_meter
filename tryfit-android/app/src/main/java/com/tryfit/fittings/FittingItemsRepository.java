package com.tryfit.fittings;

import android.support.annotation.NonNull;
import android.util.Log;

import com.tryfit.common.db.models.FittingItem;
import com.tryfit.common.db.models.Group;
import com.tryfit.common.rest.ClientFittingResponse;
import com.tryfit.common.rest.GraphQLRequest;
import com.tryfit.common.rest.GraphQLRequestBuilder;
import com.tryfit.common.rest.GroupsResponse;
import com.tryfit.common.rest.TryFitWebServiceProvider;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by alexeyreznik on 19/09/2017.
 */

public class FittingItemsRepository {

    private static final String TAG = FittingItemsRepository.class.getSimpleName();
    private static final int ITEMS_PER_PAGE = 25;
    private static FittingItemsRepository instance = null;
    private List<FittingItem> mItems;
    private List<Group> mGroups;
    private int mTotal = 0;
    private int mCurrentPage = 0;

    interface OnProductsLoadedListener {
        void onItemsLoaded();

        void onError();
    }

    interface OnGroupsLoadedListener {
        void onGroupsLoaded();

        void onError();
    }

    private FittingItemsRepository() {
        mItems = new ArrayList<>();
        mGroups = new ArrayList<>();
    }

    public static FittingItemsRepository getInstance() {
        if (instance == null) {
            instance = new FittingItemsRepository();
        }
        return instance;
    }

    public List<FittingItem> getItems() {
        return mItems;
    }

    public List<Group> getGroups() {
        return mGroups;
    }

    public void loadItems(String clientId, String groupId, final OnProductsLoadedListener listener) {
        mTotal = 0;
        mCurrentPage = 0;

        GraphQLRequest request = GraphQLRequestBuilder.buildGetClientFittingsRequest(clientId, groupId, 0, ITEMS_PER_PAGE);
        TryFitWebServiceProvider.getInstance().getClientFittings(request).enqueue(new Callback<ClientFittingResponse>() {
            @Override
            public void onResponse(@NonNull Call<ClientFittingResponse> call, @NonNull Response<ClientFittingResponse> response) {
                if (response.isSuccessful()) {
                    ClientFittingResponse body = response.body();
                    if (body != null) {
                        mCurrentPage++;
                        mTotal = body.getData().getClientFitting().getTotal();
                        mItems.clear();
                        List<FittingItem> items = body.getData().getClientFitting().getItems();
                        mItems.addAll(items);

                        Log.d(TAG, "Loaded " + items.size() + " items. Total: " + mItems.size() +
                                " Page: " + mCurrentPage + " / " + (1 + (mTotal - 1) / ITEMS_PER_PAGE));
                        listener.onItemsLoaded();
                    } else {
                        Log.e(TAG, "Response body is null");
                        showError();
                    }
                } else {
                    Log.e(TAG, "Code: " + response.code());
                    showError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ClientFittingResponse> call, @NonNull Throwable t) {
                Log.e(TAG, t.getLocalizedMessage());
                showError();
            }

            void showError() {
                listener.onError();
            }
        });
    }

    public void loadMoreItems(String clientId, String groupId, final OnProductsLoadedListener listener) {
        if (mCurrentPage * ITEMS_PER_PAGE < mTotal) {
            GraphQLRequest request = GraphQLRequestBuilder.buildGetClientFittingsRequest(clientId, groupId, mCurrentPage * ITEMS_PER_PAGE, ITEMS_PER_PAGE);
            TryFitWebServiceProvider.getInstance().getClientFittings(request).enqueue(new Callback<ClientFittingResponse>() {
                @Override
                public void onResponse(@NonNull Call<ClientFittingResponse> call, @NonNull Response<ClientFittingResponse> response) {
                    if (response.isSuccessful()) {
                        ClientFittingResponse body = response.body();
                        if (body != null) {
                            mCurrentPage++;
                            mTotal = body.getData().getClientFitting().getTotal();
                            List<FittingItem> items = body.getData().getClientFitting().getItems();
                            mItems.addAll(items);

                            Log.d(TAG, "Loaded " + items.size() + " items. Total: " + mItems.size() +
                                    " Page: " + mCurrentPage + " / " + (1 + (mTotal - 1) / ITEMS_PER_PAGE));
                            listener.onItemsLoaded();
                        } else {
                            Log.e(TAG, "Response body is null");
                            showError();
                        }
                    } else {
                        Log.e(TAG, "Code: " + response.code());
                        showError();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ClientFittingResponse> call, @NonNull Throwable t) {
                    Log.e(TAG, t.getLocalizedMessage());
                    showError();
                }

                void showError() {
                    listener.onError();
                }
            });
        } else {
            Log.d(TAG, "Limit reached. Items: " + mItems.size() + " total: " + mTotal);
        }
    }

    public void loadGroups(String contractorId, final OnGroupsLoadedListener listener) {

        GraphQLRequest request = GraphQLRequestBuilder.buildGetGroupsRequest(contractorId);
        TryFitWebServiceProvider.getInstance().getGroups(request).enqueue(new Callback<GroupsResponse>() {
            @Override
            public void onResponse(@NonNull Call<GroupsResponse> call, @NonNull Response<GroupsResponse> response) {
                if (response.isSuccessful()) {
                    GroupsResponse body = response.body();
                    if (body != null) {
                        List<Group> groups = body.getData().getGroups();
                        mGroups.clear();
                        mGroups.add(new Group(Group.GROUP_ID_ALL, Group.GROUP_NAME_ALL));
                        mGroups.addAll(groups);
                        Log.d(TAG, "Loaded " + groups.size() + " groups");
                        listener.onGroupsLoaded();
                        listener.onGroupsLoaded();
                    } else {
                        Log.e(TAG, "Response body is null");
                        showError();
                    }
                } else {
                    Log.e(TAG, "Code: " + response.code());
                    showError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GroupsResponse> call, @NonNull Throwable t) {
                Log.e(TAG, t.getLocalizedMessage());
                showError();
            }

            void showError() {
                listener.onError();
            }
        });
    }
}

package com.tryfit.fittings;

import com.tryfit.common.db.models.FittingItem;
import com.tryfit.common.db.models.Group;

import java.util.List;

/**
 * Created by alexeyreznik on 12/09/2017.
 */

public interface IFittingsPresenter {

    void attachView(IFittingsView view);
    void detachView();
    List<FittingItem> getItems();
    void loadItems(String clientId);
    void loadMoreItems(String clientId);
    List<Group> getGroups();
    void loadGroups(String clientId, String contractorId);
    void selectGroup(String groupId, String clientId);
    String getSelectedGroup();
}

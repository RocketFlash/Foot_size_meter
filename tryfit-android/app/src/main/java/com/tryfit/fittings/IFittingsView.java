package com.tryfit.fittings;

import com.tryfit.common.db.models.FittingItem;
import com.tryfit.common.db.models.Group;

import java.util.List;

/**
 * Created by alexeyreznik on 12/09/2017.
 */

public interface IFittingsView {

    void setGroups(List<Group> groups);
    void setItems(List<FittingItem> items);
    void addItems(List<FittingItem> items);
    void displayError(String error);
    void showProgress(boolean progress);
}

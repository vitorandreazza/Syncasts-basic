package com.alchemist.syncasts.ui.main;

import com.alchemist.syncasts.data.store.DataManager;
import com.alchemist.syncasts.data.inject.ConfigPersistentScope;
import com.alchemist.syncasts.ui.BasePresenter;
import javax.inject.Inject;

@ConfigPersistentScope
public class MainPresenter extends BasePresenter<MainMvpView> {

    private final DataManager mDataManager;

    @Inject
    public MainPresenter(DataManager mDataManager) {
        this.mDataManager = mDataManager;
    }

    public void saveCountry(String country) {
        mDataManager.getPreferencesHelper().setCountry(country);
    }

    public String getCountry() {
        return mDataManager.getPreferencesHelper().getCountry();
    }
}

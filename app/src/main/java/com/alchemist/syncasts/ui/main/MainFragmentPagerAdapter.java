package com.alchemist.syncasts.ui.main;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.alchemist.syncasts.R;
import com.alchemist.syncasts.data.inject.ActivityContext;
import com.alchemist.syncasts.ui.main.discover.DiscoverFragment;
import com.alchemist.syncasts.ui.main.subscriptions.SubscriptionsFragment;

import javax.inject.Inject;

public class MainFragmentPagerAdapter extends FragmentPagerAdapter {

    public static final int DISCOVER_FRAGMENT_POSITION = 0;
    public static final int SUBSCRIPTION_FRAGMENT_POSITION = 1;
    private static final int PAGE_COUNTER = 2;
    private final String[] mTabTitles;
    private SparseArray<Fragment> mRegisteredFragments = new SparseArray<>();

    @Inject
    public MainFragmentPagerAdapter(@ActivityContext Context context, FragmentManager fm) {
        super(fm);
        mTabTitles = new String[] {
                context.getString(R.string.title_tab_discover),
                context.getString(R.string.title_tab_subscriptions)
        };
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case DISCOVER_FRAGMENT_POSITION:
                if (mRegisteredFragments.get(DISCOVER_FRAGMENT_POSITION) != null) {
                    return mRegisteredFragments.get(DISCOVER_FRAGMENT_POSITION);
                }
                return DiscoverFragment.newInstance();
            case SUBSCRIPTION_FRAGMENT_POSITION:
                if (mRegisteredFragments.get(SUBSCRIPTION_FRAGMENT_POSITION) != null) {
                    return mRegisteredFragments.get(SUBSCRIPTION_FRAGMENT_POSITION);
                }
                return SubscriptionsFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNTER;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        mRegisteredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        mRegisteredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabTitles[position];
    }

    Fragment getRegisteredFragment(int position) {
        return mRegisteredFragments.get(position);
    }
}

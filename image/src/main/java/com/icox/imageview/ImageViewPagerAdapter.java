package com.icox.imageview;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by jlfxs on 2016/10/25.
 */

public class ImageViewPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mData;

    public ImageViewPagerAdapter(FragmentManager fm, List<Fragment> list) {
        super(fm);

        this.mData = list;
    }

    @Override
    public Fragment getItem(int position) {
        return mData.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }
}


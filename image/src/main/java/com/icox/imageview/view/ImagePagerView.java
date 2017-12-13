package com.icox.imageview.view;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;

import com.icox.imageview.ImageViewPagerAdapter;
import com.icox.imageview.fragment.ImageFragment;
import com.icox.imageview.fragment.ImageInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jlfxs on 2016/10/28.
 */

public class ImagePagerView extends ViewPager {

    private Context mContext;
    private FragmentManager mFragmentManager;
    private List<ImageInfo> mImageInfoList;

    private ImageViewPagerAdapter mPagerAdapter = null;
    private List<Fragment> mPagerData;

    private int mImageViewWidth;
    private int mImageViewHeight;
    private int mNumColumns = 4;
    private int mNumRank = 2;

    private ImageFragment.ImageClickListener mListener;

    public ImagePagerView(Context context) {
        super(context);
        this.mContext = context;
    }

    public ImagePagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }


    public void initView(FragmentManager fragmentManager, List<ImageInfo> imageInfoList,
                         int numColumns, int numRank, int imageViewWidth, int imageViewHeight,
                         ImageFragment.ImageClickListener listener){
        mFragmentManager = fragmentManager;
        mImageInfoList = imageInfoList;
        mNumColumns = numColumns;
        mNumRank = numRank;
        mImageViewWidth = imageViewWidth;
        mImageViewHeight = imageViewHeight;
        mListener = listener;

        initViewPager();
    }

    private void initViewPager(){
        mPagerData = new ArrayList<Fragment>();

        int viewPagerCount = 0;
        if (mImageInfoList.size() == 0){
            viewPagerCount = 0;
        }
        else if (mImageInfoList.size() < (mNumColumns*mNumRank)){
            viewPagerCount = 1;
        }
        else if (mImageInfoList.size() % (mNumColumns*mNumRank) == 0){
            viewPagerCount = mImageInfoList.size() / (mNumColumns*mNumRank);
        }else if (mImageInfoList.size() % (mNumColumns*mNumRank) != 0){
            viewPagerCount = mImageInfoList.size() / (mNumColumns*mNumRank) + 1;
        }

        for (int i = 0; i < viewPagerCount; i++){
            int fromIndex = i*(mNumColumns*mNumRank);
            int toIndex = fromIndex + (mNumColumns*mNumRank);
            if (toIndex > mImageInfoList.size()){
                toIndex = mImageInfoList.size();
            }

            ImageFragment imageFragment = new ImageFragment();
            imageFragment.initGridView(mImageInfoList.subList(fromIndex, toIndex),
                    mNumColumns, mImageViewWidth, mImageViewHeight, mListener);
            mPagerData.add(imageFragment);
        }

        if (mPagerAdapter == null) {
            mPagerAdapter = new ImageViewPagerAdapter(mFragmentManager, mPagerData);
            setAdapter(mPagerAdapter);
            setOffscreenPageLimit(3);
        }else {
            mPagerAdapter.notifyDataSetChanged();
        }
    }

}

package com.icox.manager.localview;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.ImageButton;

import com.icox.manager.R;
import com.polites.android.GestureImageView;

import java.util.ArrayList;

/**
 * Created by sony on 2016/1/10
 */
public class LocalImageShower extends Activity {

    private android.os.Handler handler = new android.os.Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    //                    hideVirtualkey(mDecorView);
                    initWindow();
                    break;
            }
        }
    };

    /**
     * 接收的值
     */
    // 当前路径下的图片集合
    private ArrayList<String> mFilePathArray;
    // 用户点的位置
    private int clickPosition;

    private ViewPager mViewPager;
    private MyPagerAdapter mMyPagerAdapter;
    // adapter data
    private View[] views;

    private Context mContext;
    private View mDecorView;
    private ImageButton mButGoBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindow();
        setContentView(R.layout.activity_imageshower);

        mContext = this;
        //        mDecorView = getWindow().getDecorView();
        //        hideVirtualkey(mDecorView);

        // 获取数据
        getData();
        // 实例化控件
        initView();
        // 设置适配器
        setImageAdapter();
        // 设置监听事件
        setListener();
    }

    /**
     * 获取图片数据与位置
     */
    private void getData() {
        // 从本地相册查看多个图片
        mFilePathArray = getIntent().getStringArrayListExtra("ArrayDirPath");
        // 从文件管理器，查看单个的图片;
        if (mFilePathArray == null) {
            mFilePathArray = new ArrayList<String>();
            mFilePathArray.add(getIntent().getStringExtra("path"));
        }
        clickPosition = getIntent().getIntExtra("clickPosition", 0);
    }

    /**
     * 隐藏虚拟按键
     */
    public void hideVirtualkey(View decorView) {
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    /**
     * 实例化控件
     */
    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.vp_view);
        mButGoBack = (ImageButton) findViewById(R.id.images_shower_iv_goBack);
        mButGoBack.setFocusable(true);
        mButGoBack.setFocusableInTouchMode(true);
        mButGoBack.setOnFocusChangeListener(mFocusChangeListener);
    }

    /**
     * 设置适配器
     */
    private void setImageAdapter() {
        mMyPagerAdapter = new MyPagerAdapter();
        mViewPager.setAdapter(mMyPagerAdapter);
        // 设置视图的位置
        mViewPager.setCurrentItem(clickPosition);
    }

    /**
     * 设置监听事件
     */
    private void setListener() {
//        mViewPager.setOnPageChangeListener(new MyPageChangeListener());

        mButGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private class MyPagerAdapter extends PagerAdapter {

        public MyPagerAdapter() {
            views = new View[mFilePathArray.size()];
        }

        @Override
        public int getCount() {
            return views.length;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            //判断将要显示的图片是否和现在显示的图片是同一个
            //arg0为当前显示的图片，arg1是将要显示的图片
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //销毁该图片
            container.removeView(views[position]);
            views[position] = null;

            // TODO 处理ViewPager的载入大图Out of Memory问题
            //            将 Bitmap bitmap 设置成全局,instantiateItem()里的bitmap也是赋予此值,进行删除
            //            Bitmap bitmap = BitmapFactory.decodeFile(mFilePathArray.get(position));
            //            bitmap.recycle();
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View convertView = LayoutInflater.from(LocalImageShower.this).inflate(R.layout.view_image_item, null);

            Bitmap bitmap = BitmapFactory.decodeFile(mFilePathArray.get(position));
            views[position] = convertView;
            container.addView(views[position]);
            //TODO GestureImageView
            GestureImageView gestureImageview = (GestureImageView) convertView.findViewById(R.id.gestureImageview);
            //            Picasso.with(mContext)
            //                    .load(new File(mFilePathArray.get(position)))
            //                    .error(R.drawable.ic_empty)
            //                    .into(gestureImageview);
            gestureImageview.setImageBitmap(bitmap);

            gestureImageview.setOnClickListener(null);
            gestureImageview.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View view) {
                    //TODO onLongClick
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("image/*");
                    String path = mFilePathArray.get(position);
                    Uri uri = Uri.parse(path);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(intent, "分享图片到"));
                    return false;
                }
            });
            return views[position];
        }
    }

    // 监听ViewPager的变化
    private class MyPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
            //            Log.i("PageChange", "onPageScrollStateChanged:" + arg0);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            //            Log.i("PageChange", "onPageScrolled:" + arg0);
            //            Log.i("PageChange", "onPageScrolled:" + arg1);
            //            Log.i("PageChange", "onPageScrolled:" + arg2);
        }

        @Override
        public void onPageSelected(int position) {
            //            Log.i("PageChange", "onPageSelected:" + position);

            handler.removeMessages(1);
            handler.sendEmptyMessageDelayed(1, 9000);
        }
    }

    /**
     * 按键监听
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            return super.onKeyDown(keyCode, event);
        }

        // 菜单键
        if (KeyEvent.KEYCODE_MENU == keyCode) {
            if (getCurrentFocus().getId() == R.id.vp_view) {
                int position = mViewPager.getCurrentItem();

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("image/*");
                String path = mFilePathArray.get(position);
                Uri uri = Uri.parse(path);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(intent, "分享图片到"));

                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    public void initWindow() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    /**
     * 提供选中放大的效果
     */
    public View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            viewAnimation(v, hasFocus);
        }
    };

    /**
     * 增加动画
     */
    private void addIcoxAnimator(View view) {
        view.getId();
        ObjectAnimator a3 = null;
        ObjectAnimator a2 = null;
        a3 = ObjectAnimator.ofFloat(view, "rotation", 0f, 0f);
        a2 = ObjectAnimator.ofFloat(view, "translationY", 0f, -50, 0f);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(1000);
        set.play(a3).with(a2);
        // 设置插值器。
        set.setInterpolator(new BounceInterpolator());
        set.start();
    }


    public void viewAnimation(View v, boolean hasFocus) {

        if (v == null) {
            return;
        }

        int focus = 0;
        if (hasFocus) {
            focus = com.icox.mediafilemanager.R.anim.enlarge;
            addIcoxAnimator(v);
        } else {
            focus = com.icox.mediafilemanager.R.anim.decrease;
        }

        //如果有焦点就放大，没有焦点就缩小
        Animation mAnimation = AnimationUtils.loadAnimation(getApplication(), focus);
        mAnimation.setBackgroundColor(Color.TRANSPARENT);
        mAnimation.setFillAfter(hasFocus);
        v.startAnimation(mAnimation);
        mAnimation.start();
        v.bringToFront();
    }

}

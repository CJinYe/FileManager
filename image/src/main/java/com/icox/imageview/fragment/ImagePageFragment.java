package com.icox.imageview.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.icox.imageview.R;
import com.icox.imageview.view.TouchImageView;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by Liujian on 2017/2/25.
 */

public class ImagePageFragment extends Fragment {

    public static ImagePageFragment newInstance(String filePath, int pageIndex) {

        Bundle args = new Bundle();
        args.putString("file_path", filePath);
        args.putInt("page_index", pageIndex);

        ImagePageFragment fragment = new ImagePageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private String mFilePath = "";
    private int mPageIndex = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            mFilePath = bundle.getString("file_path");
            mPageIndex = bundle.getInt("page_index");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_image_page, container, false);

        TouchImageView touchImageView = (TouchImageView) v.findViewById(R.id.tiv_image);
        Picasso.with(getActivity().getApplicationContext())
                .load(new File(mFilePath))
//                .resize(mScreenWidth, mScreenHeight)
                .error(R.mipmap.ic_launcher)
                .into(touchImageView);

        touchImageView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View view) {
                //TODO onLongClick
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("image/*");
                String path = mFilePath;
                Uri uri = Uri.parse(path);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(intent, "分享图片到"));
                return false;
            }
        });

        return v;
    }
}

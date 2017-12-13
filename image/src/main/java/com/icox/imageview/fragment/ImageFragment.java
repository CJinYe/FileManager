package com.icox.imageview.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.icox.imageview.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jlfxs on 2016/10/25.
 */

public class ImageFragment extends Fragment {

    private Context mContext;

    private GridView mGridView;
    private List<ImageInfo> mGridData = new ArrayList<ImageInfo>();
    private ImageGridViewAdapter mGridAdapter;

    private int mNumColumns;
    private int mImageViewWidth;
    private int mImageViewHeight;

    private ImageClickListener mListener;
    public interface ImageClickListener{
        void imageClick(ImageInfo imageInfo);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);

        mContext = getActivity();

        return inflater.inflate(R.layout.fragment_image, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGridView = (GridView) view.findViewById(R.id.gv_image);

        mGridAdapter = new ImageGridViewAdapter(mContext, mGridData);
        mGridAdapter.setInfo(mImageViewWidth, mImageViewHeight);
        mGridView.setNumColumns(mNumColumns);
        mGridView.setAdapter(mGridAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageInfo imageInfo = mGridData.get(position);
                mListener.imageClick(imageInfo);
            }
        });
        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                final ImageInfo imageInfo = mGridData.get(position);
                if (imageInfo.coverType == 1){
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage("确认删除该图片吗？");
                    builder.setTitle("提示");
                    builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            File file = new File(imageInfo.filePath);
                            if (file.exists()){
                                if (file.delete()){
                                    mGridData.remove(position);
                                    mGridAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }

                return true;
            }
        });
    }

    public void initGridView(List<ImageInfo> list, int numColumns,
                             int imageViewWidth, int imageViewHeight,
                             ImageClickListener listener){
        mGridData = list;
        mNumColumns = numColumns;
        mImageViewWidth = imageViewWidth;
        mImageViewHeight = imageViewHeight;
        mListener = listener;
    }

//    public void updateGridView(List<ImageInfo> list){
//        mGridData = list;
//        mGridAdapter.notifyDataSetChanged();
//    }
}

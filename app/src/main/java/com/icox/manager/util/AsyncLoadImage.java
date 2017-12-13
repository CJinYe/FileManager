package com.icox.manager.util;

import java.io.InputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.icox.manager.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

public class AsyncLoadImage {

    private static final String TAG = "AsyncLoadImage";
    private ConcurrentLinkedQueue<CacheImg> imageQueue;
    private Context context;
    private int cacheCount;
    private AsyncTask<Object, Object, Void> asyncTask;
    private Handler handler;
    private static final int IMAGE = 0;
    private ImageView imageView;
    private Object lock = new Object();
    private boolean allowLoad = true;

    public AsyncLoadImage(Context context, Handler handler) {
        this.handler = handler;
        this.context = context;
        this.imageQueue = new ConcurrentLinkedQueue<CacheImg>();
    }

    public void loadImage(ImageView imageView, int cacheCount) {
//        this.imageView = imageView;
//        this.cacheCount = cacheCount;
//
//        String path = (String) imageView.getTag();
//        for (CacheImg img : imageQueue) {
//            String imgPath = img.getPathOrUrl();
//            if (path.equals(imgPath)) {
//                Bitmap bitmap = img.getBitmap();
//                imageView.setImageBitmap(bitmap);
//                return;
//            }
//        }
//        InputStream is = context.getResources().openRawResource(R.drawable.format_picture);
//        Bitmap bm = BitmapFactory.decodeStream(is);
//        imageView.setImageBitmap(bm);
//        // new AsyncLoadImageThread(imageView).start();
//        asyncTask = new AsyncLoadImageTask().execute(imageView);
    }

    public void loadImage2(ImageView imageView, int cacheCount) {
        this.imageView = imageView;
        this.cacheCount = cacheCount;

        String path = (String) imageView.getTag();
        for (CacheImg img : imageQueue) {
            String imgPath = img.getPathOrUrl();
            if (path.equals(imgPath)) {
                Bitmap bitmap = img.getBitmap();
                imageView.setImageBitmap(bitmap);
                return;
            }
        }
//        InputStream is = context.getResources().openRawResource(R.drawable.format_picture);
//        Bitmap bm = BitmapFactory.decodeStream(is);

        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.format_picture);
        imageView.setImageBitmap(bm);
        new AsyncLoadImageThread2(imageView).start();
        // asyncTask = new AsyncLoadImageTask().execute(imageView);
    }

    public void lock() {
        allowLoad = false;
    }

    public void unlock() {
        allowLoad = true;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    class AsyncLoadImageThread2 extends Thread {

        String path = null;
        ImageView imageView = null;

        public AsyncLoadImageThread2(ImageView imageView) {
            this.imageView = imageView;
            this.path = (String) imageView.getTag();
        }

        @Override
        public void run() {

            if (!allowLoad) {
                Log.d(TAG, "prepare to load");
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            CacheImg imgCahe = new CacheImg();
            imgCahe.setPathOrUrl(path);

            Bitmap bitmap = null;
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inSampleSize = 4;
            bitmap = BitmapFactory.decodeFile(path, option);
            imgCahe.setBitmap(bitmap);
            if (imageQueue.size() >= cacheCount) {
                imageQueue.poll();
            }
            imageQueue.add(imgCahe);

            final Bitmap bitmap2 = bitmap;

            handler.post(new Runnable() {

                @Override
                public void run() {
                    imageView.setImageBitmap(bitmap2);
                }
            });
        }

    }

    class AsyncLoadImageThread extends Thread {

        String path = null;
        ImageView imageView = null;

        public AsyncLoadImageThread(ImageView imageView) {
            this.imageView = imageView;
            this.path = (String) imageView.getTag();
        }

        @Override
        public void run() {
            loadImage(path);
        }

    }

    private void loadImage(String path) {
        CacheImg imgCahe = new CacheImg();
        imgCahe.setPathOrUrl(path);

        Bitmap bitmap = null;
//		if (imageQueue.contains(imgCahe)) {
//			for (CacheImg img : imageQueue) {
//				if (img.equals(imgCahe)) {
//					bitmap = img.getBitmap();
//				}
//			}
//		} else {
//			BitmapFactory.Options option = new BitmapFactory.Options();
//			option.inSampleSize = 4;
//			bitmap = BitmapFactory.decodeFile(path, option);
//			imgCahe.setBitmap(bitmap);
//			if (imageQueue.size() >= cacheCount) {
//				imageQueue.poll();
//			}
//			imageQueue.add(imgCahe);
//		}

        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inSampleSize = 4;
        bitmap = BitmapFactory.decodeFile(path, option);
        imgCahe.setBitmap(bitmap);
        if (imageQueue.size() >= cacheCount) {
            imageQueue.poll();
        }
        imageQueue.add(imgCahe);
        final Bitmap bitmap2 = bitmap;

        handler.post(new Runnable() {

            @Override
            public void run() {
                imageView.setImageBitmap(bitmap2);
            }
        });

    }

    class AsyncLoadImageTask extends AsyncTask<Object, Object, Void> {

        @Override
        protected Void doInBackground(Object... params) {

//            ImageView imageView = (ImageView) params[0];
//            String path = (String) imageView.getTag();
//
//            CacheImg imgCahe = new CacheImg();
//            imgCahe.setPathOrUrl(path);
//
//            Bitmap bitmap = null;
//            BitmapFactory.Options option = new BitmapFactory.Options();
//            option.inSampleSize = 4;
//            bitmap = BitmapFactory.decodeFile(path, option);
//            imgCahe.setBitmap(bitmap);
//            if (imageQueue.size() >= cacheCount) {
//                imageQueue.poll();
//            }
//            imageQueue.add(imgCahe);
//            publishProgress(new Object[]{imageView, bitmap});
            return null;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            ImageView imageView = (ImageView) values[0];
            Bitmap bitmap = (Bitmap) values[1];
            imageView.setImageBitmap(bitmap);
        }

    }

}

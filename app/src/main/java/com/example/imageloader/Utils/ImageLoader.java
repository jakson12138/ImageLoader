package com.example.imageloader.Utils;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.annotation.Keep;
import android.telephony.IccOpenLogicalChannelResponse;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.example.imageloader.MainActivity;
import com.example.imageloader.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ImageLoader {

    private static final String TAG = "ImageLoader";

    private static final int IMG_URL = R.id.iv_url;
    private static final int BITMAP_LOAD_FINISH = 0;

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAX_POOL_SIZE = 2 * CPU_COUNT + 1;
    private static final long KEEP_ALIVE = 5L;

    private static final int DISK_CACHE_SIZE = 30 * 1024 * 1024;

    private static final Executor threadPoolExecutor = new
            ThreadPoolExecutor(CORE_POOL_SIZE,MAX_POOL_SIZE,KEEP_ALIVE, TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());

    private Handler mMainHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case BITMAP_LOAD_FINISH:
                    Result result = (Result) msg.obj;
                    ImageView imageView = result.imageView;
                    String url = (String) imageView.getTag(IMG_URL);
                    if(url.equals(result.url)){
                        imageView.setImageBitmap(result.bitmap);
                    }
                    else{
                        Log.d(TAG,"url不符");
                    }
                    MainActivity.afterLoad();
            }
        }
    };

    private LruCache<String,Bitmap> mMemoryCache;
    private DiskLruCache mDiskLruCache;

    private static ImageLoader imageLoader;

    private Context mContext;

    private ImageLoader(Context context){
        mContext = context.getApplicationContext();
        mMemoryCache = LruCacheUtils.Instance();

        File diskCacheDir = getAppCacheDir(mContext,"images");
        if(!diskCacheDir.exists()){
            diskCacheDir.mkdirs();
        }
        if(diskCacheDir.getUsableSpace() > DISK_CACHE_SIZE){
            try{
                mDiskLruCache = DiskLruCache.open(diskCacheDir,1,1,DISK_CACHE_SIZE);
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public static ImageLoader getInstance(Context context){
        if(imageLoader == null){
            synchronized (ImageLoader.class){
                if(imageLoader  == null){
                    imageLoader = new ImageLoader(context);
                }
            }
        }
        return imageLoader;
    }

    private static File getAppCacheDir(Context context,String dirName){
        String cacheDirString;
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            cacheDirString = context.getExternalCacheDir().getPath();
        }
        else{
            cacheDirString = context.getCacheDir().getPath();
        }
        return new File(cacheDirString + File.separator + dirName);
    }

    private void addToMemoryCache(String key,Bitmap bitmap){
        if(getFromMemoryCaches(key) == null){
            mMemoryCache.put(key,bitmap);
        }
    }

    private Bitmap getFromMemoryCaches(String key){
        return mMemoryCache.get(key);
    }

    //异步加载图片
    public void displayImage(final String url,final ImageView imageView,final int dstWidth,final int dstHeight){
        imageView.setTag(IMG_URL,url);
        Bitmap bitmap = loadFromMemory(url);
        if(bitmap != null){
            imageView.setImageBitmap(bitmap);
            MainActivity.afterLoad();
            return;
        }

        Runnable loadBitmapTask = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = loadBitmap(url,dstWidth,dstHeight);
                if(bitmap != null){
                    Result  result = new Result(imageView,bitmap,url);
                    Message msg = mMainHandler.obtainMessage(BITMAP_LOAD_FINISH,result);
                    msg.sendToTarget();
                }
            }
        };
        threadPoolExecutor.execute(loadBitmapTask);
    }

    //同步加载图片
    //先从内存缓存，再到磁盘缓存，最后到网络获取
    public Bitmap loadBitmap(String url,int dstWidth,int dstHeight){
        Bitmap bitmap = loadFromMemory(url);
        if(bitmap != null){
            return bitmap;
        }
        try{
            bitmap = loadFromDisk(url,dstWidth,dstHeight);
            if(bitmap != null){
                return bitmap;
            }
            bitmap = loadFromNet(url,dstWidth,dstHeight);
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return bitmap;
    }

    //从内存缓缓存中获取bitmap
    private Bitmap loadFromMemory(String url){
        String key = getKeyFromUrl(url);
        Bitmap bitmap = getFromMemoryCaches(key);
        return bitmap;
    }

    //从磁盘缓存中获取bitmap
    private Bitmap loadFromDisk(String url,int dstWidth,int dstHeight) throws  IOException{
        if(Looper.myLooper() == Looper.getMainLooper()){
            Log.d(TAG,"不能在主线程访问磁盘缓存");
        }

        if(mDiskLruCache == null){
            return null;
        }

        Bitmap bitmap = null;
        String key = getKeyFromUrl(url);
        DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
        if(snapshot != null){
            FileInputStream fileInputStream = (FileInputStream) snapshot.getInputStream(0);
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            bitmap = decodeSampledBitmapFromFD(fileDescriptor,dstWidth,dstHeight);
            if(bitmap != null){
                addToMemoryCache(key,bitmap);
            }
        }
        return bitmap;
    }

    //从网络上拉去bitmap，并放到磁盘缓存
    private Bitmap loadFromNet(String url,int dstWidth,int dstHeight) throws IOException {
        if(Looper.myLooper() == Looper.getMainLooper()){
            throw new RuntimeException("不能在主线程加载网络bitmap");
        }

        if(mDiskLruCache == null){
            return null;
        }

        String key = getKeyFromUrl(url);
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        if(editor != null){
            OutputStream outputStream = editor.newOutputStream(0);
            if(getStringFromNet(url,outputStream)){
                editor.commit();
            }
            else{
                editor.abort();
            }
            mDiskLruCache.flush();
        }
        return loadFromDisk(url,dstWidth,dstHeight);
    }

    //给定url,通过http获取图片输出流
    public boolean getStringFromNet(String urlStr, OutputStream outputStream){
        HttpURLConnection urlConnection = null;
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;

        try{
            final URL url = new URL(urlStr);
            urlConnection = (HttpURLConnection) url.openConnection();
            bufferedInputStream = new BufferedInputStream(urlConnection.getInputStream());
            bufferedOutputStream = new BufferedOutputStream(outputStream);

            int byteRead;
            while((byteRead = bufferedInputStream.read()) != -1){
                bufferedOutputStream.write(byteRead);
            }
            return true;
        }
        catch(IOException e){
            e.printStackTrace();
        }
        finally{
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            close(bufferedInputStream);
            close(bufferedOutputStream);
        }
        return false;
    }

    private void close(Closeable stream){
        if(stream != null){
            try{
                stream.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    //先获取bitmap的实际大小，然后计算采样率，再实际加载bitmap
    private Bitmap decodeSampledBitmapFromFD(FileDescriptor fd,int dstWidth,int dstHeight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; //decode时不加载，只计算实际大小
        BitmapFactory.decodeFileDescriptor(fd,null,options);
        options.inSampleSize = calInSampleSize(options,dstWidth,dstHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fd,null,options);
    }

    //根据bitmap实际大小以及我们期望的图片大小计算options的采样率
    private int calInSampleSize(BitmapFactory.Options options,int dstWidth,int dstHeight){
        int rawWidth = options.outWidth;
        int rawHeight = options.outHeight;
        int inSampleSize = 1;
        if(rawWidth > dstWidth || rawHeight > dstHeight){
            float ratioWidth = (float) rawWidth / dstWidth;
            float ratioHeight = (float) rawHeight / dstHeight;
            inSampleSize = (int) Math.min(ratioWidth,ratioHeight);
        }
        return inSampleSize;
    }

    public static String getKeyFromUrl(String url){
        String key;
        try{
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(url.getBytes());
            byte[] m = messageDigest.digest();
            return getString(m);
        }
        catch (NoSuchAlgorithmException e){
            key = String.valueOf(url.hashCode());
        }
        return key;
    }

    private static String getString(byte[] b){
        StringBuffer sb = new StringBuffer();
        for(int i = 0;i < b.length;i++)
            sb.append(b[i]);
        return sb.toString();
    }

    private static class Result{
        public ImageView imageView;
        public Bitmap bitmap;
        public String url;

        public Result(ImageView imageView,Bitmap bitmap,String url){
            this.imageView = imageView;
            this.bitmap = bitmap;
            this.url = url;
        }
    }

}

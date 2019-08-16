package com.example.imageloader.Utils;

import android.graphics.Bitmap;
import android.util.LruCache;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LruCacheUtils {

    private static LruCache<String, Bitmap> mMemoryCache;

    public static LruCache<String,Bitmap> Instance(){
        if(mMemoryCache == null){
            synchronized (LruCacheUtils.class){
                if(mMemoryCache == null){
                    int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
                    int memoryCacheSize = maxMemory / 8;
                    mMemoryCache = new LruCache<String,Bitmap>(memoryCacheSize){
                        @Override
                        protected  int sizeOf(String key,Bitmap bitmap){
                            return bitmap.getByteCount() / 1024;
                        }
                    };
                }
            }
        }
        return mMemoryCache;
    }



}

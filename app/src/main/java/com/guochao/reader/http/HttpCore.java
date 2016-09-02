package com.guochao.reader.http;

import android.content.Context;

import com.guochao.reader.net.NetService;

import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpCore {

    private OkHttpClient mHttpClient;
    private NetService mNetService;
    private volatile static HttpCore sInstance;
    private final static String BASE_URL = "http://apis.baidu.com";
    private final static int TIME_OUT_MILLISECONDS = 100000;

    private HttpCore(Context context) {
        if (mHttpClient == null) {
            mHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(TIME_OUT_MILLISECONDS, TimeUnit.MILLISECONDS)
                    .readTimeout(TIME_OUT_MILLISECONDS, TimeUnit.MILLISECONDS)
                    .writeTimeout(TIME_OUT_MILLISECONDS, TimeUnit.MILLISECONDS)
                    .cache(new Cache(context.getExternalCacheDir().getAbsoluteFile(), 10 * 1024 * 1024))
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(mHttpClient)
                    .build();
            mNetService = retrofit.create(NetService.class);
        }
    }

    public static HttpCore getInstance(Context context) {
        if (sInstance == null) {
            synchronized (HttpCore.class) {
                if (sInstance == null) {
                    sInstance = new HttpCore(context);
                }
            }
        }
        return sInstance;
    }

    public OkHttpClient getHttpClient() {
        return mHttpClient;
    }

    public NetService getNetService() {
        return mNetService;
    }

}

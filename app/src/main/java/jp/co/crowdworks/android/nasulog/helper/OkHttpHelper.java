package jp.co.crowdworks.android.nasulog.helper;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import okhttp3.OkHttpClient;

public class OkHttpHelper {
    private static OkHttpClient sHttpClient = new OkHttpClient.Builder().addNetworkInterceptor(new StethoInterceptor()).build();
    public static OkHttpClient getClient() {
        return sHttpClient;
    }
}
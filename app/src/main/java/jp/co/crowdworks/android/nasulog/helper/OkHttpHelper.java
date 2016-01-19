package jp.co.crowdworks.android.nasulog.helper;

import okhttp3.OkHttpClient;

public class OkHttpHelper {
    private static OkHttpClient sHttpClient = new OkHttpClient();
    public static OkHttpClient getClient() {
        return sHttpClient;
    }
}
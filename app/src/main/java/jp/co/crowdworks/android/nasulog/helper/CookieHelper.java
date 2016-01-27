package jp.co.crowdworks.android.nasulog.helper;

import android.content.Context;
import android.os.Build;
import android.webkit.CookieSyncManager;

public class CookieHelper {
    public static interface CookieHandler{
        public void handleCookie(String key, String value);
    }

    public static void setupCookieSyncManager(Context context){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            CookieSyncManager.createInstance(context);
        }
    }
    public static void startSyncCookie(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            CookieSyncManager.getInstance().startSync();
        }
    }
    public static void stopSyncCookie(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            CookieSyncManager.getInstance().stopSync();
        }
    }

    public static void parse(String cookie, CookieHandler handler){
        if(cookie != null){
            for(String kv : cookie.split(";")){
                String[] pair = kv.trim().split("=");
                if(pair.length==2){
                    String key = pair[0].trim();
                    String value = pair[1].trim();
                    if(key.length()>0 && value.length()>0) {
                        handler.handleCookie(key, value);
                    }
                }
            }
        }
    }
}

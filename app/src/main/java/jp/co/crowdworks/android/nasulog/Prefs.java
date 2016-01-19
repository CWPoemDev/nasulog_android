package jp.co.crowdworks.android.nasulog;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

public class Prefs {
    private static String NAME="config.xml";

    public static String KEY_SERVER="server";
    public static String KEY_TOKEN="token";
    public static String KEY_TOKEN_VERIFIED="token_verified";

    public static SharedPreferences get(@NonNull Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }
}

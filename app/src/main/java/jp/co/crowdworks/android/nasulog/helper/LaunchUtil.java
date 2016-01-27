package jp.co.crowdworks.android.nasulog.helper;

import android.content.Context;
import android.content.Intent;

import hugo.weaving.DebugLog;
import jp.co.crowdworks.android.nasulog.activity.PoemListActivity;
import jp.co.crowdworks.android.nasulog.service.NasulogAPIService;

public class LaunchUtil {
    @DebugLog
    public static void showPoemListActivity(Context context){
        NasulogAPIService.keepalive(context); //MainActivityでAPIをいろいろ使うためにバックエンドサービスを先に立ち上げておく。

        Intent intent = new Intent(context, PoemListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}

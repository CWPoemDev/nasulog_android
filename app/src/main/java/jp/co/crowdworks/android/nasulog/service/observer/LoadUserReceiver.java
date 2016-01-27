package jp.co.crowdworks.android.nasulog.service.observer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import io.realm.Realm;
import jp.co.crowdworks.android.nasulog.model.User;
import jp.co.crowdworks.android.nasulog.service.NasulogAPI;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoadUserReceiver extends BroadcastReceiver implements Registerable {
    private static final String TAG = LoadUserReceiver.class.getName();
    public static final String REQUEST_USER = "jp.co.crowdworks.android.nasulog.LOAD_USER";

    protected final Context mContext;
    protected final NasulogAPI mAPI;

    public LoadUserReceiver(Context context, NasulogAPI api) {
        mContext = context;
        mAPI = api;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (REQUEST_USER.equals(action)) {
            final Realm realm = Realm.getDefaultInstance();
            mAPI.getUser().subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(jsonUser -> {
                realm.beginTransaction();
                realm.createOrUpdateObjectFromJson(User.class, jsonUser);
            }, err -> {
                Log.e(TAG, "error", err);
                if (realm.isInTransaction()) realm.cancelTransaction();
            }, () -> {
                if (realm.isInTransaction()) realm.commitTransaction();
            });
        }
    }

    @Override
    public void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(REQUEST_USER);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(this, filter);
    }

    @Override
    public void unregister() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
    }
}

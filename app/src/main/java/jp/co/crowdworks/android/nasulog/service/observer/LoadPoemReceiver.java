package jp.co.crowdworks.android.nasulog.service.observer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import io.realm.Realm;
import jp.co.crowdworks.android.nasulog.model.Poem;
import jp.co.crowdworks.android.nasulog.service.NasulogAPI;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoadPoemReceiver extends BroadcastReceiver implements Registerable {
    private static final String TAG = LoadPoemReceiver.class.getName();
    public static final String REQUEST_POEMS = "jp.co.crowdworks.android.nasulog.LOAD_POEMS";
    public static final String REQUEST_POEM = "jp.co.crowdworks.android.nasulog.LOAD_POEM";
    public static final String KEY_POEM_ID = "_poem_id";

    protected final Context mContext;
    protected final NasulogAPI mAPI;

    public LoadPoemReceiver(Context context, NasulogAPI api) {
        mContext = context;
        mAPI = api;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (REQUEST_POEMS.equals(action)) {
            final Realm realm = Realm.getDefaultInstance();
            mAPI.getAllPoems().subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(jsonPoem -> {
                if (!realm.isInTransaction()) realm.beginTransaction();
                realm.createOrUpdateObjectFromJson(Poem.class, jsonPoem);
            }, err -> {
                Log.e(TAG, "error", err);
                if (realm.isInTransaction()) realm.cancelTransaction();
            }, () -> {
                if (realm.isInTransaction()) realm.commitTransaction();
            });
        }
        else if (REQUEST_POEM.equals(action) && intent.hasExtra(KEY_POEM_ID)) {
            final long id = intent.getLongExtra(KEY_POEM_ID,-1);
            if (id==-1) return;

            final Realm realm = Realm.getDefaultInstance();
            mAPI.getPoem(id).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(jsonPoem -> {
                realm.beginTransaction();
                realm.createOrUpdateObjectFromJson(Poem.class, jsonPoem);
            }, err -> {
                Log.e(TAG, "error", err);
                if(realm.isInTransaction()) realm.cancelTransaction();
            }, () -> {
                if(realm.isInTransaction()) realm.commitTransaction();
            });
        }
    }

    @Override
    public void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(REQUEST_POEMS);
        filter.addAction(REQUEST_POEM);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(this, filter);
    }

    @Override
    public void unregister() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
    }
}

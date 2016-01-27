package jp.co.crowdworks.android.nasulog.service.observer;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;
import jp.co.crowdworks.android.nasulog.service.NasulogAPI;

abstract class AbstractRealmObserver<T extends RealmObject> implements Registerable, RealmChangeListener {
    protected final Context mContext;
    protected final NasulogAPI mAPI;
    private RealmResults<T> mResults;

    public AbstractRealmObserver(Context context, NasulogAPI api) {
        mContext = context;
        mAPI = api;
        mResults = query(Realm.getDefaultInstance());
    }

    protected abstract RealmResults<T> query(Realm realm);

    @Override
    public void register() {
        mResults.addChangeListener(this);
    }

    @Override
    public void unregister() {
        mResults.removeChangeListener(this);
    }

    protected RealmResults<T> getResult() {
        return mResults;
    }

    @Override
    public abstract void onChange();
}

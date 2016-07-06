package jp.co.crowdworks.android.nasulog.service.observer;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import jp.co.crowdworks.android.nasulog.model.Poem;
import jp.co.crowdworks.android.nasulog.service.NasulogAPI;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CreatePoemObserver extends AbstractRealmObserver<Poem> {
    private static final String TAG = CreatePoemObserver.class.getName();

    public CreatePoemObserver(Context context, NasulogAPI api) {
        super(context, api);
    }

    @Override
    protected RealmResults<Poem> query(Realm realm) {
        return realm.where(Poem.class).equalTo("syncstate", Poem.SYNCSTATE_NOT_SYNCED).isNotNull("created_at").findAllSorted("created_at", Sort.DESCENDING);
    }

    @Override
    public void onChange() {
        ArrayList<Poem> poems = new ArrayList<>();
        for(Poem poem: getResult()) poems.add(poem); //REMARK: いったんResultsのループを終わらせないと、「ループ中にレコード更新するな！」てきなエラーが出る
        for(Poem poem: poems) handleNewPoem(poem);
    }

    @DebugLog
    private void handleNewPoem(Poem poem) {
        Realm.getDefaultInstance().executeTransaction(realm -> {
            poem.setSyncstate(Poem.SYNCSTATE_SYNCING);
        });

        mAPI.submitPoem(poem.getTitle(), poem.getDescription())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(jsonPoem -> {
                    Poem.setSyncstateJson(jsonPoem, Poem.SYNCSTATE_SYNCED);
                    Poem.setOriginalPoemIdJson(jsonPoem, poem.getOriginal_poem_id());
                    Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Poem newPoem = realm.createOrUpdateObjectFromJson(Poem.class, jsonPoem);
                            if (newPoem.getId() != poem.getId()) poem.deleteFromRealm();
                        }
                    });
                }, err -> {
                    Log.e(TAG, "error", err);
                    Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            if (poem.isValid()) poem.setSyncstate(Poem.SYNCSTATE_SYNC_FAILED);
                        }
                    });
                });
    }
}

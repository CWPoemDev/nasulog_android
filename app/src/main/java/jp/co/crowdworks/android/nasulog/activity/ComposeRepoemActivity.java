package jp.co.crowdworks.android.nasulog.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import java.util.ArrayList;

import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import jp.co.crowdworks.android.nasulog.R;
import jp.co.crowdworks.android.nasulog.model.Poem;

public class ComposeRepoemActivity extends ComposePoemActivity {
    public static final String KEY_ORIGINAL_POEM_ID = "original_poem_id";
    private long mTempPoemId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (!intent.hasExtra(KEY_ORIGINAL_POEM_ID)) {
            finish();
            return;
        }

        mTempPoemId = System.currentTimeMillis();

        if (!handleOriginalPoem(intent.getLongExtra(KEY_ORIGINAL_POEM_ID, -1))) {
            finish();
            return;
        }
    }

    private boolean handleOriginalPoem(long poemId) {
        if (poemId < 0)  return false;

        Realm.getDefaultInstance().executeTransaction(realm -> {
            long nextId = mTempPoemId;
            Poem poem = realm.createOrUpdateObjectFromJson(Poem.class, "{ id:"+nextId+"}");
            poem.setId(nextId);
            poem.setSyncstate(Poem.SYNCSTATE_NOT_SYNCED);
            poem.setOriginal_poem_id(poemId);
        });

        return true;
    }

    private RealmResults<Poem> mResults;
    private RealmResults<Poem> query(){
        if (mResults == null) mResults = Realm.getDefaultInstance().where(Poem.class).equalTo("id", mTempPoemId).findAll();
        return mResults;
    }

    @Override
    protected void onResume() {
        super.onResume();

        query().addChangeListener(new RealmChangeListener<RealmResults<Poem>>() {
            @Override
            public void onChange(RealmResults<Poem> results) {
                ArrayList<Poem> poems = new ArrayList<>();
                for(Poem poem: query()) poems.add(poem); //REMARK: いったんResultsのループを終わらせないと、「ループ中にレコード更新するな！」てきなエラーが出る
                for(Poem poem: poems) {
                    setupContent(poem);

                    if (poem.getSyncstate() == Poem.SYNCSTATE_SYNCED || poem.getSyncstate() == Poem.SYNCSTATE_SYNC_FAILED) {
                        Realm.getDefaultInstance().executeTransaction(realm -> {
                            poem.deleteFromRealm();
                        });
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        query().removeChangeListeners();
        super.onPause();
    }

    @DebugLog
    private void setupContent(Poem poem) {
        if (poem!=null) {
            ((TextView)findViewById(R.id.editor_title)).setText(poem.getTitle());
            ((TextView)findViewById(R.id.editor_description)).setText(poem.getDescription());
        }

    }
}

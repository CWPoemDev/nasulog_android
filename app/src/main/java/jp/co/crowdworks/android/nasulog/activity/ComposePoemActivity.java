package jp.co.crowdworks.android.nasulog.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Date;

import io.realm.Realm;
import io.realm.Sort;
import jp.co.crowdworks.android.nasulog.R;
import jp.co.crowdworks.android.nasulog.model.Poem;
import jp.co.crowdworks.android.nasulog.model.User;

public class ComposePoemActivity extends AbstractActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.compose_poem_screen);
        setupToolbar();
    }


    private void setupToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Compose poem");
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> {
            ComposePoemActivity.this.onBackPressed();
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);// [←] アイコンを有効にする
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.composer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_submit:{
                submit();
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void submit(){
        String title = ((TextView)findViewById(R.id.editor_title)).getText().toString();
        String description = ((TextView)findViewById(R.id.editor_description)).getText().toString();

        Realm realm = Realm.getDefaultInstance();
        User me = realm.where(User.class).isNotNull("email").findFirst();
        if (me==null) return;

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // autoincrement increatement

                long nextId = realm.where(Poem.class).findAllSorted("id", Sort.DESCENDING).first().getId() + 1;
                Poem poem = realm.createOrUpdateObjectFromJson(Poem.class, "{ id:"+nextId+"}");
                poem.setId(nextId);
                poem.setTitle(title);
                poem.setDescription(description);
                poem.setAuthor(me);
                poem.setCreated_at(new Date());
                poem.setSyncstate(Poem.SYNCSTATE_NOT_SYNCED);
            }
        });

        finish();
    }
}

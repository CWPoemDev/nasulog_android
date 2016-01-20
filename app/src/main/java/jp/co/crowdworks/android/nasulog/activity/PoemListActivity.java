package jp.co.crowdworks.android.nasulog.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;
import io.realm.Sort;
import jp.co.crowdworks.android.nasulog.R;
import jp.co.crowdworks.android.nasulog.model.Poem;


public class PoemListActivity extends AbstractPoemActivity {


    @Override
    protected int getLayout() {
        return R.layout.poem_list_screen;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupToolbar();
        setupListView();
        setupActionButton();
        requestUser();
        requestPoemList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showActionButtonIfHidden();
    }

    private void setupToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupListView() {
        RecyclerView poemListview = (RecyclerView) findViewById(R.id.poem_listview);

        RealmResults<Poem> resultPoems = Realm.getDefaultInstance().where(Poem.class).findAllSorted("created_at", Sort.DESCENDING);
        RecyclerView.Adapter adapter = new PoemListAdapter(this, resultPoems).setOnItemClickListener(poem->{
            showPoemDetailActivity(poem);
        });
        poemListview.setAdapter(adapter);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        poemListview.setLayoutManager(layoutManager);
    }

    private void showPoemDetailActivity(Poem poem) {
        Intent intent = new Intent(this, PoemDetailActivity.class);
        intent.putExtra(PoemDetailActivity.KEY_POEM_ID, poem.getId());
        startActivity(intent);
    }

    private static class PoemListViewHolder extends RealmViewHolder {
        private PoemListAdapter.OnPoemItemClickListener mListener;
        public PoemListViewHolder setOnItemClickListener(PoemListAdapter.OnPoemItemClickListener l) {
            mListener = l;
            return this;
        }

        private final TextView mTitle;
        private final ImageView mIcon;
        private final TextView mAuthorName;
        private final TextView mCreatedAt;
        public PoemListViewHolder(View itemView) {
            super(itemView);

            mTitle = (TextView) itemView.findViewById(R.id.txt_poem_title);
            mIcon = (ImageView) itemView.findViewById(R.id.img_icon);
            mAuthorName = (TextView) itemView.findViewById(R.id.txt_poem_author);
            mCreatedAt = (TextView) itemView.findViewById(R.id.txt_poem_created_at);
        }

        public void bind(Poem poem) {
            mTitle.setText(poem.getTitle());
            if (poem.getAuthor() != null) {
                Picasso.with(itemView.getContext()).load(poem.getAuthor().getImage()).into(mIcon);
                mAuthorName.setText(poem.getAuthor().getName());
            }
            if (poem.getCreated_at()!=null) mCreatedAt.setText(poem.getCreated_at().toString());

            itemView.setOnClickListener(v -> {
                if (mListener!=null) mListener.onPoemItemClick(poem);
            });
        }
    }

    private static class PoemListAdapter extends RealmBasedRecyclerViewAdapter<Poem, PoemListViewHolder> {
        public interface OnPoemItemClickListener {
            void onPoemItemClick(Poem poem);
        }

        private OnPoemItemClickListener mListener;
        public PoemListAdapter setOnItemClickListener(OnPoemItemClickListener l) {
            mListener = l;
            return this;
        }

        public PoemListAdapter(Context context, RealmResults<Poem> realmResults) {
            super(context, realmResults, true, false);
        }

        @Override
        public PoemListViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int viewType) {
            View root = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_poem, viewGroup, false);
            return new PoemListViewHolder(root).setOnItemClickListener(mListener);
        }

        @Override
        public void onBindRealmViewHolder(PoemListViewHolder poemListViewHolder, int position) {
            final Poem poem = realmResults.get(position);
            poemListViewHolder.bind(poem);
        }
    }


    private void setupActionButton(){
        findViewById(R.id.btn_compose).setOnClickListener(v -> {
            FloatingActionButton btn = (FloatingActionButton)v;
            btn.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                @Override
                public void onHidden(FloatingActionButton fab) {
                    showComposeActivity();
                }
            });
        });
    }

    private void showComposeActivity() {
        Intent intent = new Intent(this, ComposePoemActivity.class);
        startActivity(intent);
    }

    private void showActionButtonIfHidden() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.btn_compose);
        if (!fab.isShown()) fab.show();
    }
}

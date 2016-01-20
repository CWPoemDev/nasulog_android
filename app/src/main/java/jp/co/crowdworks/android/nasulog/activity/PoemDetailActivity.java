package jp.co.crowdworks.android.nasulog.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import io.realm.Realm;
import io.realm.RealmList;
import jp.co.crowdworks.android.nasulog.Prefs;
import jp.co.crowdworks.android.nasulog.R;
import jp.co.crowdworks.android.nasulog.model.Poem;
import jp.co.crowdworks.android.nasulog.model.User;
import jp.co.crowdworks.android.nasulog.service.NasulogAPI;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PoemDetailActivity extends AbstractPoemActivity {
    private static final String TAG = PoemDetailActivity.class.getName();

    public static String KEY_POEM_ID = "porm_id";

    //TODO ほんとうはActivityで直接APIは叩かず、Service側に要求をだすようにしたい
    private NasulogAPI mAPI;

    @Override
    protected int getLayout() {
        return R.layout.poem_detail_screen;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra(KEY_POEM_ID)) {
            finish();
            return;
        }
        final long poemId = intent.getLongExtra(KEY_POEM_ID, -1);
        if (poemId == -1) {
            finish();
            return;
        }

        mAPI = new NasulogAPI(getServer(), Prefs.get(this).getString(Prefs.KEY_TOKEN, null));

        setupToolbar();
        requestUser();
        requestPoem(poemId);

        Observable<Poem> poemObservable = Realm.getDefaultInstance().where(Poem.class).equalTo("id", poemId).findFirst().asObservable();
        poemObservable.subscribe(poem -> {
            setupContent(poem);
        });
    }

    private void setupToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerToggle.setDrawerIndicatorEnabled(false);//ハンバーガーアイコンの無効化
        toolbar.setNavigationOnClickListener(v -> {
            PoemDetailActivity.this.onBackPressed();
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);// [←] アイコンを有効にする
    }

    private void setText(@IdRes int res, CharSequence text) {
        ((TextView) findViewById(res)).setText(text);
    }

    private void setupContent(Poem poem) {
        ((CollapsingToolbarLayout) findViewById(R.id.collapsingToolbarLayout)).setTitle(poem.getTitle());
        if (poem.getAuthor()!=null) {
            setText(R.id.txt_poem_author, poem.getAuthor().getName());
            Picasso.with(this).load(poem.getAuthor().getImage()).into((ImageView) findViewById(R.id.img_icon));
        }

        setText(R.id.txt_poem_created_at, poem.getCreated_at().toString());

        if (!TextUtils.isEmpty(poem.getDescription())) {
            // 画像が表示できないけどWebViewは不具合多すぎなのでやめる

//            final WebView descriptionWebView = (WebView) findViewById(R.id.webview_poem_description);
//            setupWebView(descriptionWebView);
//            mAPI.getMarkedDown(poem.getDescription()).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(html -> {
//                Log.d("hoge", "html="+html);
//                descriptionWebView.loadDataWithBaseURL(getBaseURL(), html, "text/html", "UTF-8", null);
//            });

            final TextView txt = (TextView) findViewById(R.id.txt_poem_description);
            mAPI.getMarkedDown(poem.getDescription().trim()).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(html -> {
                txt.setText(Html.fromHtml(html));
            }, err -> {
                Log.w(TAG, "failed to markdown", err);
                txt.setText(poem.getDescription().trim());
            });
        }

        LinearLayout readPoemContainer = (LinearLayout) findViewById(R.id.read_poem_container);
        RealmList<User> readUsers = poem.getRead_users();
        readPoemContainer.removeAllViews();
        readPoemContainer.addView(createAddReadUserView(readPoemContainer, poem.getId()));
        if (readUsers!=null && !readUsers.isEmpty()) {
            for (User u : readUsers) {
                readPoemContainer.addView(createReadUserView(readPoemContainer, u), 1);
            }
        }
    }

    private View createReadUserView(ViewGroup parent, User u) {
        View poemRead = getLayoutInflater().inflate(R.layout.item_poem_read, parent, false);
        Picasso.with(this).load(u.getImage()).into((ImageView) poemRead.findViewById(R.id.img_icon));
        ((TextView) poemRead.findViewById(R.id.txt_poem_read_user)).setText(u.getName());
        return poemRead;
    }

    private View createAddReadUserView(ViewGroup parent, long poemId) {
        View poemRead = getLayoutInflater().inflate(R.layout.item_poem_read, parent, false);
        ((ImageView) poemRead.findViewById(R.id.img_icon)).setImageResource(R.drawable.ic_add_read);
        ((TextView) poemRead.findViewById(R.id.txt_poem_read_user)).setText("add...");
        poemRead.setOnClickListener(v -> {
            addUserRead(v, poemId);
        });
        return poemRead;
    }

    private void addUserRead(View btn, long poemId) {
        btn.setEnabled(false);
        mAPI.setPoemRead(poemId).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe( ret -> {
                    requestPoem(poemId);
                    btn.setEnabled(true);
                }, err -> {
                    Log.e(TAG, "error", err);
                    btn.setEnabled(true);
                });
    }

//    private void setupWebView(WebView webview) {
//        boolean isChromiumBasedWebView = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT);
//
//        WebSettings settings = webview.getSettings();
//        if (settings != null) {
//            settings.setJavaScriptEnabled(true);
//            settings.setLoadWithOverviewMode(true);
//            settings.setUseWideViewPort(true);
//            settings.setLayoutAlgorithm(isChromiumBasedWebView? WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING : WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
//        }
//        webview.setHorizontalScrollBarEnabled(false);
//
//        if (isChromiumBasedWebView) WebView.setWebContentsDebuggingEnabled(true);
//    }

    protected String getServer(){
        return Prefs.get(this).getString(Prefs.KEY_SERVER,null);
    }

    protected String getBaseURL(){
        return "http://"+getServer();
    }

}

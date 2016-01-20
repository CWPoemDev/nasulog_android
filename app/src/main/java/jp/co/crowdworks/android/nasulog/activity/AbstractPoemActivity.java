package jp.co.crowdworks.android.nasulog.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import jp.co.crowdworks.android.nasulog.Prefs;
import jp.co.crowdworks.android.nasulog.R;
import jp.co.crowdworks.android.nasulog.model.Poem;
import jp.co.crowdworks.android.nasulog.model.User;
import jp.co.crowdworks.android.nasulog.service.NasulogAPIService;
import jp.co.crowdworks.android.nasulog.service.observer.LoadPoemReceiver;
import jp.co.crowdworks.android.nasulog.service.observer.LoadUserReceiver;
import rx.Observable;

abstract class AbstractPoemActivity extends AbstractActivity {
    protected ActionBarDrawerToggle mDrawerToggle;

    protected abstract @LayoutRes int getLayout();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayout());
        setupDrawerLayout();
        setupSideMenu();
    }

    private void setupDrawerLayout(){
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name);
        drawerLayout.setDrawerListener(mDrawerToggle);
    }

    private class SideMenuItem {
        @DrawableRes int drawableResId;
        String text;
        View.OnClickListener listener;

        public SideMenuItem(int resId, String text) {
            this.drawableResId = resId;
            this.text = text;
        }

        public SideMenuItem setOnClickListener(View.OnClickListener listener) {
            this.listener = listener;
            return this;
        }
    }
    private void setupSideMenu(){

        Observable<RealmResults<User>> userlistObservable = Realm.getDefaultInstance().where(User.class).isNotNull("email").findAllSorted("id", Sort.DESCENDING).asObservable();
        userlistObservable
                .filter(users -> users.size()>0)
                .map(users -> users.first())
                .subscribe(user -> {
                    Picasso.with(AbstractPoemActivity.this).load(user.getImage()).into((ImageView) findViewById(R.id.img_avatar));
                    ((TextView) findViewById(R.id.txt_username)).setText(user.getName());
                    ((TextView) findViewById(R.id.txt_email)).setText(user.getEmail());
                });

        ListView listView = (ListView) findViewById(R.id.drawer_menu_listview);

        SideMenuItem[] items = new SideMenuItem[]{
                new SideMenuItem(R.drawable.ic_all_poems, "All poems").setOnClickListener(v -> {
                    Toast.makeText(v.getContext(), "[TODO] みんなのポエム",Toast.LENGTH_SHORT).show();
                }),
                new SideMenuItem(R.drawable.ic_my_poems, "My poems").setOnClickListener(v -> {
                    Toast.makeText(v.getContext(), "[TODO] My poems",Toast.LENGTH_SHORT).show();
                }),
                new SideMenuItem(R.drawable.ic_logout, "ログアウト").setOnClickListener(v -> {
                    logout();
                })

        };

        listView.setAdapter(new ArrayAdapter<SideMenuItem>(this, R.layout.listitem_drawer_menu, items){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) convertView = getLayoutInflater().inflate(R.layout.listitem_drawer_menu, null, false);

                ImageView icon = (ImageView) convertView.findViewById(R.id.item_icon);
                TextView text = (TextView) convertView.findViewById(R.id.item_text);

                SideMenuItem item = items[position];
                icon.setImageResource(item.drawableResId);
                text.setText(item.text);
                return convertView;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (items[position].listener != null) {
                    items[position].listener.onClick(view);

                    onBackPressed();//ドロワーを閉じる
                }
            }
        });
    }

    // 以下は、DrawerToggleを使う時のおまじない -->
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    // <-- DrawerToggleを使う時のおまじない

    @Override
    public void onBackPressed(){
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        LinearLayout sidemenu = (LinearLayout) findViewById(R.id.drawer_menu);
        if(drawerLayout.isDrawerOpen(sidemenu)){
            drawerLayout.closeDrawer(sidemenu);
        }
        else {
            super.onBackPressed();
        }
    }

    protected void requestUser() {
        Intent intent = new Intent(LoadUserReceiver.REQUEST_USER);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @DebugLog
    protected void requestPoemList(){
        Intent intent = new Intent(LoadPoemReceiver.REQUEST_POEMS);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @DebugLog
    protected void requestPoem(long id){
        Intent intent = new Intent(LoadPoemReceiver.REQUEST_POEM);
        intent.putExtra(LoadPoemReceiver.KEY_POEM_ID, id);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    private void logout(){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.clear(Poem.class);
        realm.clear(User.class);
        realm.commitTransaction();

        CookieManager.getInstance().removeAllCookie();
        Prefs.get(this).edit()
                .remove(Prefs.KEY_SERVER) //TODO:サーバーまで消しちゃう？
                .remove(Prefs.KEY_TOKEN)
                .remove(Prefs.KEY_TOKEN_VERIFIED)
                .commit();

        NasulogAPIService.kill(this);

        // TODO: サーバーセッションのおそうじ

        showEntryActivity();
    }

    private void showEntryActivity(){
        Intent intent = new Intent(this, EntryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}

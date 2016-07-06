package jp.co.crowdworks.android.nasulog.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import hugo.weaving.DebugLog;
import io.realm.Realm;
import jp.co.crowdworks.android.nasulog.Prefs;
import jp.co.crowdworks.android.nasulog.R;
import jp.co.crowdworks.android.nasulog.fragment.LoginFragment;
import jp.co.crowdworks.android.nasulog.fragment.ServerConfigFragment;
import jp.co.crowdworks.android.nasulog.fragment.ServerConnectingFragment;
import jp.co.crowdworks.android.nasulog.helper.ConstrainedActionManager;
import jp.co.crowdworks.android.nasulog.helper.LaunchUtil;
import jp.co.crowdworks.android.nasulog.model.Poem;
import jp.co.crowdworks.android.nasulog.model.User;

public class EntryActivity extends AbstractFrameLayoutActivity {

    SharedPreferences.OnSharedPreferenceChangeListener mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (Prefs.KEY_SERVER.equals(key)) {
                showFragment();
            }
            if (Prefs.KEY_TOKEN.equals(key) || Prefs.KEY_TOKEN_VERIFIED.equals(key)) {
                boolean ok = !TextUtils.isEmpty(sharedPreferences.getString(Prefs.KEY_TOKEN,null)) && sharedPreferences.getBoolean(Prefs.KEY_TOKEN_VERIFIED,false);

                mManager.setShouldAction(ok);
                if(!ok) {
                    showFragment();
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // token認証するため、Verifiedを削除
        Prefs.get(this).edit()
                .remove(Prefs.KEY_TOKEN_VERIFIED)
                .commit();

        setContentView(R.layout.simple_framelayout);
        showFragment();
        Prefs.get(this).registerOnSharedPreferenceChangeListener(mListener);
    }

    private void showFragment(){
        String serverconfig = Prefs.get(this).getString(Prefs.KEY_SERVER, null);
        String token = Prefs.get(this).getString(Prefs.KEY_TOKEN, null);
        if (TextUtils.isEmpty(serverconfig)) showServerConfigFragment();
        else if(TextUtils.isEmpty(token)) showLoginFragment();
        else showConnectingFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mManager.setConstrainedMet(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mManager.setConstrainedMet(false);
    }

    @Override
    protected void onDestroy() {
        Prefs.get(this).unregisterOnSharedPreferenceChangeListener(mListener);
        super.onDestroy();
    }

    @DebugLog
    private void showServerConfigFragment(){
        showFragment(new ServerConfigFragment());
    }

    private void showLoginFragment() {
        showFragment(new LoginFragment());
    }

    @DebugLog
    private void showConnectingFragment(){
        showFragmentWithBackstack(new ServerConnectingFragment());
    }

    ConstrainedActionManager mManager = new ConstrainedActionManager() {
        @Override
        protected void action() {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.delete(Poem.class);
            realm.delete(User.class);
            realm.commitTransaction();

            LaunchUtil.showPoemListActivity(EntryActivity.this);
        }
    };
}

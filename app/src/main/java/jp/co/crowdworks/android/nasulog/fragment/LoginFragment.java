package jp.co.crowdworks.android.nasulog.fragment;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.WebView;

import hugo.weaving.DebugLog;
import jp.co.crowdworks.android.nasulog.Prefs;
import jp.co.crowdworks.android.nasulog.helper.CookieHelper;

public class LoginFragment extends AbstractWebViewFragment {
    private boolean mCallebacked;
    private boolean mCookieAcquired;

    public LoginFragment(){}

    private void restart() {
        String server = getServer();
        Context context = getContext();
        if (!TextUtils.isEmpty(server) && context!=null) {
            Prefs.get(context).edit()
                    .remove(Prefs.KEY_SERVER)
                    .commit();
            Prefs.get(context).edit()
                    .putString(Prefs.KEY_SERVER, server)
                    .apply();
        }
    }

    @Override
    protected void navigateToInitialPage(WebView webview) {
        mCallebacked = false;
        mCookieAcquired = false;
        CookieHelper.setupCookieSyncManager(getContext());
        String url = getBaseURL()+"/auth/google_oauth2";
        webview.loadUrl(url);
    }

    private boolean isOAuthFailureURL(String url) {
        return url.startsWith(getBaseURL()+"/auth/failure?");
    }

    @DebugLog
    @Override
    protected boolean shouldOverride(WebView webview, String url) {
        // 失敗は即ハンドリングする
        if (isOAuthFailureURL(url)) return true;

        // OAuth成功は、コールバックでクッキーが発行されるので、そのレスポンスをみるハンドリングする
        if(!mCallebacked) mCallebacked = url.startsWith(getBaseURL()+"/auth/google_oauth2/callback?");
        return mCookieAcquired;
    }

    @Override
    protected boolean onHandleCallback(WebView webview, String url) {
        if (!mCookieAcquired) {
            // OAuthで拒否られた時には、再度LoginFragmentを立ち上げる必要がある。
            // OnSharedPrefListenerを発動させるために、SharedPrefにserver値を書き直す
            restart();
        }
        return true;
    }

    @Override
    protected void onPageLoaded(WebView webview, String url) {
        // overrideLoading してもonPageLoadedは呼ばれてしまうらしいので・・・
        if (isOAuthFailureURL(url)) return;

        if (mCallebacked) {
            CookieHelper.parse(CookieManager.getInstance().getCookie(url), (key,value) -> {
                if ("_nasulog_session".equals(key)) {
                    if (!TextUtils.isEmpty(value)) {
                        saveCookie(value);
                    }
                }
            });

            if (!mCookieAcquired) {
                // crowdworks.co.jpじゃないドメインなので、再度LoginFragmentを立ち上げる必要がある。
                // OnSharedPrefListenerを発動させるために、SharedPrefにserver値を書き直す
                restart();
            }
        }
    }

    @DebugLog
    @Override
    protected void onPageError(WebView webview, String url, int errorCode, String description) {
        Context context = getContext();
        if (context!=null) {
            Prefs.get(context).edit()
                    .remove(Prefs.KEY_SERVER)
                    .commit();
        }
    }

    private void saveCookie(String sessionID) {
        Context context = getContext();
        if (context!=null) {
            Prefs.get(context).edit()
                    .putString(Prefs.KEY_TOKEN, sessionID)
                    .putBoolean(Prefs.KEY_TOKEN_VERIFIED, true)
                    .apply();

        }
        mCookieAcquired = true;
    }

    @Override
    public boolean onBackPressed() {
        if(super.onBackPressed()) return true;

        Prefs.get(getContext()).edit()
                .remove(Prefs.KEY_SERVER)
                .commit();
        return true;
    }
}

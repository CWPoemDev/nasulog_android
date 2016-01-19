package jp.co.crowdworks.android.nasulog.fragment;

import android.content.Context;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import jp.co.crowdworks.android.nasulog.Prefs;
import jp.co.crowdworks.android.nasulog.R;
import jp.co.crowdworks.android.nasulog.helper.HttpError;
import jp.co.crowdworks.android.nasulog.helper.RedirectNotAllowdError;
import jp.co.crowdworks.android.nasulog.service.NasulogAPI;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.internal.util.SubscriptionList;
import rx.schedulers.Schedulers;

public class ServerConnectingFragment extends AbstractFragment {
    private static final String TAG = ServerConnectingFragment.class.getName();
    public ServerConnectingFragment(){}

    private SubscriptionList mSub;

    @Override
    protected int getLayout() {
        return R.layout.serverconnecting_screen;
    }

    @Override
    protected void onSetupRootView() {
        setupLoadingText();
        login();
    }

    private void login(){
        final NasulogAPI api = new NasulogAPI(getServer(), Prefs.get(getContext()).getString(Prefs.KEY_TOKEN,""));

        api.checkConnection().subscribeOn(Schedulers.newThread()).subscribe(response -> {
            Context context = getContext();
            if(context!=null) {
                Prefs.get(context).edit()
                        .putBoolean(Prefs.KEY_TOKEN_VERIFIED, true)
                        .commit();
            }
            response.body().close();
        }, err -> {
            Log.e(TAG, "error", err);
            boolean removeToken = false;
            if (err instanceof RedirectNotAllowdError) removeToken = true;
            else if (err instanceof HttpError) {
                int code = ((HttpError) err).code();
                if (code >= 400 && code <= 500) { //いまのところ、トークンが無いと500になるっぽいので、サーバエラー（500番台）の500のみは含む
                    removeToken = true;
                }
            }

            if (removeToken) {
                Context context = getContext();
                if(context!=null) {
                    CookieManager.getInstance().removeAllCookie();
                    Prefs.get(context).edit()
                            .remove(Prefs.KEY_TOKEN)
                            .remove(Prefs.KEY_TOKEN_VERIFIED)
                            .commit();
                }
            }
        });
    }

    private static String[] loadingMiscStrs = new String[]{
            ".",
            "..",
            "...",
            "....",
            "....."
    };
    private void setupLoadingText(){
        TextView t = (TextView) mRootView.findViewById(R.id.txt_connecting);
        t.setText("Connecting "+ getBaseURL());
    }

    private void sub(){
        unsub();
        TextView misc = (TextView) mRootView.findViewById(R.id.txt_connecting_misc);

        mSub = new SubscriptionList();
        mSub.add(Observable.interval(320, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(i -> {
                    misc.setText(loadingMiscStrs[(int)(i%loadingMiscStrs.length)]);
                }));
    }

    @Override
    public void onResume() {
        super.onResume();
        sub();
    }

    @Override
    public void onPause() {
        unsub();
        super.onPause();
    }

    private void unsub(){
        if(mSub!=null && mSub.hasSubscriptions() && !mSub.isUnsubscribed()) {
            mSub.unsubscribe();
            mSub = null;
        }
    }
}

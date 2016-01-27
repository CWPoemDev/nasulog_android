package jp.co.crowdworks.android.nasulog.fragment;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Message;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import hugo.weaving.DebugLog;
import jp.co.crowdworks.android.nasulog.R;
import jp.co.crowdworks.android.nasulog.helper.OnBackPressListener;

abstract class AbstractWebViewFragment extends AbstractFragment implements OnBackPressListener {
    private WebView mWebView;

    @Override
    protected int getLayout() {
        return R.layout.simple_webview;
    }

    @Override
    protected void onSetupRootView() {
        mWebView = (WebView) mRootView.findViewById(R.id.simple_webview);
        setupWebView();
        navigateToInitialPage(mWebView);
    }

    private void setupWebView() {
        WebSettings settings = mWebView.getSettings();
        if (settings != null) {
            settings.setJavaScriptEnabled(true);
        }
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebViewClient(mWebViewClient);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        private boolean mError;
        @Override
        public void onPageStarted(WebView webview, String url, Bitmap favicon){
            mError = false;
        }

        @Override
        public void onPageFinished(WebView webview, String url){
            if(!mError) onPageLoaded(webview, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            mError = true;
            onPageError(view, failingUrl, errorCode, description);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webview, String url){
            return (shouldOverride(webview,url) && onHandleCallback(webview, url)) || super.shouldOverrideUrlLoading(webview, url);
        }

        @DebugLog
        @Override
        public void onFormResubmission (WebView view, Message dontResend, Message resend){
            //resend POST request without confirmation.
            resend.sendToTarget();
        }
    };

    @Override
    public boolean onBackPressed()
    {
        if(mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        else return false;
    }

    protected abstract void navigateToInitialPage(WebView webview);
    protected void onPageLoaded(WebView webview, String url){}
    protected void onPageError(WebView webview, String url, int errorCode, String description){}

    protected boolean shouldOverride(WebView webview, String url){
        return false;
    }
    protected boolean onHandleCallback(WebView webview, String url){
        return false;
    };
}
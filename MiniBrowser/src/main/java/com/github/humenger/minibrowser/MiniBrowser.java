package com.github.humenger.minibrowser;/*
created by humenger on 2022/10/8
*/

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.RequiresApi;


import java.util.Timer;
import java.util.TimerTask;

public class MiniBrowser extends WebView {
    private WebView webView;
    private Context content;
    OnPageFinishListener onPageFinishListener;
    OnOpenFileChooserListener onOpenFileChooserListener;
    private ValueCallback<Uri> mValueCallbackUri;
    private ValueCallback<Uri[]> mValueCallbackUris;


    public MiniBrowser(Context context) {
        super(context);
        init(context);
    }

    public MiniBrowser(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MiniBrowser(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    private void init(Context context){
        this.content =context;
        this.webView=this;
        initWebSettings();

    }
    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled", "ObsoleteSdkInt"})
    private void initWebSettings() {
        WebView mWebView = this;
        mWebView.clearFocus();
        WebSettings mWebSettings = mWebView.getSettings();
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setDefaultTextEncodingName("utf-8");
        mWebSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebSettings.setPluginState(WebSettings.PluginState.ON);
        mWebSettings.setDisplayZoomControls(false);
        mWebSettings.setUseWideViewPort(true);//Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Mobile Safari/537.36
        mWebSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Mobile Safari/537.36");
        mWebSettings.setAllowFileAccess(true);
        mWebSettings.setAllowContentAccess(true);
//        mWebSettings.setBlockNetworkImage(true); // ??????????????????
        mWebSettings.setSupportZoom(true);
        mWebSettings.setAllowContentAccess(true);
        mWebSettings.setLoadWithOverviewMode(true);
        mWebSettings.setBuiltInZoomControls(true);// ??????????????????
        mWebSettings.setUseWideViewPort(true);// ?????????????????????
        mWebSettings.setLoadWithOverviewMode(true);// setUseWideViewPort????????????webview????????????????????????setLoadWithOverviewMode???????????????webview???????????????????????????
        mWebSettings.setSavePassword(true);
        mWebSettings.setSaveFormData(true);// ??????????????????
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setTextZoom(100);
        mWebSettings.setDomStorageEnabled(true);
        //????????????????????????????????????????????????????????? ref:https://blog.csdn.net/xiaoerye/article/details/78498251
//        mWebSettings.setSupportMultipleWindows(true);// ??????//?????????????????????????????????????????????MD?????????????????????????????????
        //????????????????????????  ??????1
//        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mWebSettings.setMediaPlaybackRequiresUserGesture(true);
        }
        if (Build.VERSION.SDK_INT >= 16) {
            mWebSettings.setAllowFileAccessFromFileURLs(true);
            mWebSettings.setAllowUniversalAccessFromFileURLs(true);
        }
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        mWebSettings.setLoadsImagesAutomatically(true);
        mWebSettings.setAppCacheEnabled(true);
        mWebSettings.setAppCachePath(content.getCacheDir().getAbsolutePath());
        mWebSettings.setDatabaseEnabled(true);
        mWebSettings.setGeolocationDatabasePath(content.getDir("database", 0).getPath());
        mWebSettings.setGeolocationEnabled(true);
        CookieManager instance = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT < 21) {
            CookieSyncManager.createInstance(content.getApplicationContext());
        }
        instance.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= 21) {
            instance.setAcceptThirdPartyCookies(mWebView, true);
        }
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());
        enabledCookie(webView);//??????cookie
    }

    public void setOnPageFinishListener(OnPageFinishListener onPageFinishListener) {
        this.onPageFinishListener = onPageFinishListener;
    }

    public void setOnOpenFileChooserListener(OnOpenFileChooserListener onOpenFileChooserListener) {
        this.onOpenFileChooserListener = onOpenFileChooserListener;
    }
    public void uploadChooseFile(Uri... uris){
        if(mValueCallbackUri!=null){
            mValueCallbackUri.onReceiveValue(uris[0]);
            mValueCallbackUri=null;
        }
        if(mValueCallbackUris!=null){
            mValueCallbackUris.onReceiveValue(uris);
            mValueCallbackUris=null;
        }
    }


    /*??????cookie*/
    private void enabledCookie(WebView web) {
        CookieManager instance = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT < 21) {
            CookieSyncManager.createInstance(content);
        }
        instance.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= 21) {
            instance.setAcceptThirdPartyCookies(web, true);
        }
    }
    class MyWebChromeClient extends WebChromeClient{

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if(newProgress==100){
                if(MiniBrowser.this.onPageFinishListener!=null){
                    MiniBrowser.this.onPageFinishListener.onFinish(view);
                }
            }
        }
        // For Android < 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {

            mValueCallbackUri = uploadMsg;
            if(onOpenFileChooserListener!=null)onOpenFileChooserListener.OnOpenFileChooser();
        }


        //For Android 3.0 - 4.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            mValueCallbackUri = uploadMsg;
            if(onOpenFileChooserListener!=null)onOpenFileChooserListener.OnOpenFileChooser();
        }


        // For Android > 4.1.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mValueCallbackUri = uploadMsg;
            if(onOpenFileChooserListener!=null)onOpenFileChooserListener.OnOpenFileChooser();
        }


        // For Android > 5.0??????????????????
        @Override
        public boolean onShowFileChooser(WebView webView,
                                         ValueCallback<Uri[]> uploadMsg,
                                         FileChooserParams fileChooserParams) {
            mValueCallbackUris = uploadMsg;
            if(onOpenFileChooserListener!=null)onOpenFileChooserListener.OnOpenFileChooser();
            return true;
        }

    }
    public class MyWebViewClient extends WebViewClient {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            //ref:https://blog.csdn.net/xiyangyang8110/article/details/126049595
            if(request!=null&&request.getUrl()!=null&&!TextUtils.isEmpty(request.getUrl().toString())) {
                String url = request.getUrl().toString();
                if (!TextUtils.isEmpty(url)&&(!url.toLowerCase().startsWith("http")||!checkDomain(getUrl(),url))) {
                    IntentUtil.openBrowser(getContext(),url);
                    return true;
                }
            }

            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //ref:https://blog.csdn.net/xiyangyang8110/article/details/126049595
            if (!TextUtils.isEmpty(url)&&(!url.toLowerCase().startsWith("http")||!checkDomain(getUrl(),url))) {
                IntentUtil.openBrowser(getContext(),url);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
        boolean checkDomain(String url1, String url2){
            try {
                if(TextUtils.isEmpty(url1)||TextUtils.isEmpty(url2))return false;
                String host1 = Uri.parse(url1).getHost();
                String host2 = Uri.parse(url2).getHost();
                String[] split1 = host1.split("\\.");
                String[] split2 = host2.split("\\.");
                if(split1.length>=2&&split2.length>=2){
                    return split1[split1.length-1].equals(split2[split2.length-1])&&split1[split1.length-2].equals(split2[split2.length-2]);
                }
            }catch (Throwable throwable){
                throwable.printStackTrace();
            }
            return false;
        }
        /*??????ssl????????????*/
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            return super.shouldInterceptRequest(view, url);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO: Implement this method
            super.onPageStarted(view, url, favicon);
            //startConut();//??????????????????
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        @Override
        public void onPageCommitVisible(WebView view, String url) {
            super.onPageCommitVisible(view, url);
            if(MiniBrowser.this.onPageFinishListener!=null){
                MiniBrowser.this.onPageFinishListener.onFinish(view);
            }
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            // ??????????????????
//            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    /*??????webview??????????????????*/
    private void startConut() {
        final Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                //onParseListener.onError("???????????????????????????????????????????????????????????????...");
                timer.cancel();
                timer.purge();
            }
        };
        int timeOut = 20 * 1000;
        timer.schedule(timerTask, timeOut, 1);
    }

    public interface OnParseWebUrlListener {
        void onFindUrl(String url);

        void onError(String errorMsg);
    }

}

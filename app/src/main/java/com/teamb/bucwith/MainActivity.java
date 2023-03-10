package com.teamb.bucwith;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import org.jetbrains.annotations.Nullable;

import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public WebView webView;
    public FrameLayout webViewLayout;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        webViewLayout = (FrameLayout) findViewById(R.id.webview_frame);
        webView = (WebView)findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setWebViewClient(new WebViewClientClass());
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setSupportMultipleWindows(true); // ?????? ??? ?????? ??? ????????? ??? ??????
        //webSettings.setLoadWithOverviewMode(true); // ????????? ???????????? ???????????????
        webSettings.setUseWideViewPort(true); // ???????????? ?????? width??? ??????
        webSettings.setSupportZoom(false); // ?????? ????????????
        webSettings.setBuiltInZoomControls(false); // ?????? ????????????
        //webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // ?????? ???????????? (?????? ?????? ??????)
        WebView.setWebContentsDebuggingEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webView.addJavascriptInterface(new WebAppInterface(), "NativeAndroid");
        webView.setWebChromeClient((WebChromeClient)(new MyWebChromeClient()));

        // ????????? ????????? url ??????
        webView.loadUrl("https://bucwiths.shop/");
        webView.setWebViewClient(new WebViewClient(){
            public void onPermissionRequest(PermissionRequest request) {
                if (request != null) {
                    request.grant(request.getResources());
                }

            }
        });
        webView.setDownloadListener(new DownloadListener(){
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                try {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                    contentDisposition = URLDecoder.decode(contentDisposition,"UTF-8"); //?????????
                    String FileName = contentDisposition.replace("attachment; filename=", ""); //attachment; filename*=UTF-8''?????? ????????????????????? ???????????? ?????????????????? ?????? attachment; filename*=UTF-8''??????

                    String cookie = CookieManager.getInstance().getCookie(url);
                    request.addRequestHeader("Cookie", cookie);

                    String fileName = FileName; //????????? ??????????????? ?????? ????????? ?????? ?????? ?????????
                    request.setMimeType(mimetype);
                    request.addRequestHeader("User-Agent", userAgent);
                    request.setDescription("Downloading File");
                    request.setAllowedOverMetered(true);
                    request.setAllowedOverRoaming(true);
                    request.setTitle(fileName);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        request.setRequiresCharging(false);
                    }

                    request.allowScanningByMediaScanner();
                    request.setAllowedOverMetered(true);
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                    dm.enqueue(request);
                    Toast.makeText(getApplicationContext(),"????????? ?????????????????????.", Toast.LENGTH_LONG).show();
                }
                catch (Exception e) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            Toast.makeText(getBaseContext(), "??????????????? ??????\n????????? ???????????????.", Toast.LENGTH_LONG).show();
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1004);
                        }
                        else {
                            Toast.makeText(getBaseContext(), "??????????????? ??????\n????????? ???????????????.", Toast.LENGTH_LONG).show();
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1004);
                        }
                    }
                }
            }
        });
    }
    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request){
           /*
           ???????????? ???????????? ????????? intent:// ??????????????????
           ?????? shouldOverrideUrlLoading ?????? ????????? ???????????? ??????
            */
            Log.d(TAG, request.getUrl().toString());

            if (Objects.equals(request.getUrl().getScheme(), "intent")) {
                try {
                    // Intent ??????
                    Intent intent = Intent.parseUri(request.getUrl().toString(), Intent.URI_INTENT_SCHEME);
                    Log.d("INTENT!!: {}", String.valueOf(intent));
                    // ?????? ????????? ?????? ????????? ??? ??????
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                        Log.d("ACTIVITY: {}",intent.getPackage());
                        return true;
                    }

                    // Fallback URL??? ????????? ?????? ????????? ??????
                    String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                    if (fallbackUrl != null) {
                        view.loadUrl(fallbackUrl);
                        Log.d("FALLBACK: {}",fallbackUrl);
                        return true;
                    }


                } catch (URISyntaxException e) {
                    Log.e(TAG, "Invalid intent request", e);

                }
            }

            // ????????? ????????? ?????? ??????

            return false;
        }

        // SSL ????????? ??????
        @SuppressLint("WebViewClientOnReceivedSslError")
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }
    }
    public class MyWebChromeClient extends WebChromeClient {
        public void onPermissionRequest(PermissionRequest request) {
            if (request != null) {
                request.grant(request.getResources());
            }

        }


        @SuppressLint({"SetJavaScriptEnabled", "WebViewApiAvailability"})
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            /*???????????? ??????????????? ?????? ????????? ????????? WebChromeClient?????? ????????? ????????? ????????? ???????????????
            onCreateWindow ????????? ???????????? ?????????
             */
            Log.i(TAG, "window.open ????????? ???????????????.");
            WebView childWebView = new WebView(view.getContext());
            WebSettings settings = childWebView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setJavaScriptCanOpenWindowsAutomatically(true);
            settings.setSupportMultipleWindows(true);

            childWebView.setLayoutParams(view.getLayoutParams());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                childWebView.setWebViewClient(view.getWebViewClient());
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                childWebView.setWebChromeClient(view.getWebChromeClient());
            }
            webViewLayout.addView(childWebView);

            childWebView.setWebViewClient(new WebViewClient() {
                @TargetApi(Build.VERSION_CODES.N)
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    String url = request.getUrl().toString();
                    view.loadUrl(url);
                    return true;
                }});

            WebView.WebViewTransport transport = (WebView.WebViewTransport)resultMsg.obj;
            transport.setWebView(childWebView);
            resultMsg.sendToTarget();

            return true;
        }
        @Override
        public void onCloseWindow(WebView window) {
            Log.i(getClass().getName(), "onCloseWindow");
            /*window.setVisibility(View.GONE);
            window.destroy();*/
            super.onCloseWindow(window);
            webViewLayout.removeView(window);
        }
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        /* ???????????? ?????? ??? FOCUS ?????? ?????? ????????? ??? ????????? ??????*/
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
            String pasteData = "";

            if(!clipboard.hasPrimaryClip()){
            }
            else if((clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))==false){
        }
            else{
                CharSequence item = clipboard.getPrimaryClip().getItemAt(0).coerceToText(getApplicationContext());
                if(item != null && item.length() != 0){
                    pasteData = item.toString();
                }
            }
        }
    }

    public class WebAppInterface {
        /*????????????*/
        @JavascriptInterface
        public void copyToClipboard(String text) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            String pasteData = "";

            if(!clipboard.hasPrimaryClip()){
            }
            else if((clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))==false){
            }
            else{
                CharSequence item = clipboard.getPrimaryClip().getItemAt(0).coerceToText(getApplicationContext());
                if(item != null && item.length() != 0){
                    pasteData = item.toString();
                }
            }
        }

    }


    //?????? ???????????? ????????? ?????? ??????
    @Override
    public void onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }


}

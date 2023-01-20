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
        webSettings.setSupportMultipleWindows(true); // 여러 창 또는 탭 열리는 것 허용
        //webSettings.setLoadWithOverviewMode(true); // 페이지 내에서만 이동하게끔
        webSettings.setUseWideViewPort(true); // 페이지를 웹뷰 width에 맞춤
        webSettings.setSupportZoom(false); // 확대 비활성화
        webSettings.setBuiltInZoomControls(false); // 확대 비활성화
        //webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 캐시 사용안함 (매번 새로 로딩)
        WebView.setWebContentsDebuggingEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webView.addJavascriptInterface(new WebAppInterface(), "NativeAndroid");
        webView.setWebChromeClient((WebChromeClient)(new MyWebChromeClient()));

        // 앱에서 표시할 url 입력
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

                    contentDisposition = URLDecoder.decode(contentDisposition,"UTF-8"); //디코딩
                    String FileName = contentDisposition.replace("attachment; filename=", ""); //attachment; filename*=UTF-8''뒤에 파일명이있는데 파일명만 추출하기위해 앞에 attachment; filename*=UTF-8''제거

                    String cookie = CookieManager.getInstance().getCookie(url);
                    request.addRequestHeader("Cookie", cookie);

                    String fileName = FileName; //위에서 디코딩하고 앞에 내용을 자른 최종 파일명
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
                    Toast.makeText(getApplicationContext(),"파일이 다운로드됩니다.", Toast.LENGTH_LONG).show();
                }
                catch (Exception e) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            Toast.makeText(getBaseContext(), "다운로드를 위해\n권한이 필요합니다.", Toast.LENGTH_LONG).show();
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1004);
                        }
                        else {
                            Toast.makeText(getBaseContext(), "다운로드를 위해\n권한이 필요합니다.", Toast.LENGTH_LONG).show();
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
           카카오톡 공유하기 클릭시 intent:// 처리해줘야함
           근데 shouldOverrideUrlLoading 함수 자체가 호출되지 않음
            */
            Log.d(TAG, request.getUrl().toString());

            if (Objects.equals(request.getUrl().getScheme(), "intent")) {
                try {
                    // Intent 생성
                    Intent intent = Intent.parseUri(request.getUrl().toString(), Intent.URI_INTENT_SCHEME);
                    Log.d("INTENT!!: {}", String.valueOf(intent));
                    // 실행 가능한 앱이 있으면 앱 실행
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                        Log.d("ACTIVITY: {}",intent.getPackage());
                        return true;
                    }

                    // Fallback URL이 있으면 현재 웹뷰에 로딩
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

            // 나머지 서비스 로직 구현

            return false;
        }

        // SSL 인증서 무시
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
            /*카카오톡 공유하기의 경우 새창이 열려서 WebChromeClient에서 처리가 필요할 것으로 예상했으나
            onCreateWindow 함수가 작동하지 않았음
             */
            Log.i(TAG, "window.open 협의가 필요합니다.");
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
        /* 클립보드 접근 시 FOCUS 관련 코드 필요한 것 같아서 추가*/
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
        /*클립보드*/
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


    //폰의 뒤로가기 버튼의 동작 입력
    @Override
    public void onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }


}

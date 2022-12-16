package com.teamb.bucwith;

import android.net.http.SslError;
import android.os.Bundle;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        webView = (WebView)findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClientClass());
        webSettings.setSupportMultipleWindows(false); // 여러 창 또는 탭 열리는 것 비허용
        webSettings.setLoadWithOverviewMode(true); // 페이지 내에서만 이동하게끔
        webSettings.setUseWideViewPort(true); // 페이지를 웹뷰 width에 맞춤
        webSettings.setSupportZoom(false); // 확대 비활성화
        webSettings.setBuiltInZoomControls(false); // 확대 비활성화
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 캐시 사용안함 (매번 새로 로딩)
        webSettings.setDomStorageEnabled(true);
        // 앱에서 표시할 url 입력
        webView.loadUrl("http://bucwiths.shop/");
        webView.setWebViewClient(new WebViewClient());
    }
    private class WebViewClientClass extends WebViewClient {
        // SSL 인증서 무시
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        // 페이지 내에서만 url 이동하게끔 만듬
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
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

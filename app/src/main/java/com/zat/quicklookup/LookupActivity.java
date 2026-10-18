package com.zat.quicklookup;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/** 選択した文字を小さいウィンドウで調べる。 */
public class LookupActivity extends Activity {

    private static final String[] TAB_NAMES = {"国語", "英和", "Google", "Wiki"};
    private static final int KOKUGO = 0, EIWA = 1, GOOGLE = 2, WIKI = 3;
    private static final int MAX_LEN = 200;

    private WebView web;
    private EditText queryView;
    private ProgressBar progress;
    private Button[] tabs;
    private String query = "";
    private int mode = KOKUGO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_lookup);
        resizeWindow();
        setFinishOnTouchOutside(true);

        web = findViewById(R.id.web);
        queryView = findViewById(R.id.query);
        progress = findViewById(R.id.progress);

        setupWebView();
        setupTabs();

        findViewById(R.id.close).setOnClickListener(v -> finish());
        findViewById(R.id.open_browser).setOnClickListener(v -> openInBrowser());
        queryView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String q = queryView.getText().toString().trim();
                if (!q.isEmpty()) {
                    query = q;
                    hideKeyboard();
                    load();
                }
                return true;
            }
            return false;
        });

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String q = extractText(intent);
        if (q == null || q.isEmpty()) {
            finish();
            return;
        }
        query = q;
        queryView.setText(q);
        mode = isAsciiOnly(q) ? EIWA : KOKUGO;
        load();
    }

    private static String extractText(Intent intent) {
        if (intent == null) return null;
        CharSequence cs = null;
        if (Intent.ACTION_PROCESS_TEXT.equals(intent.getAction())) {
            cs = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        } else if (Intent.ACTION_SEND.equals(intent.getAction())) {
            cs = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
        }
        if (cs == null) return null;
        String s = cs.toString().replaceAll("\\s+", " ").trim();
        if (s.length() > MAX_LEN) s = s.substring(0, MAX_LEN);
        return s;
    }

    private static boolean isAsciiOnly(String s) {
        boolean hasLetter = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 0x7F) return false;
            if (Character.isLetter(c)) hasLetter = true;
        }
        return hasLetter;
    }

    /** 画面の幅92%・高さ62%の小ウィンドウにする */
    private void resizeWindow() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        boolean landscape = dm.widthPixels > dm.heightPixels;
        int w = (int) (dm.widthPixels * (landscape ? 0.6 : 0.92));
        int h = (int) (dm.heightPixels * (landscape ? 0.85 : 0.62));
        getWindow().setLayout(w, h);
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resizeWindow();
    }

    private void setupWebView() {
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);   // 辞書サイトの表示に必要
        s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                String scheme = req.getUrl().getScheme();
                // http/https だけウィンドウ内で開く。アプリ起動リンク等は無視
                return !("http".equals(scheme) || "https".equals(scheme));
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int p) {
                progress.setProgress(p);
                progress.setVisibility(p >= 100 ? View.INVISIBLE : View.VISIBLE);
            }
        });
    }

    private void setupTabs() {
        LinearLayout bar = findViewById(R.id.tabs);
        tabs = new Button[TAB_NAMES.length];
        for (int i = 0; i < TAB_NAMES.length; i++) {
            Button b = new Button(this, null, android.R.attr.borderlessButtonStyle);
            b.setText(TAB_NAMES[i]);
            b.setAllCaps(false);
            b.setMinWidth(0);
            b.setMinimumWidth(0);
            final int idx = i;
            b.setOnClickListener(v -> {
                mode = idx;
                load();
            });
            bar.addView(b, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tabs[i] = b;
        }
    }

    private void updateTabs() {
        for (int i = 0; i < tabs.length; i++) {
            boolean on = i == mode;
            tabs[i].setAlpha(on ? 1f : 0.45f);
            tabs[i].setTypeface(null, on ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private void load() {
        updateTabs();
        web.clearHistory();
        web.loadUrl(buildUrl(mode, query));
    }

    private static String buildUrl(int mode, String q) {
        String enc;
        try {
            enc = URLEncoder.encode(q, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            enc = q;
        }
        switch (mode) {
            case EIWA:   return "https://ejje.weblio.jp/content/" + enc;
            case GOOGLE: return "https://www.google.com/search?q=" + enc;
            case WIKI:   return "https://ja.m.wikipedia.org/w/index.php?search=" + enc;
            case KOKUGO:
            default:     return "https://www.weblio.jp/content/" + enc;
        }
    }

    private void openInBrowser() {
        String url = web.getUrl() != null ? web.getUrl() : buildUrl(mode, query);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            finish();
        } catch (Exception ignored) {
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(queryView.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (web != null) {
            web.stopLoading();
            web.destroy();
        }
        super.onDestroy();
    }
}

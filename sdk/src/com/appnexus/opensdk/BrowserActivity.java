/*
 *    Copyright 2013 APPNEXUS INC
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.appnexus.opensdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.*;
import android.webkit.WebSettings.PluginState;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.VideoView;
import com.appnexus.opensdk.AdView.BrowserStyle;
import com.appnexus.opensdk.utils.Clog;
/**
 * This is the in-app browser activity. You must add a reference to in your app's AndroidManifest.xml
 * file. {@link AdActivity} Also needs to be added allow the in-app browser functionality,
 * <pre>
 * {@code
 * <application>
 *   ....
 *   <activity android:name="com.appnexus.opensdk.AdActivity" />
     <activity android:name="com.appnexus.opensdk.BrowserActivity" />
 * </application>
 * }
 * </pre>
 */
public class BrowserActivity extends Activity {
    private WebView webview;

    private ProgressBar progressBar;

    @SuppressWarnings("deprecation")
    @SuppressLint({"SetJavaScriptEnabled", "NewApi"})
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_in_app_browser);

        webview = (WebView) findViewById(R.id.web_view);
        ImageButton  back = (ImageButton) findViewById(R.id.browser_back);
        ImageButton forward = (ImageButton) findViewById(R.id.browser_forward);
        ImageButton refresh = (ImageButton) findViewById(R.id.browser_refresh);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setSupportZoom(true);
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setPluginState(PluginState.ON_DEMAND);

        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (webview.canGoBack()) {
                    webview.goBack();
                } else {
                    finish();
                }
            }

        });

        forward.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                webview.goForward();
            }
        });

        refresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                webview.reload();
            }
        });

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http")) {
                    Clog.d(Clog.baseLogTag,
                            Clog.getString(R.string.opening_url, url));
                    return false;
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        startActivity(i);
                        finish();
                    } catch (ActivityNotFoundException e) {
                        Clog.w(Clog.browserLogTag,
                                Clog.getString(R.string.opening_url_failed, url));
                    }
                    return true;
                }
            }
        });

        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                if (view instanceof FrameLayout) {
                    FrameLayout frame = (FrameLayout) view;
                    if (frame.getFocusedChild() instanceof VideoView) {
                        VideoView video = (VideoView) frame.getFocusedChild();
                        frame.removeView(video);
                        ((Activity) webview.getContext()).setContentView(video);
                        video.start();
                    }
                }
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                // super.onConsoleMessage(consoleMessage);
                Clog.w(Clog.browserLogTag,
                        Clog.getString(R.string.console_message,
                                consoleMessage.message(),
                                consoleMessage.lineNumber(),
                                consoleMessage.sourceId()));
                return true;
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message,
                                     JsResult result) {
                // /super.onJsAlert(view, url, message, result);
                Clog.w(Clog.browserLogTag,
                        Clog.getString(R.string.js_alert, message, url));
                result.confirm();
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100
                        && progressBar.getVisibility() == ProgressBar.GONE) {
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                }
                progressBar.setProgress(progress);
                if (progress == 100) {
                    progressBar.setVisibility(ProgressBar.GONE);
                }
            }
        });

        String url = (String) getIntent().getExtras().get("url");

        String id = (String) getIntent().getExtras().get("bridgeid");
        if (id != null) {
            BrowserStyle style = null;
            for (Pair<String, BrowserStyle> p : AdView.BrowserStyle.bridge) {
                if (p.first.equals(id)) {
                    style = p.second;
                    AdView.BrowserStyle.bridge.remove(p);
                }
            }
            if (style != null) {
                int sdk = android.os.Build.VERSION.SDK_INT;
                if (sdk >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    back.setBackground(style.backButton);
                    forward.setBackground(style.forwardButton);
                    refresh.setBackground(style.refreshButton);
                } else {
                    back.setBackgroundDrawable(style.backButton);
                    forward.setBackgroundDrawable(style.forwardButton);
                    refresh.setBackgroundDrawable(style.refreshButton);
                }
            }
        }

        webview.loadUrl(url);
    }

    @Override
    protected void onResume() {
        webview.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        webview.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (webview != null) {
            if (webview.getParent() != null)
                ((ViewGroup) webview.getParent()).removeView(webview);
            webview.destroy();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem open = menu.add("Open With Browser");
        open.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Clog.d(Clog.baseLogTag,
                        Clog.getString(R.string.opening_native_current));
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(webview
                        .getUrl()));
                startActivity(i);
                finish();
                return true;
            }

        });
        return true;
    }
}

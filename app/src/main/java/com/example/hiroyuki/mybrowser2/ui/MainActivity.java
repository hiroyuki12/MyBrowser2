package com.example.hiroyuki.mybrowser2.ui;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.hiroyuki.mybrowser2.R;
import com.example.hiroyuki.mybrowser2.model.Entry;
import com.example.hiroyuki.mybrowser2.model.OptionMenu;
import com.example.hiroyuki.mybrowser2.model.Scale;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends Activity {

    private WebView webView;
    private Entry entry = new Entry();
    private String bookmarkFileName = "bookmark.txt";
    private int newsIndex = 1;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);   //既存のコード
        entry.setXlarge(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  //アクションバーを非表示にする
        setContentView(R.layout.activity_main);  //既存のコード
        webView = (WebView) findViewById(R.id.webview);  // Webビューの作成
        Log.d("MyBrowser", "onCreate(1)");

        //webView.setVerticalScrollbarOverlay(true);  //スクロールバーの領域を消す　不要？
        webView.setWebViewClient(new WebViewClient()  //リンクをタップしたときに標準ブラウザを起動させない
        {
            @Override
            public void onPageStarted(final WebView view, final String url, final Bitmap favicon) {
                //ページのURL・タイトルを取得
                entry.setUrlTitle(url, view.getTitle());
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar1);
                progressBar.setVisibility(View.VISIBLE);

                //googlePlay, mp3, pdf, apkを別アプリで開く
                if (url.startsWith("https://play.google.com/") || url.startsWith("market://")
                        || url.startsWith("http://www.slideshare.net/")
                        || url.endsWith("mp3") || url.endsWith("pdf") || url.endsWith("apk")
                        ) {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    return;
                }

                Scale s = new Scale();
                entry.setScale(s.getScale(entry.getXlarge(), url));
                webView.setInitialScale(entry.getScale());  //拡大率を設定
                Log.d("MyBrowser", "onPageStarted(4)");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //タイトルを取得
                entry.setTitle(view.getTitle());

                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar1);
                progressBar.setVisibility(View.GONE);  //プログレスバーを非表示
                Log.d("MyBrowser", "onPageFinished(5)");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                super.shouldOverrideUrlLoading(webView, url);
                Log.d("MyBrowser", "shouldOverrideUrlLoading");
                return false;
            }

        });

        /*
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar1);
                if(progress >= 100)
                {
                    progressBar.setVisibility(View.GONE);  //プログレスバーを非表示
                }
                Log.d("MyBrowser", "setWebChromeClient(3)");
            }
        });
        */

        // ロングタップ時
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                WebView webView = (WebView) v;
                WebView.HitTestResult hr = webView.getHitTestResult();
                String url = hr.getExtra();

                if (url != null)  //リンクをロングタップ時
                {
                    // urlがnullの時は空白ページを開く 新しいViewでリンク開く
                    Intent intentNew = new Intent(getApplicationContext(), MainActivity.class);
                    intentNew.putExtra("url", url);
                    startActivity(intentNew);
                }
                Log.d("MyBrowser", "setOnLongClickListener");
                return true;  //onClickイベントを発生させない。テキストを選択しない。
            }
        });

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean bJavascriptOn = pref.getBoolean("javascriptOn", true);

        WebSettings settings = webView.getSettings();
        settings.setSupportMultipleWindows(false);  //trueではGoogleニュースのリンクが開けない/
        settings.setLoadsImagesAutomatically(true);
        //settings.setSupportZoom(true);
        //settings.setLightTouchEnabled(true);

        if (entry.getXlarge()) {
            settings.setBuiltInZoomControls(true);  // 読み込んだWebページをWebView上で拡大・縮小（ピンチイン・アウト）可能に
            //settings.setBuiltInZoomControls(false);  // メモリリーク対策
            // スクロールした時に、ズームボタンが表示
        }
        settings.setJavaScriptEnabled(bJavascriptOn);  //javascript有効化
        Bundle extras = getIntent().getExtras();
        // 他のアプリでURLをタップした時の処理(暗黙的インテント経由で起動)
        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {  //暗黙的インテント
            String url = getIntent().getDataString();
            webView.loadUrl(url);
        } else if (extras != null) {  // MyBrowserでリンクを長押しした時の処理
            String url = extras.getString("url");
            webView.loadUrl(url);
        } else {
            String url = "http://www.nikkei.com/";
                         /*
                 webView.getSettings().setLoadWithOverviewMode(true);    //Overviewモードはページが画面に収まるように自動で縮小します
                 webView.getSettings().setUseWideViewPort(true);
-
            webView.getSettings().setLoadWithOverviewMode(false);
            webView.getSettings().setUseWideViewPort(false);
        */
            webView.loadUrl(url);
            Log.d("MyBrowser", "start(2)");
        }
    }

    // オプション・メニューを作成
    private static final int SENDTO_MENU_ID = 1;
    private static final int OTHER_BROWSER = 2;
    private static final int SHOW_SCALE = 3;
    private static final int NEW_TAB = 4;
    private static final int ADD_BOOKMARK_MENU_ID = 5;
    private static final int VIEW_BOOKMARK_MENU_ID = 6;
    private static final int SETTING_MENU_ID = 7;
    private static final int FINISH_MENU_ID = 8;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.clear();
        OptionMenu o = new OptionMenu();
        menu = o.addSubMenu(menu);
        Log.d("MyBrowser", "onCreateOptionsMenu");
        return true;
    }

    // メニューを選択した時の処理
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        OptionMenu o = new OptionMenu();
        String[] url = o.getUrl(item, newsIndex);

        if (url[0] != "") {
            webView.loadUrl(url[0]);
            newsIndex = Integer.parseInt(url[1]);
            return super.onMenuItemSelected(featureId, item);
        } else {
            switch (item.getItemId()) {
                case SENDTO_MENU_ID:
                    if (entry.getTitle() != null)
                        showToast("追加:" + entry.getTitle(), "short");

                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, entry.getUrl());
                    startActivityForResult(intent, 0);
                    break;
                case OTHER_BROWSER:
                    if (entry.getUrl() != null) {
                        Uri uri = Uri.parse(entry.getUrl());
                        Intent intentBrowser = new Intent(Intent.ACTION_VIEW, uri);
                        Intent chooser = Intent.createChooser(intentBrowser, "ブラウザの選択");
                        startActivity(chooser);
                    }
                    break;
                case SHOW_SCALE:
                    showToast("拡大率は " + String.valueOf(entry.getScale()) + " %です", "short");
                    break;
                case NEW_TAB:
                    // 新しいViewで空白ページを開く
                    Intent intentNew = new Intent(getApplicationContext(), MainActivity.class);
                    intentNew.putExtra("url", "about:blank");
                    startActivity(intentNew);
                    showToast("about:blank", "short");

                    break;
                case ADD_BOOKMARK_MENU_ID:
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    // アラートダイアログのタイトルを設定します
                    alertDialogBuilder.setTitle("ブックマーク追加");
                    // アラートダイアログのメッセージを設定します
                    //alertDialogBuilder.setMessage(currentUrl);
                    alertDialogBuilder.setMessage(entry.getTitle());
                    // アラートダイアログの肯定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
                    alertDialogBuilder.setPositiveButton("追加",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Write
                                    {
                                        FileOutputStream fileOutputStream = null;
                                        try {
                                            fileOutputStream = openFileOutput(bookmarkFileName, MODE_APPEND);  //追記モード
                                            //FileOutputStream fileOutputStream = openFileOutput(bookmarkFileName, MODE_PRIVATE);
                                            String writeString = entry.getTitle() + "," + entry.getUrl() + "\n";
                                            fileOutputStream.write(writeString.getBytes());
                                        } catch (FileNotFoundException e) {
                                        } catch (IOException e) {
                                        } finally {
                                            if (fileOutputStream != null) {
                                                try {
                                                    fileOutputStream.close();
                                                } catch (Exception ex) {
                                                }
                                            }
                                        }
                                    }

                                    //Toast.makeText(getApplicationContext(), "追加:" + entry.getUrl(), Toast.LENGTH_LONG).show();
                                    showToast("追加:" + entry.getUrl(), "short");
                                }
                            }
                    );
                    // アラートダイアログの否定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
                    alertDialogBuilder.setNegativeButton("キャンセル",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }
                    );
                    // アラートダイアログのキャンセルが可能かどうかを設定します
                    alertDialogBuilder.setCancelable(true);
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    // アラートダイアログを表示します
                    alertDialog.show();
                    break;
                case VIEW_BOOKMARK_MENU_ID:
                    // 起動先アクティビティからデータを返してもらいたい場合は
                    // Activity#startActivityForResult(intent, requestCode) を使う
                    // 第二引数の requestCode は 下の onActivityResult の
                    // 第一引数に渡される値で、条件分岐のための数値。
                    // requestCode が 0 未満の場合は startActivity(intent) と等価
                    Intent intentBookmark = new Intent(this, BookmarkActivity.class);
                    int requestCode = 123;
                    startActivityForResult(intentBookmark, requestCode);
                    break;
                case SETTING_MENU_ID:
                    //設定画面を表示
                    startActivity(new Intent(getApplicationContext(), SettingActivity.class));
                    break;
                case FINISH_MENU_ID:
                    //終了ボタンが押されたとき
                    webView.clearCache(true); // キャッシュのクリア
                    webView.clearHistory(); // 履歴のクリア
                    this.finish();  //閉じる
                    break;
                default:
                    break;
            }
        }
        Log.d("MyBrowser", "onMenuItemSelected");
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // トースト表示
    public void showToast(String Message, String Length) {
        if (Length == "long") {
            Toast toast = Toast.makeText(this, Message, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.show();
        } else {
            Toast toast = Toast.makeText(this, Message, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.show();
        }
    }
}

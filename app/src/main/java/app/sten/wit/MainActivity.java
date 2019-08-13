package app.sten.wit;

import java.util.Date;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.net.Uri;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.KeyEvent;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
    public static final String URL = "http://112.74.170.24/";

    CallReceiver callReceiver;
    WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // We do not have this permission. Let's ask the user
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        }

        if (callReceiver == null) {
            callReceiver = new CallReceiver();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        // dynamically register CallReceiver
        registerReceiver(callReceiver, intentFilter);

        webView = (WebView) findViewById(R.id.webview);

        webView.addJavascriptInterface(new JsObject(), "sten");
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        webView.loadUrl(URL);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (uri == null) return false;
                String url = uri.toString();

                try {
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);

                        return true;
                    }
                } catch (Exception e){
                    return true;
                }

                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // manually unregister dynamically registered CallReceiver
        if (callReceiver != null) {
            unregisterReceiver(callReceiver);
            callReceiver = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted!
                } else {
                    // permission denied or has been cancelled
                }
                return;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    class CallReceiver extends PhonecallReceiver {

        @Override
        protected void onIncomingCallStarted(Context ctx, String number, Date start) {
            String msg = "start incoming call: " + number + " at " + start;

            Toast.makeText(ctx.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            Log.d("#", msg);

            webView.loadUrl("javascript:writeNumber('" + number + "')");
        }

        @Override
        protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
            String msg = "start outgoing call: " + number + " at " + start;

            Toast.makeText(ctx.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            Log.d("#", msg);
        }

        @Override
        protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
            String msg = "end incoming call: " + number + " at " + end;

            Toast.makeText(ctx.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            Log.d("#", msg);
        }

        @Override
        protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
            String msg = "end outgoing call: " + number + " at " + end;

            Toast.makeText(ctx.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            Log.d("#", msg);
        }

        @Override
        protected void onMissedCall(Context ctx, String number, Date start) {
            String msg = "missed call: " + number + " at " + start;

            Toast.makeText(ctx.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            Log.d("#", msg);
        }
    }

    class JsObject {

        @JavascriptInterface
        public void debug(String msg) {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        }
    }
}
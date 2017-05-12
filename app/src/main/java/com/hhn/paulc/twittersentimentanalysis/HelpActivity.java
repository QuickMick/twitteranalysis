package com.hhn.paulc.twittersentimentanalysis;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

public class HelpActivity extends AppCompatActivity {

    private WebView webView;
    private TextView title;

    public static final String HELP = "Help";
    public static final String IMPRINT = "Imprint";

    public static final String VIEW_MODE = "HelpActivity.VIEW_MODE";

    public static final String THANKS_TO ="Oliver Ruoff for helping in relation to android related questions.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        this.webView = (WebView) findViewById(R.id.webview);
        this.title = (TextView) findViewById(R.id.titlelbl);

        String html = "";
        String current = this.getIntent().getStringExtra(VIEW_MODE);

        if(current == null || current.equals("")){
            finish();

            Toast.makeText(this,"Unknown action",Toast.LENGTH_SHORT).show();
        }

        switch (current){
            case HELP:
                html = getString(R.string.help_html);
                break;
            case IMPRINT:
                html = getString(R.string.imprint_html);
                break;
        }

        this.title.setText(current);


        String color_h1 = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, R.color.colorTextH1)));
        String color_h2 = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, R.color.colorTextH2)));
        String color_p = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, R.color.colorTextH3)));
        String bgColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, R.color.colorBackground)));

      /*  String size_H1 =  String.valueOf(getResources().getDimension(R.dimen.h2))+"px";
        String size_H2 = String.valueOf(getResources().getDimension(R.dimen.h3))+"px";
        String size_p = String.valueOf(getResources().getDimension(R.dimen.p))+"px";*/
        String size_H1 =  "24";
        String size_H2 = "20";
        String size_p = "18px";

        html = html.replace("#color_H1",color_h1)
                .replace("#color_H2",color_h2).replace("#color_p",color_p)
                .replace("#bgColor",bgColor).replace("#size_H1",size_H1)
                .replace("#size_H2",size_H2).replace("#size_p",size_p);

        webView.loadData(html, "text/html; charset=utf-8", "utf-8");

        webView.setWebViewClient(new SSLTolerentWebViewClient());
    }

    private class SSLTolerentWebViewClient extends WebViewClient {

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url != null /* && (url.startsWith("http://") || url.startsWith("https://"))*/) {
                view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            } else {
                return false;
            }
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest wrr) {
            Uri url = wrr.getUrl();
            if (url != null /* && (url.startsWith("http://") || url.startsWith("https://"))*/) {
                view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, url));
                return true;
            } else {
                return false;
            }
        }

        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {

            AlertDialog.Builder builder = new AlertDialog.Builder(HelpActivity.this);
            AlertDialog alertDialog = builder.create();
            String message = "SSL Certificate error.";
            switch (error.getPrimaryError()) {
                case SslError.SSL_UNTRUSTED:
                    message = "The certificate authority is not trusted.";
                    break;
                case SslError.SSL_EXPIRED:
                    message = "The certificate has expired.";
                    break;
                case SslError.SSL_IDMISMATCH:
                    message = "The certificate Hostname mismatch.";
                    break;
                case SslError.SSL_NOTYETVALID:
                    message = "The certificate is not yet valid.";
                    break;
            }

            message += " Do you want to continue anyway?";
            alertDialog.setTitle("SSL Certificate Error");
            alertDialog.setMessage(message);
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Ignore SSL certificate errors
                    handler.proceed();
                }
            });

            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    handler.cancel();
                }
            });
            alertDialog.show();
        }
    }
}

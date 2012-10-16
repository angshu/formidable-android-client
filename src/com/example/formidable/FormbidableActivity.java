package com.example.formidable;

import java.io.IOException;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.impl.StdCouchDbInstance;

import com.couchbase.touchdb.TDDatabase;
import com.couchbase.touchdb.TDServer;
import com.couchbase.touchdb.ektorp.TouchDBHttpClient;
import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class FormbidableActivity extends Activity {
	
	static { 
	    TDURLStreamHandlerFactory.registerSelfIgnoreError();
	}
	
	private static final String TAG = "MainActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        TDServer server = null;
        String filesDir = getFilesDir().getAbsolutePath();
        try {
            server = new TDServer(filesDir);
        } catch (IOException e) {
            Log.e(TAG, "Error starting TouchDB Server.", e);
        }
        
        CouchDbInstance client = new StdCouchDbInstance(new TouchDBHttpClient(server));  
        CouchDbConnector events = client.createConnector("events", true);
        
        setContentView(R.layout.activity_main);
        WebView myWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.loadUrl("http://enketo.org/launch?server=http%3A%2F%2Fformhub.org%2Fwho_forms");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
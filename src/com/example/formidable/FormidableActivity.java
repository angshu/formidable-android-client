package com.example.formidable;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.codehaus.jackson.JsonNode;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.ReplicationCommand;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;
import org.ektorp.ViewResult.Row;
import org.ektorp.impl.StdCouchDbInstance;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.couchbase.touchdb.TDDatabase;
import com.couchbase.touchdb.TDServer;
import com.couchbase.touchdb.TDView;
import com.couchbase.touchdb.ektorp.TouchDBHttpClient;
import com.couchbase.touchdb.replicator.TDReplicator;
import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;

public class FormidableActivity extends Activity {
	
	static { 
	    TDURLStreamHandlerFactory.registerSelfIgnoreError();
	}
	
	private static final String TAG = "MainActivity";
	private TDServer localServer;
	CouchDbConnector events;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        initialize(savedInstanceState);
        
        String recordId = newId();
        createEvent(2, recordId, "Angshu", "Sakar");
        createEvent(1, recordId, "Chris", "Ford");
        createEvent(3, recordId, "Pulkit", "Bhuwalka");
        
		ViewQuery view = new ViewQuery().designDocId("_design/records").viewName("latest");
		ViewResult result = events.queryView(view);
		
		for( Row record : result.getRows()) {
			JsonNode data = record.getValueAsNode().get(recordId).get("data");
			JsonNode surname = data.get("surname");
			JsonNode name = data.get("name");
			System.out.println(String.format("Name: %s %s", name.getTextValue(), surname.getTextValue()));
		}
		//localServer.close();
        
        setContentView(R.layout.activity_main);
        WebView myWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationDatabasePath("/data/data/com.example.formidable/databases");
        webSettings.setGeolocationEnabled(true);
        
        myWebView.loadUrl("http://enketo.org/launch?server=http%3A%2F%2Fformhub.org%2Fwho_forms");
    }

	private void initialize(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);        
        startServer();    
        startClient();
	}

	private void createEvent(int epoch, String recordId, String givenName, String familyName) {
		Event event = new Event(epoch, recordId);
		event.put("name", givenName);
		event.put("surname", familyName);
		events.create(newId(), event);
	}

	private void startClient() {
		CouchDbInstance client = new StdCouchDbInstance(new TouchDBHttpClient(localServer));  
        events = client.createConnector("events", true);	
		beginReplicating(client);
	}

	private void startServer() {
        String filesDir = getFilesDir().getAbsolutePath();
        try {
            localServer = new TDServer(filesDir);
            startViews();
        } catch (IOException e) {
            Log.e(TAG, "Error starting TouchDB Server.", e);
        }
	}
	
	private TDView startViews() {
		TDDatabase db = localServer.getDatabaseNamed("events");
		TDView view = db.getViewNamed("records/latest");
		new EventAggregation().setMapReduceBlocksFor(view);
		return view;
	}

	private String newId() {
		return UUID.randomUUID().toString();
	}
	
	private void beginReplicating(CouchDbInstance client) {		
		ReplicationCommand push = new ReplicationCommand.Builder()
			.source("events")
			.target(Messages.getString("FormbidableActivity.serverURL"))
			.continuous(true)
			.build();
	
		client.replicate(push);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}

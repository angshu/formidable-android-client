package com.example.formidable;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import com.couchbase.touchdb.TDViewMapBlock;
import com.couchbase.touchdb.TDViewMapEmitBlock;
import com.couchbase.touchdb.TDViewReduceBlock;
import com.couchbase.touchdb.ektorp.TouchDBHttpClient;
import com.couchbase.touchdb.replicator.TDReplicator;
import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;

public class FormbidableActivity extends Activity {
	
	static { 
	    TDURLStreamHandlerFactory.registerSelfIgnoreError();
	}
	
	private static final String TAG = "MainActivity";
	private TDServer localServer;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        startServer();    
        CouchDbConnector events = startClient();
        
        String id = newId();
        Event created = new Event();
        created.put("name", "Angshu");
        created.put("surname", "sarkar");
		events.create(id, created);
		Event retrieved = events.find(Event.class, id);
		ViewQuery view = new ViewQuery().designDocId("_design/records").viewName("latest");
		ViewResult result = events.queryView(view);
		JsonNode valueNode = result.getRows().get(0).getValueAsNode();
		Iterator<String> fieldNames = valueNode.getFieldNames();
		
		while (fieldNames.hasNext()) {
			String field = fieldNames.next();
			JsonNode jsonNode = valueNode.get(field);
			System.out.println(String.format("field %s = %s", field, jsonNode.asText()));	
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

	private TDView startViews() {
		TDDatabase db = localServer.getDatabaseNamed("events");
		TDView view = db.getViewNamed("records/latest");
		view.setMapReduceBlocks(new TDViewMapBlock() {
			@Override
			public void map(Map<String, Object> document, TDViewMapEmitBlock emitter) {
				emitter.emit(document.get("_id"), document.get("data"));				
			}
		  }
		, new TDViewReduceBlock() {
				@Override
				public Object reduce(List<Object> keys, List<Object> values, boolean rereduce) {
					return values.get(values.size() - 1);
//					Map<String, Object> map = (Map<String, Object>) values.get(values.size() - 1);
//					for(String foo : map.keySet()) {
//						Object value = map.get(foo);
//						System.out.println("value="+value);
//					}
//					return values.get(values.size() - 1);
					
					
				}
		  }, newId());
		return view;
	}

	private CouchDbConnector startClient() {
		CouchDbInstance client = new StdCouchDbInstance(new TouchDBHttpClient(localServer));  
        CouchDbConnector events = client.createConnector("events", true);	
		beginReplicating(client);
		return events;
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

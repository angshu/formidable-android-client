package com.example.formidable;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.ReplicationCommand;
import org.ektorp.impl.StdCouchDbInstance;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import com.couchbase.touchdb.lucene.TDLucene;
import com.couchbase.touchdb.lucene.TDLucene.Callback;
import com.couchbase.touchdb.lucene.TDLuceneRequest;
import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;

public class FormidableActivity extends Activity {
	
	static {
	    TDURLStreamHandlerFactory.registerSelfIgnoreError();
	}

	private static final String TAG = "MainActivity";
	private TDServer localServer;
	CouchDbConnector events;
	private TDLucene lucene;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        initialize();       
        RecordRepository repository = new RecordRepository(events);
        
        String recordId = newId();     
        repository.put(createSimpleEvent(3, recordId, "surname", "Bhuwalka"));     
        repository.put(createSimpleEvent(2, recordId, "name", "Angshu"));
        repository.put(createSimpleEvent(1, recordId, "name", "Chris"));
        		
		Map<String, Object> record = repository.get(recordId);
		System.out.println(String.format("Name: %s %s", record.get("name"), record.get("surname")));
        
        setContentView(R.layout.activity_main);
        WebView myWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationDatabasePath("/data/data/com.example.formidable/databases");
        webSettings.setGeolocationEnabled(true);
        
        myWebView.loadUrl("http://enketo.org/launch?server=http%3A%2F%2Fformhub.org%2Fwho_forms");
        	
        
		doLuceneSearch("");
		
        
    }

	private void initialize() {
        startServer();    
        startClient();
	}

	private Event createSimpleEvent(int epoch, String recordId, String key, String value) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(key, value);
		return new Event(epoch, recordId, map);		
	}

	private void startClient() {
		TouchDBHttpClient touchDBHttpClient = new TouchDBHttpClient(localServer);
		CouchDbInstance client = new StdCouchDbInstance(touchDBHttpClient);
		events = client.createConnector("events", true);
		createSearchIndexer(touchDBHttpClient);
		beginReplicating(client);
	}

	private void createSearchIndexer(TouchDBHttpClient touchDBHttpClient) {
		touchDBHttpClient.put("/events/_design/records", "{\"fulltext\": " +
				"{\"byName\": { \"analyzer\":\"standard\"," +
				"\"index\": \"function(doc) { if (doc.data) { var ret=new Document(); ret.add(doc.data.name); return ret; } return null; } \"}}}");
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

	private void startViews() {
		TDDatabase db = localServer.getDatabaseNamed("events");
		TDView view = db.getViewNamed("records/latest");
		new CurrentState().setMapReduceBlocksFor(view);
	}

	private String newId() {
		return UUID.randomUUID().toString();
	}
	
	private void beginReplicating(CouchDbInstance client) {		
		ReplicationCommand push = new ReplicationCommand.Builder()
			.source("events")
			.target(Messages.getString("FormidableActivity.serverURL"))
			.continuous(true)
			.build();
	
		client.replicate(push);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    
    private void doLuceneSearch(String queryStr) {
    	try {
			if (lucene == null) {
				lucene = new TDLucene(localServer);
			}
			TDLuceneRequest req = new TDLuceneRequest();
			req.setUrl("/local/events/_design/records/byName")
					.addParam("q", "Chris")
					.addParam("include_docs", "true")
					.addParam("highlights", "5");

			lucene.fetch(req, new Callback() {
				@Override
				public void onSucess(Object resp) {
					if (resp instanceof JSONObject) {
						try {
							JSONArray rows = ((JSONObject) resp).getJSONArray("rows");
							if (rows.length() > 0) {
								JSONArray jsonArray = rows.getJSONArray(0);
								System.out.println("json array" + jsonArray);
							}
							else {
//								System.out.println("***********************");
//								System.out.println("Didn't find any records." + resp.toString());
//								System.out.println("***********************");
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}

				@Override
				public void onError(Object resp) {
					System.out.println("****** Error : " + resp.toString());
				}
			});

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    	
}

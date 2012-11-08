package com.example.formidable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;

public class FormidableActivity extends Activity {
	
	static {
	    TDURLStreamHandlerFactory.registerSelfIgnoreError();
	}

	//private static final String TAG = "MainActivity";
	private EventSource eventSource;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        initialize();       
        RecordRepository repository = new RecordRepository(eventSource.getConnector());
        
        String recordId = newId();     
        repository.put(createSimpleEvent(3, recordId, "surname", "Bhuwalka"));     
        repository.put(createSimpleEvent(2, recordId, "name", "Angshu"));
        repository.put(createSimpleEvent(1, recordId, "name", "Chris"));
        		
		Map<String, Object> record = repository.get(recordId);
		System.out.println(String.format("Name: %s %s", record.get("name"), record.get("surname")));
        
        initializeView();
        eventSource.getSearchAgent().triggerSearch("chris*");
        
    }

	private void initializeView() {
		setContentView(R.layout.activity_main);
        WebView myWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationDatabasePath("/data/data/com.example.formidable/databases");
        webSettings.setGeolocationEnabled(true);
        
        //myWebView.loadUrl("http://enketo.org/launch?server=http%3A%2F%2Fformhub.org%2Fwho_forms");
        myWebView.loadUrl("http:/m.google.com");
	}

	private void initialize() {
		this.eventSource = new EventSource(this);
	}

	private Event createSimpleEvent(int epoch, String recordId, String key, String value) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(key, value);
		return new Event(epoch, recordId, map);		
	}

		

	private String newId() {
		return UUID.randomUUID().toString();
	}
	
	

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    
    
    	
}

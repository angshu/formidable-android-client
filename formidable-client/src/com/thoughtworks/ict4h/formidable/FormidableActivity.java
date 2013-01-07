package com.thoughtworks.ict4h.formidable;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage.QuotaUpdater;
import android.webkit.WebView;

import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;
import com.thoughtworks.ict4h.formidable.bridge.OdkSurveyBridge;
import com.thoughtworks.ict4h.formidable.bridge.OdkSurveyBridge.RepositoryLocator;
import com.thoughtworks.ict4h.formidable.service.StorageService;

@SuppressLint("SetJavaScriptEnabled")
public class FormidableActivity extends Activity {

//	static {
//		TDURLStreamHandlerFactory.registerSelfIgnoreError();
//	}

	// private static final String TAG = "MainActivity";
	private EventSource eventSource;
	
	private StorageService storageSvc;
	
	private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  Because we have bound to a explicit
	        // service that we know is running in our own process, we can
	        // cast its IBinder to a concrete class and directly access it.
	        storageSvc = ((StorageService.LocalStorage)service).getService();
	        System.out.println("Storage Service Bound *********** ");
	        
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        // Because it is running in our same process, we should never
	        // see this happen.
	        storageSvc = null;
	        System.out.println("Storage Service UNBound *********** ");
	    }
	};

	private boolean isStorageSvcBound;

	private RepositoryLocator repositoryLocator;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		doBindService();
		//initialize();
		//createDummyData();
		
		Intent intent = getIntent();
		String formName = intent.getStringExtra("formName");
		WebView view = initView();
		view.loadUrl(formateUrl("opendatakit.collect2/form-files/default/index.html#formPath=../"
				+ formName + "/&pageRef=1"));
		// search(eventSource, "name:Angshu AND surname:Bhuwalka");
		//view.loadUrl(formateUrl("opendatakit.collect2/form-files/default/index.html#formPath=../example/&pageRef=1"));
		//search(eventSource, "name:vivek AND surname:singh");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	    doUnbindService();
	}

	private void createDummyData() {
		RecordRepository repository = this.eventSource.getRepository();
		
		 String recordId = newId();
		 repository.put(Event.createSimpleEvent(3, recordId, "surname:Bhuwalka"));
		 repository.put(Event.createSimpleEvent(2, recordId, "name:Angshu"));
		 repository.put(Event.createSimpleEvent(1, recordId, "name:Chris"));
		
		 //check search functionality
		 repository.put(Event.createSimpleEvent(1, newId(), "name:Vivek",
		 "surname:Singh"));
		
		 Map<String, Object> record = repository.get(recordId);
		 System.out.println(String.format("Name: %s %s", record.get("name"),
		 record.get("surname")));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private WebView initView() {
		setContentView(R.layout.activity_main);
		WebView browser = (WebView) findViewById(R.id.webview);
		browser.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onExceededDatabaseQuota(String url,
					String databaseIdentifier, long quota,
					long estimatedDatabaseSize, long totalQuota,
					QuotaUpdater quotaUpdater) {
				quotaUpdater.updateQuota(estimatedDatabaseSize * 2);
			}

			public boolean onConsoleMessage(ConsoleMessage cm) {
				Log.d("MyApplication",
						cm.message() + " -- From line " + cm.lineNumber()
								+ " of " + cm.sourceId());
				return true;
			}
		});

		WebSettings settings = browser.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setDatabaseEnabled(true);

		// String databasePath = this.getApplicationContext().getDir("database",
		// MODE_PRIVATE).getPath();
		// System.out.println("****** Database path = " + databasePath);
		settings.setDatabasePath("/data/data/com.thoughtworks.ict4h.formidable/databases");

		settings.setDomStorageEnabled(true);
		settings.setGeolocationDatabasePath(Configuration.getGeolocDbPath());
		settings.setGeolocationEnabled(true);
		settings.setAllowFileAccess(true);
		
		browser.addJavascriptInterface(new OdkSurveyBridge(repositoryLocator), "extRepo");
		// browser.loadUrl("http://"+getLocalIpAddress()+":"+PORT);
		// browser.loadUrl("http://enketo.org/launch?server=http%3A%2F%2Fformhub.org%2Fwho_forms");
		return browser;
	}

	private void initialize() {
		this.eventSource = new EventSource(this.getFilesDir().getAbsolutePath());
	}	

	private String newId() {
		return UUID.randomUUID().toString();
	}

	private String formateUrl(String url) {
		return "file:///android_asset/" + url;
	}

	private boolean validStr(String value) {
		return (value != null) && (!"".equals(value));
	}
	
	void doBindService() {
	    // Establish a connection with the service.  We use an explicit
	    // class name because we want a specific service implementation that
	    // we know will be running in our own process (and thus won't be
	    // supporting component replacement by other applications).
	    bindService(new Intent(this, StorageService.class), mConnection, Context.BIND_AUTO_CREATE);
	    isStorageSvcBound = true;
	    
	    repositoryLocator = new OdkSurveyBridge.RepositoryLocator() {
			@Override
			public RecordRepository getRepository() {
				return storageSvc.getRepository();
			}
		};
	}

	void doUnbindService() {
	    if (isStorageSvcBound) {
	        // Detach our existing connection.
	        unbindService(mConnection);
	        isStorageSvcBound = false;
	    }
	}

	private void search(EventSource src, String criteria) {
		src.getSearchAgent().triggerSearch(criteria, new SearchHandler() {
			@Override
			void realize(JSONObject[] results) {
				for (JSONObject result : results) {
					// we should probably create something
					// Event.parse(JSOBObject) to get a Event object out of json
					// and return an array of Event Objects. parse method can
					// extract attributes like
					// epoch, docid, revid, and all the elements in the data
					// element recursively
					System.out.println("search result record => %s"
							.format(result.toString()));
				}
			}

			@Override
			void fault(Object resp) {
				System.out.println("SearchAgent.triggerSearch => Error : "
						+ resp.toString());
			}
		});
	}

	private String getLocalIpAddress() {
		try {
			for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en
					.hasMoreElements();) {
				NetworkInterface intf = (NetworkInterface) en.nextElement();
				for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr
						.hasMoreElements();) {
					InetAddress inetAddress = (InetAddress) enumIpAddr
							.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("Exception", ex.toString());
		}
		return null;
	}

}

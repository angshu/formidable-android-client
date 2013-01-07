package com.thoughtworks.ict4h.formidable.service;

import com.thoughtworks.ict4h.formidable.EventSource;
import com.thoughtworks.ict4h.formidable.RecordRepository;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class StorageService extends Service {

	private EventSource eventSource = null;
	
	public class LocalStorage extends Binder {
		public StorageService getService() {
            return StorageService.this;
        }
    }
	
	private final IBinder mBinder = new LocalStorage();
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("StorageService", "Creating storage service");
		if (eventSource == null) {
			initializeEventSrc(getFilesDir().getAbsolutePath());
		}
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("StorageService", "Received start id " + startId + ": " + intent);
        // continue running until it is explicitly
        return START_STICKY;
    }
	
	@Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
	
	@Override
    public void onDestroy() {
		super.onDestroy();
		Log.i("StorageService", "Destroying storage service");
    }
	
	public EventSource getEventSource() {
		return eventSource;
	}


	private void initializeEventSrc(final String filesDir) {
		new Thread(new Runnable() {
			public void run() {
	            eventSource = new EventSource(filesDir);
	            Log.i("StorageService", "Event Source Created");
	        }
	    }).start();
	}

	public RecordRepository getRepository() {
		return (eventSource != null) ? eventSource.getRepository() : null;
		
	}

	

}

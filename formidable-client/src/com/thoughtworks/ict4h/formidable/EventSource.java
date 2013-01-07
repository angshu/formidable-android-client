package com.thoughtworks.ict4h.formidable;

import java.io.IOException;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.ReplicationCommand;
import org.ektorp.impl.StdCouchDbInstance;

import android.util.Log;

import com.couchbase.touchdb.TDDatabase;
import com.couchbase.touchdb.TDServer;
import com.couchbase.touchdb.TDView;
import com.couchbase.touchdb.ektorp.TouchDBHttpClient;
import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;

public class EventSource {
	
	static {
		TDURLStreamHandlerFactory.registerSelfIgnoreError();
	}

	private TDServer localServer;
	private CouchDbConnector events;
	private SearchAgent searchAgent;	
	
	public EventSource(String filesDir) {
		super();
		startServer(filesDir);
		startClient();
	}

	public SearchAgent getSearchAgent() {
		return searchAgent;
	}
	
	private void startServer(String filesDir) {
        try {
            localServer = new TDServer(filesDir);
            startViews();
        } catch (IOException e) {
            Log.e("Formidable", "Error starting TouchDB Server.", e);
        }
	}
	
	private void startViews() {
		TDDatabase db = localServer.getDatabaseNamed(Configuration.getDatabaseName());
		TDView view = db.getViewNamed("records/latest");
		new CurrentState().setMapReduceBlocksFor(view);
	}

	private void startClient() {
		TouchDBHttpClient touchDBHttpClient = new TouchDBHttpClient(localServer);
		CouchDbInstance client = new StdCouchDbInstance(touchDBHttpClient);
		events = client.createConnector(Configuration.getDatabaseName(), true);
		searchAgent = new SearchAgent(events.getConnection(), localServer);
		beginReplicating(client);
	}

	private void beginReplicating(CouchDbInstance client) {		
		ReplicationCommand push = new ReplicationCommand.Builder()
			.source(Configuration.getDatabaseName())
			.target(Configuration.getServerURL())
			.continuous(true)
			.build();
	
		client.replicate(push);
	}

	public RecordRepository getRepository() {
		return new RecordRepository(events);
	}
	

}

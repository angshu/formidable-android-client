package com.example.formidable.touchdb;

import android.util.Log;
import com.couchbase.touchdb.TDDatabase;
import com.couchbase.touchdb.TDServer;
import com.couchbase.touchdb.TDView;
import com.couchbase.touchdb.ektorp.TouchDBHttpClient;
import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;
import com.example.formidable.CurrentState;
import com.example.formidable.Event;
import com.example.formidable.FormidableActivity;
import com.example.formidable.Messages;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.ReplicationCommand;
import org.ektorp.impl.StdCouchDbInstance;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TouchDBSetup {

    private static final String TAG = "Touch DB Setup";

    private TDServer localServer;
    private CouchDbConnector events;

    static {
        TDURLStreamHandlerFactory.registerSelfIgnoreError();
    }

    public static CouchDbConnector init(String filesPath) {
        TouchDBSetup touchDBSetup = new TouchDBSetup();
        touchDBSetup.startServer(filesPath);
        touchDBSetup.startClient();
        return touchDBSetup.events;
    }

    private void startServer(String filesPath) {

        try {
            localServer = new TDServer(filesPath);
            startViews();
        } catch (IOException e) {
            Log.e(TAG, "Error starting TouchDB Server.", e);
        }
    }

    private TDView startViews() {
        TDDatabase db = localServer.getDatabaseNamed("events");
        TDView view = db.getViewNamed("records/latest");
        new CurrentState().setMapReduceBlocksFor(view);
        return view;
    }

    private void startClient() {
        CouchDbInstance client = new StdCouchDbInstance(new TouchDBHttpClient(localServer));
        events = client.createConnector("events", true);
        beginReplicating(client);
    }

    private void beginReplicating(CouchDbInstance client) {
        ReplicationCommand push = new ReplicationCommand.Builder()
                .source("events")
                .target(Messages.getString("FormidableActivity.serverURL"))
                .continuous(true)
                .build();

        client.replicate(push);
    }

}

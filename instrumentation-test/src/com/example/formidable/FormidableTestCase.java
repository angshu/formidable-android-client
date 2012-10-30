package com.example.formidable;

import android.test.InstrumentationTestCase;
import android.util.Log;
import com.couchbase.touchdb.TDDatabase;
import com.couchbase.touchdb.TDServer;
import com.couchbase.touchdb.TDView;
import com.couchbase.touchdb.ektorp.TouchDBHttpClient;
import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;
import com.couchbase.touchdb.support.FileDirUtils;
import junit.framework.Assert;
import org.codehaus.jackson.map.ObjectMapper;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.ReplicationCommand;
import org.ektorp.impl.StdCouchDbInstance;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

abstract class FormidableTestCase extends InstrumentationTestCase {

    public static final String TAG = "FormidableTestCase";

    private static boolean initializedUrlHandler = false;

    protected ObjectMapper mapper = new ObjectMapper();

    protected TDServer server = null;
    protected TDDatabase database = null;
    protected CouchDbConnector connector;

    protected String DEFAULT_TEST_DB = "formidable-test";

    @Override
    protected void setUp() throws Exception {
        Log.v(TAG, "setUp");
        super.setUp();

        //for some reason a traditional static initializer causes junit to die
        if(!initializedUrlHandler) {
            TDURLStreamHandlerFactory.registerSelfIgnoreError();
            initializedUrlHandler = true;
        }

        startServer();
        startClient();
    }

    private String getFilesDir() {
        //return getInstrumentation().getContext().getFilesDir().getAbsolutePath();
        return "/data/data/com.example.formidable/files";
    }

	private void startServer() {
        try {
            server = new TDServer(getFilesDir());
            startViews();
        } catch (IOException e) {
            Log.e(TAG, "Error starting TouchDB Server.", e);
        }
	}
  
	private void startViews() {
		TDDatabase db = server.getDatabaseNamed("events");
		TDView view = db.getViewNamed("records/latest");
		new CurrentState().setMapReduceBlocksFor(view);
	}
	
	private void startClient() {
		CouchDbInstance client = new StdCouchDbInstance(new TouchDBHttpClient(server));
        connector = client.createConnector("events", true);
	}
}

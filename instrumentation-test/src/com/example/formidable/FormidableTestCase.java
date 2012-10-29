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
    protected CouchDbConnector db;

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

        loadCustomProperties();
        startTouchDB();
        startDatabase();
        startViews();
        startClient();
    }

    @Override
    protected void tearDown() throws Exception {
        Log.v(TAG, "tearDown");
        super.tearDown();
        stopDatabase();
        stopTouchDB();
    }

    protected String getServerPath() {
//        String filesDir = getInstrumentation().getContext().getFilesDir().getAbsolutePath();
        String filesDir = "/data/data/com.example.formidable/files";
        return filesDir;
    }

    protected void startTouchDB() {
        try {
            String serverPath = getServerPath();
            File serverPathFile = new File(serverPath);
            FileDirUtils.deleteRecursive(serverPathFile);
            serverPathFile.mkdir();
            server = new TDServer(getServerPath());
        } catch (IOException e) {
            fail("Creating server caused IOException");
        }
    }

    protected void startDatabase() {
        database = ensureEmptyDatabase(DEFAULT_TEST_DB);
        boolean status = database.open();
        Assert.assertTrue(status);
    }

    private void startViews() {
        TDView view = database.getViewNamed("records/latest");
        new CurrentState().setMapReduceBlocksFor(view);
    }

    private void startClient() {
        CouchDbInstance client = new StdCouchDbInstance(new TouchDBHttpClient(server));
        db = client.createConnector("events", true);
        //beginReplicating(client);
    }

    private void beginReplicating(CouchDbInstance client) {
        ReplicationCommand push = new ReplicationCommand.Builder()
                .source("events")
                .target(Messages.getString("FormidableActivity.serverURL"))
                .continuous(true)
                .build();

        client.replicate(push);
    }

    protected void stopTouchDB() {
        if(server != null) {
            server.close();
        }
    }

    protected void stopDatabase() {
        if(database != null) {
            database.close();
        }
    }

    protected TDDatabase ensureEmptyDatabase(String dbName) {
        TDDatabase db = server.getExistingDatabaseNamed(dbName);
        if(db != null) {
            boolean status = db.deleteDatabase();
            Assert.assertTrue(status);
        }
        db = server.getDatabaseNamed(dbName, true);
        return db;
    }

    protected void loadCustomProperties() throws IOException {
        Properties systemProperties = System.getProperties();

        InputStream mainProperties = FormidableTestCase.class.getResourceAsStream("test.properties");
        if(mainProperties != null) {
            systemProperties.load(mainProperties);
        }
    }

}

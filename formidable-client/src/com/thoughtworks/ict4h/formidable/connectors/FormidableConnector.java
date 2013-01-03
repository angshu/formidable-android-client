package com.thoughtworks.ict4h.formidable.connectors;

import org.ektorp.CouchDbInstance;
import org.ektorp.impl.ObjectMapperFactory;
import org.ektorp.impl.StdCouchDbConnector;

import java.util.TimerTask;

public class FormidableConnector extends StdCouchDbConnector {

    TimerTask timerTask;

    public FormidableConnector(String databaseName, CouchDbInstance dbInstance) {
        super(databaseName, dbInstance);
        this.timerTask=new TimerTask() {

            @Override
            public void run() {
                createCache();
            }

            @Override
            public long scheduledExecutionTime() {
                return super.scheduledExecutionTime();
            }
        };
    }

    public FormidableConnector(String databaseName, CouchDbInstance dbi, ObjectMapperFactory om) {
        super(databaseName, dbi, om);
    }

    @Override
    public void create(String id, Object node) {
        super.create(id, node);
        createCacheForId(id);
    }

    private void createCache(){

    }

    private void createCacheForId(String id){

    }

}

package com.example.formidable;

import java.util.Map;
import java.util.UUID;

import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;

public class RecordRepository {

	private CouchDbConnector events;

	public RecordRepository(CouchDbConnector events) {
		this.events = events;
	}

	public void put(Event event) {
		events.create(UUID.randomUUID().toString(), event);		
	}

	public Map<String, Object> get(String recordId) {
		 ViewQuery viewQuery = new ViewQuery()
         .group(true)
         .groupLevel(1)
         .designDocId("_design/records")
         .viewName("latest");
		 
		ViewResult result = events.queryView(viewQuery);
        return new RecordBuilder(result.getRows().get(0)).build();
	}

}

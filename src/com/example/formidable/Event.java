package com.example.formidable;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;
import org.ektorp.support.CouchDbDocument;

public class Event extends CouchDbDocument {
	private static final long serialVersionUID = 1L;
	
	@JsonProperty
	private int epoch;
	
	@JsonProperty
	private String recordId;
	
	@JsonProperty
	private Map<String, Object> data = new HashMap<String, Object>();
	
	public Event(int epoch, String recordId) {
		this.epoch = epoch;
		this.recordId = recordId;
	}	
	
	public String getEpoch() {
		return recordId;
	}
	
	public String getRecordId() {
		return recordId;
	}

	public String get(String key) {
		return (String) data.get(key);
	}	
	public void put(String key, String value) {
		data.put(key, value);
	}
}

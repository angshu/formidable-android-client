package com.example.formidable;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;
import org.ektorp.support.CouchDbDocument;

public class Event extends CouchDbDocument {
	private static final long serialVersionUID = 1L;
	
	@JsonProperty
	private Map<String, Object> data = new HashMap<String, Object>();
	
	public String get(String key) {
		return (String) data.get(key);
	}	
	public void put(String key, String value) {
		data.put(key, value);
	}
}

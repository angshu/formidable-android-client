package com.example.formidable;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;
import org.ektorp.support.CouchDbDocument;

public class Event extends CouchDbDocument implements Comparable<Event> {
	private static final long serialVersionUID = 1L;
	
	@JsonProperty
	private int epoch;
	
	@JsonProperty
	private String recordId;
	
	@JsonProperty
	private Map<String, String> data = new HashMap<String, String>();
	
	public Event(int epoch, String recordId, Map<String, String> data) {
		this.epoch = epoch;
		this.recordId = recordId;
        this.data.putAll(data);
	}	
	
	public int getEpoch() {
		return epoch;
	}
	
	public String getRecordId() {
		return recordId;
	}

    public Event appliedOnto(Event older) {
        Map<String, String> map = new HashMap<String, String>(older.data);
        map.putAll(this.data);
        return new Event(this.epoch, this.recordId, map);
    }

    @Override
    public int compareTo(Event another) {
        return this.epoch - another.epoch;
    }
}

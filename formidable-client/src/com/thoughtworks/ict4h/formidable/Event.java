package com.thoughtworks.ict4h.formidable;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.annotate.JsonProperty;
import org.ektorp.support.CouchDbDocument;

public class Event extends CouchDbDocument implements Comparable<Event> {
	private static final long serialVersionUID = 1L;
	
	@JsonProperty
	private long epoch;
	
	@JsonProperty
	private String recordId;
	
	
	@JsonProperty
	private Map<String, Object> data = new HashMap<String, Object>();
	
	public Event(long epoch, String recordId, Map<String, Object> data) {
		this.epoch = epoch;
		this.recordId = recordId;
        this.data.putAll(data);
	}
	
	public Event(long epoch, String recordId) {
		this.epoch = epoch;
		this.recordId = recordId;
	}
	
	public void addAttribute(String key, Object value) {
		this.data.put(key, value);
	}
	
	public long getEpoch() {
		return epoch;
	}
	
	public String getRecordId() {
		return recordId;
	}

    public Event appliedOnto(Event older) {
    	
    		if(!older.recordId.equals(this.recordId)) {
    			throw new IllegalArgumentException("Only events for the same record can be aggregated.");
    		}
    	
        Map<String, Object> merged = merge(older.data, this.data);
        return new Event(this.epoch, this.recordId, merged);
    }

    //@Override
    public int compareTo(Event another) {
    	Long thisEpoch = new Long(this.epoch);
    	Long thatEpoch = new Long(another.epoch);
    	return thisEpoch.compareTo(thatEpoch);
        //return this.epoch - another.epoch;
    }

	public Object get(String key) {
		return data.get(key);
	}
	
	public static Event createSimpleEvent(int epoch, String recordId,
			String... arguments) {
		Map<String, Object> map = new HashMap<String, Object>();
		for (String arg : arguments) {
			String[] parts = arg.split(":");
			map.put(parts[0], parts[1]);
		}
		return new Event(epoch, recordId, map);
	}
	
	
	private Map<String, Object> merge(Map<String, Object> under,
			Map<String, Object> over) {
		
		if(under == null) return over;
		if(over == null) return under;
		
		Map<String, Object> result = new HashMap<String, Object>();
		overwrite(result, under);
		
		for(Map.Entry<String, Object> entry : over.entrySet()) {
			if(entry.getValue() instanceof Map) {
				Map<String, Object> nextLevel = merge((Map<String, Object>) under.get(entry.getKey()), (Map<String, Object>) over.get(entry.getKey()));
				overwrite(result, entry.getKey(), nextLevel);
			} else {
				overwrite(result, entry.getKey(), entry.getValue());
			}
		}
		return result;
	}

	private void overwrite(Map<String, Object> map, String key, Object value) {
		if(isEmpty(value)) {
			map.remove(key);
		} else {
			map.put(key, value);
		}
	}

	private boolean isEmpty(Object value) {
		return value == null || (value instanceof Map && ((Map) value).isEmpty());
	}
	
	private void overwrite(Map<String, Object> map, Map<String, Object> values) {	
		for(Entry<String, Object> entry : values.entrySet()) {
			overwrite(map, entry.getKey(), entry.getValue());
		}
	}
}

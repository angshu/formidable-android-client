package com.example.formidable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonNode;
import org.ektorp.ViewResult;

public class RecordBuilder {

	private ViewResult.Row row;

	public RecordBuilder(ViewResult.Row result) {
		this.row = result;
	}

	public Map<String, Object> build() {	    
        JsonNode data = row.getValueAsNode().get("data");
        return map(data);
	}
	
	private Map<String, Object> map(JsonNode node) {
  
        Map<String, Object> record = new HashMap<String, Object>();
        
        for(Iterator<Entry<String, JsonNode>> iter = node.getFields(); iter.hasNext(); ) {
        		Entry<String, JsonNode> next = iter.next();
        		if(isMap(next)) {
                record.put(next.getKey(), map(next.getValue()));
        		} else {
		    		record.put(next.getKey(), next.getValue().getTextValue());
        		}
        }
        
        return record;
	}

	private boolean isMap(Entry<String, JsonNode> next) {
		return next.getValue().getFields().hasNext();
	}

}

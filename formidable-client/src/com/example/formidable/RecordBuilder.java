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
        		if(next.getValue().getFields().hasNext()) {
                    Map<String, Object> map = map(next.getValue());
                    if (map != null && map.keySet().size() > 0) {
                        record.put(next.getKey(), map);
                    }
        		} else {
                    if (next.getValue().getTextValue() != null) {
        			    record.put(next.getKey(), next.getValue().getTextValue());
                    }
        		}
        }
        
        return record;
	}

}

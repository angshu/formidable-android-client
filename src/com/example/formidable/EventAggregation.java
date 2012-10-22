package com.example.formidable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.couchbase.touchdb.TDView;
import com.couchbase.touchdb.TDViewMapBlock;
import com.couchbase.touchdb.TDViewMapEmitBlock;
import com.couchbase.touchdb.TDViewReduceBlock;

public class EventAggregation {

	public void setMapReduceBlocksFor(TDView view) {
		view.setMapReduceBlocks(map(), reduce(), getId());	
	}
	
	private TDViewMapBlock map() {
		return new TDViewMapBlock() {
			@Override
			public void map(Map<String, Object> document, TDViewMapEmitBlock emitter) {
				emitter.emit(document.get("recordId"), document);			
			}			
		};
	}
	
	private TDViewReduceBlock reduce() {
		return new TDViewReduceBlock() {
			@Override
			public Object reduce(List<Object> keys, List<Object> values, boolean rereduce) {
				Map<String, Event> currentEvents = new HashMap<String, Event>();
				Map<String, Object> currentDocuments = new HashMap<String, Object>();
				
				for(Object document : values) {
					Event event = hydrateEvent((Map<String, Object>) document);
					
					if(!currentEvents.containsKey(event.getRecordId())
							|| event.isAfter(currentEvents.get(event.getRecordId()))) {
						currentEvents.put(event.getRecordId(), event);
						currentDocuments.put(event.getRecordId(), document);
					}
				}
				return currentDocuments;
			}
		};
	}

	private Event hydrateEvent(Map<String, Object> document) {
		Event event = new Event((Integer) document.get("epoch"), (String) document.get("recordId"));
		event.putAll((Map<String, Object>) document.get("data"));
		return event;
	}
	
	private String getId() {
		return UUID.randomUUID().toString();
	}
}

package com.example.formidable;

import java.util.*;

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
				Map<String, List<Event>> recordEventsMap = new HashMap<String, List<Event>>();
				
				for(Object document : values) {
                    Event event = hydrateEvent(document);
                    if (!recordEventsMap.containsKey(event.getRecordId())) {
                        recordEventsMap.put(event.getRecordId(), new ArrayList<Event>());
                    }

                    recordEventsMap.get(event.getRecordId()).add(event);
				}

                Map<String, Event> records = new HashMap<String, Event>();
                for (String recordId : recordEventsMap.keySet()) {
                    List<Event> eventList = recordEventsMap.get(recordId);
                    Collections.sort(eventList);

                    Event result = new Event(0, recordId, new HashMap<String, String>());
                    for(Event event : eventList) {
                        result = event.apply(result);
                    }

                    records.put(recordId, result);
                }

                return records;
			}

            private Event hydrateEvent(Object document) {
                Map<String, Object> documentMap = (Map<String, Object>) document;
                return new Event((Integer)documentMap.get("epoch"), (String)documentMap.get("recordId"),
                        (Map<String,String>)documentMap.get("data"));
            }
        };
	}
	
	private String getId() {
		return UUID.randomUUID().toString();
	}
}

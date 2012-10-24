package com.example.formidable;

import java.util.*;

import com.couchbase.touchdb.TDView;
import com.couchbase.touchdb.TDViewMapBlock;
import com.couchbase.touchdb.TDViewMapEmitBlock;
import com.couchbase.touchdb.TDViewReduceBlock;

public class CurrentState {

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
                Map<String, List<Event>> eventsGroupedByRecord = groupEvents(values);
                Map<String, Event> records = aggregateEvents(eventsGroupedByRecord);
                return records;
            }

            private Map<String, Event> aggregateEvents(Map<String, List<Event>> eventsGroupedByRecord) {
                Map<String, Event> records = new HashMap<String, Event>();
                for (String recordId : eventsGroupedByRecord.keySet()) {
                    List<Event> eventList = eventsGroupedByRecord.get(recordId);

                    Event result = replay(recordId, eventList);

                    records.put(recordId, result);
                }
                return records;
            }

            private Event replay(String recordId, List<Event> eventList) {
                return new EventAggregation(recordId, eventList).replay();
            }

            private Map<String, List<Event>> groupEvents(List<Object> values) {
                Map<String, List<Event>> recordEventsMap = new HashMap<String, List<Event>>();

                for (Object document : values) {
                    Event event = hydrateEvent(document);
                    if (!recordEventsMap.containsKey(event.getRecordId())) {
                        recordEventsMap.put(event.getRecordId(), new ArrayList<Event>());
                    }

                    recordEventsMap.get(event.getRecordId()).add(event);
                }
                return recordEventsMap;
            }

            private Event hydrateEvent(Object document) {
                Map<String, Object> documentMap = (Map<String, Object>) document;
                return new Event((Integer) documentMap.get("epoch"), (String) documentMap.get("recordId"),
                        (Map<String, String>) documentMap.get("data"));
            }
        };
    }

    private String getId() {
        return UUID.randomUUID().toString();
    }
}

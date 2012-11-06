package com.example.formidable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class EventAggregation {
	
    private List<Event> events;
    
    public EventAggregation(List<Event> orderedEvents) {
        this.events = orderedEvents;
        Collections.sort(events);
    }

    public Event replay() {    
		Event result = nullEvent(events.get(0).getRecordId());
		for (Event event : events) {
			result = event.appliedOnto(result);
		}
		return result;
    }

	private Event nullEvent(String recordId) {
		return new Event(0, recordId, new HashMap<String, Object>());
	}
}

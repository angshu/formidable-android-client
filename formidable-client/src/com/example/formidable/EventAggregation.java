package com.example.formidable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class EventAggregation {
	
    private List<Event> events;
    private Event nullEvent =  new Event(0, null, new HashMap<String, Object>());
    
    public EventAggregation(List<Event> events) {
        this.events = events;
        Collections.sort(this.events);
    }

    public Event replay() {    
		Event result = nullEvent;
		for (Event event : events) {
			result = event.appliedOnto(result);
		}
		return result;
    }
}

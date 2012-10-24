package com.example.formidable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class EventAggregation {
    private String recordId;
    private List<Event> events;

    public EventAggregation(String recordId, List<Event> events) {
        this.recordId = recordId;
        this.events = events;
    }

    public Event replay() {
        Collections.sort(events);
        Event result = new Event(0, recordId, new HashMap<String, String>());
        for (Event event : events) {
            result = event.apply(result);
        }
        return result;
    }
}

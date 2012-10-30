package com.example.formidable;

import org.codehaus.jackson.JsonNode;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FormidableTest extends FormidableTestCase {

    public void testReadingView() {
        String recordId = newId();
        createEvent(3, recordId, "surname", "Bhuwalka");
        createEvent(2, recordId, "name", "Angshu");
        createEvent(1, recordId, "name", "Chris");

        ViewQuery viewQuery = new ViewQuery()
                .group(true)
                .groupLevel(1)
                .designDocId("_design/records")
                .viewName("latest");

        Map<String, String> patient = getOnlyResult(viewQuery);
        assertEquals("Angshu", patient.get("name"));

        createEvent(4, recordId, "name", "Vivek");

        patient = getOnlyResult(viewQuery);
        assertEquals("Vivek", patient.get("name"));
    }

    private Map<String, String> getOnlyResult(ViewQuery viewQuery ) {
    	
        ViewResult result = events.queryView(viewQuery);

        ViewResult.Row record = result.getRows().get(0);     
        JsonNode data = record.getValueAsNode().get("data");
        JsonNode surname = data.get("surname");
        JsonNode name = data.get("name");
        
        Map<String, String> patient = new HashMap<String, String>();
        patient.put("name", name.getTextValue());
        patient.put("surname", surname.getTextValue());
  
        return patient;
    }

    private void createEvent(int epoch, String recordId, String key, String value) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(key, value);
        Event event = new Event(epoch, recordId, map);
        events.create(newId(), event);
    }

    private String newId() {
        return UUID.randomUUID().toString();
    }

}

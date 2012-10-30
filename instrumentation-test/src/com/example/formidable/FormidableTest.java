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

        printResults(viewQuery);

        createEvent(4, recordId, "name", "Vivek");
        createEvent(5, recordId, "age", "25");

        printResults(viewQuery);
    }

    private void printResults(ViewQuery viewQuery ) {
    	
        ViewResult result = events.queryView(viewQuery);

        for(ViewResult.Row record : result.getRows()) {
            JsonNode data = record.getValueAsNode().get("data");
            JsonNode surname = data.get("surname");
            JsonNode name = data.get("name");
            System.out.println(String.format("Name: %s %s", name.getTextValue(), surname.getTextValue()));
        }
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

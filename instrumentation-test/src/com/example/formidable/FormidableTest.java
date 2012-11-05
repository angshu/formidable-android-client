package com.example.formidable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FormidableTest extends FormidableTestCase {

    public void testReadingView() {
    	
    		RecordRepository repository = new RecordRepository(super.events);
    		
        String recordId = newId();
        repository.put(createEvent(6, recordId, "surname", "Bhuwalka"));
        repository.put(createEvent(2, recordId, "name", "Angshu"));
        repository.put(createEvent(1, recordId, "name", "Chris"));

        Map<String, Object> patient = repository.get(recordId);
        assertEquals("Angshu", patient.get("name"));

        repository.put(createEvent(4, recordId, "name", "Vivek"));
        repository.put(createEvent(5, recordId, "surname", "Singh"));
        
        Map<String, Object> skills = new HashMap<String, Object>();
        skills.put("abac339", "computer hacking");
		repository.put(createEvent(8, recordId, "skills", skills ));
		
        Map<String, Object> skills2 = new HashMap<String, Object>();
        skills2.put("34ac333", "nunchuku");
        repository.put(createEvent(7, recordId, "skills", skills2));

        patient = repository.get(recordId);
        assertEquals("Vivek", patient.get("name"));
        assertEquals("Bhuwalka", patient.get("surname"));
        assertTrue(((Map<String, Object>) patient.get("skills"))
        		.containsValue("nunchuku"));
        assertTrue(((Map<String, Object>) patient.get("skills"))
        		.containsValue("computer hacking"));
    }

    private Event createEvent(int epoch, String recordId, String key, Object value) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(key, value);
        return new Event(epoch, recordId, map);
    }

    private String newId() {
        return UUID.randomUUID().toString();
    }

}

package com.example.formidable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class EventTest extends TestCase {
   
   public void testThatAppliedEventDeterminesMetadata() {	   
	   Event older = new Event(1, "abc", Collections.<String, Object> emptyMap());
	   Event newer = new Event(2, "abc", Collections.<String, Object> emptyMap());
	   
	   Event result = newer.appliedOnto(older);
	   
	   assertEquals(2, result.getEpoch());
	   assertEquals("abc", result.getRecordId());
   }
   
   public void testPartialOverride() {  
	   Map<String, Object> olds = new HashMap<String, Object>();
	   olds.put("name", "Angshu");
	   olds.put("surname", "Sarkar");
	   Event older = new Event(1, "", olds);
	   
	   Map<String, Object> news = new HashMap<String, Object>();
	   news.put("name", "Pulkit");
	   Event newer = new Event(2, "", news);
	   
	   Event result = newer.appliedOnto(older);
	   
	   assertEquals("Pulkit", result.get("name"));
	   assertEquals("Sarkar", result.get("surname"));
   }
   
   public void testNestedMerging() {
	   Map<String, Object> olds = new HashMap<String, Object>();
	   olds.put("skills", new HashMap<String, String>());
	   ((Map<String, Object>) olds.get("skills")).put("martial", "nunchuku");
	   Event older = new Event(1, "", olds);
	   
	   Map<String, Object> news = new HashMap<String, Object>();
	   news.put("skills", new HashMap<String, String>());
	   ((Map<String, Object>) news.get("skills")).put("IT", "computer hacking");
	   Event newer = new Event(2, "", news);
	     
	   Map<String, String> skills = (Map<String, String>) newer.appliedOnto(older).get("skills");
	   
	   assertEquals("nunchuku", skills.get("martial"));
	   assertEquals("computer hacking", skills.get("IT"));
   }
   
   public void testThatNullsAreDeletions() {
	   Map<String, Object> olds = new HashMap<String, Object>();
	   olds.put("skills", new HashMap<String, String>());
	   ((Map<String, Object>) olds.get("skills")).put("martial", "nunchuku");
	   ((Map<String, Object>) olds.get("skills")).put("IT", "computer hacking");
	   Event older = new Event(1, "", olds); 
	   
	   Map<String, Object> news = new HashMap<String, Object>();
	   news.put("skills", new HashMap<String, String>());
	   ((Map<String, Object>) news.get("skills")).put("martial", null);	   
	   Event newer = new Event(2, "", news);
	     
	   Map<String, String> skills = (Map<String, String>) newer.appliedOnto(older).get("skills");
	   
	   assertFalse(skills.containsKey("martial"));
	   assertEquals("computer hacking", skills.get("IT"));
   }
   
   public void testThatEmptyMapsAreRemoved() {
	   Map<String, Object> olds = new HashMap<String, Object>();
	   olds.put("skills", new HashMap<String, String>());
	   ((Map<String, Object>) olds.get("skills")).put("martial", "nunchuku");
	   Event older = new Event(1, "", olds); 
	   
	   Map<String, Object> news = new HashMap<String, Object>();
	   news.put("skills", new HashMap<String, String>());
	   ((Map<String, Object>) news.get("skills")).put("martial", null);	   
	   Event newer = new Event(2, "", news);
	     
	   assertNull(newer.appliedOnto(older).get("skills"));
   }
}

package com.example.formidable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class EventTest extends TestCase {
   
   public void testAppliedEventDeterminesMetadata() {	   
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
	   Map<String, Object> angshu = new HashMap<String, Object>();
	   angshu.put("skills", new HashMap<String, String>());
	   ((Map<String, Object>) angshu.get("skills")).put("martial", "nunchuku");
	   Event a = new Event(1, "", angshu);
	   
	   Map<String, Object> pulkit = new HashMap<String, Object>();
	   pulkit.put("skills", new HashMap<String, String>());
	   ((Map<String, Object>) pulkit.get("skills")).put("IT", "computer hacking");
	   Event p = new Event(2, "", pulkit);
	     
	   Map<String, String> skills = (Map<String, String>) p.appliedOnto(a).get("skills");
	   
	   assertEquals("nunchuku", skills.get("martial"));
	   assertEquals("computer hacking", skills.get("IT"));
   }
}

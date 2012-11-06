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
   
   public void testEventsMerge() {
	   Map<String, Object> olds = new HashMap();
	   olds.put("foo", "bar");
	   
	   Map<String, Object> news = new HashMap();
	   olds.put("foo", "baz");
	   
	   Event older = new Event(1, "abc", olds);
	   Event newer = new Event(2, "abc", news);
	   Event result = newer.appliedOnto(older);
	   
	   assertEquals("baz", result.get("foo"));
   }
}

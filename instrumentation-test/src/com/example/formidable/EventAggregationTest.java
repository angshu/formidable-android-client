package com.example.formidable;

import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;

public class EventAggregationTest extends TestCase {
   
   public void testEventsAreAppliedInEpochOrder() {
	   	   
	   Event older = new Event(1, "", Collections.<String, Object> emptyMap());
	   Event newest = new Event(3, "", Collections.<String, Object> emptyMap());
	   Event newer = new Event(2, "", Collections.<String, Object> emptyMap());

	   EventAggregation aggregation = new EventAggregation(Arrays.asList(older, newest, newer));
	   
	   assertEquals(3, aggregation.replay().getEpoch());
   }
}

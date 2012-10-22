package com.example.formidable;

import java.util.List;
import java.util.Map;

import com.couchbase.touchdb.TDViewReduceBlock;

final class EventReduce implements TDViewReduceBlock {
		@Override
		public Object reduce(List<Object> keys, List<Object> values, boolean rereduce) {
			Map<String, Object> mostRecent = null;
			int mostRecentEpoch = 0;
			
			for(Object next : values) {
				Map<String, Object> nextMap = (Map<String, Object>) next;
				
				try {
					int nextEpoch = (Integer) nextMap.get("epoch");
					if(nextEpoch > mostRecentEpoch) {
						mostRecentEpoch = nextEpoch;
						mostRecent = nextMap;
					}
				} catch(Exception e) {}
			}
			return mostRecent;
		}
	}
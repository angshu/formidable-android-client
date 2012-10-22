package com.example.formidable;

import java.util.List;

import com.couchbase.touchdb.TDViewReduceBlock;

final class EventReduce implements TDViewReduceBlock {
		@Override
		public Object reduce(List<Object> keys, List<Object> values, boolean rereduce) {
			return values.get(values.size() - 1);
//					Map<String, Object> map = (Map<String, Object>) values.get(values.size() - 1);
//					for(String foo : map.keySet()) {
//						Object value = map.get(foo);
//						System.out.println("value="+value);
//					}
//					return values.get(values.size() - 1);
			
			
		}
	}
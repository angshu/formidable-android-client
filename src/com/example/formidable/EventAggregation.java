package com.example.formidable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.couchbase.touchdb.TDView;
import com.couchbase.touchdb.TDViewMapBlock;
import com.couchbase.touchdb.TDViewMapEmitBlock;
import com.couchbase.touchdb.TDViewReduceBlock;

public class EventAggregation {

	public void setMapReduceBlocksFor(TDView view) {
		view.setMapReduceBlocks(map(), reduce(), getId());	
	}
	
	private TDViewMapBlock map() {
		return new TDViewMapBlock() {
			@Override
			public void map(Map<String, Object> document, TDViewMapEmitBlock emitter) {
				emitter.emit(document.get("recordId"), document);			
			}			
		};
	}
	
	private TDViewReduceBlock reduce() {
		return new TDViewReduceBlock() {
			@Override
			public Object reduce(List<Object> keys, List<Object> values, boolean rereduce) {
				Map<String, Object> records = new HashMap<String, Object>();
				
				for(Object document : values) {
					Map<String, Object> candidate = (Map<String, Object>) document;
					Map<String, Object> current = (Map<String, Object>) records.get((String) candidate.get("recordId"));	
					records.put((String) candidate.get("recordId"), merge(current, candidate));
				}
				return records;
			}

			private Map<String, Object> merge(
						Map<String, Object> current,
						Map<String, Object> candidate) {
				if(current == null) return candidate;
				
				Map<String, Object> currentData = (Map<String, Object>) current.get("data");
				Map<String, Object> candidateData = (Map<String, Object>) candidate.get("data");
				
				if(isBefore(current, candidate)) {
					currentData.putAll(candidateData);
					candidate.put("data", currentData);
					return candidate;
				} else {
					candidateData.putAll(currentData);
					current.put("data", candidateData);
					return current;
				}
			}

			private boolean isBefore(
					Map<String, Object> a,
					Map<String, Object> b) {
				return (Integer) a.get("epoch") < (Integer) b.get("epoch");
			}
		};
	}
	
	private String getId() {
		return UUID.randomUUID().toString();
	}
}

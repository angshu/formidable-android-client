package com.example.formidable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.couchbase.touchdb.TDView;
import com.couchbase.touchdb.TDViewMapBlock;
import com.couchbase.touchdb.TDViewMapEmitBlock;
import com.couchbase.touchdb.TDViewReduceBlock;

public class EventAggregation {

	public void setMapReduceBlocksFor(TDView view) {
		view.setMapReduceBlocks(mapper(), reducer(), getId());	
	}
	
	private TDViewReduceBlock reducer() {
		return new TDViewReduceBlock() {
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
		};
	}

	private TDViewMapBlock mapper() {
		return new TDViewMapBlock() {
			@Override
			public void map(Map<String, Object> document, TDViewMapEmitBlock emitter) {
				emitter.emit(document.get("recordId"), document);			
			}			
		};
	}

	private String getId() {
		return UUID.randomUUID().toString();
	}
}

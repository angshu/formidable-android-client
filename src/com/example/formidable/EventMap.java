package com.example.formidable;

import java.util.Map;

import com.couchbase.touchdb.TDViewMapBlock;
import com.couchbase.touchdb.TDViewMapEmitBlock;

public class EventMap implements TDViewMapBlock {
	@Override
	public void map(Map<String, Object> document, TDViewMapEmitBlock emitter) {
		Object recordId = document.get("recordId");
		if(recordId != null) {
			emitter.emit(recordId, document);
		}
	}
}
package com.example.formidable;

import java.util.HashMap;
import java.util.Map;

public class MapMerger {

	public Map<String, String> of(Map<String, String> under,
			Map<String, String> over) {
		Map<String, String> result = new HashMap<String, String>();
		result.putAll(under);
		result.putAll(over);
		return result;
	}

}

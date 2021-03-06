package com.thoughtworks.ict4h.formidable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.couchbase.touchdb.lucene.TDLucene.Callback;

public abstract class SearchHandler extends Callback {
	@Override
	public void onSucess(Object response) {
		if (response instanceof JSONObject) {
			System.out.println("Lucene response:" + response.toString());
			JSONObject results = ((JSONObject) response);
			try {
				int noOfRecords = results.getInt("total_rows");
				JSONObject[] records = new JSONObject[noOfRecords];
				if (noOfRecords > 0) {
					JSONArray rows = results.getJSONArray("rows");
					for (int i = 0; i < noOfRecords; i++) {
						records[i] = rows.getJSONObject(i);
					}
				}
				realize(records);
			} catch (JSONException e) {
				e.printStackTrace();
				fault(e);
			}
		} else {
			fault("Can not process search response. Response is not a JSONObject!");
		}
	}

	@Override
	public void onError(Object resp) {
		fault(resp);
	}
	
	abstract void realize(JSONObject[] results);
	abstract void fault(Object resp);
}
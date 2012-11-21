package com.example.formidable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.couchbase.touchdb.lucene.TDLucene.Callback;

public abstract class SearchCallback extends Callback {
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
				onSearchSuccess(records);
			} catch (JSONException e) {
				e.printStackTrace();
				onSearchError(e);
			}
		} else {
			onSearchError("Can not process search response. Response is not a JSONObject!");
		}
	}

	@Override
	public void onError(Object resp) {
		onSearchError(resp);
	}
	
	abstract void onSearchSuccess(JSONObject[] results);
	abstract void onSearchError(Object resp);
}
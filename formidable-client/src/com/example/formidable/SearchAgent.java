package com.example.formidable;

import java.io.IOException;
import java.net.MalformedURLException;

import org.ektorp.CouchDbConnector;
import org.ektorp.http.HttpClient;
import org.ektorp.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.couchbase.touchdb.TDServer;
import com.couchbase.touchdb.lucene.TDLucene;
import com.couchbase.touchdb.lucene.TDLucene.Callback;
import com.couchbase.touchdb.lucene.TDLuceneRequest;

public class SearchAgent {
	public TDServer localServer;
	public CouchDbConnector events;
	private TDLucene lucene;
	

	public SearchAgent(HttpClient client, TDServer localServer) {
		this.localServer = localServer;
		createSearchIndexer(client);
	}
	

    private void createSearchIndexer(HttpClient client) {
    	String idxFn = Configuration.getIndexerFuncDescr();
    	HttpResponse response = client.put("/events/_design/records", idxFn);
		System.out.println(String.format("SearchAgent.createSearchIndexer => succeeded: %b; http code: %d", response.isSuccessful(), response.getCode()));
	}


	public void triggerSearch(String criteria) {
		try {
			if (lucene == null) {
				lucene = new TDLucene(localServer);
			}
			TDLuceneRequest req = new TDLuceneRequest();
			req.setUrl("/local/events/_design/records/byContent")
					.addParam("q", criteria)
					.addParam("include_docs", "true")
					.addParam("highlights", "5");

			lucene.fetch(req, new Callback() {
				@Override
				public void onSucess(Object response) {
					if (response instanceof JSONObject) {
						System.out.println("Lucene response:" + response.toString());
						JSONObject results = ((JSONObject) response);
						try {
							int noOfRecords = results.getInt("total_rows");
							if (noOfRecords > 0) {
								JSONArray rows = results.getJSONArray("rows");
								JSONObject[] records = new JSONObject[noOfRecords];
								for (int i = 0; i < noOfRecords; i++) {
									records[i] = rows.getJSONObject(i);
									System.out.println("search result record => %s".format(records[i].toString()));
									//we should probably create something Event.parse(JSOBObject) to get a Event object out of json
									//and return an array of Event Objects
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}

				@Override
				public void onError(Object resp) {
					System.out.println("SearchAgent.triggerSearch => Error : " + resp.toString());
				}
			});

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }	
    
    
}
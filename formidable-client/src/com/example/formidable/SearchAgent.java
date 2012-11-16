package com.example.formidable;

import java.io.IOException;
import java.net.MalformedURLException;

import org.ektorp.CouchDbConnector;
import org.ektorp.http.HttpClient;
import org.ektorp.http.HttpResponse;
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
    	String idxFn = Messages.getString("search.indexerFunction");
    	HttpResponse response = client.put("/events/_design/records", idxFn);
		System.out.println(String.format("SearchAgent.createSearchIndexer => succeeded: %b; http code: %d", response.isSuccessful(), response.getCode()));
	}


	public void triggerSearch(String criteria) {
		try {
			if (lucene == null) {
				lucene = new TDLucene(localServer);
			}
			TDLuceneRequest req = new TDLuceneRequest();
			req.setUrl("/local/events/_design/records/byName")
					.addParam("q", "chris*")
					.addParam("include_docs", "true")
					.addParam("highlights", "5");

			lucene.fetch(req, new Callback() {
				@Override
				public void onSucess(Object resp) {
					if (resp instanceof JSONObject) {
						System.out.println("Lucene response:" + resp.toString());
//						try {
//							JSONArray rows = ((JSONObject) resp).getJSONArray("rows");
//							if (rows.length() > 0) {
//								JSONArray jsonArray = rows.getJSONArray(0);
//								System.out.println("json array" + jsonArray);
//							}
//							else {
//								System.out.println("***********************");
//								System.out.println("Didn't find any records." + resp.toString());
//								System.out.println("***********************");
//							}
//						} catch (JSONException e) {
//							e.printStackTrace();
//						}
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
package com.thoughtworks.ict4h.formidable;

import java.io.IOException;
import java.net.MalformedURLException;

import org.ektorp.CouchDbConnector;
import org.ektorp.http.HttpClient;
import org.ektorp.http.HttpResponse;
import org.json.JSONException;

import com.couchbase.touchdb.TDServer;
import com.couchbase.touchdb.lucene.TDLucene;
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


	public void triggerSearch(String criteria, SearchHandler callback) {
		try {
			if (lucene == null) {
				lucene = new TDLucene(localServer);
			}
			TDLuceneRequest req = new TDLuceneRequest();
			req.setUrl("/local/events/_design/records/byContent")
					.addParam("q", criteria)
					.addParam("include_docs", "true")
					.addParam("highlights", "5");

			lucene.fetch(req, callback);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }	
    
    
}
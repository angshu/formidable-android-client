package com.example.formidable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

import org.ektorp.CouchDbConnector;
import org.ektorp.http.HttpClient;
import org.ektorp.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import com.couchbase.touchdb.TDServer;
import com.couchbase.touchdb.lucene.TDLucene;
import com.couchbase.touchdb.lucene.TDLuceneRequest;
import com.couchbase.touchdb.lucene.TDLucene.Callback;

public class SearchAgent {
	public TDServer localServer;
	public CouchDbConnector events;
	private TDLucene lucene;
	

	public SearchAgent(HttpClient client, TDServer localServer) {
		this.localServer = localServer;
		createSearchIndexer(client);
	}
	

    private void createSearchIndexer(HttpClient client) {
		HttpResponse response = client.put("/events/_design/records", "{\"fulltext\": " +
				"{\"byName\": { " + //\"analyzer\":\"NGRAM\"," +
				 "\"index\": \"function(doc) { " +
				 				"if (typeof doc.data != undefined) { "  +
				 				//"if (1==1)  { " +
				 					"var ret=new Document(); ret.add(doc.data.name); " +
				 					"return ret; " +
				 				//"} "  +	
			 					"} else return null; " +
			 				  "}\" " +
				 "}}}");
				 //"\"index\": \"function(doc) { var ret=new Document(); ret.add(doc.epoch); return ret; }\" }}}");
				
		System.out.println(String.format("******** PUT Search Indexer response, success: %b; code: %d", response.isSuccessful(), response.getCode()));
		InputStream content = response.getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(content), 500);
		String line = null;
		try {
			while((line = reader.readLine()) != null) {
				  System.out.println("******** Search Indexer creation response: " + line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
					System.out.println("****** Error : " + resp.toString());
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
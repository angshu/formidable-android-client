package com.thoughtworks.ict4h.formidable.bridge;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.thoughtworks.ict4h.formidable.Event;
import com.thoughtworks.ict4h.formidable.RecordRepository;

public class OdkSurveyBridge {
	private RepositoryLocator locator;
	
	public interface RepositoryLocator {
		RecordRepository getRepository();
	}
	
	public OdkSurveyBridge(RepositoryLocator locator) {
		this.locator = locator;
	}

	public void addEvent(String event) {
		try {
			JSONObject eventJson = new JSONObject(event);

			String formId = eventJson.optString("form_id");
			String currentInstanceId = eventJson.optString("currentInstanceId");
			boolean isComplete = eventJson.optBoolean("asComplete");

			System.out
					.println(String
							.format("Collect form submission of formId: %s, instanceId: %s, final:%b",
									formId, currentInstanceId, isComplete));

			JSONObject modelJson = eventJson.optJSONObject("model");
			JSONObject dataJson = eventJson.optJSONObject("data");
			JSONObject metadataJson = eventJson.optJSONObject("metadata");

			JSONArray attributeNames = modelJson.names();
			// merge the model and data
			for (int i = 0; i < attributeNames.length(); i++) {
				String attrName = (String) attributeNames.get(i);
				JSONObject attribute = modelJson.getJSONObject(attrName);

				Object attrData = dataJson.opt(attrName);
				if (attrData != null) {
					// we can get actual typed values if required
					attribute.put("value", attrData);
				}
			}

			Map<String, Object> formData = new HashMap<String, Object>();
			for (int i = 0; i < attributeNames.length(); i++) {
				String attrName = (String) attributeNames.get(i);
				JSONObject attribute = modelJson.getJSONObject(attrName);
				System.out.println(String.format("****** field %s = %s",
						attrName, attribute));
				String attrType = attribute.optString("type");
				if (attrType.equals("array")) {
					formData.put(attrName, attribute.optString("value"));
				} else {
					formData.put(attrName, attribute.opt("value"));
				}
			}
			
			formData.put("instanceName", metadataJson.optString("instanceName"));
			long epoch = (new Date()).getTime();
			Event formEvent = new Event(epoch, formId, formData);
			RecordRepository recordRepo = locator.getRepository();
			if (recordRepo != null) {
				Log.i("ODKSurveyBridge", "****** adding event *********");
				recordRepo.put(formEvent);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

package com.example.formidable;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Configuration {
	private static final String BUNDLE_NAME = "com.example.formidable.config"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private Configuration() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	public static String getServerURL() {
		return getString("FormidableActivity.serverURL");
	}

	public static String getDatabaseName() {
		return "events";
	}

	public static String getGeolocDbPath() {
		return "/data/data/com.example.formidable/databases";
	}

	public static String getIndexerFuncDescr() {
		return getString("search.indexerFunction");
	}
}

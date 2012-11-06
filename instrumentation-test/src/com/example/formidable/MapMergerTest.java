package com.example.formidable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.ektorp.util.Assert;

public class MapMergerTest extends FormidableTestCase {

   public void testNullMapsMergeSafely() {
	   MapMerger merger = new MapMerger();
	   Map<String, Object> angshu = new HashMap<String, Object>();
	   angshu.put("name", "Angshu");
	   
	   Map<String, Object> record = merger.merge(null, angshu);
	   Assert.isTrue("Angshu".equals((String) record.get("name")));
   }
   
   public void testPartialOverride() {
	   MapMerger merger = new MapMerger();
	   Map<String, Object> angshu = new HashMap<String, Object>();
	   angshu.put("name", "Angshu");
	   angshu.put("surname", "Sarkar");
	   
	   Map<String, Object> pulkit = new HashMap<String, Object>();
	   angshu.put("name", "Pulkit");
	   
	   Map<String, Object> record = merger.merge(angshu, pulkit);
	   
	   Assert.isTrue("Pulkit".equals((String) record.get("name")));
	   Assert.isTrue("Sarkar".equals((String) record.get("surname")));
   }
   
   public void testAppendingToASet() {
	   MapMerger merger = new MapMerger();
	   
	   Map<String, Object> angshu = new HashMap<String, Object>();
	   angshu.put("skills", new HashMap<String, String>());
	   ((Map<String, Object>) angshu.get("skills")).put(UUID.randomUUID().toString(), "nunchuku");
	   
	   Map<String, Object> pulkit = new HashMap<String, Object>();
	   pulkit.put("skills", new HashMap<String, String>());
	   ((Map<String, Object>) pulkit.get("skills")).put(UUID.randomUUID().toString(), "computer hacking");
	   
	   Map<String, Object> merged = merger.merge(angshu, pulkit);	   
	   Map<String, String> skills = (Map<String, String>) merged.get("skills");
	   
	   Assert.isTrue(skills.containsValue("nunchuku"));
	   Assert.isTrue(skills.containsValue("computer hacking"));
   }
}

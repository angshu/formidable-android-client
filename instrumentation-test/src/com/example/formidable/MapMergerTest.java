package com.example.formidable;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

public class MapMergerTest extends TestCase {

   public void testNullMapsMergeSafely() {
	   MapMerger merger = new MapMerger();
	   
	   Map<String, Object> angshu = new HashMap<String, Object>();
	   angshu.put("name", "Angshu");
	   
	   Map<String, Object> record = merger.merge(null, angshu);
	   
	   assertEquals("Angshu", record.get("name"));
   }
   
   public void testPartialOverride() {
	   MapMerger merger = new MapMerger();
	   
	   Map<String, Object> angshu = new HashMap<String, Object>();
	   angshu.put("name", "Angshu");
	   angshu.put("surname", "Sarkar");
	   
	   Map<String, Object> pulkit = new HashMap<String, Object>();
	   pulkit.put("name", "Pulkit");
	   
	   Map<String, Object> record = merger.merge(angshu, pulkit);
	   
	   assertEquals("Pulkit", record.get("name"));
	   assertEquals("Sarkar", record.get("surname"));
   }
   
   public void testNestedMerging() {
	   MapMerger merger = new MapMerger();
	   
	   Map<String, Object> angshu = new HashMap<String, Object>();
	   angshu.put("skills", new HashMap<String, String>());
	   ((Map<String, Object>) angshu.get("skills")).put("martial", "nunchuku");
	   
	   Map<String, Object> pulkit = new HashMap<String, Object>();
	   pulkit.put("skills", new HashMap<String, String>());
	   ((Map<String, Object>) pulkit.get("skills")).put("IT", "computer hacking");
	   
	   Map<String, Object> record = merger.merge(angshu, pulkit);	   
	   Map<String, String> skills = (Map<String, String>) record.get("skills");
	   
	   assertEquals("nunchuku", skills.get("martial"));
	   assertEquals("computer hacking", skills.get("IT"));
   }
}

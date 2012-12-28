'use strict';
// TODO: Instance level: locale (used), at Table level: locales (available), formPath, 
define(['mdl','opendatakit','jquery', 'dbImpl'], function(mdl,opendatakit,$, dbImpl) {
    return {
    	initializeTables:function(ctxt, formDef, table_id, formPath) {
		    var that = this;
		    var tlo = {data: {},  // dataTable instance data values
		        metadata: {}, // dataTable instance Metadata: (instanceName, locale)
		        tableMetadata: {}, // table_definitions and key_value_store_active values for ("table", "global") of: table_id, tableKey, dbTableName
		        columnMetadata: {},// column_definitions and key_value_store_active values for ("column", elementKey) of: none...
		        dataTableModel: {},// inverted and extended formDef.model for representing data store
		        formDef: formDef, 
		        formPath: formPath, 
		        instanceId: null, 
		        table_id: table_id
		        };
		                            
		    ctxt.append('initializeTables');
		    var tmpctxt = $.extend({},ctxt,{success:function() {
		                ctxt.append('getAllTableMetaData.success');
		                // these values come from the current webpage
		                // update table_id and qp
		                mdl.formDef = tlo.formDef;
		                mdl.tableMetadata = tlo.tableMetadata;
		                mdl.columnMetadata = tlo.columnMetadata;
		                mdl.data = tlo.data;
		                opendatakit.setCurrentTableId(table_id);
		                opendatakit.setCurrentFormPath(formPath);
		                ctxt.success();
		            }});
		    dbImpl.initializeSchema(ctxt, tmpctxt, table_id, tlo, mdl);
		},
		initializeInstance:function(ctxt, instanceId, instanceMetadataKeyValueMap) {
		    var that = this;
		    if ( instanceId == null ) {
		        ctxt.append('initializeInstance.noInstance');
		        mdl.metadata = {};
		        mdl.data = {};
		        opendatakit.setCurrentInstanceId(null);
		        ctxt.success();
		    } else {
		        ctxt.append('initializeInstance.access', instanceId);
		        var tmpctxt = $.extend({},ctxt,{success:function() {
		                that.cacheAllData(ctxt, instanceId);
		            }});
		        dbImpl.initializeInstance(tmpctxt, mdl.formDef, instanceId, mdl.dataTableModel, mdl.tableMetadata.dbTableName, instanceMetadataKeyValueMap);
		    }
		},
		get_all_instances:function(ctxt, subsurveyType) {
		      var that = this;
		      // TODO: support subforms. The subsurveyType is the form_id of the 
		      // subform. This should then be used to read its config, issue the 
		      // query against it's mdl.tableMetadata.dbTableName, etc.
		      var instanceList = [];
		      ctxt.append('get_all_instances', subsurveyType);
		      var tmpctxt = $.extend({},ctxt,{
		        success: function() {
		            ctxt.success(instanceList);
		        }});
		      dbImpl.getAllInstances(tmpctxt, instanceList, mdl);
		      
		},
		getInstanceMetaDataValue:function(name) {
		    var path = name.split('.');
		    var v = mdl.metadata;
		    for ( var i = 0 ; i < path.length ; ++i ) {
		        v = v[path[i]];
		        if ( v == null ) {
		            return v;
		        }
		    }
		    return v;
		},
		cacheAllData:function(ctxt, instanceId) {
		    this.getAllData($.extend({},ctxt,{success:function(tlo) {
		        ctxt.append("cacheAllData.success");
		        mdl.metadata = tlo.metadata;
		        mdl.data = tlo.data;
		        opendatakit.setCurrentInstanceId(instanceId);
		        ctxt.success();
		    }}), mdl.dataTableModel, mdl.tableMetadata.dbTableName, instanceId);
		}, 
		//private method?  called from cacheAllData
		getAllData:function(ctxt, dataTableModel, dbTableName, instanceId) {
		    var tlo = { data: {}, metadata: {}};
		    ctxt.append('getAllData');
		    var tmpctxt = $.extend({},ctxt,{success:function() {
		                ctxt.append("getAllData.success");
		                ctxt.success(tlo);
		        }});
		    dbImpl.getAllData(tmpctxt, dbTableName, instanceId, dataTableModel, tlo);
		},
		delete_all:function(ctxt, instanceId) {
	        var that = this;
	        ctxt.append('delete_all');
	        dbImpl.deleteAll(ctxt, instanceId, mdl.tableMetadata.dbTableName);
		},
		setInstanceMetaData:function(ctxt, name, value) {
		    ctxt.append('setInstanceMetaData: ' + name);
		    var that = this;
		    that.putInstanceMetaData($.extend({}, ctxt, {success: function() {
		                that.cacheAllData(ctxt, opendatakit.getCurrentInstanceId());
		            }}), name, value);
		},
		//private method? called from setInstanceMetaData
		putInstanceMetaData:function(ctxt, name, value) {
		      var that = this;
		      var kvMap = {};
		      var dbColumnName;
		      var f;
		      ctxt.append('putInstanceMetaData', 'name: ' + name);
		      for ( f in dbImpl.dataTablePredefinedColumns ) {
		        if ( dbImpl.dataTablePredefinedColumns[f].elementPath == name ) {
		            dbColumnName = f;
		            break;
		        }
		      }
		      if ( dbColumnName == null ) {
		        ctxt.append('putInstanceMetaData.elementPath.missing', 'name: ' + name);
		        ctxt.failure({message:"Unrecognized instance metadata"});
		        return;
		      }
		      // and still use the elementPath for the lookup.
		      // this simply ensures that the name is exported above 
		      // the database layer. 
		      // The database layer uses putDataKeyValueMap()
		      // for lower-level manipulations.
		      kvMap[name] = {value: value, isInstanceMetadata: true };
		      that.putDataKeyValueMap(ctxt, kvMap );
		},
		putDataKeyValueMap:function(ctxt, kvMap) {
		      var that = this;
		      var property;
		      var names = '';
		      for ( property in kvMap ) {
		        names += "," + property;
		      }
		      names = names.substring(1);
		      ctxt.append('database.putDataKeyValueMap.initiated', names );
		      dbImpl.insertKeyValueMapData(ctxt, opendatakit.getCurrentInstanceId(), kvMap, names, mdl);
		      
		},
		getDataValue:function(name) {
		    var path = name.split('.');
		    var v = mdl.data;
		    for ( var i = 0 ; i < path.length ; ++i ) {
		        v = v[path[i]];
		        if ( v == null ) {
		            return null;
		        }
		    }
		    return v;
		},
		getAllDataValues:function() {
		    return mdl.data;
		},
		save_all_changes:function(ctxt, asComplete) {
		      var that = this;
		    // TODO: if called from Java, ensure that all data on the current page is saved...
		      ctxt.append('save_all_changes');
		      var tmpctxt = $.extend({}, ctxt, {success:function() {
		                ctxt.append('save_all_changes.markCurrentStateSaved.success', 
		                opendatakit.getSettingValue('form_id') + " instanceId: " + opendatakit.getCurrentInstanceId() + " asComplete: " + asComplete);
		                ctxt.success();
		            }});
		      dbImpl.saveAllChanges(tmpctxt, opendatakit.getCurrentInstanceId(), asComplete, mdl);
		}
    }
});
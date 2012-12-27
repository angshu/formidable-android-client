'use strict';
// TODO: Instance level: locale (used), at Table level: locales (available), formPath, formId, formVersion, 
// depends upon: opendatakit 
define(['mdl','opendatakit','jquery', 'dbImpl'], function(mdl,opendatakit,$, dbImpl) {
    return {
    	mdl:mdl,
    	initializeTables:function(ctxt, formDef, tableId, protoTableMetadata, formPath) {
		    var that = this;
		    ctxt.append('initializeTables');
		    dbImpl.initializeMetaData(ctxt, formDef, tableId, protoTableMetadata, formPath,mdl);
		    
		},
		getTableMetaDataValue:function(name) {
		    var path = name.split('.');
		    var v = mdl.qp;
		    for ( var i = 0 ; i < path.length ; ++i ) {
		        v = v[path[i]];
		        if ( v == null ) return v;
		    }
		    return v.value;
		},
		initializeInstance:function(ctxt, instanceId, instanceMetadataKeyValueList) {
		    var that = this;
		    if ( instanceId == null ) {
		        ctxt.append('initializeInstance.noInstance');
		        mdl.metadata = {};
		        mdl.data = {};
		        opendatakit.setCurrentInstanceId(null);
		        ctxt.success();
		    } else {
		        ctxt.append('initializeInstance.access', instanceId);
		        dbImpl.insertIntoNewDbTable( 
		        	$.extend({},ctxt, {success:function() { that._cacheAllData(ctxt, instanceId); }}),
		        	instanceId, mdl, instanceMetadataKeyValueList 
		        );
		    }
		},
		_cacheAllData:function(ctxt, instanceId) {
			//[NOTE] this method should be private? 
		    var that = this;
		    this.getAllData($.extend({},ctxt,{success:function(tlo) {
		        ctxt.append("_cacheAllData.success");
		        mdl.metadata = tlo.metadata;
		        mdl.data = tlo.data;
		        opendatakit.setCurrentInstanceId(instanceId);
		        ctxt.success();
		    }}), instanceId);
		},
		getInstanceMetaDataValue:function(name) {
		    var path = name.split('.');
		    var v = mdl.metadata;
		    for ( var i = 0 ; i < path.length ; ++i ) {
		        v = v[path[i]];
		        if ( v == null ) return v;
		    }
		    return v.value;
		},
		withDb:function(ctxt, transactionBody) {
			//delegate to dbimpl
			dbImpl.withDb(ctxt, transactionBody);
		}, 
		getAllFormInstancesStmt:function() {
			//delegate to dbImpl
			return dbImpl.getAllFormInstancesStmt(mdl);
		},
		getAllData:function(ctxt, instanceId) {
	      	var that = this;
	      	var tlo = { data: {}, metadata: {}};
	      	ctxt.append('getAllData');
	      	dbImpl.getAllData(
	      		$.extend({},ctxt,{success:function() {
	                ctxt.append("getAllData.success");
	                ctxt.success(tlo)}}),
	      		instanceId, mdl,
	      		function(result, dbTableMetadata) {
  					var len = result.rows.length;
		            if (len == 0 ) {
		                alert("no record for getAllData!");
		            } else if (len != 1 ) {
		                alert("not exactly one record in getAllData!");
		            } else {
		                var row = result.rows.item(0);
		                var model = mdl.model;
		                for (var i = 0 ; i < dbTableMetadata.length ; ++i) {
		                    var f = dbTableMetadata[i];
		                    var dbKey = f.key;
		                    var dbValue = row[dbKey];
		                    var dbType = f.type;
		    
		                    var elem = {};
		                    elem['type'] = dbType;
		                    if ( dbType == 'string' ) {
		                    } else if ( dbType == 'integer') {
		                    } else if ( dbType == 'number') {
		                    } else if ( dbType == 'boolean') {
		                    } else if ( dbType == 'array') {
		                    } else if ( dbType == 'object') {
		                    }
		                    elem['value'] = dbValue;
		                    
		                    var path = dbKey.split('.');
		                    var e = tlo.metadata;
		                    var term;
		                    for (var j = 0 ; j < path.length-1 ; ++j) {
		                        term = path[j];
		                        if ( term == null || term == "" ) {
		                            throw new Error("unexpected empty string in dot-separated variable name");
		                        }
		                        if ( e[term] == null ) {
		                            e[term] = {};
		                        }
		                        e = e[term];
		                    }
		                    term = path[path.length-1];
		                    if ( term == null || term == "" ) {
		                        throw new Error("unexpected empty string in dot-separated variable name");
		                    }
		                    e[term] = elem;
		                }

		                for ( var f in mdl.model ) {
		                    var dbKey = f;
		                    var dbValue = row[dbKey];
		                    var dbType = f.type;
		    
		                    var elem = {};
		                    elem['type'] = dbType;
		                    elem['value'] = dbValue;
		                    
		                    var path = dbKey.split('.');
		                    var e = tlo.data;
		                    var term;
		                    for (var j = 0 ; j < path.length-1 ; ++j) {
		                        term = path[j];
		                        if ( term == null || term == "" ) {
		                            throw new Error("unexpected empty string in dot-separated variable name");
		                        }
		                        if ( e[term] == null ) {
		                            e[term] = {};
		                        }
		                        e = e[term];
		                    }
		                    term = path[path.length-1];
		                    if ( term == null || term == "" ) {
		                        throw new Error("unexpected empty string in dot-separated variable name");
		                    }
		                    e[term] = elem;
		                }
		            }
	      		}
	    	);
		},
		delete_all:function(ctxt, formid, instanceId) {
			//delegate to impl
		      var that = this;
		      ctxt.append('delete_all');
		      dbImpl.delete_all(ctxt, formid, instanceId, mdl);
		},
		setInstanceMetaData:function(ctxt, name, datatype, value) {
		    ctxt.append('setInstanceMetaData: ' + name);
		    var that = this;
		    dbImpl.putInstanceMetaData($.extend({}, ctxt, {success: function() {
		                that._cacheAllData(ctxt, opendatakit.getCurrentInstanceId());
		            }}), name, datatype, value, mdl);
		}, 
		getDataValue:function(name) {
		    var path = name.split('.');
		    var v = mdl.data;
		    for ( var i = 0 ; i < path.length ; ++i ) {
		        v = v[path[i]];
		        if ( v == null ) return v;
		    }
		    return v.value;
		},
		save_all_changes:function(ctxt, asComplete) {
	      	var that = this;
	    	// TODO: if called from Java, ensure that all data on the current page is saved...
	        ctxt.append('save_all_changes');
	        dbImpl.saveAllChanges(ctxt, mdl, asComplete);
		    // TODO: should we have a failure callback in to ODK Collect?
		}
    };
});

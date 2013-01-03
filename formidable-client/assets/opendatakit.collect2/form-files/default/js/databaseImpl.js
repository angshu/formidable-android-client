'use strict';
// TODO: Instance level: locale (used), at Table level: locales (available), formPath, formId, formVersion, 
// depends upon: opendatakit 
define(['jquery', 'opendatakit'], function($, opendatakit) {

	function databaseImpl($, opendatakit) {
		var that = this;
		var dbTableMetadata = [ { key: 'srcPhoneNum', type: 'string', isNullable: true },
                     { key: 'lastModTime', type: 'string', isNullable: false },
                     { key: 'syncTag', type: 'string', isNullable: true },
                     { key: 'syncState', type: 'integer', isNullable: false, defaultValue: 0 },
                     { key: 'transactioning', type: 'integer', isNullable: false, defaultValue: 1 },
                     { key: 'timestamp', type: 'integer', isNullable: false },
                     { key: 'saved', type: 'string', isNullable: true },
                     { key: 'instanceName', type: 'string', isNullable: false },
                     { key: 'locale', type: 'string', isNullable: true },
                     { key: 'instanceArguments', type: 'object', isNullable: true },
                     { key: 'xmlPublishTimestamp', type: 'integer', isNullable: true },
                     { key: 'xmlPublishStatus', type: 'string', isNullable: true } ];
		this.getDbTableNameStmt = function(tableId) {
    		return {
        		stmt : 'select * from keyValueStoreActive where TABLE_UUID=? and _KEY=?',
        		bind : [tableId, 'dbTableName']
    		};
		};

		this.createColPropsAndKeyValueStore = function(transaction) {
			transaction.executeSql(
				'CREATE TABLE IF NOT EXISTS colProps('+
                'tableId TEXT NOT NULL,'+
                'elementKey TEXT NOT NULL,'+
                'elementName TEXT NOT NULL,'+
                'elementType TEXT NULL,'+
                'listChildElementKeys TEXT NULL,'+
                'isPersisted INTEGER NOT NULL,'+
                'joinTableId TEXT NULL,'+
                'joinElementKey TEXT NULL,'+
                'displayVisible INTEGER NOT NULL,'+
                'displayName TEXT NOT NULL,'+
                'displayChoicesMap TEXT NULL,'+
                'displayFormat TEXT NULL,'+
                'smsIn INTEGER NOT NULL,'+
                'smsOut INTEGER NOT NULL,'+
                'smsLabel TEXT NULL,'+
                'footerMode TEXT NOT NULL'+
                ');', []);
            transaction.executeSql(
				'CREATE TABLE IF NOT EXISTS keyValueStoreActive('+
                'TABLE_UUID TEXT NOT NULL,'+ 
                '_KEY TEXT NOT NULL,'+
                '_TYPE TEXT NOT NULL,'+
                'VALUE TEXT NOT NULL'+
                ');', []);
		};

		this.withDb = function(ctxt, transactionBody) {
		    var inContinuation = false;
		    ctxt.append('database.withDb');
		    try {
		        if ( that.submissionDb ) {
		            that.submissionDb.transaction(transactionBody, function(error,a) {
		                    ctxt.append("withDb.transaction.error", error.message);
		                    ctxt.append("withDb.transaction.error.transactionBody", transactionBody.toString());
		                    inContinuation = true;
		                    ctxt.failure();
		                    }, function() {
		                        ctxt.append("withDb.transaction.success");
		                        inContinuation = true;
		                        ctxt.success();
		                    });
		        } else if(!window.openDatabase) {
		            ctxt.append('database.withDb.notSupported');
		            alert('not supported');
		            inContinuation = true;
		            ctxt.failure();
		        } else {
		            var settings = opendatakit.getDatabaseSettings(ctxt);
		            var database = openDatabase(settings.shortName, settings.version, settings.displayName, settings.maxSize);
		              // create the database...
		            database.transaction(
		            	function(transaction) {
		            		that.createColPropsAndKeyValueStore(transaction);
		                }, function(error) {
		                    ctxt.append("withDb.createDb.transaction.error", error.message);
		                    ctxt.append("withDb.transaction.error.transactionBody", "initializing database tables");
		                    inContinuation = true;
		                    ctxt.failure();
		                }, function() {
		                    // DB is created -- record the submissionDb and initiate the transaction...
		                    that.submissionDb = database;
		                    ctxt.append("withDb.createDb.transacton.success");
		                    that.submissionDb.transaction(transactionBody, function(error) {
		                                ctxt.append("withDb.transaction.error", error.message);
		                                ctxt.append("withDb.transaction.error.transactionBody", transactionBody.toString());
		                                inContinuation = true;
		                                ctxt.failure();
		                            }, function() {
		                                ctxt.append("withDb.transaction.success");
		                                inContinuation = true;
		                                ctxt.success();
		                            });
		                }
		            );
		        }
		    } catch(e) {
		        // Error handling code goes here.
		        if(e.INVALID_STATE_ERR) {
		            // Version number mismatch.
		            ctxt.append('withDb.exception', 'invalid db version');
		            alert("Invalid database version.");
		        } else {
		            ctxt.append('withDb.exception', 'unknown error: ' + e);
		            alert("Unknown error " + e + ".");
		        }
		        if ( !inContinuation ) {
		            try {
		                ctxt.failure();
		            } catch(e) {
		                ctxt.append('withDb.ctxt.failure.exception', 'unknown error: ' + e);
		                alert("withDb.ctxt.failure.exception " + e);
		            }
		        }
		        return;
		    }
		};

		this.selectAllTableMetaDataStmt = function(tableId) {
		    return {
		            stmt : 'select _KEY, _TYPE, VALUE from keyValueStoreActive where TABLE_UUID=?',
		            bind : [tableId]    
		        };
		};

		this.selectDbTableCountStmt = function(mdl,instanceId) {
		    var dbTableName = mdl.dbTableName;
		    
		    var stmt = 'select count(*) as rowcount from "' + dbTableName + '" where id=?';
		    return {
		        stmt : stmt,
		        bind : [instanceId]
		    };
		};

		this.insertNewDbTableStmt = function(instanceId,instanceName,locale,instanceMetadataKeyValueListAsJSON, mdl) {
		    var t = new Date();
		    var now = t.getTime();
		    var isoNow = t.toISOString();

		    var tableId = opendatakit.getCurrentTableId();
		    var dbTableName = mdl.dbTableName;
		    var model = mdl.model;
		    
		    var bindings = [];
		    
		    var stmt = 'insert into "' + dbTableName + '" (id';
		    for (var j = 0 ; j < dbTableMetadata.length ; ++j) {
		        var f = dbTableMetadata[j];
		        stmt += ", " + f.key;
		    }
		    for ( var f in model ) {
		        stmt += ', "' + f + '"';
		    }
		    stmt += ") values (?";
		    bindings.push(instanceId);
		    
		    for (var j = 0 ; j < dbTableMetadata.length ; ++j) {
		        var f = dbTableMetadata[j];
		        if ( f.key == "lastModTime" ) {
		            stmt += ", ?";
		            bindings.push(isoNow);
		        } else if ( f.key == "timestamp" ) {
		            stmt += ", ?";
		            bindings.push(now);
		        } else if ( f.key == "instanceName" ) {
		            stmt += ", ?";
		            bindings.push(instanceName);
		        } else if ( f.key == "locale" ) {
		            stmt += ", ?";
		            bindings.push(locale);
		        } else if ( f.key == "instanceArguments" ) {
		            stmt += ", ?";
		            bindings.push(instanceMetadataKeyValueListAsJSON);
		        } else if ( f.isNullable ) {
		            stmt += ", null";
		        } else {
		            stmt += ", " + f.defaultValue
		        }
		    }
		    for ( var f in model ) {
		        stmt += ", null";
		    }
		    stmt += ")"; 
		    return {
		        stmt : stmt,
		        bind : bindings
		    };
		};

		this.getAllFormInstancesStmt = function(mdl) {
		    var dbTableName = mdl.dbTableName;
		    return {
		            stmt : 'select instanceName, timestamp, saved, locale, xmlPublishTimestamp, xmlPublishStatus, id from "' +
		                    dbTableName + '" where instanceName is not null group by id having timestamp = max(timestamp);',
		            bind : []
		            };
		};

		this.initializeMetaData = function(ctxt, formDef, tableId, protoTableMetadata, formPath, mdl) {
			//appended method. Extracted from database.initializeTables.
			var tlo = {};
			that.withDb($.extend({},ctxt,{success:function() {
		                ctxt.append('getAllTableMetaData.success');
		                // these values come from the current webpage
		                tlo = $.extend(tlo, protoTableMetadata);
		                // update tableId and qp
		                mdl.qp = tlo;
		                opendatakit.setCurrentTableId(tableId);
		                opendatakit.setCurrentFormPath(formPath);
		                ctxt.success();
		            }}), 
		    		function(transaction) {
		                // now insert records into these tables...
		                var ss = that.getDbTableNameStmt(tableId);
		                transaction.executeSql(ss.stmt, ss.bind, function(transaction, result) {
		                    if (result.rows.length == 0 ) {
		                        // TODO: use something other than formId for the dbTableName...
		                        that._insertTableAndColumnProperties(transaction, tableId, protoTableMetadata.formId.value, protoTableMetadata.formTitle, formDef, tlo);
		                    } else {
		                        if(result.rows.length != 1) {
		                            throw new Error("getMetaData: multiple rows! " + name + " count: " + result.rows.length);
		                        } else {
		                            var rec = result.rows.item(0);
		                            var dbTableName = rec['VALUE'];
		                            mdl.dbTableName = dbTableName;
		                            mdl.model = formDef.model;
		                            that.coreGetAllTableMetadata(transaction, tableId, tlo);
		                        }
		                    }
		                });
		    		});

		};

		this.coreGetAllTableMetadata = function(transaction, tableId, tlo) {
		    var ss = that.selectAllTableMetaDataStmt(tableId);
		    transaction.executeSql(ss.stmt, ss.bind, function(transaction, result) {
		        var len = result.rows.length;
		        for (var i = 0 ; i < len ; ++i ) {
		            var row = result.rows.item(i);
		            var dbKey = row['_KEY'];
		            var dbValue = row['VALUE'];
		            var dbType = row['_TYPE'];
		            
		            var elem = {};
		            elem['type'] = dbType;
		            elem['value'] = dbValue;
		            
		            var path = dbKey.split('.');
		            var e = tlo; //Q: why is the local variable used? any specific reason for not using tlo directly?
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
		    });
		};

		function _insertTableAndColumnProperties(transaction, tableId, dbTableName, formTitle, formDef, tlo) {
		    var fullDef = {
		        keyValueStoreActive: [],
		        colProps: []
		        };

		    
		    // TODO: verify that dbTableName is not already in use...
		    var createTableCmd = 'CREATE TABLE IF NOT EXISTS "' + dbTableName + '"(id TEXT NOT NULL';
		    for ( var j = 0 ; j < that.dbTableMetadata.length ; ++j ) {
		        var f = that.dbTableMetadata[j];
		        createTableCmd = createTableCmd + ',' + f.key + " ";
		        if ( f.type == "string" ) {
		            createTableCmd = createTableCmd + "TEXT" + (f.isNullable ? " NULL" : " NOT NULL");
		        } else if ( f.type == "integer" ) {
		            createTableCmd = createTableCmd + "INTEGER" + (f.isNullable ? " NULL" : " NOT NULL");
		        } else if ( f.type == "number" ) {
		            createTableCmd = createTableCmd + "REAL" + (f.isNullable ? " NULL" : " NOT NULL");
		        } else if ( f.type == "boolean" ) {
		            createTableCmd = createTableCmd + "INTEGER" + (f.isNullable ? " NULL" : " NOT NULL");
		        } else if ( f.type == "object" ) {
		            createTableCmd = createTableCmd + "TEXT" + (f.isNullable ? " NULL" : " NOT NULL");
		        } else if ( f.type == "array" ) {
		            createTableCmd = createTableCmd + "TEXT" + (f.isNullable ? " NULL" : " NOT NULL");
		        } else {
		            alert("unhandled type");
		        }
		    }

		    var displayColumnOrder = [];
		    for ( var df in formDef.model ) {
		    
		        var collectElementName = df;
		        
		        displayColumnOrder.push(collectElementName);
		        
		        var collectDataTypeName;
		        
		        var defn = $.extend({key: collectElementName},formDef.model[df]);
		        var type = defn.type;
		        if ( type == 'integer' ) {
		            collectDataTypeName = 'integer';
		            createTableCmd += ',"' + collectElementName + '" INTEGER NULL';
		        } else if ( type == 'number' ) {
		            collectDataTypeName = 'number';
		            createTableCmd += ',"' + collectElementName + '" REAL NULL';
		        } else if ( type == 'string' ) {
		            collectDataTypeName = 'string';
		            createTableCmd += ',"' + collectElementName + '" TEXT NULL';
		        } else if ( type == 'image/*' ) {
		            collectDataTypeName = 'mimeUri';
		            createTableCmd += ',"' + collectElementName + '" TEXT NULL';
		        } else if ( type == 'audio/*' ) {
		            collectDataTypeName = 'mimeUri';
		            createTableCmd += ',"' + collectElementName + '" TEXT NULL';
		        } else if ( type == 'video/*' ) {
		            collectDataTypeName = 'mimeUri';
		            createTableCmd += ',"' + collectElementName + '" TEXT NULL';
		        } else {
		            // TODO: handle composite types...
		            collectDataTypeName = 'text';
		            createTableCmd += ',"' + collectElementName + '" TEXT NULL';
		        }
		        
		        // case: simple type
		        // TODO: case: geopoint -- expand to different persistence columns
		    
		        fullDef.colProps.push( {
		            tableId: tableId,
		            elementKey: collectElementName,
		            elementName: collectElementName,
		            elementType: collectDataTypeName,
		            listChildElementKeys: null,
		            isPersisted: 1,
		            joinTableId: null,
		            joinElementKey: null,
		            displayVisible: 1,
		            displayName: collectElementName,
		            displayChoicesMap: null,
		            displayFormat: null,
		            smsIn: 1,
		            smsOut: 1,
		            smsLabel: null,
		            footerMode: '0'
		        } );
		    }
		    createTableCmd += ');';
		    
		    // construct the kvPairs to insert into kvstore
		    fullDef.keyValueStoreActive.push( { TABLE_UUID: tableId, _KEY: 'dbTableName', _TYPE: 'string', VALUE: dbTableName } );
		    fullDef.keyValueStoreActive.push( { TABLE_UUID: tableId, _KEY: 'displayName', _TYPE: 'string', VALUE: formTitle } );
		    fullDef.keyValueStoreActive.push( { TABLE_UUID: tableId, _KEY: 'type', _TYPE: 'integer', VALUE: '0' } );
		    fullDef.keyValueStoreActive.push( { TABLE_UUID: tableId, _KEY: 'primeCols', _TYPE: 'string', VALUE: '' } );
		    fullDef.keyValueStoreActive.push( { TABLE_UUID: tableId, _KEY: 'sortCol', _TYPE: 'string', VALUE: '' } );
		    fullDef.keyValueStoreActive.push( { TABLE_UUID: tableId, _KEY: 'readAccessTid', _TYPE: 'string', VALUE: '' } );
		    fullDef.keyValueStoreActive.push( { TABLE_UUID: tableId, _KEY: 'writeAccessTid', _TYPE: 'string', VALUE: '' } );
		    fullDef.keyValueStoreActive.push( { TABLE_UUID: tableId, _KEY: 'syncTag', _TYPE: 'string', VALUE: '' } );
		    fullDef.keyValueStoreActive.push( { TABLE_UUID: tableId, _KEY: 'lastSyncTime', _TYPE: 'integer', VALUE: '-1' } );
		    fullDef.keyValueStoreActive.push( { TABLE_UUID: tableId, _KEY: 'coViewSettings', _TYPE: 'string', VALUE: '' } );
		    fullDef.keyValueStoreActive.push( { TABLE_UUID: tableId, _KEY: 'detailViewFile', _TYPE: 'string', VALUE: '' } );
		    fullDef.keyValueStoreActive.push( { TABLE_UUID: tableId, _KEY: 'summaryDisplayFormat', _TYPE: 'string', VALUE: '' } );
		    fullDef.keyValueStoreActive.push( { TABLE_UUID: tableId, _KEY: 'syncState', _TYPE: 'integer', VALUE: '' } );
		    fullDef.keyValueStoreActive.push( { TABLE_UUID: tableId, _KEY: 'transactioning', _TYPE: 'integer', VALUE: '' } );
		    fullDef.keyValueStoreActive.push( { TABLE_UUID: tableId, _KEY: 'colOrder', _TYPE: 'string', VALUE: JSON.stringify(displayColumnOrder) } );
		    fullDef.keyValueStoreActive.push( { TABLE_UUID: tableId, _KEY: 'ovViewSettings', _TYPE: 'string', VALUE: '' } );

		    transaction.executeSql(createTableCmd, [], function(transaction, result) {
		        that.fullDefHelper(transaction, true, 0, fullDef, tableId, dbTableName, formDef, tlo);
		    });
		}

		this.insertIntoNewDbTable = function(ctxt, instanceId, mdl, instanceMetadataKeyValueList) {
			//new method
			that.withDb(ctxt, function(transaction) {
	            var cs = that.selectDbTableCountStmt(mdl,instanceId);
	            transaction.executeSql(cs.stmt, cs.bind, function(transaction, result) {
	                var count = 0;
	                if ( result.rows.length == 1 ) {
	                    var row = result.rows.item(0);
	                    count = row['rowcount'];
	                }
	                if ( count == null || count == 0) {
	                    ctxt.append('initializeInstance.insertEmptyInstance');
	                    // construct a friendly name for this new form...
	                    var date = new Date();
	                    var dateStr = date.toISOString();
	                    var locale = opendatakit.getDefaultFormLocale(mdl.qp.formDef.value);
	                    var instanceName = dateStr; // .replace(/\W/g, "_")
	                    var cs = that.insertNewDbTableStmt(instanceId, instanceName, locale, JSON.stringify(instanceMetadataKeyValueList), mdl);
	                    transaction.executeSql(cs.stmt, cs.bind);
	                }
	            });
	        });
		};

		this.selectAllDbTableStmt = function(instanceId, mdl) {
		    var t = new Date();
		    var now = t.getTime();
		    var isoNow = t.toISOString();

		    var tableId = opendatakit.getCurrentTableId();
		    var dbTableName = mdl.dbTableName;
		    var model = mdl.model;
		    
		    // TODO: select * ... for cross-table referencing...
		    var stmt = "select id";
		    for (var j = 0 ; j < dbTableMetadata.length ; ++j) {
		        var f = dbTableMetadata[j];
		        stmt += ", " + f.key;
		    }
		    for ( var f in model ) {
		        stmt += ', "' + f + '"';
		    }
		    stmt += ' from "' + dbTableName + '" where id=? group by id having timestamp = max(timestamp)'; 
		    return {
		        stmt : stmt,
		        bind : [instanceId]
		    };
		};

		this.delete_all = function(ctxt, formid, instanceId, mdl) {
			that.withDb( ctxt, function(transaction) {
		            var cs = deleteDbTableStmt(formid, instanceId, mdl);
		            transaction.executeSql(cs.stmt, cs.bind);
		    });
		};

		function deleteDbTableStmt(formid, instanceid, mdl) {
		    var dbTableName = mdl.dbTableName;
		    return {
		        stmt : 'delete from "' + dbTableName + '" where id=?;',
		        bind : [instanceid]
		    };
		};

		//new method
		this.getAllData = function(ctxt, instanceId, mdl, onResult) {
			//new method
			that.withDb( ctxt, 
	      			function(transaction) {
	        			var ss = that.selectAllDbTableStmt(instanceId, mdl);
	        			transaction.executeSql(ss.stmt, ss.bind, function(transaction, result) {
	        				onResult(result, dbTableMetadata);
		        		});
	      			});
		};

		this.getDbTableMetaData = function() {
			return dbTableMetadata;
		};

		function insertDbTableStmt(name, type, value, isInstanceMetadata, mdl ) {
		    var t = new Date();
		    var now = t.getTime();
		    var isoNow = t.toISOString();

		    var tableId = opendatakit.getCurrentTableId();
		    var dbTableName = mdl.dbTableName;
		    var model = mdl.model;
		    
		    var bindings = [];
		    
		    var stmt = 'insert into "' + dbTableName + '" (id';
		    for (var j = 0 ; j < dbTableMetadata.length ; ++j) {
		        var f = dbTableMetadata[j];
		        stmt += ", " + f.key;
		    }
		    for ( var f in model ) {
		        stmt += ', "' + f + '"';
		    }
		    stmt += ") select id";
		    for (var j = 0 ; j < dbTableMetadata.length ; ++j) {
		        var f = dbTableMetadata[j];
		        if ( f.key == name && isInstanceMetadata ) {
		            if (value == null) {
		                stmt += ", null";
		            } else {
		                stmt += ", ?";
		                bindings.push(value);
		            }
		        } else if ( f.key == "lastModTime" ) {
		            stmt += ", ?";
		            bindings.push(isoNow);
		        } else if ( f.key == "timestamp" ) {
		            stmt += ", ?";
		            bindings.push(now);
		        } else if ( f.key == "xmlPublishTimestamp" ) {
		            stmt += ", null";
		        } else if ( f.key == "saved" || f.key == "xmlPublishStatus" ) {
		            stmt += ", null";
		        } else {
		            stmt += ", " + f.key;
		        }
		    }
		    for ( var f in model ) {
		        if ( f == name && !isInstanceMetadata ) {
		            if (value == null) {
		                stmt += ", null";
		            } else {
		                stmt += ", ?";
		                bindings.push(value);
		            }
		        } else {
		            stmt += ', "' + f + '"';
		        }
		    }
		    stmt += ' from "' + dbTableName + '" where id=? group by id having timestamp = max(timestamp)'; 
		    bindings.push(opendatakit.getCurrentInstanceId());
		    return {
		        stmt : stmt,
		        bind : bindings
		        };
		}

		this.putInstanceMetaData = function(ctxt, name, type, value, mdl) {
		      ctxt.append('putInstanceMetaData', 'name: ' + name);
		      that.withDb( ctxt, function(transaction) {
		            var is = insertDbTableStmt(name, type, value, true, mdl);
		            transaction.executeSql(is.stmt, is.bind, function(transaction, result) {
		                ctxt.append("putInstanceMetaData: successful insert: " + name);
		            });
		        });
		};

		function markCurrentStateAsSavedDbTableStmt(status, mdl) {
		    var t = new Date();
		    var now = t.getTime();
		    var isoNow = t.toISOString();

		    var tableId = opendatakit.getCurrentTableId();
		    var dbTableName = mdl.dbTableName;
		    var model = mdl.model;
		    
		    var bindings = [];
		    
		    var stmt = 'insert into "' + dbTableName + '" (id';
		    for (var j = 0 ; j < dbTableMetadata.length ; ++j) {
		        var f = dbTableMetadata[j];
		        stmt += ", " + f.key;
		    }
		    for ( var f in model ) {
		        stmt += ', "' + f + '"';
		    }
		    stmt += ") select id";
		    for (var j = 0 ; j < dbTableMetadata.length ; ++j) {
		        var f = dbTableMetadata[j];
		        if ( f.key == "timestamp" ) {
		            stmt += ", ?";
		            bindings.push(now);
		        } else if ( f.key == "saved" ) {
		            stmt += ", ?";
		            bindings.push(status);
		        } else if ( f.key == "xmlPublishTimestamp" ) {
		            stmt += ", null";
		        } else if ( f.key == "xmlPublishStatus" ) {
		            stmt += ", null";
		        } else {
		            stmt += ", " + f.key;
		        }
		    }
		    for ( var f in model ) {
		        stmt += ', "' + f + '"';
		    }
		    stmt += ' from "' + dbTableName + '" where id=? group by id having timestamp = max(timestamp)'; 
		    bindings.push(opendatakit.getCurrentInstanceId());
		    return {
		        stmt : stmt,
		        bind : bindings
		        };
		}

		function deletePriorChangesDbTableStmt(mdl) {
		    var dbTableName = mdl.dbTableName;
		    
		    var stmt = 'delete from "' + dbTableName + '" where id=? and timestamp not in (select max(timestamp) from "' + dbTableName + '" where id=?);';
		    return {
		        stmt : stmt,
		        bind : [opendatakit.getCurrentInstanceId(), opendatakit.getCurrentInstanceId()]
		    };
		}

		this.saveAllChanges = function(ctxt, mdl, asComplete) {
			that.withDb( $.extend({}, ctxt, {success:function() {
	                ctxt.append('save_all_changes.markCurrentStateSaved.success', 
	                mdl.qp.formId.value + " instanceId: " + opendatakit.getCurrentInstanceId() + " asComplete: " + asComplete);
	                ctxt.success();
	            }}), 
	            function(transaction) {
	                var cs = markCurrentStateAsSavedDbTableStmt((asComplete ? 'COMPLETE' : 'INCOMPLETE'), mdl);
	                transaction.executeSql(cs.stmt, cs.bind, function(transaction, result) {
	                    if ( asComplete ) {
	                        ctxt.append('save_all_changes.cleanup');
	                        // and now delete the change history...
	                        var cs = deletePriorChangesDbTableStmt(mdl);
	                        transaction.executeSql(cs.stmt, cs.bind);
	                    }
	                });
	            }
	        );	

		};
	}

	return new databaseImpl($, opendatakit);
});
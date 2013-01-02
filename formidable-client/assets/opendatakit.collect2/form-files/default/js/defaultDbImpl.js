define(['jquery', 'opendatakit'], function($, opendatakit) {
	function DefaultDbImpl($, opendatakit) {
		this.submissionDb = false;
		this.dataTablePredefinedColumns = { id: {type: 'string', isNotNullable: true, isPersisted: true, dbColumnConstraint: 'PRIMARY KEY', elementSet: 'instanceMetadata' },
                     uri_user: { type: 'string', isNotNullable: false, isPersisted: true, elementSet: 'instanceMetadata' },
                     last_mod_time: { type: 'integer', isNotNullable: true, 'default': -1, isPersisted: true, elementSet: 'instanceMetadata' },
                     sync_tag: { type: 'string', isNotNullable: false, isPersisted: true, elementSet: 'instanceMetadata' },
                     sync_state: { type: 'integer', isNotNullable: true, 'default': 0, isPersisted: true, elementSet: 'instanceMetadata' },
                     transactioning: { type: 'integer', isNotNullable: true, 'default': 1, isPersisted: true, elementSet: 'instanceMetadata' },
                     timestamp: { type: 'integer', isNotNullable: true, isPersisted: true, elementSet: 'instanceMetadata' },
                     saved: { type: 'string', isNotNullable: false, isPersisted: true, elementSet: 'instanceMetadata' },
                     instance_name: { type: 'string', isNotNullable: true, isPersisted: true, elementPath: 'instanceName', elementSet: 'instanceMetadata' },
                     locale: { type: 'string', isNotNullable: false, isPersisted: true, elementPath: 'locale', elementSet: 'instanceMetadata' } };

        this.columnDefinitionsTableConstraint = 'PRIMARY KEY ( "table_id", "element_key" )';
		this.columnDefinitionsPredefinedColumns = {
		                    table_id: { type: 'string', isNotNullable: true, isPersisted: true, elementPath: 'table_id', elementSet: 'columnMetadata' },
		                    element_key: { type: 'string', isNotNullable: true, isPersisted: true, elementPath: 'elementKey', elementSet: 'columnMetadata' },
		                    element_name: { type: 'string', isNotNullable: true, isPersisted: true, elementPath: 'elementName', elementSet: 'columnMetadata' },
		                    element_type: { type: 'string', isNotNullable: false, isPersisted: true, elementSet: 'columnMetadata' },
		                    list_child_element_keys: { type: 'array', items: { type: 'string' }, isNotNullable: false, isPersisted: true, elementSet: 'columnMetadata' },
		                    is_persisted: { type: 'integer', isNotNullable: true, isPersisted: true, elementSet: 'columnMetadata' },
		                    joins: { type: 'array', items: {
		                                type: 'object', 
		                                listChildElementKeys: [],
		                                properties: {
		                                    "table_id": { type: "string", isNotNullable: false, isPersisted: false, elementKey: 'table_id', elementSet: 'columnMetadata' },
		                                    "element_key": { type: "string", isNotNullable: false, isPersisted: false, elementKey: 'elementKey', elementSet: 'columnMetadata' } } },
		                            isNotNullable: false, isPersisted: true, elementPath: 'joins', elementSet: 'columnMetadata' } };
		this.keyValueStoreActiveTableConstraint = 'PRIMARY KEY ("table_id", "partition", "aspect", "key")';	
		this.keyValueStoreActivePredefinedColumns = {
                    table_id: { type: 'string', isNotNullable: true, isPersisted: true },
                    partition: { type: 'string', isNotNullable: true, isPersisted: true },
                    aspect: { type: 'string', isNotNullable: true, isPersisted: true },
                    key: { type: 'string', isNotNullable: true, isPersisted: true },
                    type: { type: 'string', isNotNullable: false, isPersisted: true },
                    value: { type: 'string', isNotNullable: false, isPersisted: true } };
  	    this.tableDefinitionsPredefinedColumns = {
                    table_id: { type: 'string', isNotNullable: true, isPersisted: true, dbColumnConstraint: 'PRIMARY KEY', elementPath: 'table_id', elementSet: 'tableMetadata' },
                    table_key: { type: 'string', isNotNullable: true, isPersisted: true, dbColumnConstraint: 'UNIQUE', elementPath: 'tableKey', elementSet: 'tableMetadata' },
                    db_table_name: { type: 'string', isNotNullable: true, isPersisted: true, dbColumnConstraint: 'UNIQUE', elementPath: 'dbTableName', elementSet: 'tableMetadata' },
                    type: { type: 'string', isNotNullable: true, isPersisted: true, elementSet: 'tableMetadata' },
                    table_id_access_controls: { type: 'string', isNotNullable: false, isPersisted: true, elementSet: 'tableMetadata' },
                    sync_tag: { type: 'string', isNotNullable: false, isPersisted: true, elementSet: 'tableMetadata' },
                    last_sync_time: { type: 'integer', isNotNullable: true, isPersisted: true, elementSet: 'tableMetadata' },
                    sync_state: { type: 'string', isNotNullable: true, isPersisted: true, elementSet: 'tableMetadata' },
                    transactioning: { type: 'integer', isNotNullable: true, isPersisted: true, elementSet: 'tableMetadata' } };
             
        this._columnKeyValueStoreActiveAccessibleKeys = {
	            // keys we are allowing the user to access from within Javascript
	            // partition: column
	            // aspect: element_key
	            // mapped as psuedo-columns per PredefinedColumns objects, above.
	            // propertyName is the value of the 'key' in the database.
	    };

	    this._tableKeyValueStoreActiveAccessibleKeys = {
	            // keys we are allowing the user to access from within Javascript
	            // partition: table
	            // aspect: global
	            // mapped as psuedo-columns per PredefinedColumns objects, above.
	            // propertyName is the value of the 'key' in the database.
	    };

        var that = this;            

		this.withDb = function(ctxt, transactionBody) {
		    var inContinuation = false;
		    ctxt.append('database.withDb');
		    ctxt.sqlStatement = null;
		    var that = this;
		    try {
		        if ( that.submissionDb ) {
		            that.submissionDb.transaction(transactionBody, function(error,a) {
		                    if ( ctxt.sqlStatement != null ) {
		                        ctxt.append("withDb.transaction.error.sqlStmt", ctxt.sqlStatement.stmt);
		                    }
		                    ctxt.append("withDb.transaction.error", error.message);
		                    ctxt.append("withDb.transaction.error.transactionBody", transactionBody.toString());
		                    inContinuation = true;
		                    ctxt.failure({message: "Error while accessing or saving values to the database."});
		                    }, function() {
		                        ctxt.append("withDb.transaction.success");
		                        inContinuation = true;
		                        ctxt.success();
		                    });
		        } else if(!window.openDatabase) {
		            ctxt.append('database.withDb.notSupported');
		            console.error('w3c SQL interface is not supported');
		            inContinuation = true;
		            ctxt.failure({message: "Web client does not support the W3C SQL database standard."});
		        } else {
		            var settings = opendatakit.getDatabaseSettings();
		            var database = openDatabase(settings.shortName, settings.version, settings.displayName, settings.maxSize);
		              // create the database...
		            database.transaction(function(transaction) {
		                    var td;
		                    td = _createTableStmt('column_definitions', 
		                                                that.columnDefinitionsPredefinedColumns,
		                                                that.columnDefinitionsTableConstraint );
		                    ctxt.sqlStatement = td;
		                    transaction.executeSql(td.stmt, td.bind);
		                    td = _createTableStmt('key_value_store_active', 
		                                                that.keyValueStoreActivePredefinedColumns,
		                                                that.keyValueStoreActiveTableConstraint );
		                    ctxt.sqlStatement = td;
		                    transaction.executeSql(td.stmt, td.bind);
		                    td = _createTableStmt('table_definitions', 
		                                                that.tableDefinitionsPredefinedColumns);
		                    ctxt.sqlStatement = td;
		                    transaction.executeSql(td.stmt, td.bind);
		                }, function(error) {
		                    if ( ctxt.sqlStatement != null ) {
		                        ctxt.append("withDb.createDb.transaction.error.sqlStmt", ctxt.sqlStatement.stmt);
		                    }
		                    ctxt.append("withDb.createDb.transaction.error", error.message);
		                    ctxt.append("withDb.createDb.transaction.error.transactionBody", "initializing database tables");
		                    inContinuation = true;
		                    ctxt.failure({message: "Error while initializing database tables."});
		                }, function() {
		                    // DB is created -- record the submissionDb and initiate the transaction...
		                    that.submissionDb = database;
		                    ctxt.append("withDb.createDb.transacton.success");
		                    that.submissionDb.transaction(transactionBody, function(error) {
		                                if ( ctxt.sqlStatement != null ) {
		                                    ctxt.append("withDb.afterCreateDb.transaction.error.sqlStmt", ctxt.sqlStatement.stmt);
		                                }
		                                ctxt.append("withDb.afterCreateDb.transaction.error", error.message);
		                                ctxt.append("withDb.afterCreateDb.transaction.error.transactionBody", transactionBody.toString());
		                                inContinuation = true;
		                                ctxt.failure({message: "Error while accessing or saving values to the database."});
		                            }, function() {
		                                ctxt.append("withDb.afterCreateDb.transaction.success");
		                                inContinuation = true;
		                                ctxt.success();
		                            });
		                });
		        }
		    } catch(e) {
		        // Error handling code goes here.
		        if ( ctxt.sqlStatement != null ) {
		            ctxt.append("withDb.exception.sqlStmt", ctxt.sqlStatement.stmt);
		        }
		        if(e.INVALID_STATE_ERR) {
		            // Version number mismatch.
		            ctxt.append('withDb.exception', 'invalid db version');
		            console.error("Invalid database version.");
		        } else {
		            ctxt.append('withDb.exception', 'unknown error: ' + String(e));
		            console.error("Unknown error " + String(e) + ".");
		        }
		        if ( !inContinuation ) {
		            try {
		                ctxt.failure({message: "Exception while accessing or saving values to the database."});
		            } catch(e) {
		                ctxt.append('withDb.ctxt.failure.exception', 'unknown error: ' + String(e));
		                console.error("withDb.ctxt.failure.exception " + String(e));
		                ctxt._log('E','withDb: exception caught while executing ctxt.failure(msg)');
		                alert('Fatal error while accessing or saving values to database');
		            }
		        } else {
		            console.error("Unrecoverable Internal Error: Exception during success/failure continuation");
		            ctxt._log('E',"withDb: Unrecoverable Internal Error: Exception during success/failure continuation");
		            alert('Fatal error while accessing or saving values to database');
		        }
		        return;
		    }
		};

		this.initializeSchema = function(ctxt, tmpctxt, table_id, tlo, mdl) {
			that.withDb(tmpctxt, function(transaction) {
                // now insert records into these tables...
                var ss = _selectTableDefinitionsDataStmt(table_id);
                ctxt.sqlStatement = ss;
                transaction.executeSql(ss.stmt, ss.bind, function(transaction, result) {
                    var len = result.rows.length;
                    if (len == 0 ) {
                        // TODO: use something other than form_id for the dbTableName...
                        _insertTableAndColumnProperties(transaction, tmpctxt, tlo, true, mdl);
                    } else if(len != 1) {
                        throw new Error("getMetaData: multiple rows! " + name + " count: " + len);
                    } else {
                        // we have the table and column definitions in the database -- 
                        // assume the formPath description exactly matches and just build the metadata
                        // TODO: this does not verify that the database structure matches the
                        // structure defined by the formPath.
                        _insertTableAndColumnProperties(transaction, tmpctxt, tlo, false, mdl);
                    }
                });
            });
		};


		//not classified as private but can't find references from other modules.
		this.fullDefHelper = function(transaction, ctxt, tableToUpdate, idx, fullDef, dbTableName, dataTableModel, tlo, mdl) {
		    var that = this;
		    var row = null;
		    
		    for (;;) {
		        if ( fullDef[tableToUpdate].length > idx ) {
		            row = fullDef[tableToUpdate][idx];
		        }
		    
		        if ( row != null ) {
		            break;
		        }

		        // find the next table to insert records into...
		        var old = tableToUpdate;
		        tableToUpdate = null;
		        var found = false;
		        for ( var prop in fullDef ) {
		            if ( prop == old ) {
		                found = true; // get the table after this...
		            } else if ( found ) {
		                tableToUpdate = prop;
		                break;
		            }
		        }
		        
		        if ( tableToUpdate == null ) {
		            // end of the array -- we are done!
		            mdl.dataTableModel = dataTableModel;
		            that.coreGetAllTableMetadata(transaction, ctxt, tlo);
		            return;
		        }

		        // reset to the start of the insert array
		        idx = 0;
		    }
		    
		    // assemble insert statement...
		    var insertStart = 'REPLACE INTO ' + tableToUpdate + ' (';
		    var insertMiddle = ') VALUES (';
		    var bindArray = [];
		    for ( var col in row ) {
		        insertStart += col + ',';
		        bindArray.push(row[col]);
		        insertMiddle += '?,';
		    }
		    var insertCmd = insertStart.substr(0,insertStart.length-1) + insertMiddle.substr(0,insertMiddle.length-1) + ');';
		    
		    transaction.executeSql(insertCmd, bindArray, function(transaction, result) {
		        that.fullDefHelper(transaction, ctxt, tableToUpdate, idx+1, fullDef, dbTableName, dataTableModel, tlo, mdl);
		    });
		};

		this.coreGetAllTableMetadata = function(transaction, ctxt, tlo) {
		    var outcome = true;
		    var ss;
		    ss = _selectTableDefinitionsDataStmt(tlo.table_id);
		    ctxt.sqlStatement = ss;
		    transaction.executeSql(ss.stmt, ss.bind, function(transaction, result) {
		        var len = result.rows.length;
		        var f;
		        var row;
		        var defn;
		        if ( len != 1 ) {
		            throw new Error("Internal error: unknown table_id");
		        }
		        row = result.rows.item(0);
		        for ( f in that.tableDefinitionsPredefinedColumns ) {
		            defn = that.tableDefinitionsPredefinedColumns[f];
		            if ( defn.elementPath ) {
		                _reconstructElementPath(defn.elementPath, defn, row[f], tlo.tableMetadata );
		            }
		        }
		        ss = _selectAllTableMetaDataStmt(tlo.table_id);
		        ctxt.sqlStatement = ss;
		        transaction.executeSql(ss.stmt, ss.bind, function(transaction, result) {
		            var len = result.rows.length;
		            var defn;
		            for (var i = 0 ; i < len ; ++i ) {
		                var row = result.rows.item(i);
		                var dbKey = row['key'];
		                var dbValue = row['value'];
		                var dbType = row['type'];
		                defn = _getAccessibleTableKeyDefinition(dbKey);
		                if ( defn != null ) {
		                    _reconstructElementPath(defn.elementPath, defn, dbValue, tlo.tableMetadata );
		                }
		            }
		            // read all column definitions...
		            ss = _selectColumnDefinitionsDataStmt(tlo.table_id);
		            ctxt.sqlStatement = ss;
		            transaction.executeSql(ss.stmt, ss.bind, function(transaction, result) {
		                var len = result.rows.length;
		                var i, row;
		                var f;
		                var defn;
		                var colDefn;
		                tlo.columnMetadata = {};
		                for ( i = 0 ; i < len ; ++i ) {
		                    row = result.rows.item(i);
		                    colDefn = {};
		                    for ( f in that.columnDefinitionsPredefinedColumns ) {
		                        defn = that.columnDefinitionsPredefinedColumns[f];
		                        if ( defn.elementPath ) {
		                            _reconstructElementPath(defn.elementPath, defn, row[f], colDefn );
		                        }
		                    }
		                    tlo.columnMetadata[defn.elementKey] = defn;
		                }
		                
		                ss = _selectAllColumnMetaDataStmt(tlo.table_id);
		                ctxt.sqlStatement = ss;
		                transaction.executeSql(ss.stmt, ss.bind, function(transaction, result) {
		                    var len = result.rows.length;
		                    var defn;
		                    for (var i = 0 ; i < len ; ++i ) {
		                        var row = result.rows.item(i);
		                        var elementKey = row['element_key'];
		                        var dbKey = row['key'];
		                        var dbValue = row['value'];
		                        var dbType = row['type'];
		                        defn = _getAccessibleColumnKeyDefinition(dbKey);
		                        if ( defn != null ) {
		                            _reconstructElementPath(defn.elementPath, defn, dbValue, tlo.columnMetadata[elementKey] );
		                        }
		                    }
		                });
		            });
		        });
		    });
		};

		this.initializeInstance = function(tmpctxt, formDef, instanceId, dataTableModel, dbTableName, instanceMetadataKeyValueMap) {
			that.withDb( tmpctxt, function(transaction) {
	            var cs = _selectDataTableCountStmt(dbTableName, instanceId);
	            tmpctxt.sqlStatement = cs;
	            transaction.executeSql(cs.stmt, cs.bind, function(transaction, result) {
	                var count = 0;
	                if ( result.rows.length == 1 ) {
	                    var row = result.rows.item(0);
	                    count = row['rowcount'];
	                }
	                if ( count == null || count == 0) {
	                    tmpctxt.append('initializeInstance.insertEmptyInstance');
	                    // construct a friendly name for this new form...
	                    var date = new Date();
	                    var dateStr = date.toISOString();
	                    var locale = opendatakit.getDefaultFormLocale(formDef);
	                    var instanceName = dateStr; // .replace(/\W/g, "_")
	                    
	                    var kvMap = {};
	                    kvMap.id = { value: instanceId, isInstanceMetadata: true };
	                    kvMap.instance_name = { value: instanceName, isInstanceMetadata: true };
	                    kvMap.locale = { value: locale, isInstanceMetadata: true };
	                    var propertyCount = 0;
	                    for ( var f in instanceMetadataKeyValueMap ) {
	                        ++propertyCount;
	                    }
	                    if ( propertyCount != 0 ) {
	                        console.error("Extra arguments found in instanceMetadataKeyValueMap: " + instanceMetadataKeyValueMap );
	                    }
	                    var cs = _insertNewKeyValueMapDataTableStmt(dbTableName, dataTableModel, kvMap);
	                    tmpctxt.sqlStatement = cs;
	                    transaction.executeSql(cs.stmt, cs.bind);
	                }
	            });
	        });
		};

		this.getAllData = function(tmpctxt, dbTableName, instanceId, dataTableModel, tlo) {
			that.withDb( tmpctxt, function(transaction) {
		        var ss = _selectAllFromDataTableStmt(dbTableName, instanceId);
		        tmpctxt.sqlStatement = ss;
		        transaction.executeSql(ss.stmt, ss.bind, function(transaction, result) {
		            var len = result.rows.length;
		            if (len == 0 ) {
		                throw new Error("no record for getAllData!");
		            } else if (len != 1 ) {
		                throw new Error("not exactly one record in getAllData!");
		            } else {
		                var row = result.rows.item(0);
		                var dbKey, dbValue, jsonType;
		                var elementPath;
		                
		                // and then just snarf the fields...
		                for ( dbKey in dataTableModel ) {
		                    jsonType = dataTableModel[dbKey];
		                    if ( jsonType.isPersisted ) {
		                        dbValue = row[dbKey];
		                        elementPath = jsonType.elementPath;
		                        if ( elementPath != null ) {
		                            // we expose it to the Javascript layer if it has an elementPath...
		                            if ( jsonType.elementSet == 'instanceMetadata' ) {
		                                _reconstructElementPath(elementPath, jsonType, dbValue, tlo.metadata );
		                            } else {
		                                _reconstructElementPath(elementPath, jsonType, dbValue, tlo.data );
		                            }
		                        }
		                    }
		                }
		            }
		        });
		    });
		};

		this.getAllInstances = function(ctxt, instanceList, mdl) {
			that.withDb(ctxt, function(transaction) {
		            var ss = _getAllInstancesDataTableStmt(mdl.tableMetadata.dbTableName);
		            ctxt.sqlStatement = ss;
		            transaction.executeSql(ss.stmt, ss.bind, function(transaction, result) {
		                for ( var i = 0 ; i < result.rows.length ; i+=1 ) {
		                    var instance = result.rows.item(i);
		                    instanceList.push({
		                        instanceName: instance.instance_name,
		                        instance_id: instance.id,
		                        last_saved_timestamp: new Date(instance.timestamp),
		                        saved_status: instance.saved,
		                        locale: instance.locale
		                    });
		                }
		            });
		        });
		};

		this.deleteAll = function(ctxt, instanceId, dbTableName) {
			that.withDb( ctxt, function(transaction) {
	            var cs = _deleteDataTableStmt(dbTableName, instanceId);
	            ctxt.sqlStatement = cs;
	            transaction.executeSql(cs.stmt, cs.bind);
	        });
		};

		this.insertKeyValueMapData = function(ctxt, instanceId, kvMap, names, mdl) {
			that.withDb( ctxt, function(transaction) {
		            var is = _insertKeyValueMapDataTableStmt(mdl.tableMetadata.dbTableName, mdl.dataTableModel, instanceId, kvMap);
		            ctxt.sqlStatement = is;
		            transaction.executeSql(is.stmt, is.bind, function(transaction, result) {
		                ctxt.append("putDataKeyValueMap.success", names);
		            });
		        });
		};

		this.saveAllChanges = function(ctxt, instanceId, asComplete, mdl) {
			that.withDb( ctxt, 
		            function(transaction) {
		                var kvMap = {};
		                kvMap['saved'] = {value: (asComplete ? 'COMPLETE' : 'INCOMPLETE'), isInstanceMetadata: true };
		                var is = _insertKeyValueMapDataTableStmt(mdl.tableMetadata.dbTableName, mdl.dataTableModel, opendatakit.getCurrentInstanceId(), kvMap);
		                ctxt.sqlStatement = is;
		                transaction.executeSql(is.stmt, is.bind, function(transaction, result) {
		                    ctxt.append('save_all_changes.cleanup');
		                    // and now delete the change history...
		                    var cs = _deletePriorChangesDataTableStmt(mdl.tableMetadata.dbTableName, instanceId);
		                    ctxt.sqlStatement = cs;
		                    transaction.executeSql(cs.stmt, cs.bind);
		                });
		            }
		    );

		};

		//private methods
		function _selectTableDefinitionsDataStmt(table_id) {
		    return {
		            stmt : 'select * from table_definitions where table_id=?',
		            bind : [table_id]    
		        };
		}

		function _insertTableAndColumnProperties(transaction, ctxt, tlo, writeDatabase, mdl) {
		    var fullDef = {
		        table_definitions: [],
		        key_value_store_active: [],
		        column_definitions: []
		        };

		    var displayColumnOrder = [];
		    
		    // TODO: synthesize dbTableName from some other source...
		    var dbTableName = '_' + opendatakit.getSetting(tlo.formDef, 'form_id');
		    // dataTableModel holds an inversion of the tlo.formDef.model
		    //
		    //  elementKey : jsonSchemaType
		    //
		    // with the addition of:
		    //    isPersisted : true if elementKey is a dbColumnName
		    //    elementPath : pathToElement
		    //    elementSet : 'data'
		    //    listChildElementKeys : ['key1', 'key2' ...]
		    //
		    // within the jsonSchemaType to be used to transform to/from
		    // the model contents and data table representation.
		    //    
		    var dataTableModel = {};
		    var f;
		    for ( f in that.dataTablePredefinedColumns ) {
		        dataTableModel[f] = that.dataTablePredefinedColumns[f];
		    }
		    
		    // go through the supplied tlo.formDef model
		    // and invert it into the dataTableModel
		    var jsonDefn;
		    for ( f in tlo.formDef.model ) {
		        displayColumnOrder.push(f);
		        jsonDefn = _flattenElementPath( dataTableModel, null, f, null, tlo.formDef.model[f] );
		        fullDef.column_definitions.push( {
		            table_id: tlo.table_id,
		            element_key: jsonDefn.elementKey,
		            element_name: jsonDefn.elementName,
		            element_type: (jsonDefn.elementType == null ? jsonDefn.type : jsonDefn.elementType),
		            list_child_element_keys : ((jsonDefn.listChildElementKeys == null) ? null : JSON.stringify(jsonDefn.listChildElementKeys)),
		            is_persisted : jsonDefn.isPersisted,
		            joins: null
		        } );
		    }

		    // and now traverse the dataTableModel making sure all the
		    // elementSet: 'data' values have columnDefinitions entries.
		    //
		    for ( var dbColumnName in dataTableModel ) {
		        // case: simple type
		        // TODO: case: geopoint -- expand to different persistence columns
		        jsonDefn = dataTableModel[dbColumnName];
		        
		        if ( jsonDefn.elementSet == 'data' ) {
		            var surveyElementName = jsonDefn.elementName;
		            
		            fullDef.column_definitions.push( {
		                table_id: tlo.table_id,
		                element_key: dbColumnName,
		                element_name: jsonDefn.elementName,
		                element_type: (jsonDefn.elementType == null ? jsonDefn.type : jsonDefn.elementType),
		                list_child_element_keys : ((jsonDefn.type == 'array') ? jsonDefn.items : jsonDefn.properties),
		                is_persisted : true,
		                joins: null
		            } );
		            fullDef.key_value_store_active.push( {
		                table_id: tlo.table_id,
		                partition: "Column",
		                aspect: dbColumnName,
		                key: "displayVisible",
		                type: "boolean",
		                value: true
		            } );
		            fullDef.key_value_store_active.push( {
		                table_id: tlo.table_id,
		                partition: "Column",
		                aspect: dbColumnName,
		                key: "displayName",
		                type: "string",
		                value: surveyElementName
		            } );
		            fullDef.key_value_store_active.push( {
		                table_id: tlo.table_id,
		                partition: "Column",
		                aspect: dbColumnName,
		                key: "displayChoicesList",
		                type: "string",
		                value: ((jsonDefn.choicesList == null) ? null : JSON.stringify(tlo.formDef.choices[jsonDefn.choicesList]))
		            } );
		            fullDef.key_value_store_active.push( {
		                table_id: tlo.table_id,
		                partition: "Column",
		                aspect: dbColumnName,
		                key: "displayFormat",
		                type: "string",
		                value: jsonDefn.displayFormat
		            } );
		            fullDef.key_value_store_active.push( {
		                table_id: tlo.table_id,
		                partition: "Column",
		                aspect: dbColumnName,
		                key: "smsIn",
		                type: "boolean",
		                value: true
		            } );
		            fullDef.key_value_store_active.push( {
		                table_id: tlo.table_id,
		                partition: "Column",
		                aspect: dbColumnName,
		                key: "smsOut",
		                type: "boolean",
		                value: true
		            } );
		            fullDef.key_value_store_active.push( {
		                table_id: tlo.table_id,
		                partition: "Column",
		                aspect: dbColumnName,
		                key: "smsLabel",
		                type: "string",
		                value: null
		            } );
		            fullDef.key_value_store_active.push( {
		                table_id: tlo.table_id,
		                partition: "Column",
		                aspect: dbColumnName,
		                key: "footerMode",
		                type: "string",
		                value: '0'
		            } );
		        }
		    }

		    fullDef.table_definitions.push( { 
		        table_id: tlo.table_id, 
		        table_key: dbTableName, 
		        db_table_name: dbTableName, 
		        type: 'data', 
		        table_id_access_controls: null, 
		        sync_tag: "", 
		        last_sync_time: -1, 
		        sync_state: 'REST', 
		        transactioning: 0 } );

		    // construct the kvPairs to insert into kvstore
		    fullDef.key_value_store_active.push( { table_id: tlo.table_id, partition: "Table", aspect: "global", key: 'displayName', type: 'string', value: opendatakit.getSetting(tlo.formDef, 'form_title') } );
		    fullDef.key_value_store_active.push( { table_id: tlo.table_id, partition: "Table", aspect: "global", key: 'primeCols', type: 'string', value: '' } );
		    fullDef.key_value_store_active.push( { table_id: tlo.table_id, partition: "Table", aspect: "global", key: 'sortCol', type: 'string', value: '' } );
		    fullDef.key_value_store_active.push( { table_id: tlo.table_id, partition: "Table", aspect: "global", key: 'coViewSettings', type: 'string', value: '' } );
		    fullDef.key_value_store_active.push( { table_id: tlo.table_id, partition: "Table", aspect: "global", key: 'detailViewFile', type: 'string', value: '' } );
		    fullDef.key_value_store_active.push( { table_id: tlo.table_id, partition: "Table", aspect: "global", key: 'summaryDisplayFormat', type: 'string', value: '' } );
		    fullDef.key_value_store_active.push( { table_id: tlo.table_id, partition: "Table", aspect: "global", key: 'colOrder', type: 'string', value: JSON.stringify(displayColumnOrder) } );
		    fullDef.key_value_store_active.push( { table_id: tlo.table_id, partition: "Table", aspect: "global", key: 'ovViewSettings', type: 'string', value: '' } );

		    // get first property in fullDef -- we use native iteration ordering to step through the properties.
		    var tableToUpdate = null;
		    for ( var prop in fullDef ) {
		        tableToUpdate = prop;
		        break;
		    }

		    if ( writeDatabase ) {
		        var createTableCmd = _createTableStmt(dbTableName, dataTableModel);
		        ctxt.sqlStatement = createTableCmd;
		        transaction.executeSql(createTableCmd.stmt, createTableCmd.bind, function(transaction, result) {
		            that.fullDefHelper(transaction, ctxt, tableToUpdate, 0, fullDef, dbTableName, dataTableModel, tlo, mdl);
		        });
		    } else {
		        // we don't need to write the database -- just update everything
		        mdl.dataTableModel = dataTableModel;
		        that.coreGetAllTableMetadata(transaction, ctxt, tlo);
		    }
		}

		function _flattenElementPath( dbKeyMap, elementPathPrefix, elementName, elementKeyPrefix, jsonType ) {
		    var fullPath;
		    var elementKey;
		    var i = 0;

		    // remember the element name...
		    jsonType.elementName = elementName;
		    // and the set is 'data' because it comes from the data model...
		    jsonType.elementSet = 'data';
		    
		    // update element path prefix for recursive elements
		    elementPathPrefix = (elementPathPrefix == null ) ? elementName : (elementPathPrefix + '.' + elementName);
		    // and our own element path is exactly just this prefix
		    jsonType.elementPath = elementPathPrefix;

		    // use the user's elementKey if specified
		    elementKey = jsonType.elementKey;

		    if ( elementKey != null ) {
		        // throw an error if the elementkey is longer than 64 characters
		        // or if it is already being used and not by myself...
		        if ( elementKey.length > 64 ) {
		            throw new Error("supplied elementKey is longer than 64 characters");
		        }
		        if ( dbKeyMap[elementKey] != null && dbKeyMap[elementKey] != jsonType ) {
		            throw new Error("supplied elementKey is already used (autogenerated?) for another model element");
		        }
		        if ( elementKey.charAt(0) != '_' ) {
		            throw new Error("supplied elementKey does not start with underscore");
		        }
		    }
		    
		    if ( elementKey == null ) {
		        // synthesize an element key...
		        elementKey = (( elementKeyPrefix == null ) ? ('_' + elementName) : (elementKeyPrefix + '_' + elementName));
		    }

		    // assume the primitive types are persisted.
		    // this will be updated if there is an outer array or object
		    // that persists itself (below).
		    if ( jsonType.type == 'string' || 
		            jsonType.type == 'number' || 
		            jsonType.type == 'integer' ||
		            jsonType.type == 'boolean' ) {
		        // these should be persisted...
		        jsonType.isPersisted = true;
		    }

		    // and whether or not we persist, we need to add this to the dbKeyMap
		    //
		    if ( elementKey.length > 64 ) {
		        // shorten by eliminating silent consonants and vowels...
		        elementKey = elementKey.replace(/aehiouy/,'');
		    }
		    if ( elementKey.length > 64 ) {
		        // remove leading characters...
		        elementKey = elementKey.substring(elementKey.length - 64);
		    }
		        
		    // find a not-yet-used element key
		    i = 0;
		    while ( dbKeyMap[elementKey] != null ) {
		        elementKey = elementKey.substr(0,60);
		        elementKey = elementKey + _padWithLeadingZeros(i,4);
		        ++i;
		    }
		    // remember the elementKey we have chosen...
		    jsonType.elementKey = elementKey;
		    dbKeyMap[elementKey] = jsonType;

		    // handle the recursive structures...
		    if ( jsonType.type == 'array' ) {
		        // explode with subordinate elements
		        f = _flattenElementPath( dbKeyMap, elementPathPrefix, 'items', elementKey, jsonType.items );
		        jsonType.listChildElementKeys = [ f.elementKey ];
		        jsonType.isPersisted = true;
		    } else if ( jsonType.type == 'object' ) {
		        // object...
		        var hasProperties = false;
		        var e;
		        var f;
		        var listChildElementKeys = [];
		        for ( e in jsonType.properties ) {
		            hasProperties = true;
		            f = _flattenElementPath( dbKeyMap, elementPathPrefix, e, elementKey, jsonType.properties[e] );
		            listChildElementKeys.push(f.elementKey);
		        }
		        jsonType.listChildElementKeys = listChildElementKeys;
		        if ( !hasProperties ) {
		            jsonType.isPersisted = true;
		        }
		    }

		    if ( jsonType.isPersisted && (jsonType.listChildElementKeys != null)) {
		        // we have some sort of structure that is persisting
		        // clear the isPersisted tags on the nested elements
		        _clearPersistedFlag(dbKeyMap, jsonType.listChildElementKeys);
		    }
		    return jsonType;
		}

		function _createTableStmt( dbTableName, kvMap, tableConstraint ) {
	        // TODO: verify that dbTableName is not already in use...
	        var createTableCmd = 'CREATE TABLE IF NOT EXISTS "' + dbTableName + '"(';
	        var comma = '';
	        for ( var dbColumnName in kvMap ) {
	            var f = kvMap[dbColumnName];
	            if ( f.isPersisted ) {
	                createTableCmd += comma + dbColumnName + " ";
	                comma = ',';
	                if ( f.type == "string" ) {
	                    createTableCmd += "TEXT" + (f.isNotNullable ? " NOT NULL" : " NULL");
	                } else if ( f.type == "integer" ) {
	                    createTableCmd += "INTEGER" + (f.isNotNullable ? " NOT NULL" : " NULL");
	                } else if ( f.type == "number" ) {
	                    createTableCmd += "REAL" + (f.isNotNullable ? " NOT NULL" : " NULL");
	                } else if ( f.type == "boolean" ) {
	                    createTableCmd += "INTEGER" + (f.isNotNullable ? " NOT NULL" : " NULL");
	                } else if ( f.type == "object" ) {
	                    createTableCmd += "TEXT" + (f.isNotNullable ? " NOT NULL" : " NULL");
	                } else if ( f.type == "array" ) {
	                    createTableCmd += "TEXT" + (f.isNotNullable ? " NOT NULL" : " NULL");
	                } else {
	                    throw new Error("unhandled type: " + f.type);
	                }
	            }
	        }
	        if ( tableConstraint != null ) {
	            createTableCmd += comma + tableConstraint + " ";
	        }
	        createTableCmd += ');';
	        return  {
	            stmt : createTableCmd,
	            bind : []
	        };
	    }

	    function _padWithLeadingZeros( value, places ) {
	        var digits = [];
	        var d, i, s;
	        var sign = (value >= 0);
	        value = Math.abs(value);
	        while ( value != 0 ) {
	            d = (value % 10);
	            digits.push(d);
	            value = Math.floor(value/10);
	        }
	        while ( digits.length < places ) {
	            digits.push(0);
	        }
	        digits.reverse();
	        s = '';
	        for ( i = 0 ; i < digits.length ; ++i ) {
	            d = digits[i];
	            s += d;
	        }
	        return (sign ? '' : '-') + s;
	    }

	    function _clearPersistedFlag(dbKeyMap, listChildElementKeys) {
		    var i;
		    var f;
		    if ( listChildElementKeys != null ) {
		        for ( i = 0 ; i < listChildElementKeys.length ; ++i ) {
		            var f = listChildElementKeys[i];
		            var jsonType = dbKeyMap[f];
		            jsonType.isPersisted = false;
		            if ( jsonType.type == 'array' ) {
		                _clearPersistedFlag(dbKeyMap, jsonType.listChildElementKeys);
		            } else if ( jsonType.type == 'object' ) {
		                _clearPersistedFlag(dbKeyMap, jsonType.listChildElementKeys);
		            }
		        }
		    }
		}

		function _fromDatabaseToElementType( jsonType, value ) {
	        // date conversion elements...
	        var yyyy, mm, dd, hh, min, sec, msec, zsign, zhh, zmm;

	        if ( value == null ) {
	            if ( jsonType.isNotNullable ) {
	                throw new Error("unexpected null value for non-nullable field");
	            }
	            return null;
	        }
	        
	        if ( jsonType.type == 'array' ) {
	            value = JSON.parse(value);
	            // TODO: ensure object spec conformance on read?
	            return value;
	        } else if ( jsonType.type == 'object' ) {
	            if ( jsonType.elementType == 'date' ||
	                 jsonType.elementType == 'dateTime' ) {
	                // convert an iso8601 date yyyymmddTHH:MM:SS.ssszzzzz 
	                // to a Date object...
	                // TODO: FIX FRAGILE: loss of timezone info
	                yyyy = Number(value.substr(0,4));
	                mm = Number(value.substr(4,2))-1;// months are 0-11
	                dd = Number(value.substr(6,2));
	                hh = Number(value.substr(9,2));
	                min = Number(value.substr(12,2));
	                sec = Number(value.substr(15,2));
	                msec = Number(value.substr(18,3));
	                zsign = value.substr(21,1);
	                zhh = Number(value.substr(22,2));
	                zmm = Number(value.substr(24,2));
	                value = new Date(Date.UTC(yyyy,mm,dd,hh,min,sec,msec));
	                return value;
	            } else if ( jsonType.elementType == 'time' ) {
	                // convert an iso8601 time HH:MM:SS.ssszzzzz to a Date object...
	                // TODO: FIX FRAGILE: loss of timezone info
	                var idx = value.indexOf(':');
	                hh = Number(value.substring(0,idx));
	                min = Number(value.substr(idx+1,2));
	                sec = Number(value.substr(idx+3,2));
	                msec = Number(value.substr(idx+6,3));
	                zsign = value.substr(idx+10,1);
	                zhh = Number(value.substr(idx+11,2));
	                zmm = Number(value.substr(idx+13,2));
	                value = new Date(Date.UTC(0,0,0,hh,min,sec,msec));
	                return value;
	            } else {
	                value = JSON.parse(value);
	                return value;
	            }
	        } else if ( jsonType.type == 'boolean' ) {
	            return Boolean(value); // make it a boolean
	        } else if ( jsonType.type == 'integer' ) {
	            value = Number(value);
	            if ( Math.round(value) != value ) {
	                throw new Error("non-integer value for integer type");
	            }
	            return value;
	        } else if ( jsonType.type == 'number' ) {
	            return Number(value);
	        } else if ( jsonType.type == 'string' ) {
	            return '' + value;
	        } else {
	            throw new Error("unrecognized JSON schema type");
	        }
	    }

		function _reconstructElementPath(elementPath, jsonType, dbValue, topLevelObject) {
	        var value = _fromDatabaseToElementType( jsonType, dbValue );
	        var path = elementPath.split('.');
	        var e = topLevelObject;
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
	        e[term] = value;
	    }

	    function _selectColumnDefinitionsDataStmt(table_id) {
		    return {
		            stmt : 'select * from column_definitions where table_id=?',
		            bind : [table_id]    
		        };
		}

		function _selectAllColumnMetaDataStmt(table_id) {
		    return {
		            stmt : 'select aspect as element_key, key, type, value from key_value_store_active where table_id=? and partition=?',
		            bind : [table_id, "Column"]    
		        };
		}

		function _getAccessibleColumnKeyDefinition( key ) {
	        return that._columnKeyValueStoreActiveAccessibleKeys[key];
	    }

	    function _selectAllTableMetaDataStmt(table_id) {
		    return {
		            stmt : 'select key, type, value from key_value_store_active where table_id=? and partition=? and aspect=?',
		            bind : [table_id, "Table", "global"]    
		        };
		}

		function _getAccessibleTableKeyDefinition( key ) {
	        return that._tableKeyValueStoreActiveAccessibleKeys[key];
	    }

	    function _selectAllFromDataTableStmt(dbTableName, instanceId) {
		    var stmt = 'select * from "' + dbTableName + '" where id=? group by id having timestamp = max(timestamp)'; 
		    return {
		        stmt : stmt,
		        bind : [instanceId]
		    };
		}

		function _selectDataTableCountStmt(dbTableName, instanceId) {
		    var stmt = 'select count(*) as rowcount from "' + dbTableName + '" where id=?';
		    return {
		        stmt : stmt,
		        bind : [instanceId]
		    };
		}

		function _getAllInstancesDataTableStmt(dbTableName) {
		    return {
		            stmt : 'select instance_name, timestamp, saved, locale, id from "' +
		                    dbTableName + '" group by id having timestamp = max(timestamp) order by timestamp desc;',
		            bind : []
		            };
		}

		function _insertNewKeyValueMapDataTableStmt(dbTableName, dataTableModel, kvMap) {
		    var t = new Date();
		    var now = t.getTime();
		    
		    var bindings = [];
		    var processSet = {};
		    
		    var j, f, defElement, kvElement;
		    var comma = '';
		    
		    var stmt = 'insert into "' + dbTableName + '" (';
		    for ( f in dataTableModel ) {
		        defElement = dataTableModel[f];
		        if ( defElement.isPersisted ) {
		            stmt += comma;
		            comma = ', ';
		            stmt += '"' + f + '"';
		        }
		    }
		    stmt += ") values (";
		    comma = '';
		    for (f in dataTableModel) {    
		        defElement = dataTableModel[f];
		        if ( defElement.isPersisted ) {
		            stmt += comma;
		            comma = ', ';
		            kvElement = kvMap[f];
		            if ( kvElement != null ) {
		                // track that we matched the keyname...
		                processSet[f] = true;
		                if (kvElement.value == null) {
		                    stmt += "null";
		                } else {
		                    stmt += "?";
		                    bindings.push(kvElement.value);
		                }
		            } else if ( f == "last_mod_time" ) {
		                stmt += "?";
		                bindings.push(now);
		            } else if ( f == "timestamp" ) {
		                stmt += "?";
		                bindings.push(now);
		            } else {
		                // use default values from reference map...
		                if ( defElement['default'] == null ) {
		                    stmt += "null";
		                } else {
		                    stmt += "?";
		                    bindings.push(defElement['default']);
		                }
		            }
		        }
		    }
		    stmt += ');'; 
		    
		    for ( f in kvMap ) {
		        if ( processSet[f] != true ) {
		            console.error("_insertNewKeyValueMapDataTableStmt: kvMap contains unrecognized database column " + dbTableName + "." + f );
		        }
		    }
		    return {
		        stmt : stmt,
		        bind : bindings
		        };
		}

		function _deleteDataTableStmt(dbTableName, instanceid) {
		    return {
		        stmt : 'delete from "' + dbTableName + '" where id=?;',
		        bind : [instanceid]
		    };
		}

		function _insertKeyValueMapDataTableStmt(dbTableName, dataTableModel, instanceId, kvMap) {
		    var t = new Date();
		    var now = t.getTime();
		    
		    var bindings = [];
		    var processSet = {};
		    
		    var j, f, defElement, elementPathPair, kvElement, v;
		    var comma = '';
		    
		    var stmt = 'insert into "' + dbTableName + '" (';
		    for ( f in dataTableModel ) {
		        defElement = dataTableModel[f];
		        if ( defElement.isPersisted ) {
		            stmt += comma;
		            comma = ', ';
		            stmt += '"' + f + '"';
		        }
		    }
		    stmt += ") select ";
		    comma = '';
		    for (f in dataTableModel) {
		        defElement = dataTableModel[f];
		        if ( defElement.isPersisted ) {
		            stmt += comma;
		            comma = ', ';
		            var elementPath = defElement['elementPath'];
		            if ( elementPath == null ) elementPath = f;
		            // TODO: get kvElement for this elementPath
		            elementPathPair = _getElementPathPairFromKvMap(kvMap, elementPath);
		            if ( elementPathPair != null ) {
		                kvElement = elementPathPair.element;
		                // track that we matched the keyname...
		                processSet[elementPathPair.elementPath] = true;
		                if (kvElement.value == null) {
		                    stmt += "null";
		                } else {
		                    stmt += "?";
		                    // remap kvElement.value into storage value...
		                    v = _toDatabaseFromElementType(defElement, kvElement.value);
		                    bindings.push(v);
		                }
		            } else if ( f == "last_mod_time" ) {
		                stmt += "?";
		                bindings.push(now);
		            } else if ( f == "timestamp" ) {
		                stmt += "?";
		                bindings.push(now);
		            } else if ( f == "saved" ) {
		                stmt += "null";
		            } else {
		                stmt += '"' + f + '"';
		            }
		        }
		    }
		    stmt += ' from "' + dbTableName + '" where id=? group by id having timestamp = max(timestamp)'; 
		    bindings.push(instanceId);
		    
		    for ( f in kvMap ) {
		        if ( processSet[f] != true ) {
		            console.error("_insertKeyValueMapDataTableStmt: kvMap contains unrecognized database column " + dbTableName + "." + f );
		        }
		    }
		    return {
		        stmt : stmt,
		        bind : bindings
		        };
		}

		function _getElementPathPairFromKvMap(kvMap, elementPath) {
		    var path = elementPath.split('.');
		    var e = kvMap;
		    var i, j, term, value, pathChain;
		    // work from most specific to least specific searching for a value match
		    for (j = path.length-1 ; j >= 0 ; --j) {
		        pathChain = '';
		        for (i = 0 ; i <= j ; ++i) {
		            pathChain = '.' + path[i] + pathChain;
		        }
		        pathChain = pathChain.substring(1);
		        if ( kvMap[pathChain] != null ) {
		            // found a definition...
		            term = kvMap[pathChain];
		            value = term.value;
		            // now find the definition for this element
		            // within the composite value
		            for ( i = j+1 ; i <= path.length-1 ; ++i ) {
		                value = value[path[i]];
		                if ( value == null ) break;
		            }
		            var e = {};
		            var f;
		            for ( f in term ) {
		                if ( f != "value" ) {
		                    e[f] = term[f];
		                }
		            }
		            e['value'] = value;
		            return {element: e, elementPath: pathChain};
		        }
		    }
		    return null;
		}

		function _toDatabaseFromElementType( jsonType, value ) {
	        var refined;
	        var itemType;
	        var item;
	        var itemValue;
	        // date conversion elements...
	        var yyyy, mm, dd, hh, min, sec, msec, zsign, zhh, zmm;

	        if ( value == null ) {
	            if ( jsontype.isNotNullable ) {
	                throw new Error("unexpected null value for non-nullable field");
	            }
	            return null;
	        }
	        
	        if ( jsonType.type == 'array' ) {
	            // ensure that it is an array of the appropriate type...
	            if ( value instanceof Array ) {
	                refined = [];
	                itemType = jsonType.items;
	                if ( itemType == null ) {
	                    value = JSON.stringify(value);
	                } else {
	                    for ( item = 0 ; item < value.length ; ++item ) {
	                        itemValue = _toDatabaseFromElementType( itemType, value[item] );
	                        refined.push(itemValue);
	                    }
	                    value = JSON.stringify(refined);
	                }
	            } else {
	                throw new Error("unexpected non-array value");
	            }
	            return value;
	        } else if ( jsonType.type == 'object' ) {
	            if ( jsonType.elementType == 'dateTime' ||
	                 jsonType.elementType == 'date' ) {

	                yyyy = value.getUTCFullYear();
	                mm = value.getUTCMonth() + 1; // months are 0-11
	                dd = value.getUTCDate();
	                hh = value.getUTCHours();
	                min = value.getUTCMinutes();
	                sec = value.getUTCSeconds();
	                msec = value.getUTCMilliseconds();
	                zsign = 'Z';
	                zhh = '';
	                zmm = '';
	                value = _padWithLeadingZeros(yyyy,4) + 
	                        _padWithLeadingZeros(mm,2) +
	                        _padWithLeadingZeros(dd,2) + 'T' +
	                        _padWithLeadingZeros(hh,2) + ':' +
	                        _padWithLeadingZeros(min,2) + ':' +
	                        _padWithLeadingZeros(sec,2) + '.' +
	                        _padWithLeadingZeros(msec,3) + 'Z';
	                return value;
	            } else if ( jsonType.elementType == 'time' ) {
	                yyyy = value.getUTCFullYear();
	                mm = value.getUTCMonth(); // months are 0-11
	                if ( mm != 0 || yyyy != 1970 ) {
	                    throw new Error("time value is not based upon Jan 1, 1970");
	                }
	                // TODO: this is broken w.r.t. leap years
	                dd = value.getUTCDate();
	                hh = value.getUTCHours() + 24*(dd);
	                min = value.getUTCMinutes();
	                sec = value.getUTCSeconds();
	                msec = value.getUTCMilliseconds();
	                zsign = 'Z';
	                zhh = '';
	                zmm = '';
	                value = _padWithLeadingZeros(hh,2) + ':' +
	                        _padWithLeadingZeros(min,2) + ':' +
	                        _padWithLeadingZeros(sec,2) + '.' +
	                        _padWithLeadingZeros(msec,3) + 'Z';
	                return value;
	            } else if ( !jsonType.properties ) {
	                // this is an opaque BLOB w.r.t. database layer
	                return JSON.stringify(value);
	            } else {
	                // otherwise, enforce spec conformance...
	                // Only values in the properties list, and those
	                // must match the type definitions recursively.
	                refined = {};
	                for ( item in jsonType.properties ) {
	                    if ( value[item] != null ) {
	                        itemType = jsonType.properties[item];
	                        itemValue = _toDatabaseFromElementType(itemType, value[item]);
	                        if ( itemValue != null ) {
	                            refined[item] = itemValue;
	                        }
	                    }
	                }
	                value = JSON.stringify(refined);
	                return value;
	            }
	        } else if ( jsonType.type == 'boolean' ) {
	            return (value ? '1' : '0'); // make it a boolean
	        } else if ( jsonType.type == 'integer' ) {
	            value = '' + Math.round(value);
	            return value;
	        } else if ( jsonType.type == 'number' ) {
	            return '' + value;
	        } else if ( jsonType.type == 'string' ) {
	            return '' + value;
	        } else {
	            throw new Error("unrecognized JSON schema type");
	        }
	    }

	    function _deletePriorChangesDataTableStmt(dbTableName, instanceId) {
		    var stmt = 'delete from "' + dbTableName + '" where id=? and timestamp not in (select max(timestamp) from "' + dbTableName + '" where id=?);';
		    return {
		        stmt : stmt,
		        bind : [instanceId, instanceId]
		    };
		}

	}

	return new DefaultDbImpl($, opendatakit);
});
function PagerParams(offset, max) {
	return {
		offset : offset || 0,
		max : max || 20
	};
}

var OrderBy = {
	Asc : function(fieldName) {
		return {
			fieldName : fieldName,
			direction : "ASC"
		};
	},
	Desc : function(fieldName) {
		return {
			fieldName : fieldName,
			direction : "DESC"
		};
	}
};

function FilterParams() {
	var list = {};
	var ret = {
		build : function() {
			return list;
		}
	};

	ret.add = function(field, command, values) {
		list[field] = {
			command : command,
			values : values
		};
		return ret;
	};

	ret.equals = function(field, equalsTo) {
		return ret.add(field, "equal", [ equalsTo ]);
	};

	ret.contain = function(field, subElement) {
		return ret.add(field, "contain", [ subElement ]);
	};

	// TODO: Add other shortcut methods

	return ret;
}

function EasyCrudQueryParams(pagerParams, orderByArr, filterParams) {
	var ret = {
		pagerParams : pagerParams
	};
	if (!!orderByArr) {
		ret.orderBy = orderByArr;
	}
	if (!!filterParams) {
		ret.filterParams = filterParams;
	}
	return ret;
}

function CrudUrlProviderDefault(baseUrl) {
	return {
		getBase : function() {
			return contextPath + baseUrl;
		}
	};
}

KfCtrlApp.factory('CrudAjaxClientFactory', [ 'HttpFactory', function(HttpFactory) {
	/**
	 * Provide impl for crudUrlProvider.getBase() -- it must return base url for
	 * all queries to this service
	 */
	return function(crudUrlProvider) {
		var appendReferencesToResolveUrlParam = function(url, referencesToResolve) {
			if (!referencesToResolve || !referencesToResolve.length || referencesToResolve.length == 0) {
				return url;
			}
			var hasQMark = url.indexOf('?') > 0;
			var ret = "";
			for (var i = 0; i < referencesToResolve.length; i++) {
				if (ret != "") {
					ret += "&";
				}
				ret += "referencesToResolve=" + referencesToResolve[i];
			}
			return url + (hasQMark ? "&" : "?") + ret;
		};
		return {
			getList : function(pagerParams, orderBy, needPerms, referencesToResolve, callback) {
				var url = crudUrlProvider.getBase();
				var oneAdded = false;
				if (pagerParams) {
					oneAdded = true;
					url += '?offfset=' + pagerParams.offset + '&max=' + pagerParams.max;
				}

				if (orderBy) {
					url += (oneAdded ? "&" : "?") + "fieldName=" + fieldName + "&direction=" + direction;
					oneAdded = true;
				}

				url += (oneAdded ? "&" : "?") + "needPerms=" + (needPerms || false);
				url = appendReferencesToResolveUrlParam(url, referencesToResolve);

				return HttpFactory.get(url, callback);
			},
			getListWithQuery : function(easyCrudQueryParams, needPerms, referencesToResolve, callback) {
				var url = crudUrlProvider.getBase() + "/query";
				url += "?needPerms=" + (needPerms || false);
				url = appendReferencesToResolveUrlParam(url, referencesToResolve);
				return HttpFactory.post(url, easyCrudQueryParams, callback);
			},
			getItem : function(id, needPerms, referencesToResolve, callback) {
				var url = crudUrlProvider.getBase() + "/" + id;
				url += "?needPerms=" + (needPerms || false);
				url = appendReferencesToResolveUrlParam(url, referencesToResolve);
				return HttpFactory.get(url, callback);
			},
			createItem : function(dto, needPerms, callback) {
				var url = crudUrlProvider.getBase();
				url += "?needPerms=" + (needPerms || false);
				return HttpFactory.post(url, dto, callback);
			},
			updateItem : function(dto, needPerms, callback) {
				var url = crudUrlProvider.getBase() + "/" + dto.id;
				url += "?needPerms=" + (needPerms || false);
				return HttpFactory.put(url, dto, callback);
			},
			deleteItem : function(id, callback) {
				return HttpFactory.httpDelete(crudUrlProvider.getBase() + "/" + id, callback);
			}
		};
	};
} ]);

function SearchPredicateByString(searchStr) {
	return function(candidateRow) {
		if (!candidateRow || !searchStr) {
			return false;
		}

		for ( var field in candidateRow) {
			if (!candidateRow.hasOwnProperty(field)) {
				continue;
			}
			var val = candidateRow[field];
			if (typeof val != "string") {
				continue;
			}
			if (val.toLowerCase().indexOf(searchStr.toLowerCase()) >= 0) {
				return true;
			}
		}

		return false;
	};
}

/**
 * Designed to be used with lists of relatively small capacity. Perfect for
 * dictionaries
 */
KfCtrlApp.factory('CrudDictionaryFactory', [ 'CrudAjaxClientFactory', function(CrudAjaxClientFactory) {
	return function(crudUrlProvider) {
		var restClient = CrudAjaxClientFactory(crudUrlProvider);
		var loadedRows = [];

		var ret = [];

		// 1000 here is a magic number. Assuming there
		// will be no more than that for dictionary
		// items
		var loadAll = function() {
			return restClient.getList(PagerParams(0, 1000), null, null, null, function(err, response) {
				if (err) {
					loadedRows = [];
					return loadedRows;
				}
				return loadedRows = response.data.rows;
			}).then(function() {
				return loadedRows;
			});
		};

		var loadAllPromise = loadAll(); // this will trigger load immediately

		ret.localSearch = function(searchPredicate) {
			return loadAllPromise.then(function(allRows) {
				var searchResults = [];
				for (var i = 0; i < allRows.length; i++) {
					if (searchPredicate(allRows[i]) === true) {
						searchResults.push(allRows[i]);
					}
				}
				return searchResults;
			});
		};

		ret.all = function() {
			return loadAllPromise;
		};

		ret.createItem = function(dto, callback) {
			return loadAllPromise.then(function() {
				return restClient.createItem(dto, false, callback).then(function(response) {
					if (!response) {
						return;
					}
					loadedRows.push(response.data.row);
					return response.data.row;
				});
			});
		};

		return ret;
	};
} ]);

KfCtrlApp.controller('CrudListControllerBase', [
		'$scope',
		'$mdDialog',
		'$timeout',
		function($scope, $mdDialog, $timeout) {
			// NOTE: This has to be provided by subclass - actual
			// controller
			$scope.crudAjaxClient = null;
			$scope.ajax = false;

			// Data
			$scope.rows = [];
			$scope.totalResults = 0;
			$scope.perms = {
				rows : null,
				table : null
			};
			$scope.refsResolved = {};
			$scope.refTables = {};

			$scope.selected = [];

			$scope.isQuickSearchEnabled = false;
			/**
			 * This can be implemented by subclass to provide list of fields
			 * ready for text search by substring
			 */
			$scope.getSearchableField = function() {
				// i.e. 'name'
				return null;
			};
			$scope.buildFilterParamsForSearchStr = function(searchStr) {
				if (!$scope.getSearchableField()) {
					return null;
				}

				return FilterParams().contain($scope.getSearchableField(), searchStr).build();
			};

			/**
			 * Array of references to resolve when querying data
			 */
			$scope.referencesToResolve = null;
			$scope.fieldsToRemoveBeforeSubmit = [ 'actions' ];
			/**
			 * Add your row enrichers here. Row enricher is supposed to add new
			 * fields to the original row
			 */
			$scope.rowEnrichers = [];
			$scope.enrichFields = function(rows, refsResolved, tables) {
				if (!$scope.rowEnrichers || $scope.rowEnrichers.length == 0) {
					return;
				}

				for (var i = 0; i < rows.length; i++) {
					var row = rows[i];

					for (var m = 0; m < $scope.rowEnrichers.length; m++) {
						var tempFieldsAdded = [];
						$scope.rowEnrichers[m](row, refsResolved, tables, tempFieldsAdded);
						// now record fields to be removed before submit, as
						// they are not the part of the Dto
						for (var j = 0; j < tempFieldsAdded.length; j++) {
							var fieldAdded = tempFieldsAdded[j];
							if ($scope.fieldsToRemoveBeforeSubmit.indexOf(fieldAdded) < 0) {
								$scope.fieldsToRemoveBeforeSubmit.push(fieldAdded);
							}
						}
					}
				}
			};
			$scope.getRowWithoutEnrichedFields = function(row) {
				var ret = clone(row);
				for (var i = 0; i < $scope.fieldsToRemoveBeforeSubmit.length; i++) {
					var fieldToRemove = $scope.fieldsToRemoveBeforeSubmit[i];
					delete ret[fieldToRemove];
				}
				return ret;
			};

			/**
			 * To be provided by subclass
			 */
			$scope.getDefaultOrderBy = function() {
				// i.e. OrderBy.Asc('name')
				return null;
			};

			$scope.buildInitialQueryParams = function() {
				var orderBy = $scope.getDefaultOrderBy();
				var orderByArr = !orderBy ? null : [ orderBy ];
				return EasyCrudQueryParams(PagerParams(0, $scope.pagerOptions[0]), orderByArr, null);
			};

			$scope.pagerOptions = [ 20, 100 ];
			var calculatePage = function(pagerParams) {
				return Math.floor(pagerParams.offset / pagerParams.max) + 1;
			};

			$scope.calculateFieldSort = function(orderByArr) {
				if (!orderByArr || orderByArr.length == 0) {
					return '';
				}

				var o = orderByArr[0];
				if (o == null) {
					return '';
				}
				return (o.direction == OrderBy.Desc('none').direction ? '-' : '') + o.fieldName;
			};

			/**
			 * View parameters, excluding search criteria
			 */
			$scope.viewParams = {
				queryParams : null,
				page : 1,
				fieldSort : null
			};

			// Quick search feature
			$scope.searchStr = '';
			$scope.searchInputOptions = {
				debounce : 400
			};
			$scope.cancelSearch = function() {
				$scope.searchStr = '';
			};
			$scope.hideSearchIcon = false;
			$scope.hideSearchDiscard = true;
			var viewParamsBookmark = null;
			$scope.$watch('searchStr', function(newValue, oldValue) {
				if (newValue === oldValue) {
					return;
				}

				// this 'if' here is for nice flip-coin animation for search
				// icon
				if (newValue) {
					$scope.hideSearchIcon = true;
					$timeout(function() {
						$scope.hideSearchDiscard = false;
					}, 160);
				} else {
					$scope.hideSearchDiscard = true;
					$timeout(function() {
						$scope.hideSearchIcon = false;
					}, 160);
				}

				if (!oldValue) {
					$scope.selected = [];
					viewParamsBookmark = $scope.viewParams;
					$scope.viewParams = clone(viewParamsBookmark);
				}
				if (newValue !== oldValue) {
					$scope.viewParams.page = 1;
					$scope.viewParams.queryParams.filterParams = FilterParams().contain($scope.getSearchableField(), $scope.searchStr)
							.build();
				}
				if (!newValue) {
					$scope.selected = [];
					$scope.viewParams = viewParamsBookmark;
				}
				$scope.reload();
			});

			$scope.start = function() {
				$scope.isQuickSearchEnabled = !!$scope.getSearchableField();

				$scope.viewParams.queryParams = $scope.buildInitialQueryParams();
				$scope.viewParams.page = calculatePage($scope.viewParams.queryParams.pagerParams);
				$scope.viewParams.fieldSort = $scope.calculateFieldSort($scope.viewParams.queryParams.orderBy);

				$scope.$watch('viewParams.page', function(newValue, oldValue) {
					$scope.viewParams.queryParams.pagerParams.offset = (newValue - 1) * $scope.viewParams.queryParams.pagerParams.max;
				});

				$scope.$watch('viewParams.fieldSort', function(newValue, oldValue) {
					if (!newValue) {
						$scope.viewParams.queryParams.orderBy = null;
						return;
					}

					var isDesc = !!newValue && newValue.indexOf('-') == 0;
					var field = '';
					if (isDesc) {
						$scope.viewParams.queryParams.orderBy = [ OrderBy.Desc(newValue.substring(1)) ];
					} else {
						$scope.viewParams.queryParams.orderBy = [ OrderBy.Asc(newValue) ];
					}
				});

				$scope.reload();
			};

			$scope.onDataFromServer = function(err, result) {
				$scope.ajax = false;

				if (err) {
					alert("Faield to get list from server: " + JSON.stringify(err.data));
					return;
				}

				$scope.entityTypeName = msg(result.data.entityMessageCode);

				var newRows = result.data.rows;
				$scope.refsResolved = result.data.refsResolved || {};
				$scope.refTables = !!result.data.refs ? result.data.refs.tables : {};
				$scope.enrichFields(newRows, $scope.refsResolved, $scope.refTables);
				$scope.rows = newRows;

				$scope.totalResults = result.data.totalResults;
				$scope.viewParams.queryParams.pagerParams = result.data.pagerParams;
				$scope.viewParams.page = calculatePage($scope.viewParams.queryParams.pagerParams);
				$scope.perms.rows = result.data.rowPermissions;
				$scope.perms.table = result.data.tablePermissions;

				$scope.populateRowsActions($scope.rows, $scope.refsResolved, $scope.refTables);
				$scope.tableActions = $scope.buildTableActions($scope.perms.table);
			};

			$scope.reload = function() {
				$scope.ajax = true;
				var promise = $scope.crudAjaxClient.getListWithQuery($scope.viewParams.queryParams, true, $scope.referencesToResolve,
						$scope.onDataFromServer);
				$scope.promise = promise;
			};

			$scope.isRowActionsEnabled = function() {
				return true;
			}

			$scope.isActionAllowedForRow = function(row, action) {
				return (!!$scope.perms.table && $scope.perms.table[action])
						|| (!!$scope.perms.rows && $scope.perms.rows[row.id] && $scope.perms.rows[row.id][action]);
			};

			$scope.getCrudRowActionsFor = function(row) {
				var canEdit = $scope.isActionAllowedForRow(row, 'update');
				var canDelete = $scope.isActionAllowedForRow(row, 'delete');
				var ret = [];
				if (canEdit && $scope.openDialogForUpdate) {
					ret.push({
						title : msgs['action.update'],
						handler : function(event, row) {
							$scope.openDialogForUpdate(event, row);
						}
					});
				}
				if (canDelete && $scope.openDialogForDelete) {
					ret.push({
						title : msgs['action.delete'],
						handler : function(event, row) {
							$scope.openDialogForDelete(event, row);
						}
					});
				}
				return ret;
			};

			/**
			 * Subclass supposed to override this method to provide custom
			 * actions if needed
			 */
			$scope.getAdditionalRowActionsFor = function(row) {
				return [];
			};

			$scope.buildRowActionsFor = function(row, refsResolved, refTables) {
				var ret = [];
				var crudActions = $scope.getCrudRowActionsFor(row);
				var additionalActions = $scope.getAdditionalRowActionsFor(row, refsResolved, refTables);
				ret = ret.concat(crudActions);
				if (crudActions.length > 0 && additionalActions.length > 0) {
					ret.push(null);
				}
				ret = ret.concat(additionalActions);
				return ret;
			};

			$scope.populateRowsActions = function(rows, refsResolved, refTables) {
				for (var i = 0; i < rows.length; i++) {
					var row = rows[i];
					row.actions = $scope.buildRowActionsFor(row, refsResolved, refTables);
				}
			};

			$scope.tableActions = [];

			/**
			 * That is supposed to be overridden by subclass and provide any
			 * additional table actions to use
			 */
			$scope.buildAdditionalTableActions = function(permsTable) {
				return [];
			};

			$scope.buildTableActions = function(permsTable) {
				var ret = [];
				if (!!permsTable && permsTable.create && $scope.openDialogForCreate != null) {
					ret.push({
						title : msgs['action.create'],
						icon : 'create',
						handler : function(event) {
							$scope.openDialogForCreate(event);
						}
					});
				}
				ret = ret.concat($scope.buildAdditionalTableActions(permsTable));
				return ret;
			};

			// Create new item dialog
			// Subclass these to configure behavior
			$scope.dialogForCreateControllerName = 'CrudItemFormDialogBase';
			$scope.dialogForCreateTemplate = staticResourcesBase + '/static/templates/crud/item-form-dialog.tmpl.html';
			$scope.dialogForCreateParamsBuilder = function() {
				return {
					dialogTitle : msg('action.create') + " " + $scope.entityTypeName
				};
			};
			$scope.onNewItemCreated = function(dto) {
				$scope.reload();
			};
			$scope.openDialogForCreate = function(event) {
				$mdDialog.show({
					controller : $scope.dialogForCreateControllerName,
					templateUrl : $scope.dialogForCreateTemplate,
					parent : angular.element(document.body),
					targetEvent : event,
					clickOutsideToClose : true,
					fullscreen : true,
					locals : {
						dialogParams : $scope.dialogForCreateParamsBuilder(),
						restClient : $scope.crudAjaxClient
					}
				}).then(function(dto) {
					$scope.onNewItemCreated(dto);
				}, function() {
					// no action on cancel
				});
			};

			// Update item dialog
			// Subclass these to configure behavior
			$scope.dialogForUpdateControllerName = 'CrudItemFormDialogBase';
			$scope.dialogForUpdateTemplate = staticResourcesBase + '/static/templates/crud/item-form-dialog.tmpl.html';
			$scope.dialogForUpdateParamsBuilder = function(row) {
				return {
					existingRow : clone(row),
					dtoCanonizer : $scope.getRowWithoutEnrichedFields,
					dialogTitle : msg('action.update') + " " + $scope.entityTypeName
				};
			};
			$scope.onRowUpdated = function(dto) {
				$scope.reload();
			};
			$scope.openDialogForUpdate = function(event, row) {
				$mdDialog.show({
					controller : $scope.dialogForUpdateControllerName,
					templateUrl : $scope.dialogForUpdateTemplate,
					parent : angular.element(document.body),
					targetEvent : event,
					clickOutsideToClose : true,
					fullscreen : true,
					locals : {
						dialogParams : $scope.dialogForUpdateParamsBuilder(row),
						restClient : $scope.crudAjaxClient
					}
				}).then(function(dto) {
					$scope.onRowUpdated(dto);
				}, function() {
					// no action on cancel
				});
			};

			// Delete item
			/**
			 * Subclass it for customized item name output. This used in
			 * confirmation dialogs to refer to 1 row
			 */
			$scope.getRowName = function(row) {
				return '' + row.id;
			};
			$scope.onItemDeleted = function(dto) {
				$scope.reload();
			};
			$scope.openDialogForDelete = function(event, row) {
				var text = msg('phrase.areYouSureToDelete', $scope.getRowName(row));
				var confirm = $mdDialog.confirm().title(msgs['phrase.pleaseConfirm']).textContent(text).ariaLabel(text).targetEvent(event)
						.ok(msgs['term.yes']).cancel(msgs['term.no']);

				$mdDialog.show(confirm).then(function() {
					$scope.promise = $scope.crudAjaxClient.deleteItem(row.id, function(err, data) {
						if (!err) {
							$scope.onItemDeleted(row);
							return;
						}

						if (!!err.data) {
							alert(msg('phrase.couldNotDeleteObject', $scope.getRowName(row)) + '. ' + JSON.stringify(err.data));
						} else {
							alert(msg('phrase.couldNotDeleteObject', $scope.getRowName(row)) + '. Generic failure.');
						}
					});
				}, function() {
					// no problem
				});
			};
		} ]);

/**
 * Strategy to concat all dictionary rows and put it as 1 field using
 * rowToStringStrategy strategy.
 * 
 * @param refFromInitiatorToM2m
 *            name of the reference (same as in ReferenceRegistry). This
 *            reference comes from reference initiator and goes to m2m
 *            (intermediate) table.
 * @param refFromM2mToDictionary
 *            this is a reference name from m2m table to dictionary table.
 * @param rowToStringStrategy
 *            strategy to convert dictionary row to String when rendering table
 * @returns new RowEnricher strategy
 */
function ManyToManyReferencesRowEnricher(refFromInitiatorToM2m, refFromM2mToDictionary, enrichingStrategy) {
	return function(initiatorRow, refsResolved, tables, outAddedFields) {
		try {
			var refToM2m = refsResolved[refFromInitiatorToM2m];
			var m2mTable = tables[refToM2m.toEntity].rows;

			var refToDict = refsResolved[refFromM2mToDictionary];
			var dictTable = tables[refToDict.toEntity].rows;

			var dictionaryRowsArr = [];
			for ( var rowId in m2mTable) {
				if (!m2mTable.hasOwnProperty(rowId)) {
					continue;
				}
				var m2mRow = m2mTable[rowId];
				if (initiatorRow[refToM2m.fromField] == m2mRow[refToM2m.toField]) {
					// NOTE: Here we assuming that m2m table references dict
					// using
					// primary key
					dictionaryRowsArr.push(dictTable[m2mRow[refToDict.fromField]]);
				}
			}

			var newFields = enrichingStrategy(initiatorRow, dictionaryRowsArr);
			for ( var fld in newFields) {
				if (!newFields.hasOwnProperty(fld)) {
					continue;
				}
				initiatorRow[fld] = newFields[fld];
				outAddedFields.push(fld);
			}
		} catch (e) {
			console.log("ManyToManyReferencesRowEnricher failed. " + e);
		}
	}
}

function OneToManyReferencesRowEnricher(referenceName, enrichingStrategy) {
	return function(initiatorRow, refsResolved, tables, outAddedFields) {
		try {
			var refToDict = refsResolved[referenceName];
			var dictTable = tables[refToDict.toEntity].rows;

			var srcItemId = initiatorRow[refToDict.fromField];
			var refedRows = [];
			for (var candidateRowIdx in dictTable) {
				if (!dictTable.hasOwnProperty(candidateRowIdx)) {
					continue;
				}
				var candidateRow = dictTable[candidateRowIdx];
				if (candidateRow[refToDict.toField] != srcItemId) {
					continue;
				}
				refedRows.push(candidateRow);
			}

			var newFields = enrichingStrategy(initiatorRow, refedRows);
			for ( var fld in newFields) {
				if (!newFields.hasOwnProperty(fld)) {
					continue;
				}
				initiatorRow[fld] = newFields[fld];
				outAddedFields.push(fld);
			}
		} catch (e) {
			console.log("OneToManyReferencesRowEnricher failed. " + e);
		}
	}
}


function ManyToOneReferencesRowEnricher(referenceName, enrichingStrategy) {
	return function(initiatorRow, refsResolved, tables, outAddedFields) {
		try {
			var refToDict = refsResolved[referenceName];
			var dictTable = tables[refToDict.toEntity].rows;

			var dictItemId = initiatorRow[refToDict.fromField];
			var dictItem = dictTable[dictItemId]; // NOTE: We're ignoring
													// "toField" here, assuming
													// it's always an ID

			var newFields = enrichingStrategy(initiatorRow, dictItem);
			for ( var fld in newFields) {
				if (!newFields.hasOwnProperty(fld)) {
					continue;
				}
				initiatorRow[fld] = newFields[fld];
				outAddedFields.push(fld);
			}
		} catch (e) {
			console.log("ManyToOneReferencesRowEnricher failed. " + e);
		}
	}
}

function OneToOneReferencesRowEnricher(referenceName, enrichingStrategy) {
	return function(initiatorRow, refsResolved, tables, outAddedFields) {
		try {
			var refToDict = refsResolved[referenceName];
			var dictTable = tables[refToDict.toEntity].rows;

			var fromId = initiatorRow[refToDict.fromField];

			// now we need to scan target table and find items which match this item
			for (toIdx in dictTable) {
				if (!dictTable.hasOwnProperty(toIdx)) {
					continue;
				}
				var toRpw = dictTable[toIdx];
				if (fromId == toRpw[refToDict.toField]) {
					var newFields = enrichingStrategy(initiatorRow, toRpw);
					for ( var fld in newFields) {
						if (!newFields.hasOwnProperty(fld)) {
							continue;
						}
						initiatorRow[fld] = newFields[fld];
						outAddedFields.push(fld);
					}
					
					break;
				}
			}
		} catch (e) {
			console.log("ManyToOneReferencesRowEnricher failed. " + e);
		}
	}
}

/**
 * This controller supposed to be sub-classed
 */
KfCtrlApp.controller('CrudItemFormDialogBase', [ '$scope', '$mdDialog', 'dialogParams', 'restClient', '$q',
		function($scope, $mdDialog, dialogParams, restClient, $q) {
			$scope.restClient = restClient;
			$scope.params = dialogParams;

			$scope.formState = {
				ajax : 0,
				lastExceptionMessage : null,
				ve : {}
			};
			$scope.formState.canSubmit = function() {
				return $scope.formState.ajax == 0;
			};

			$scope.dto = {
				formField : "sample value"
			};

			/**
			 * To be sub-classed
			 */
			$scope.initialDtoBuilder = function() {
				return {};
			};

			var defaultSubmitActionTitle = dialogParams.existingRow ? msgs['action.update'] : msgs['action.create'];
			$scope.dialogTitle = dialogParams.dialogTitle || defaultSubmitActionTitle;
			$scope.submitButtonTitle = dialogParams.submitButtonTitle || defaultSubmitActionTitle;
			$scope.cancelButtonTitle = dialogParams.cancelButtonTitle || msgs['action.cancel'];

			$scope.fields = [];
			$scope.buildFields = function() {
				return [ {
					title : 'Sample title',
					name : 'formField',
					type : "text",
					hint : 'Optional hint',
					isRequired : false
				} ];
			};
			$scope.field = function(name) {
				for (var i = 0; i < $scope.fields.length; i++) {
					if ($scope.fields[i].name === name) {
						return $scope.fields[i];
					}
				}
			};

			$scope.submitDataToServer = function(callback) {
				var dtoToSubmit = !!dialogParams.dtoCanonizer ? dialogParams.dtoCanonizer($scope.dto) : $scope.dto;
				if (dialogParams.existingRow) {
					return $scope.restClient.updateItem(dtoToSubmit, false, callback);
				} else {
					return $scope.restClient.createItem(dtoToSubmit, false, callback);
				}
			};

			$scope.collectFieldsPreSubmitPromises = function(fields) {
				var ret = [];
				for (var i = 0; i < fields.length; i++) {
					var c = fields[i].controller;
					if (!c || typeof c.getPreSubmitPromises == 'undefined') {
						continue;
					}
					ret = ret.concat(c.getPreSubmitPromises());
				}
				return ret;
			};

			$scope.collectFieldsPostSubmitPromisesBuilders = function(fields) {
				var ret = [];
				for (var i = 0; i < fields.length; i++) {
					var c = fields[i].controller;
					if (!c || typeof c.handlePostSubmit == 'undefined') {
						continue;
					}
					ret.push(c.handlePostSubmit);
				}
				return ret;
			};

			$scope.save = function() {
				$scope.formState.ajax++;

				// Ask fields if they want to process before we submit dto
				var preSubmitPromises = $scope.collectFieldsPreSubmitPromises($scope.fields);
				var p = preSubmitPromises.length == 0 ? $q.when("doesnt matter") : $q.all(preSubmitPromises);

				// Now let's submit dto changes
				p = p.then(function() {
					return $scope.submitDataToServer(function(err, response) {
						$scope.formState.lastExceptionMessage = null;
						$scope.formState.ve = {};

						if (!err) {
							copyFields(response.data.row, $scope.dto);
							return;
						}

						if (!!err.data) {
							if (!!err.data.ve) {
								$scope.formState.ve = err.data.ve;
								return;
							} else if (!!err.data.exc) {
								$scope.formState.lastExceptionMessage = err.data.exc;
								return;
							}
						}
						$scope.formState.lastExceptionMessage = "Unrecognized error during call to the server";
					});
				});

				// Some fields might required post-submit actions, i.e. m2m
				// fields will need to submit all associations
				var postSubmitPromiseBuilders = $scope.collectFieldsPostSubmitPromisesBuilders($scope.fields);
				if (postSubmitPromiseBuilders.length > 0) {
					p = p.then(function(response) {
						var promises = postSubmitPromiseBuilders.map(function(x) {
							return x();
						}).filter(function(x) {
							return x != null;
						});

						return $q.all(promises).then(function() {
							return response;
						}, function(err) {
							return $q.reject(err);
						});
					}, function(err) {
						return $q.reject(err);
					});
				}

				// Finalize submission processing
				p = p.then(function(response) {
					$scope.formState.ajax--;
					$mdDialog.hide();
				}, function(err) {
					$scope.formState.ajax--;
				});
			};

			$scope.cancel = function() {
				$mdDialog.cancel();
			};

			$scope.start = function() {
				$scope.dto = dialogParams.existingRow || $scope.initialDtoBuilder();
				$scope.fields = $scope.buildFields();
			};
		} ]);

KfCtrlApp.directive("crudInputText", function() {
	return {
		restrict : "E",
		scope : true,
		replace : true,
		templateUrl : staticResourcesBase + '/static/templates/crud/input-text.html',

		link : function(scope, element, attributes) {
			scope.field = scope.$parent.field(attributes.name);
			scope.formState = scope.$parent.formState;
			scope.dto = scope.$parent.dto;
		}
	};
});

KfCtrlApp.directive("crudInputCheckbox", function() {
	return {
		restrict : "E",
		scope : true,
		replace : true,
		templateUrl : staticResourcesBase + '/static/templates/crud/input-checkbox.html',

		link : function(scope, element, attributes) {
			scope.field = scope.$parent.field(attributes.name);
			scope.formState = scope.$parent.formState;
			scope.dto = scope.$parent.dto;
		}
	};
});

KfCtrlApp.directive("crudInputSwitch", function() {
	return {
		restrict : "E",
		scope : true,
		replace : true,
		templateUrl : staticResourcesBase + '/static/templates/crud/input-switch.tmpl.html',

		link : function(scope, element, attributes) {
			scope.field = scope.$parent.field(attributes.name);
			scope.formState = scope.$parent.formState;
			scope.dto = scope.$parent.dto;
		}
	};
});

KfCtrlApp.directive("crudInputCombobox", function() {
	return {
		restrict : "E",
		scope : true,
		replace : true,
		templateUrl : staticResourcesBase + '/static/templates/crud/input-combobox.tmpl.html',

		link : function(scope, element, attributes) {
			scope.field = scope.$parent.field(attributes.name);
			scope.formState = scope.$parent.formState;
			scope.dto = scope.$parent.dto;
		}
	};
});

KfCtrlApp.directive("crudInputChips", [ '$mdConstant', function($mdConstant) {
	return {
		restrict : "E",
		scope : true,
		replace : false,
		templateUrl : staticResourcesBase + '/static/templates/crud/input-chips.html',

		link : function(scope, element, attributes) {
			scope.field = scope.$parent.field(attributes.name);
			scope.chips = scope.field.controller; // shortcut
			scope.formState = scope.$parent.formState;
			scope.dto = scope.$parent.dto;

			// track if user enters comma "," or semi-colon ";" and then append
			// this chip
			// NOTE: md-separator-keys is not compatible with md-autocomplete
			scope.$watch('chips.searchText', function(newValue, oldValue) {
				if (!newValue || newValue.length < 2) {
					return;
				}
				if (";,".indexOf(newValue.substring(newValue.length - 1)) < 0) {
					return;
				}

				var chipName = newValue.substring(0, newValue.length - 1);
				if (!scope.chips.selectedChips) {
					scope.chips.selectedChips = [];
				}
				scope.chips.selectedChips.push(scope.chips.transformChip(chipName));
				scope.chips.searchText = '';
			});

		}
	};
} ]);

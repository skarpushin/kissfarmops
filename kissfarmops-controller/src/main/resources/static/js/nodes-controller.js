/**
 * This controll is NOT a dashboard like, it's just a CRUD for nodes
 */

KfCtrlApp.controller('NodesController', [
		'$scope',
		'$mdDialog',
		'$controller',
		'CrudAjaxClientFactory',
		function($scope, $mdDialog, $controller, CrudAjaxClientFactory) {
			$controller('CrudListControllerBase', {
				$scope : $scope
			});

			$scope.crudAjaxClient = CrudAjaxClientFactory(CrudUrlProviderDefault(contextPath + "/rest/api/v1/node"));
			$scope.getDefaultOrderBy = function() {
				return OrderBy.Asc('createdAt');
			};
			$scope.getSearchableField = function() {
				return 'id';
			};

			$scope.dialogForCreateControllerName = 'NodeFormController';
			$scope.dialogForCreateTemplate = staticResourcesBase + '/static/templates/node-form-dialog.tmpl.html';

			$scope.dialogForUpdateControllerName = 'NodeFormController';
			$scope.dialogForUpdateTemplate = staticResourcesBase + '/static/templates/node-form-dialog.tmpl.html';

			$scope.referencesToResolve = [ "nodeTags", "nodeStatus" ];
			$scope.rowEnrichers.push(OneToManyReferencesRowEnricher('nodeTags', function(initiatorRow, referencedRows) {
				if (!referencedRows) {
					return {};
				}
				var ret = "";
				for (var i = 0; i < referencedRows.length; i++) {
					if (i > 0) {
						ret += ", ";
					}
					ret += referencedRows[i].tag;
				}
				return {
					tags : ret,
					tagsArr : referencedRows
				};
			}));
			$scope.rowEnrichers.push(OneToOneReferencesRowEnricher("nodeStatus", function(initiatorRow, referencedRow) {
				return {
					online: referencedRow.online
				};
			}));
			
			// ======================== STOMP stuff
			// TODO: A bit bulky code here.. It's just asking to be extracted / abstracted somehow 
			// TODO: Encapsulate rows updates based on EntityChangeEvents from backend
		    $scope.initSockets = function() {
		        $scope.socket = {};
		        $scope.socket.client = new SockJS('/ws');  
		        $scope.socket.stomp = Stomp.over($scope.socket.client);
		        $scope.socket.stomp.connect({}, function() {
		            $scope.socket.stomp.subscribe('/stoui', $scope.onMessage);
		        });
		        $scope.socket.client.onclose = $scope.reconnect;
		    };		
		    var findRowById = function(rowId) {
				for (var i = 0; i < $scope.rows.length; i++) {
					var row = $scope.rows[i];
					if (row.id == rowId) {
						return row;
					}
				}
				return null;
		    }
			$scope.onMessage = function(message) {
				var rowId = null;
				var dto = JSON.parse(message.body);
				if (message.headers.payloadType == "org.kissfarmops.shared.websocket.api.NodeConnectedEvent") {
					var row = findRowById(dto.nodeId);
					if (row == null) return;
			        $scope.$apply(function() {
						row.online = true;
			        });
				} else if (message.headers.payloadType == "org.kissfarmops.shared.websocket.api.NodeDisconnectedEvent") {
					var row = findRowById(dto.nodeId);
					if (row == null) return;
			        $scope.$apply(function() {
						row.online = false;
			        });
				} else {
					return; // not interested
				}
		    };
		    $scope.reconnect = function() {
		        setTimeout($scope.initSockets, 10000);
		    };

			$scope.start();
			$scope.initSockets();
		} ]);

KfCtrlApp.controller('NodeFormController', [ '$scope', '$mdDialog', '$controller', 'dialogParams', 'restClient', 'HttpFactory',
		function($scope, $mdDialog, $controller, dialogParams, restClient, HttpFactory) {
			$controller('CrudItemFormDialogBase', {
				$scope : $scope,
				dialogParams : dialogParams,
				restClient : restClient
			});

			$scope.initialDtoBuilder = function() {
				return dialogParams.existingRow || {
					"id" : "",
					"agentAuthToken" : "",
					"password" : "",
					"hostName" : "",
					"publicIp" : "",
					"blocked" : false,
					"tagsArr": []
				};
			};

			$scope.buildFields = function() {
				return [ {
					title : msgs['term.nodeId'],
					name : 'id',
					type : "text",
					hint : msgs['term.nodeId.hint'],
					isRequired : true
				}, {
					title : msgs['term.agentAuthToken'],
					name : 'agentAuthToken',
					type : "text",
					isRequired : true
				}, {
					title : msgs['term.password'],
					name : 'password',
					type : "text",
					isRequired : true
				}, {
					title : msgs['term.hostName'],
					name : 'hostName',
					type : "text",
					isRequired : true
				}, {
					title : msgs['term.publicIp'],
					name : 'publicIp',
					type : "text",
					isRequired : true
				}, {
					title : msgs['term.blocked'],
					name : 'blocked',
					type : "checkbox",
					isRequired : false
				}, {
					title : msgs['term.nodeTags'],
					name : 'tagsArr',
					type : "chips",
					isRequired : false,
					controller : NodeTagsChipsProvider($scope.dto, HttpFactory)
				} ];
			};

			$scope.start();
		} ])

		
function NodeTagsChipsProvider(rowEnriched, HttpFactory) {
	var promises = [];

	var newObj = {
		selectedChips : rowEnriched.tagsArr,

		getText : function(chip) {
			return chip.tag;
		},

		transformChip : function(chip) {
			// If it is an object, it's already a known chip
			if (angular.isObject(chip)) {
				return chip;
			}

			return NewTag(chip);
		},

		allowToAddNew : true,

		selectedItem : null,

		searchText : null,

		querySearch : function(str) {
			// NOTE: We're not picking from existing tags. Yet.
			return []; // tagsDictionary.localSearch(SearchPredicateByString(str));
		},

		handlePostSubmit : function() {
			var newTags = rowEnriched.tagsArr.map(function(dto) {
				return dto.tag;
			});
			
			var url = contextPath + '/rest/api/v1/node/' + rowEnriched.id + '/tags';
			return HttpFactory.put(url, newTags, function(err, response) {
				if (!!err) {
					alert('Failed to save Node Tags. ' + getErrorMsg(err));
					return;
				}
			});
		}
	};

	/**
	 * NewTag will return dummy object, then async create it on server, then
	 * populate initial dummy object with real fields
	 */
	var NewTag = function(name) {
		return {
			subjectId : rowEnriched.id,
			tag : name
		};
	};

	return newObj;
}

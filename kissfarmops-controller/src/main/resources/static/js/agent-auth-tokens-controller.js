KfCtrlApp.controller('AgentAuthTokensController', [
		'$scope',
		'$mdDialog',
		'$controller',
		'CrudAjaxClientFactory',
		function($scope, $mdDialog, $controller, CrudAjaxClientFactory) {
			$controller('CrudListControllerBase', {
				$scope : $scope
			});

			$scope.crudAjaxClient = CrudAjaxClientFactory(CrudUrlProviderDefault(contextPath + "/rest/api/v1/agent-auth-token"));
			$scope.getDefaultOrderBy = function() {
				return OrderBy.Asc('createdAt');
			};
			$scope.getSearchableField = function() {
				return 'id';
			};

			$scope.dialogForCreateControllerName = 'AgentAuthTokenFormController';
			$scope.dialogForCreateTemplate = staticResourcesBase + '/static/templates/agent-auth-token-form-dialog.tmpl.html';

			$scope.dialogForUpdateControllerName = 'AgentAuthTokenFormController';
			$scope.dialogForUpdateTemplate = staticResourcesBase + '/static/templates/agent-auth-token-form-dialog.tmpl.html';

			$scope.start();
		} ]);

KfCtrlApp.controller('AgentAuthTokenFormController', [ '$scope', '$mdDialog', '$controller', 'dialogParams', 'restClient',
		function($scope, $mdDialog, $controller, dialogParams, restClient) {
			$controller('CrudItemFormDialogBase', {
				$scope : $scope,
				dialogParams : dialogParams,
				restClient : restClient
			});

			$scope.initialDtoBuilder = function() {
				return dialogParams.existingRow || {
					"id" : "",
					"comment" : "",
					"enabled" : true,
				};
			};

			$scope.buildFields = function() {
				return [ {
					title : msgs['agentAuthToken.token'],
					name : 'id',
					type : "text",
					hint : msgs['agentAuthToken.token.creationHint'],
					isRequired : false
				}, {
					title : msgs['term.comment'],
					name : 'comment',
					type : "text",
					isRequired : false
				}, {
					title : msgs['term.enabled'],
					name : 'enabled',
					type : "checkbox",
					isRequired : false
				} ];
			};

			$scope.start();
		} ])

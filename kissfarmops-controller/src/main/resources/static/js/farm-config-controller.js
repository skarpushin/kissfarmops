function FarmConfigControllerFunc($scope, $mdDialog, $controller, HttpFactory, $q) {
	$scope.loading = true;
	$scope.dto = {};
	$scope.isConfigPresent = false;
	$scope.isModifyEnabled = false;
	$scope.isPullEnabled = false;
	$scope.smData = null;

	$scope.updateActionsAvailability = function() {
		$scope.isConfigPresent = !!$scope.dto.uri && !!$scope.dto.branch && !!$scope.dto.user && !!$scope.dto.password;
		$scope.loading = false;
		$scope.isModifyEnabled = true;
		$scope.isPullEnabled = $scope.smData.currentStateName == 'Ready';
	};

	HttpFactory.get(contextPath + "/rest/api/v1/farm-config/sm-data").then(function(response) {
		$scope.smData = response.data;
		$scope.dto = $scope.smData.vars.gitConfig;
		if ($scope.dto == null) {
			$scope.dto = { // TODO: Make it empty when release
				uri : '/home/sergeyk/kfa-remote-git/',
				branch : 'master',
				user : '',
				password : ''
			};
		}

		$scope.updateActionsAvailability();
	}, function(err) {
		alert('Failed to get current config from server. ' + err);
	});

	$scope.openGitConfigDialog = function(event) {
		$mdDialog.show({
			controller : 'GitConfigFormController',
			templateUrl : staticResourcesBase + '/static/templates/git-config-dialog.tmpl.html',
			parent : angular.element(document.body),
			targetEvent : event,
			clickOutsideToClose : true,
			fullscreen : true,
			locals : {
				dto : $scope.dto
			}
		}).then(function(dto) {
			$scope.dto = dto;
			$scope.updateActionsAvailability();
		}, function() {
			// no action on cancel
		});
	};

	$scope.pullChanges = function(event) {
		HttpFactory.get('/rest/api/v1/farm-config/actions/check-updates');
	};
}
KfCtrlApp.controller('FarmConfigController', [ '$scope', '$mdDialog', '$controller', 'HttpFactory', '$q',
		FarmConfigControllerFunc ]);

function GitConfigFormControllerFunc($scope, $mdDialog, $controller, HttpFactory, dto) {
	$controller('DtoDialogBase', {
		$scope : $scope,
		dialogParams : {
			dialogTitle : 'Git Config',
			dto : JSON.parse(JSON.stringify(dto))
		},
		submissionStrategy : function(dtoToSubmit, callback) {
			return HttpFactory.put('/rest/api/v1/farm-config/git-config', dtoToSubmit, callback);
		}
	});

	$scope.buildFields = function() {
		return [ {
			title : msgs['term.url'],
			name : 'uri',
			type : "text",
			isRequired : true
		}, {
			title : msgs['term.branch'],
			name : 'branch',
			type : "text",
			isRequired : true
		}, {
			title : msgs['term.user'],
			name : 'user',
			type : "text",
			isRequired : true
		}, {
			title : msgs['term.password'],
			name : 'password',
			type : "text",
			isRequired : true
		} ];
	};

	$scope.start();
}
KfCtrlApp.controller('GitConfigFormController', [ '$scope', '$mdDialog', '$controller', 'HttpFactory', 'dto',
		GitConfigFormControllerFunc ]);

function FarmConfigStateControllerFunc($scope, $mdDialog, $controller, HttpFactory, $q, CrudAjaxClientFactory) {
	$controller('CrudListControllerBase', {
		$scope : $scope
	});

	$scope.crudAjaxClient = CrudAjaxClientFactory(CrudUrlProviderDefault(contextPath + "/rest/api/v1/farm-config/state"));
	$scope.getDefaultOrderBy = function() {
		return OrderBy.Desc('createdAt');
	};
	$scope.getSearchableField = function() {
		return 'resultMessage';
	};

	$scope.start();
}
KfCtrlApp.controller('FarmConfigStateController', [ '$scope', '$mdDialog', '$controller', 'HttpFactory', '$q',
		'CrudAjaxClientFactory', FarmConfigStateControllerFunc ]);

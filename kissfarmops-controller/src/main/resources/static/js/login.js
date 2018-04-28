KfCtrlApp.factory('ajaxLogin', [ 'HttpFactory', function(HttpFactory) {
	var service = {};

	service.register = function(registration, callback) {
		return HttpFactory.post('/rest/login/register', registration, callback);
	};

	service.changePassword = function(dto, callback) {
		return HttpFactory.post('/rest/login/change', dto, callback);
	};
	
	return service;
} ]);

KfCtrlApp.controller('LoginController', [ '$scope', function($scope) {
	$scope.j_username = userName;
	$scope.j_password = "";
} ]);

KfCtrlApp.controller('RegisterController', [
		'$scope',
		'$window',
		'$mdDialog',
		'ajaxLogin',
		function($scope, $window, $mdDialog, ajaxLogin) {
			$scope.registration = {
				email : "",
				displayName : "",
				password : ""
			};
			$scope.ve = {};
			$scope.lastExceptionMessage = null;

			var showConfirmationDialog = function(ev, user) {
				var confirm = $mdDialog.confirm().parent(angular.element(document.querySelector('#popupContainer'))).clickOutsideToClose(
						false).title(msgs['security.register']).textContent(msgs['security.nowYouCanLogin']).ok(msgs['security.signIn'])
						.targetEvent(ev);
				var confirmed = function() {
					$window.location = contextPath + "/login/form?userName=" + user.email;
				};
				$mdDialog.show(confirm).then(confirmed, confirmed);
			};

			$scope.submitForm = function(ev) {
				ajaxLogin.register($scope.registration, function(err, response) {
					$scope.ve = {};
					$scope.lastExceptionMessage = "";
					if (!err) {
						return showConfirmationDialog(ev, response.data);
					}

					if (!!err && !!err.data) {
						if (!!err.data.ve) {
							$scope.ve = err.data.ve;
							return;
						} else if (!!err.data.exc) {
							$scope.lastExceptionMessage = err.data.exc;
							return;
						}
					}
					$scope.lastExceptionMessage = "Unrecognized error during call to the server";
				});
			};
		} ]);

KfCtrlApp.controller('ChangePasswordController', [
		'$scope',
		'$window',
		'$mdDialog',
		'ajaxLogin',
		function($scope, $window, $mdDialog, ajaxLogin) {
			$scope.dto = {
				currentPassword : "",
				password : "",
				newPasswordAgain : ""
			};
			$scope.ve = {};
			$scope.lastExceptionMessage = null;
			
			var showConfirmationDialog = function(ev, user) {
				var confirm = $mdDialog.confirm().parent(angular.element(document.querySelector('#popupContainer'))).clickOutsideToClose(
						false).title(msgs['security.changePassword']).textContent(msgs['security.newPasswordSet']).ok('OK').targetEvent(ev);
				var confirmed = function() {
					$window.location = contextPath + "/";
				};
				$mdDialog.show(confirm).then(confirmed, confirmed);
			};

			$scope.submitForm = function(ev) {
				ajaxLogin.changePassword($scope.dto, function(err, response) {
					$scope.ve = {};
					$scope.lastExceptionMessage = "";
					if (!err) {
						return showConfirmationDialog(ev, response.data);
					}

					if (!!err && !!err.data) {
						if (!!err.data.ve) {
							$scope.ve = err.data.ve;
							return;
						} else if (!!err.data.exc) {
							$scope.lastExceptionMessage = err.data.exc;
							return;
						}
					}
					$scope.lastExceptionMessage = "Unrecognized error during call to the server";
				});
			};
		} ]);

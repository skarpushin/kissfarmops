// TODO: Try to get rid of dependencies which are not necesary for each page
var KfCtrlApp = angular.module("KfCtrlApp", [ 'ngMaterial', 'ngMessages', 'chart.js', 'md.data.table', 'ngAnimate' ]);

// KfCtrlApp.config([ '$httpProvider', function($httpProvider) {
// $httpProvider.defaults.headers.common['X-TranslateAuthorizationErrors'] =
// 'true';
// } ]);

KfCtrlApp.directive("hasVe", function() {
	return {
		restrict : "A",
		require : "ngModel",

		link : function(scope, element, attributes, ngModel) {
			var param = attributes.hasVe;
			ngModel.$validators.hasVe = function(modelValue, viewValue) {
				var expr = scope.$eval(param);
				var valid = !expr;
				ngModel.$setValidity('has-ve', valid);
				return viewValue;
			};

			scope.$watch(param, function() {
				ngModel.$validate();
			});

			// NOTE: This thing is needed for the cases when we need to show
			// errors prerendered by server on page load
			setTimeout(function() {
				ngModel.$touched = true;
				scope.$apply();
			}, 100);
		}
	};
});

KfCtrlApp.directive("msg", function() {
	return {
		restrict : "E",
		scope : false,
		link : function(scope, element, attributes) {
			if (!msgs) {
				return;
			}
			var resolved = msgs[element.text()];
			if (resolved) {
				element.text(resolved);
			}
		}
	};
});

KfCtrlApp.factory('HttpFactory', [
		'$http',
		'$mdDialog',
		'$q',
		function($http, $mdDialog, $q) {
			var factory = {};

			factory.http = function() {
				return $http;
			};

			factory.handleSuccess = function(callback) {
				return function(res) {
					callback && callback(null, res);
					return res;
				}
			}
			
			factory.buildTranslatedValidationErrorsViewModel = function(data) {
				var veeVm = {};
				for (var veIdx in data.errors) {
					if (!data.errors.hasOwnProperty(veIdx)) {
						continue;
					}

					var ve = data.errors[veIdx];
					var veTranaslated = "";
					if (typeof ve.messageArgs == 'undefined') {
						veTranaslated = msg(ve.messageCode);
					} else {
						var args = [ve.messageCode];
						args.push(ve.messageArgs);
						veTranaslated = msg.aaply(null, args);
					}
					veTranaslated = veTranaslated.trim();
					
					if (typeof veeVm[ve.fieldToken] == 'undefined') {
						veeVm[ve.fieldToken] = veTranaslated; 
					} else {
						var existing = veeVm[ve.fieldToken];
						if (existing.endsWith(".")) {
							veeVm[ve.fieldToken] = existing + " " + veTranaslated; 
						} else {
							veeVm[ve.fieldToken] = existing + ". " + veTranaslated; 
						}
					}
				}
				return veeVm;
			}

			factory.handleError = function(callback) {
				return function(res) {
					if (!!res && !!res.data && res.data.messageCode === 'security.authorization.missing') {
						var confirm = $mdDialog.alert().parent(angular.element(document.querySelector('#popupContainer')))
								.clickOutsideToClose(true).title(msgs['security.invalidSession']).textContent(
										msgs['security.loginRequired']).ok("OK");
						var confirmed = function() {
							return window.location.reload();
						};
						$mdDialog.show(confirm).then(confirmed, confirmed);
						return;
					}

					if (res.status == 400 && !!res.data && !!res.data.errors) {
						// handling ValidationErrors here
						var veeVm = factory.buildTranslatedValidationErrorsViewModel(res.data);
						res.data.ve = veeVm; 
					}

					callback && callback(res);
					return $q.reject(res);
				}
			}

			factory.get = function(url, callback, config) {
				var p = factory.http().get(url, config);
				return p.then(factory.handleSuccess(callback), factory.handleError(callback));
			};

			factory.post = function(url, body, callback, config) {
				var p = factory.http().post(url, body, config);
				return p.then(factory.handleSuccess(callback), factory.handleError(callback));
			};

			factory.put = function(url, body, callback, config) {
				var p = factory.http().put(url, body, config);
				return p.then(factory.handleSuccess(callback), factory.handleError(callback));
			};

			factory.httpDelete = function(url, callback, config) {
				var p = factory.http()['delete'](url, config);
				return p.then(factory.handleSuccess(callback), factory.handleError(callback));
			};

			return factory;
		} ]);

function getErrorMsg(errorResponse) {
	var e = errorResponse;
	if (!e) {
		return "Undefined error";
	}

	if (!e.data) {
		return "Unknown error";
	}

	if (!!e.data.exc) {
		return e.data.exc;
	}

	return JSON.stringify(e.data);
}

/**
 * 
 * @param messageCode
 *            message code
 * @varargs any number of arguments if assumed by msg template
 * @returns translated message
 */
function msg(messageCode) {
	if (!messageCode) {
		return '';
	}
	if (!msgs) {
		return 'messageCode+' + JSON.stringify(arguments);
	}
	var template = msgs[messageCode];
	if (!template) {
		return 'messageCode+' + JSON.stringify(arguments);
	}
	for (var i = 1; i < arguments.length; i++) {
		template = template.replace('{' + (i - 1) + '}', arguments[i]);
	}
	return template;
}

function clone(dto) {
	return JSON.parse(JSON.stringify(dto));
}

function inheritStrategy(obj, methondName, newMethodThis, newMethod) {
	var parentImpl = obj[methondName];
	obj[methondName] = function overrideFunc() {
		var ret = parentImpl.apply(obj, arguments);
		var newArgs = [ ret ];
		if (typeof arguments != 'undefined') {
			newArgs = newArgs.concat(arguments);
		}
		return newMethod.apply(newMethodThis, newArgs);
	};
}

function copyFields(from, to) {
	for ( var field in from) {
		if (!from.hasOwnProperty(field)) {
			continue;
		}
		to[field] = from[field];
	}
}

/**
 * This controller supposed to be sub-classed
 */
function DtoDialogBaseFunc($scope, $mdDialog, dialogParams, submissionStrategy, $q) {
	$scope.submissionStrategy = submissionStrategy;
	$scope.params = dialogParams;
	$scope.dto = dialogParams.dto;

	$scope.formState = {
		ajax : 0,
		lastExceptionMessage : null,
		ve : {}
	};
	$scope.formState.canSubmit = function() {
		return $scope.formState.ajax == 0;
	};

	$scope.dialogTitle = dialogParams.dialogTitle || 'Dialog';
	$scope.submitButtonTitle = dialogParams.submitButtonTitle || msgs['action.save'];
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

	$scope.save = function() {
		$scope.formState.ajax++;

		// Now let's submit dto changes
		$scope.submissionStrategy($scope.dto, function(err, response) {
			$scope.formState.lastExceptionMessage = null;
			$scope.formState.ve = {};

			if (!err) {
				return;
			}

			if (!!err.data) {
				if (!!err.data.ve) {
					$scope.formState.ve = err.data.ve;
					return;
				} else if (!!err.data.exc) {
					$scope.formState.lastExceptionMessage = err.data.exc;
					return;
				} else {
				}
			}
			$scope.formState.lastExceptionMessage = "Unrecognized error during call to the server. " + err;

		}).then(function(response) {
			$scope.formState.ajax--;
			$mdDialog.hide($scope.dto);
		}, function(err) {
			$scope.formState.ajax--;
		});
	};

	$scope.cancel = function() {
		$mdDialog.cancel();
	};

	$scope.start = function() {
		$scope.fields = $scope.buildFields();
	};
}
KfCtrlApp.controller('DtoDialogBase', [ '$scope', '$mdDialog', 'dialogParams', 'submissionStrategy', '$q',
		DtoDialogBaseFunc ]);

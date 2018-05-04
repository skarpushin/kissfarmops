KfCtrlApp.controller('SideNavController',
		[
				'$scope',
				'$window',
				'$log',
				'$controller',
				'$mdSidenav',
				function($scope, $window, $log, $controller, $mdSidenav) {
					$controller('MainMenuController', {
						$scope : $scope
					});

					$scope.user = currentUser;

					$scope.breadcrumb = [];

					var makeMenuGroup = function(groupName, menuItems) {
						var ret = {
							title : groupName,
							menuItems : menuItems
						};
						if ($scope.breadcrumb.length > 0 && ret.menuItems.indexOf($scope.breadcrumb[$scope.breadcrumb.length - 1]) >= 0) {
							$scope.breadcrumb.splice($scope.breadcrumb.length - 1, 0, {
								title : ret.title
							});
						}
						return ret;
					};

					var makeMenuItem = function(itemName, link, icon) {
						var ret = {
							title : itemName,
							icon : !icon ? '' : icon,
							href : contextPath + link,
							active : $window.location.pathname == (contextPath + link),
							subItems: []
						};
						if (ret.active) {
							$scope.breadcrumb.push(ret);
						}
						return ret;
					};
					$scope.makeMenuItem = makeMenuItem;

					$scope.breadcrumb.push(makeMenuItem(msgs['app.title'], contextPath + "/"));

					$scope.menuGroups = [];

					$scope.menuGroups.push(makeMenuGroup(msgs['term.mainFeatures'], [ 
						makeMenuItem(msgs['term.dashboard'], '/web/dashboard', 'multiline_chart'),
						makeMenuItem(msgs['term.agentAuthTokens'], '/web/agent-auth-token', null),
						makeMenuItem(msgs['term.nodes'], '/web/node', null),
					]));

					// if (currentUserRoles.indexOf('ROLE_ADMIN') >= 0) {
					$scope.menuGroups.push(makeMenuGroup(msgs['term.administration'], [
						makeMenuItem('Java Melody', '/monitoring'), 
						makeMenuItem('Exceptions monitor', '/error/exc') 
					]));
					// }

					$scope.menuGroups.push(makeMenuGroup(msgs['term.user'], [
							makeMenuItem(msgs['security.changePassword'], '/login/change', 'settings_backup_restore'),
							makeMenuItem(msgs['security.signOut'], '/j_spring_security_logout', 'exit_to_app'),
							makeMenuItem('Swagger', '/swagger-ui.html', 'code') ]));

					$scope.showSideNav = function() {
						$mdSidenav('left').open();
					};

					$scope.hasPageActions = false;
					$scope.pageActions = [];
					$scope.setPageActions = function(actions) {
						$scope.pageActions = actions || [];
						$scope.hasPageActions = $scope.pageActions.length > 0;
					};

				} ]);

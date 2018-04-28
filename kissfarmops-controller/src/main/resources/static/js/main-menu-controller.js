KfCtrlApp.controller('MainMenuController', [ '$scope', '$window', '$log', function($scope, $window, $log) {
	var buildLangImgSrc = function(forLang) {
		return staticResourcesBase + "/static/img/lang_" + forLang + ".jpeg";
	};

	$scope.currentLangImgSrc = buildLangImgSrc(lang);
	$scope.currentLang = lang;

	$scope.langs = [ {
		lang : "ru",
		imgSrc : buildLangImgSrc("ru"),
		title : "Русский"
	}, {
		lang : "en",
		imgSrc : buildLangImgSrc("en"),
		title : "English"
	} ];
	
	$scope.currentLangTitle = function() {
		for (i in $scope.langs) {
			if ($scope.currentLang == $scope.langs[i].lang) {
				return $scope.langs[i].title;
			}
		}
		return $scope.currentLang;
	};;

	var replaceQueryParam = function(param, newval, search) {
		var regex = new RegExp("([?;&])" + param + "[^&;]*[;&]?");
		var query = search.replace(regex, "$1").replace(/&$/, '');

		return (query.length > 2 ? query + "&" : "?") + (newval ? param + "=" + newval : '');
	};

	$scope.switchToLang = function(ev, switchToLang) {
		$window.location = $window.location.pathname + replaceQueryParam("locale", switchToLang, $window.location.search);
	};

	$scope.getClassForMenuItem = function(forPath) {
		if (forPath == "/") {
			if ($window.location.pathname == contextPath + "/") {
				return "active-menu-item";
			}
		} else if ($window.location.pathname.indexOf(contextPath + forPath) == 0) {
			return "active-menu-item";
		}
		return "";
	};
} ]);

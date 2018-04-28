KfCtrlApp.factory('ajaxExcStats', [ 'HttpFactory', function(HttpFactory) {
	var service = {};
	service.getStats = function(callback) {
		return HttpFactory.get(contextPath + '/rest/error/exc', callback);
	};
	return service;
} ]);

KfCtrlApp.controller('ExceptionsStatisticsController', [ '$scope', 'ajaxExcStats', function($scope, ajaxExcStats) {
	$scope.isPageLoaded = true;
	$scope.isDataStillLoading = true;
	$scope.hasData = false;
	$scope.exceptions = [];
	
	ajaxExcStats.getStats(function(err, response) {
		$scope.isDataStillLoading = false;
		if (!!err) {
			// TODO: handle error
			alert('Error loading exception stats. ' + JSON.stringify(err.data));
			return;
		}

		var data = response.data;
		for (var i=0; i<data.length; i++) {
			var exc = data[i];
			exc.firstLine = exc.msgs.substring(0, exc.msgs.indexOf('\n'));
		}
		$scope.exceptions = data;
		if (data.length > 0) {
			$scope.hasData = true;
		}
	});
} ]);

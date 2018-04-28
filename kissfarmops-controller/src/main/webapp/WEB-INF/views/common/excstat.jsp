<%@ page pageEncoding="UTF-8"%>
<%@ page session="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<!DOCTYPE html>
<!--[if lt IE 7]>      <html lang="${lang}" ng-app="KfCtrlApp" class="no-js lt-ie9 lt-ie8 lt-ie7"> <![endif]-->
<!--[if IE 7]>         <html lang="${lang}" ng-app="KfCtrlApp" class="no-js lt-ie9 lt-ie8"> <![endif]-->
<!--[if IE 8]>         <html lang="${lang}" ng-app="KfCtrlApp" class="no-js lt-ie9"> <![endif]-->
<!--[if gt IE 8]><!-->
<html lang="${lang}" ng-app="KfCtrlApp" class="no-js">
<!--<![endif]-->

<head>
	<meta name="robots" content="noindex" />
	<c:import url="/WEB-INF/views/common/page-head.jsp" />
	<title><s:message code="app.title" /> ERROR</title>
</head>
<body layout="column">
	<c:import url="/WEB-INF/views/common/page-body-header.jsp" />

	<div ng-controller="ExceptionsStatisticsController" ng-cloak class="ng-cloak">
		<div ng-show="!isPageLoaded">Page is loading, please wait...</div>
		<md-content layout="column" layout-align="center center"  ng-show="isDataStillLoading">
		    <div><md-progress-circular md-mode="indeterminate"></md-progress-circular></div>
			<div>Data is loading...</div>
		</md-content>

 		<div flex ng-show="!isDataStillLoading && !hasData">
			<md-content id="contentAjaxLoading" layout="column" layout-align="center center" >
				<div>No data to display</div>
			</md-content>
		</div>
		
		<md-content ng-show="!isDataStillLoading && hasData" layout-padding>
			<table class="simpleTable">
				<thead>
					<tr>
						<th>Count</th>
						<th>Message</th>
						<th>Users affected</th>
						<th>HashCode</th>
					</tr>
				</thead>
				<tbody>
					<tr ng-repeat="exc in exceptions">
						<td>{{ exc.count }}</td>
						<td>
							<div class="clickableLikeLink" ng-click="exc.showAllMsgs = !exc.showAllMsgs">{{ exc.firstLine }}</div>
							<pre ng-show="exc.showAllMsgs">{{ exc.msgs }}</pre>
						</td>
						<td>{{ exc.affectedUsers | json }}</td>
						<td>{{ exc.id }}</td>
					</tr>
				</tbody>
			</table>
		</md-content>
	</div> 
	
	<c:import url="/WEB-INF/views/common/page-body-footer.jsp" />
	
	<script src="${staticResourcesBase}/static/js/exceptions-statistics.js"></script>
</body>
</html>


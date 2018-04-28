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
	<c:import url="/WEB-INF/views/common/page-head.jsp" />
	<title><s:message code="app.title" /> - <s:message code="term.dashboard" /></title>
</head>
<body layout="column">
	<c:import url="/WEB-INF/views/common/page-body-side-nav-begin.jsp" />

	<div ng-cloak class="ng-cloak" flex layout="column">
	<md-content id="contentAjaxLoading" layout="column" layout-align="center center" flex ng-show="isDataStillLoading">
	    <div>
	    	<md-progress-circular md-mode="indeterminate"></md-progress-circular>
		</div>
		<div>
			Loading...
		</div>
	</md-content>
	<md-content flex id="content" ng-controller="DashboardController" ng-cloak class="ng-cloak" layout="row" 
		layout-align="start start" flex ng-show="!isDataStillLoading">
		
		<div>TBD</div>
	</md-content>
	</div>
     
	<c:import url="/WEB-INF/views/common/page-body-side-nav-end.jsp" />
	<c:import url="/WEB-INF/views/common/page-body-footer.jsp" />
	<script src="${staticResourcesBase}/static/js/dashboard-controller.js"></script>
</body>
</html>


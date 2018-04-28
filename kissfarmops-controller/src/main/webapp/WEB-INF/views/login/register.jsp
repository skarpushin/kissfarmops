<%@ page pageEncoding="UTF-8"%>
<%@ page session="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
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
	<title><s:message code="security.register" /></title>
</head>
<body layout="column">
	<c:import url="/WEB-INF/views/common/page-body-header.jsp" />

<div layout="row" layout-align="center center" flex ng-cloak ng-controller="RegisterController">
  <div layout="column" class="md-whiteframe-z1" style="width: 320px;">
	<form name="register" ng-submit="submitForm($event)" novalidate>
    <md-toolbar>
      <h2 class="md-toolbar-tools"><s:message code="security.register" /></h2>
    </md-toolbar>
    <md-content layout="column" class="md-padding">
      <md-input-container>
        <label><s:message code="term.email"/></label>
        <input name="email" type="email" ng-model="registration.email" has-ve="!!ve.email" autofocus/>
		<div ng-messages="register.email.$error" md-auto-hide='false'>
		    <span ng-message="has-ve">{{ ve.email }}</span>
		</div>        
      </md-input-container>
      <md-input-container>
        <label><s:message code="term.humanName"/></label>
        <input name="displayName" type="text" ng-model="registration.displayName" has-ve="!!ve.displayName"/>
		<div ng-messages="register.displayName.$error" md-auto-hide='false'>
		    <span ng-message="has-ve">{{ ve.displayName }}</span>
		</div>        
      </md-input-container>
      <md-input-container>
        <label><s:message code="term.password"/></label>
        <input name="password" type="password" ng-model="registration.password" has-ve="!!ve.password"/>
		<div ng-messages="register.password.$error">
		    <span ng-message="has-ve">{{ ve.password }}</span>
		</div>        
      </md-input-container>
      <div layout="row" layout-align="center center" style="padding-top:20px;">
        <div flex="flex"></div>
        <md-button type="submit" class="md-raised md-primary"><s:message code="security.register" /></md-button>
      </div>
    </md-content>
	<div class="md-padding error" style="color: rgb(221,44,0)" ng-show="!!lastExceptionMessage">
		{{ lastExceptionMessage }}
	</div>
	</form>
  </div>
</div>		
	
	<c:import url="/WEB-INF/views/common/page-body-footer.jsp" />
	<script src="${staticResourcesBase}/static/js/login.js"></script>

		
</body>
</html>


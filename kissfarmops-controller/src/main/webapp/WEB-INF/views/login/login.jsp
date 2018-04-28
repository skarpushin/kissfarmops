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
	<title><s:message code="security.signIn" /></title>
</head>
<body layout="column">
	<c:import url="/WEB-INF/views/common/page-body-header.jsp" />

<div layout="row" layout-align="center center" flex ng-cloak ng-controller="LoginController">
  <div layout="column" class="md-whiteframe-z1" style="width: 320px;">
	<form name="login" action="${contextPath}/j_spring_security_check" method="POST" novalidate>
    <md-toolbar>
      <h2 class="md-toolbar-tools"><s:message code="security.signIn" /></h2>
    </md-toolbar>
    <md-content layout="column" class="md-padding">
      <md-input-container>
        <label><s:message code="term.email"/></label>
        <input name="j_username" type="email" ng-model="j_username" has-ve="${ve_email != null}" autofocus />
		<div ng-messages="login.j_username.$error" md-auto-hide='false'>
		    <span ng-message="has-ve">${ve_email}</span>
		</div>        
      </md-input-container>
      <md-input-container>
        <label><s:message code="term.password"/></label>
        <input name="j_password" type="password" ng-model="j_password" has-ve="${ve_password != null}"/>
		<div ng-messages="login.j_password.$error">
		    <span ng-message="has-ve">${ve_password}</span>
		</div>        
      </md-input-container>
      <div layout="row" layout-align="center center" style="padding-top:20px;">
        <md-checkbox ng-model="rememberme" style="margin-bottom: 0"><s:message code="term.rememberMe" /></md-checkbox>
        <input type="checkbox" name="_spring_security_remember_me" style="display: none" ng-model="rememberme" />
        <div flex="flex"></div>
        <md-button type="submit" class="md-raised md-primary"><s:message code="security.signIn" /></md-button>
      </div>
    </md-content>
	<div class="md-padding error" style="color: rgb(221,44,0)" ng-show="${lastExceptionMessage != null}">
		${lastExceptionMessage}
	</div>
	</form>
  </div>
</div>		
	
<script type="text/javascript">
var userName = "<c:out value='${param.userName}' />";
</script>
	
	<c:import url="/WEB-INF/views/common/page-body-footer.jsp" />
	<script src="${staticResourcesBase}/static/js/login.js"></script>
	
</body>
</html>


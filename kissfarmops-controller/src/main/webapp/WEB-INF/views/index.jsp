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
	<title><s:message code="app.title" /></title>
</head>
<body layout="column">
	<c:import url="/WEB-INF/views/common/page-body-header.jsp" />

	<div layout="column" layout-align="center center" flex>
	<div>Please login</div>
	</div>
	
	<c:import url="/WEB-INF/views/common/page-body-footer.jsp" />
</body>
</html>


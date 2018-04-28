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
<title><s:message code="app.title" /></title>
<c:import url="/WEB-INF/views/common/page-head.jsp" />
</head>
<body>
	<c:import url="/WEB-INF/views/common/page-body-header.jsp" />

	<h2>Ooops! HTTP 404: Requested page wasn't found</h2>
	Are you playing with urls :o) ?

	<c:import url="/WEB-INF/views/common/page-body-footer.jsp" />
</body>
</html>

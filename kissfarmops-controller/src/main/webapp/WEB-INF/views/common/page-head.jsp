<%@ page pageEncoding="UTF-8"%>
<%@ page session="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">

<link rel="stylesheet" href="${staticResourcesBase}/webjars/angular-material/1.1.5/angular-material.min.css">
<link rel="stylesheet" href="${staticResourcesBase}/webjars/md-data-table/0.10.9/md-data-table.min.css">
<link rel="stylesheet" href="${staticResourcesBase}/static/css/app.css">

<link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons" >

<style type="text/css">
[ng\:cloak], [ng-cloak], .ng-cloak {
	display: none !important;
}
</style>

<script type="text/javascript">
var staticResourcesBase = "${staticResourcesBase}";
var contextPath = "${contextPath}";
var lang = "${lang}";
var currentUser = ${currentUserJson};
var currentUserRoles = ${currentUserRolesJson};
</script>

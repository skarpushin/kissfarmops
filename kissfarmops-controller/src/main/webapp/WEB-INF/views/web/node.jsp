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
	<title><s:message code="app.title" /> - <s:message code="term.nodes" /></title>
</head>
<body layout="column">
	<c:import url="/WEB-INF/views/common/page-body-side-nav-begin.jsp" />

	<md-content flex id="content" ng-controller="NodesController" ng-cloak class="ng-cloak">
		<md-card>
			<ng-include src="'${staticResourcesBase}/static/templates/crud/list-table-actions.html'"></ng-include>
			
			<md-table-container ng-show="rows.length > 0">
			  <table md-table ng-model="selected" md-progress="promise">
			    <thead md-head md-order="viewParams.fieldSort" md-on-reorder="reload">
			      <tr md-row>
			        <th md-column ng-if="isRowActionsEnabled"><md-icon style="margin-right: 15px;">toc</md-icon></th>
			        <th md-column><span><s:message code="term.nodeId" /></span></th>
			        <th md-column md-order-by="hostName" hide-xs hide-sm hide-md show-gt-md><s:message code="term.hostName" /></th>
			        <th md-column md-order-by="publicIp" hide-xs hide-sm hide-md show-gt-md><s:message code="term.publicIp" /></th>
			        <th md-column><span><s:message code="term.nodeTags" /></span></th>
			        <th md-column md-order-by="online" hide-xs hide-sm hide-md show-gt-md><s:message code="term.online" /></th>
			      </tr>
			    </thead>
			    <tbody md-body>
			      <tr md-row md-auto-select ng-repeat="row in rows" md-select="row" md-select-id="id">
					<td md-cell class="no-padding-right" ng-if="isRowActionsEnabled()">
						<ng-include src="'${staticResourcesBase}/static/templates/crud/list-row-menu.html'"></ng-include>
					</td>
			        <td md-cell>{{row.id}}</td>
			        <td md-cell hide-xs hide-sm hide-md show-gt-md>{{row.hostName}}</td>
			        <td md-cell hide-xs hide-sm hide-md show-gt-md>{{row.publicIp}}</td>
			        <td md-cell hide-xs hide-sm hide-md show-gt-md>{{row.tags}}</td>
			        <td md-cell hide-xs hide-sm hide-md show-gt-md><i ng-show="row.online" class="material-icons">check_circle</i></td>
			      </tr>
			    </tbody>
			  </table>
			</md-table-container>
			<md-table-pagination ng-show="rows.length > 0" md-limit="viewParams.queryParams.pagerParams.max" md-limit-options="pagerOptions" md-page="viewParams.page" md-total="{{totalResults}}" md-on-paginate="reload" md-page-select></md-table-pagination>	
		</md-card>		
	</md-content>
     
	<c:import url="/WEB-INF/views/common/page-body-side-nav-end.jsp" />
	<c:import url="/WEB-INF/views/common/page-body-footer.jsp" />
	<script src="${staticResourcesBase}/static/js/crud-list-controller.js"></script>
	<script src="${staticResourcesBase}/static/js/nodes-controller.js"></script>
	
</body>
</html>


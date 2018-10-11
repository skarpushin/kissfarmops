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
	<title><s:message code="app.title" /> - <s:message code="term.repoConfig" /></title>
</head>
<body layout="column">
	<c:import url="/WEB-INF/views/common/page-body-side-nav-begin.jsp" />

	<md-content flex id="content" ng-cloak class="ng-cloak">
		<div layout-gt-sm="row" ng-controller="FarmConfigController">
			<md-card flex-gt-sm>
		        <md-card-title>
		          <md-card-title-text>
		            <span class="md-headline"><s:message code="term.repo" /></span>
		          </md-card-title-text>
		        </md-card-title>

				<md-card-content ng-if="loading">
					<s:message code="phrase.loadingPleaseWait" />
				</md-card-content>
				<md-card-content ng-if="!loading && !isConfigPresent">
					<s:message code="phrase.noConfigPleaseProvide" />
				</md-card-content>
				<md-card-content ng-if="!loading && isConfigPresent">
					<ul>
						<li><b><s:message code="term.url" /></b>: {{dto.uri}}</li>
						<li><b><s:message code="term.branch" /></b>: {{dto.branch}}</li>
					</ul>
				</md-card-content>
				
		        <md-card-actions layout="row" layout-align="beginning center">
		          <md-button ng-click="openGitConfigDialog($event)" ng-disabled="!isModifyEnabled" class="md-primary md-raised"><s:message code="action.modify" /></md-button>
		          <md-button ng-click="pullChanges($event)" ng-disabled="!isPullEnabled" class="md-primary md-raised"><s:message code="action.checkForUpdates" /></md-button>
		        </md-card-actions>
			</md-card>
			<md-card flex-gt-sm>
		        <md-card-title>
		          <md-card-title-text>
		            <span class="md-headline">State Machine</span>
		          </md-card-title-text>
		        </md-card-title>
	
				<md-card-content ng-if="loading">
					<s:message code="phrase.loadingPleaseWait" />
				</md-card-content>
				<md-card-content ng-if="!loading">
					<ul>
						<li><b><s:message code="term.state" /></b>: {{smData.currentStateName}}</li>
						<li><b><s:message code="term.exception" /></b>: {{smData.exception}}</li>
						<li><b><s:message code="term.activeWorkTree" /></b>: {{smData.vars.activeWorkTree}}</li>
						<li><b><s:message code="term.activeVersion" /></b>: {{smData.vars.activeVersion}}</li>
					</ul>
				</md-card-content>
			</md-card>
		</div>		
		<md-card ng-controller="FarmConfigStateController" >
	        <md-card-title>
	          <md-card-title-text>
	            <span class="md-headline"><s:message code="term.state" /></span>
	          </md-card-title-text>
	        </md-card-title>
			<md-card-content flex layout-padding>
				<md-table-container ng-show="rows.length > 0">
				  <table md-table ng-model="selected" md-progress="promise">
				    <thead md-head md-order="viewParams.fieldSort" md-on-reorder="reload">
				      <tr md-row>
				        <th md-column md-order-by="createdAt"><span><s:message code="term.date" /></span></th>
				        <th md-column><s:message code="term.state" /></th>
				        <th md-column hide-xs hide-sm show-gt-sm><s:message code="term.stateResult" /></th>
				      </tr>
				    </thead>
				    <tbody md-body>
				      <tr md-row md-auto-select ng-repeat="row in rows" md-select="row" md-select-id="id">
				        <td md-cell>{{row.createdAt | date:'yyyy-MM-dd HH:mm:ss'}}</td>
				        <td md-cell>{{row.stateName}}</td>
				        <td md-cell hide-xs hide-sm show-gt-sm>{{row.resultMessage}}</td>
				      </tr>
				    </tbody>
				  </table>
				</md-table-container>
				<md-table-pagination ng-show="rows.length > 0" md-limit="viewParams.queryParams.pagerParams.max" md-limit-options="pagerOptions" md-page="viewParams.page" md-total="{{totalResults}}" md-on-paginate="reload" md-page-select></md-table-pagination>	
			</md-card-content>        
		</md-card>		
	</md-content>
	<c:import url="/WEB-INF/views/common/page-body-side-nav-end.jsp" />
	<c:import url="/WEB-INF/views/common/page-body-footer.jsp" />
	
	<script src="${staticResourcesBase}/static/js/crud-list-controller.js"></script>
	<script src="${staticResourcesBase}/static/js/farm-config-controller.js"></script>
	
</body>
</html>


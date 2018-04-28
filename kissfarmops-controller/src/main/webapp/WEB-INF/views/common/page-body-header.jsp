<%@ page pageEncoding="UTF-8"%>
<%@ page session="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>

<md-toolbar ng-cloak class="ng-cloak" layout="column" ng-controller="MainMenuController">
	<div class="md-toolbar-tools">
		<h2>
			<a href="${contextPath}/" ng-class="::getClassForMenuItem('/')"><s:message code="app.title" /></a>
		</h2>
		<span flex></span>
		<md-menu>
			<md-button class="md-icon-button" ng-click="$mdMenu.open($event)">
				<md-icon>language</md-icon>
      		</md-button>
			<md-menu-content>
		        <md-menu-item ng-repeat="lang in langs" ng-if="lang.lang != currentLang">
		          <md-button ng-click="switchToLang($event, lang.lang)">
		          	<img ng-src="{{ ::lang.imgSrc }}" class="btn-img-icon">
		            <span>{{ ::lang.title }}</span>
		          </md-button>
		        </md-menu-item>
	      </md-menu-content>      		
		</md-menu>
		<sec:authorize access="hasRole('ROLE_ANONYMOUS')">
			<md-menu>
				<md-button  ng-click="$mdMenu.open($event)">
					<md-icon md-menu-origin>account_circle</md-icon>
					<s:message code="security.anonymousUser" />
	      		</md-button>
				<md-menu-content>
			        <md-menu-item>
			          <a href="${contextPath}/login/form" class="md-button">
			          	<md-icon>lock_open</md-icon>
			            <span><s:message code="security.signIn" /></span>
			          </a>
			        </md-menu-item>

			        <md-menu-item>
			          <a href="${contextPath}/login/register" class="md-button">
			          	<md-icon></md-icon>
			            <span><s:message code="security.register" /></span>
			          </a>
			        </md-menu-item>
		      </md-menu-content>      		
			</md-menu>
		</sec:authorize>
		<sec:authorize access="hasRole('ROLE_USER')">
			<md-menu>
				<md-button  ng-click="$mdMenu.open($event)">
					<md-icon md-menu-origin>account_circle</md-icon>
					${currentUser.displayName}
	      		</md-button>
				<md-menu-content>
					<sec:authorize access="hasRole('ROLE_ADMIN')">
				        <md-menu-item>
				          <a href="${contextPath}/swagger-ui.html" class="md-button">
				          	<md-icon></md-icon>
				            <span>Swagger</span>
				          </a>
				        </md-menu-item>
				        <md-menu-item>
				          <a href="${contextPath}/monitoring" class="md-button">
				          	<md-icon></md-icon>
				            <span>Java Melody</span>
				          </a>
				        </md-menu-item>
				        <md-menu-item>
				          <a href="${contextPath}/error/exc" class="md-button">
				          	<md-icon></md-icon>
				            <span>Exceptions statistics</span>
				          </a>
				        </md-menu-item>
			        </sec:authorize>
			        
			        <md-menu-item>
			          <a href="${contextPath}/login/change" class="md-button">
			          	<md-icon></md-icon>
			            <span><s:message code="security.changePassword" /></span>
			          </a>
			        </md-menu-item>
			        
			        <md-menu-item>
			          <a href="${contextPath}/j_spring_security_logout" class="md-button">
			          	<md-icon>lock</md-icon>
			            <span><s:message code="security.signOut" /></span>
			          </a>
			        </md-menu-item>
		      </md-menu-content>      		
			</md-menu>
		</sec:authorize>
		
	</div>
</md-toolbar>

<%@ page pageEncoding="UTF-8"%>
<%@ page session="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>

<div layout="row" ng-controller="SideNavController" flex>

	<md-sidenav layout="column" ng-cloak class="ng-cloak md-sidenav-left md-whiteframe-z2" md-component-id="left" md-is-locked-open="$mdMedia('gt-md')">
	     <md-toolbar class="md-tall md-hue-2">
	       <span flex></span>
	       <div layout="column" class="md-toolbar-tools-bottom inset">
	         <div>{{::user.displayName}}</div>
	         <div>{{::user.email}}</div>
	       </div>
	     </md-toolbar>
	     
		<md-content flex>	
		
		<md-list>
	      <div ng-repeat="group in menuGroups">
	        <md-divider ng-if="$index > 0"></md-divider>
	        <md-subheader ng-if="$index > 0">{{::group.title}}</md-subheader>
      	
		      <md-list-item ng-repeat-start="item in group.menuItems" ng-class="{'active-list-item': item.active}" md-ink-ripple ng-href="{{::item.href}}">
				<md-icon style="margin-right: 15px;">{{::item.icon}}</md-icon>
		      	<span>{{item.title}}</span>
		      	 <span flex></span>
		      </md-list-item>
		      <md-list-item ng-repeat-end ng-repeat="subItem in item.subItems" ng-class="{'active-list-item': subItem.active}" md-ink-ripple ng-href="{{::subItem.href}}">
				<md-icon style="margin-right: 30px;">{{::subItem.icon}}</md-icon>
		      	<span>{{subItem.title}}</span>
		      	 <span flex></span>
		      </md-list-item>
	      </div>


	        <md-divider></md-divider>
		      <md-list-item md-ink-ripple>
				<md-icon style="margin-right: 15px;">language</md-icon>
				<md-menu>
					<span ng-click="$mdMenu.open($event)">
						{{::currentLangTitle()}}
		      		</span>
					<md-menu-content>
				        <md-menu-item ng-repeat="lang in langs" ng-if="lang.lang != currentLang">
				          <md-button ng-click="switchToLang($event, lang.lang)">
				          	<img ng-src="{{ ::lang.imgSrc }}" class="btn-img-icon">
				            <span>{{ ::lang.title }}</span>
				          </md-button>
				        </md-menu-item>
			      </md-menu-content>      		
				</md-menu>

		      	 <span flex></span>
		      </md-list-item>

	   </md-list>
	      
	    </md-content>
	</md-sidenav>	 	

	 <div flex layout="column">
		<md-toolbar ng-cloak class="ng-cloak md-whiteframe-z2" layout="row">
			<div class="md-toolbar-tools">
				 <md-button hide-gt-md ng-click="showSideNav()" >
				      <md-icon>menu</md-icon>
				      <span></span>
			    </md-button>				
			
				<h2 ng-repeat="item in breadcrumb" show-gt-xs hide-xs>
					<span ng-if="$index > 0">&nbsp;&nbsp;/&nbsp;&nbsp;</span>
					<span>
						{{item.title}}
					</span>
				</h2>
				<h2 hide-gt-xs>
					<span>
						{{breadcrumb[breadcrumb.length - 1].title}}
					</span>
				</h2>
				<span ng-show="hasPageActions">
					 <md-button ng-repeat="action in pageActions" ng-click="action.handler()" class="md-raised md-accent">
					      <md-icon ng-if="action.icon">{{ action.icon }}</md-icon>
					      <span ng-if="action.title">{{ action.title }}</span>
				    </md-button>				
				</span>
				<span flex></span>
			</div>
		</md-toolbar>
	
		
		

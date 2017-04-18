'use strict';

/* App Module */

var hadrianApp = angular.module('HadrianApp', [
    'ngRoute',
    'ngAnimate',
    'ui.bootstrap',
    'hadrianDirectives',
    'hadrianControllers',
    'hadrianFilters',
    'hadrianServices',
    'ui.ace',
    'ui.select'
]);

hadrianApp.config(['$routeProvider',
    function ($routeProvider) {
        $routeProvider.
                when('/Team/:teamId', {
                    templateUrl: 'partials/team.html',
                    controller: 'TeamCtrl'
                }).
                when('/Dashboard/:teamId/:env', {
                    templateUrl: 'partials/teamDashboard.html',
                    controller: 'TeamDashboardCtrl'
                }).
                when('/DashboardAll/:env', {
                    templateUrl: 'partials/allDashboard.html',
                    controller: 'AllDashboardCtrl'
                }).
                when('/Service/:serviceId', {
                    templateUrl: 'partials/service.html',
                    controller: 'ServiceCtrl'
                }).
                when('/Service/:serviceId/:tabName', {
                    templateUrl: 'partials/service.html',
                    controller: 'ServiceCtrl'
                }).
                when('/Service/:serviceId/:tabName/:envName', {
                    templateUrl: 'partials/service.html',
                    controller: 'ServiceCtrl'
                }).
                when('/Catalog', {
                    templateUrl: 'partials/catalog.html',
                    controller: 'CatalogCtrl'
                }).
                when('/Graph', {
                    templateUrl: 'partials/graph.html',
                    controller: 'GraphCtrl'
                }).
                when('/FindHost', {
                    templateUrl: 'partials/findHost.html',
                    controller: 'FindHostCtrl'
                }).
                when('/Parameters', {
                    templateUrl: 'partials/parameters.html',
                    controller: 'ParametersCtrl'
                }).
                when('/WorkItems', {
                    templateUrl: 'partials/workItems.html',
                    controller: 'WorkItemsCtrl'
                }).
                when('/Help', {
                    templateUrl: 'partials/help.html',
                    controller: 'HelpCtrl'
                }).
                otherwise({
                    redirectTo: '/Catalog'
                });
    }]);

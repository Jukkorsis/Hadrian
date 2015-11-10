'use strict';

/* App Module */

var soaRepApp = angular.module('SoaRepApp', [
    'ngRoute',
    'ngAnimate',
    'ui.bootstrap',
    'soaRepDirectives',
    'soaRepControllers',
    'soaRepFilters',
    'soaRepServices'
]);

soaRepApp.config(['$routeProvider',
    function ($routeProvider) {
        $routeProvider.
                when('/Home', {
                    templateUrl: 'partials/home.html',
                    controller: 'HomeCtrl'
                }).
                when('/Team/:teamId', {
                    templateUrl: 'partials/team.html',
                    controller: 'TeamCtrl'
                }).
                when('/Service/:serviceId', {
                    templateUrl: 'partials/service.html',
                    controller: 'ServiceCtrl'
                }).
                when('/Graph', {
                    templateUrl: 'partials/graph.html',
                    controller: 'GraphCtrl'
                }).
                when('/Portal', {
                    templateUrl: 'partials/portal.html',
                    controller: 'portalCtrl'
                }).
                when('/OpsTeam', {
                    templateUrl: 'partials/opsteam.html',
                    controller: 'OpsTeamCtrl'
                }).
                when('/Admin', {
                    templateUrl: 'partials/admin.html',
                    controller: 'AdminCtrl'
                }).
                when('/Help', {
                    templateUrl: 'partials/help.html',
                    controller: 'HelpCtrl'
                }).
                otherwise({
                    redirectTo: '/Home'
                });
    }]);

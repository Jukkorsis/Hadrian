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
                when('/home', {
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
                when('/graph', {
                    templateUrl: 'partials/graph.html',
                    controller: 'GraphCtrl'
                }).
                when('/admin', {
                    templateUrl: 'partials/admin.html',
                    controller: 'AdminCtrl'
                }).
                when('/help', {
                    templateUrl: 'partials/help.html',
                    controller: 'HelpCtrl'
                }).
                otherwise({
                    redirectTo: '/home'
                });
    }]);

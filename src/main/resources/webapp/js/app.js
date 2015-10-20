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
                when('/tree', {
                    templateUrl: 'partials/tree.html',
                    controller: 'TreeCtrl'
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
                    redirectTo: '/tree'
                });
    }]);

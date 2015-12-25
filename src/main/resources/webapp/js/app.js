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
                when('/Proxy', {
                    templateUrl: 'partials/proxy.html',
                    controller: 'ProxyCtrl'
                }).
                when('/Parameters', {
                    templateUrl: 'partials/parameters.html',
                    controller: 'ParametersCtrl'
                }).
                when('/CrossService', {
                    templateUrl: 'partials/crossService.html',
                    controller: 'CrossServiceCtrl'
                }).
                when('/WorkItems', {
                    templateUrl: 'partials/workItems.html',
                    controller: 'WorkItemsCtrl'
                }).
                when('/Backfill', {
                    templateUrl: 'partials/backfill.html',
                    controller: 'BackfillCtrl'
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

soaRepApp.factory('redirectInterceptor', function ($q, $location, $window) {
    return {
        'response': function (response) {
            if (typeof response.data === 'string' && response.data.indexOf("/ui/login.html") > -1) {
                console.log("LOGIN!!");
                console.log(response.data);
                $window.location.href = "/ui/login.html";
                return $q.reject(response);
            } else {
                return response;
            }
        }
    }

});

soaRepApp.config(['$httpProvider', function ($httpProvider) {
        $httpProvider.interceptors.push('redirectInterceptor');
    }]); 
'use strict';

/* App Module */

var hadrianApp = angular.module('HadrianApp', [
    'ngRoute',
    'ngAnimate',
    'ui.bootstrap',
    'hadrianDirectives',
    'hadrianControllers',
    'hadrianFilters',
    'hadrianServices'
]);

hadrianApp.config(['$routeProvider',
    function ($routeProvider) {
        $routeProvider.
                when('/Activty', {
                    templateUrl: 'partials/activity.html',
                    controller: 'ActivityCtrl'
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
                when('/Users', {
                    templateUrl: 'partials/users.html',
                    controller: 'UsersCtrl'
                }).
                when('/Help', {
                    templateUrl: 'partials/help.html',
                    controller: 'HelpCtrl'
                }).
                otherwise({
                    redirectTo: '/Activty'
                });
    }]);

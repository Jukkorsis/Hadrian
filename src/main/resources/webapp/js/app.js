'use strict';

/* App Module */

var soaRepApp = angular.module('SoaRepApp', [
  'ngRoute',
  'soaRepAnimations',

  'soaRepDirectives',
  'soaRepControllers',
  'soaRepFilters',
  'soaRepServices'
]);

soaRepApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
      when('/services', {
        templateUrl: 'partials/service-list.html',
        controller: 'ServiceListCtrl'
      }).
      when('/services/:serviceId', {
        templateUrl: 'partials/service-detail.html',
        controller: 'ServiceDetailCtrl'
      }).
       when('/createService', {
        templateUrl: 'partials/service-create.html',
        controller: 'ServiceCreateCtrl'
      }).
       when('/editService/:serviceId', {
        templateUrl: 'partials/service-edit.html',
        controller: 'ServiceEditCtrl'
      }).
       when('/createVersion/:serviceId', {
        templateUrl: 'partials/version-create.html',
        controller: 'VersionCreateCtrl'
      }).
       when('/editVersion/:serviceId/:versionId', {
        templateUrl: 'partials/version-edit.html',
        controller: 'VersionEditCtrl'
      }).
       when('/editVersionUses/:serviceId/:versionId', {
        templateUrl: 'partials/versionUses-edit.html',
        controller: 'VersionUsesEditCtrl'
      }).
       when('/graphService', {
        templateUrl: 'partials/service-graph.html',
        controller: 'ServiceGraphCtrl'
      }).
       when('/help', {
        templateUrl: 'partials/service-help.html',
        controller: 'ServiceHelpCtrl'
      }).
      otherwise({
        redirectTo: '/services'
      });
  }]);

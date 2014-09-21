'use strict';

/* Services */

var soaRepServices = angular.module('soaRepServices', ['ngResource']);

soaRepServices.factory('Service', ['$resource', function($resource) {
        return $resource('/services/:serviceId.json', {}, {
            query: {method: 'GET', params: {serviceId: 'services'}, isArray: true}
        });
    }]);

soaRepServices.factory('VersionUses', ['$resource', function($resource) {
        return $resource('/services/:serviceId/versions/:versionId/uses.json', {}, {
            query: {method: 'GET', params: {}, isArray: true}
        });
    }]);

soaRepServices.factory('Graph', ['$resource', function($resource) {
        return $resource('/servicesGraph.json', {}, {
            query: {method: 'GET', isArray: false}
        });
    }]);

soaRepServices.factory('Config', ['$resource', function($resource) {
        return $resource('/config.json', {}, {
            query: {method: 'GET', isArray: false}
        });
    }]);

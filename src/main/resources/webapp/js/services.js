'use strict';

/* Services */

var soaRepServices = angular.module('soaRepServices', ['ngResource']);

soaRepServices.factory('Service', ['$resource', function($resource) {
        return $resource('/services/:serviceId.json', {}, {
            query: {method: 'GET', params: {serviceId: 'services'}, isArray: true}
        });
    }]);

soaRepServices.factory('Graph', ['$resource', function($resource) {
        return $resource('/servicesGraph.json', {}, {
            query: {method: 'GET', isArray: false}
        });
    }]);

'use strict';

/* Services */

var soaRepServices = angular.module('soaRepServices', ['ngResource']);

soaRepServices.factory('Tree', ['$resource', function($resource) {
        return $resource('/v1/tree', {}, {
            query: {method: 'GET', isArray: true}
        });
    }]);

soaRepServices.factory('Team', ['$resource', function($resource) {
        return $resource('/v1/team/:teamId', {}, {
            query: {method: 'GET', params: {teamId: 'services'}, isArray: false}
        });
    }]);

soaRepServices.factory('Services', ['$resource', function($resource) {
        return $resource('/v1/service', {}, {
            query: {method: 'GET', isArray: true}
        });
    }]);

soaRepServices.factory('Service', ['$resource', function($resource) {
        return $resource('/v1/service/:serviceId', {}, {
            query: {method: 'GET', params: {serviceId: 'services'}, isArray: false}
        });
    }]);

soaRepServices.factory('ServiceNotUses', ['$resource', function($resource) {
        return $resource('/v1/service/:serviceId/notuses', {}, {
            query: {method: 'GET', params: {serviceId: 'services'}, isArray: false}
        });
    }]);

soaRepServices.factory('HostDetails', ['$resource', function($resource) {
        return $resource('/v1/host/:serviceId/:hostId/details', {}, {
            query: {method: 'GET', params: {serviceId: 'services', hostId: 'services'}, isArray: false}
        });
    }]);

soaRepServices.factory('DataStore', ['$resource', function($resource) {
        return $resource('/v1/datastore/:dataStoreId', {}, {
            query: {method: 'GET', params: {dataStoreId: 'services'}, isArray: false}
        });
    }]);

soaRepServices.factory('Portal', ['$resource', function($resource) {
        return $resource('/v1/portal', {}, {
            query: {method: 'GET', isArray: true}
        });
    }]);

soaRepServices.factory('Config', ['$resource', function($resource) {
        return $resource('/v1/config', {}, {
            query: {method: 'GET', isArray: false}
        });
    }]);

soaRepServices.factory('Calendar', ['$resource', function($resource) {
        return $resource('/v1/calendar', {}, {
            query: {method: 'GET', isArray: false}
        });
    }]);

soaRepServices.factory('User', ['$resource', function($resource) {
        return $resource('/v1/users', {}, {
            query: {method: 'GET', isArray: false}
        });
    }]);

soaRepServices.factory('WorkItem', ['$resource', function($resource) {
        return $resource('/v1/workitems', {}, {
            query: {method: 'GET', isArray: false}
        });
    }]);

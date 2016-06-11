'use strict';

/* Services */

var hadrianServices = angular.module('hadrianServices', ['ngResource']);

hadrianServices.factory('Tree', ['$resource', function($resource) {
        return $resource('/v1/tree', {}, {
            query: {method: 'GET', isArray: true}
        });
    }]);

hadrianServices.factory('Team', ['$resource', function($resource) {
        return $resource('/v1/team', {}, {
            query: {method: 'GET', isArray: false}
        });
    }]);

hadrianServices.factory('Services', ['$resource', function($resource) {
        return $resource('/v1/services', {}, {
            query: {method: 'GET', isArray: false}
        });
    }]);

hadrianServices.factory('Service', ['$resource', function($resource) {
        return $resource('/v1/service', {}, {
            query: {method: 'GET', isArray: false}
        });
    }]);

hadrianServices.factory('ServiceRefresh', ['$resource', function($resource) {
        return $resource('/v1/service/refresh', {}, {
            query: {method: 'GET', isArray: false}
        });
    }]);

hadrianServices.factory('ServiceNotUses', ['$resource', function($resource) {
        return $resource('/v1/service/notuses', {}, {
            query: {method: 'GET', isArray: false}
        });
    }]);

hadrianServices.factory('HostDetails', ['$resource', function($resource) {
        return $resource('/v1/host/details', {}, {
            query: {method: 'GET', isArray: false}
        });
    }]);

hadrianServices.factory('VipDetails', ['$resource', function($resource) {
        return $resource('/v1/vip/details', {}, {
            query: {method: 'GET', isArray: false}
        });
    }]);

hadrianServices.factory('DataStore', ['$resource', function($resource) {
        return $resource('/v1/datastore/:dataStoreId', {}, {
            query: {method: 'GET', params: {dataStoreId: 'services'}, isArray: false}
        });
    }]);

hadrianServices.factory('Portal', ['$resource', function($resource) {
        return $resource('/v1/portal', {}, {
            query: {method: 'GET', isArray: true}
        });
    }]);

hadrianServices.factory('Config', ['$resource', function($resource) {
        return $resource('/v1/config', {}, {
            query: {method: 'GET', isArray: false}
        });
    }]);

hadrianServices.factory('Calendar', ['$resource', function($resource) {
        return $resource('/v1/calendar', {}, {
            query: {method: 'GET', isArray: false}
        });
    }]);

hadrianServices.factory('User', ['$resource', function($resource) {
        return $resource('/v1/users', {}, {
            query: {method: 'GET', isArray: false}
        });
    }]);

hadrianServices.factory('WorkItem', ['$resource', function($resource) {
        return $resource('/v1/workitems', {}, {
            query: {method: 'GET', isArray: false}
        });
    }]);

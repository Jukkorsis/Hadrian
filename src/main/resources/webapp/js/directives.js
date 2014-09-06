'use strict';

/* Directives */

var soaRepDirectives = angular.module('soaRepDirectives', ['angularFileUpload']);

soaRepDirectives.directive('visNetwork', function() {
    return {
        restrict: 'AE',
        link: function(scope, element, attrs) {
            var buildGraph;
            buildGraph = function(scope, element) {
                var container, visg;
                container = element[0];
                visg = null;
                visg = new vis.Network(container, scope.data, scope.options);
            };
            // Wait for data asynchronously with $resource in the controller 
            scope.$watch('data', function(newval, oldval) {
                buildGraph(scope, element);
            }, true);
        }
    };
});

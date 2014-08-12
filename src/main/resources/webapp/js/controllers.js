'use strict';

/* Controllers */

var soaRepControllers = angular.module('soaRepControllers', []);

soaRepControllers.controller('ServiceListCtrl', ['$scope', 'Service',
    function($scope, Service) {
        $scope.services = Service.query();
        $scope.orderProp = 'name';
    }]);

soaRepControllers.controller('ServiceDetailCtrl', ['$scope', '$routeParams', 'Service',
    function($scope, $routeParams, Service) {
        $scope.service = Service.get({serviceId: $routeParams.serviceId}, function(service) {
            $scope.mainImageUrl = service.images[0];
        });

        $scope.setImage = function(imageUrl) {
            $scope.mainImageUrl = imageUrl;
        }
    }]);

soaRepControllers.controller('ServiceCreateCtrl', ['$scope', '$http', '$window',
    function($scope, $http, $window) {
        $scope.myForm = {};
        $scope.myForm.access = "Internal";
        $scope.myForm.type = "Service";
        $scope.myForm.state = "Stateless";

        $scope.myForm.submitTheForm = function(item, event) {
            console.log("--> Submitting create form");
            var dataObject = {
                _id: $scope.myForm._id,
                name: $scope.myForm.name,
                team: $scope.myForm.team,
                description: $scope.myForm.description,
                access: $scope.myForm.access,
                type: $scope.myForm.type,
                state: $scope.myForm.state
            };

            var responsePromise = $http.post("/services/services.json", dataObject, {});
            responsePromise.success(function(dataFromServer, status, headers, config) {
                console.log(dataFromServer.title);
                $window.location.href = "#/services/" + $scope.myForm._id;
            });
            responsePromise.error(function(data, status, headers, config) {
                alert("Submitting form failed!");
            });
        }
    }]);

soaRepControllers.controller('ServiceEditCtrl', ['$scope', '$routeParams', 'Service', '$http', '$window',
    function($scope, $routeParams, Service, $http, $window) {
        $scope.editForm = Service.get({serviceId: $routeParams.serviceId}, function(service) {
        });

        $scope.submitEditForm = function(item, event) {
            console.log("--> Submitting edit form");
            var dataObject = {
                _id: $scope.editForm._id,
                name: $scope.editForm.name,
                team: $scope.editForm.team,
                description: $scope.editForm.description,
                access: $scope.editForm.access,
                type: $scope.editForm.type,
                state: $scope.editForm.state
            };

            var responsePromise = $http.post("/services/services.json", dataObject, {});
            responsePromise.success(function(dataFromServer, status, headers, config) {
                console.log(dataFromServer.title);
                $window.location.href = "#/services/" + $scope.editForm._id;
            });
            responsePromise.error(function(data, status, headers, config) {
                alert("Submitting form failed!");
            });
        }
    }]);

soaRepControllers.controller('ServiceGraphCtrl', ['$scope', 'Graph',
    function($scope, Graph) {
        $scope.data = Graph.query();
        $scope.options = {navigation: true, width: '100%', height: '600px'};
    }]);

soaRepControllers.controller('ServiceHelpCtrl', ['$scope',
    function($scope) {
    }]);


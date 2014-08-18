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
        $scope.createForm = {};
        $scope.createForm.access = "Internal";
        $scope.createForm.type = "Service";
        $scope.createForm.state = "Stateless";
        $scope.createForm.status = "Proposed";

        $scope.submitCreateServiceForm = function(item, event) {
            console.log("--> Submitting create form");
            var dataObject = {
                _id: $scope.createForm._id,
                name: $scope.createForm.name,
                team: $scope.createForm.team,
                description: $scope.createForm.description,
                access: $scope.createForm.access,
                type: $scope.createForm.type,
                state: $scope.createForm.state,
                api: $scope.createForm.api,
                impl: $scope.createForm.impl,
                status: $scope.createForm.status
            };

            var responsePromise = $http.post("/services/services.json", dataObject, {});
            responsePromise.success(function(dataFromServer, status, headers, config) {
                console.log(dataFromServer.title);
                $window.location.href = "#/services/" + $scope.createForm._id;
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

        $scope.submitEditServiceForm = function(item, event) {
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

            var responsePromise = $http.post("/services/" + $scope.editForm._id + ".json", dataObject, {});
            responsePromise.success(function(dataFromServer, status, headers, config) {
                console.log(dataFromServer.title);
                $window.location.href = "#/services/" + $scope.editForm._id;
            });
            responsePromise.error(function(data, status, headers, config) {
                alert("Submitting form failed!");
            });
        }
    }]);

soaRepControllers.controller('VersionCreateCtrl', ['$scope', '$routeParams', '$http', '$window',
    function($scope, $routeParams, $http, $window) {
        $scope.createForm = {};
        $scope.createForm._id = $routeParams.serviceId;
        $scope.createForm.status = "Proposed";

        $scope.submitCreateVersionForm = function(item, event) {
            console.log("--> Submitting create form");
            var dataObject = {
                _id: $scope.createForm._id,
                api: $scope.createForm.api,
                impl: $scope.createForm.impl,
                status: $scope.createForm.status
            };

            var responsePromise = $http.post("/services/" + $scope.createForm._id + "/versions.json", dataObject, {});
            responsePromise.success(function(dataFromServer, status, headers, config) {
                console.log(dataFromServer.title);
                $window.location.href = "#/services/" + $scope.createForm._id;
            });
            responsePromise.error(function(data, status, headers, config) {
                alert("Submitting form failed!");
            });
        }
    }]);

soaRepControllers.controller('VersionEditCtrl', ['$scope', '$routeParams', 'Service', 'VersionUses', '$http', '$window',
    function($scope, $routeParams, Service, VersionUses, $http, $window) {
        $scope.editForm = {};
        Service.get({serviceId: $routeParams.serviceId}, function(service) {
            $scope.editForm._id = service._id;
            service.versions.forEach(function(version) {
                if (version.api === $routeParams.versionId) {
                    $scope.editForm.api = version.api;
                    $scope.editForm.impl = version.impl;
                    $scope.editForm.status = version.status;
                    var responsePromise = $http.get("/services/" + $scope.editForm._id + "/" + $scope.editForm.api + "/uses.json", {});
                    responsePromise.success(function(dataFromServer, status, headers, config) {
                        $scope.editForm.uses = dataFromServer;
                    });
                }
            });
        });


        $scope.submitEditVersionForm = function(item, event) {
            console.log("--> Submitting edit form");
            var dataObject = {
                _id: $scope.editForm._id,
                api: $scope.editForm.api,
                impl: $scope.editForm.impl,
                status: $scope.editForm.status,
                form: $scope.editForm
            };

            var responsePromise = $http.post("/services/" + $scope.editForm._id + "/" + $scope.editForm.api + ".json", dataObject, {});
            responsePromise.success(function(dataFromServer, status, headers, config) {
                console.log(dataFromServer.title);
                $window.location.href = "#/services/" + $scope.editForm._id;
            });
            responsePromise.error(function(data, status, headers, config) {
                alert("Submitting form failed!");
            });
        }
    }]);

soaRepControllers.controller('VersionUsesEditCtrl', ['$scope', '$routeParams', 'VersionUses', '$http', '$window',
    function($scope, $routeParams, VersionUses, $http, $window) {
        $scope.usesForm = VersionUses.get({serviceId: $routeParams.serviceId, versionId: $routeParams.versionId});

        $scope.submitEditVersionUsesForm = function(item, event) {
            console.log("--> Submitting edit uses form");
            $window.location.href = "#/services/" + $scope.usesForm._id;
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


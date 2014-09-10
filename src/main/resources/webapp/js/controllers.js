'use strict';

/* Controllers */

var soaRepControllers = angular.module('soaRepControllers', []);

soaRepControllers.controller('ServiceListCtrl', ['$scope', 'Config', 'Service',
    function($scope, Config, Service) {
        $scope.config = Config.get();
        
        $scope.services = Service.query();
        $scope.orderProp = 'name';
    }]);

soaRepControllers.controller('ServiceDetailCtrl', ['$scope', '$routeParams', 'Config', 'Service',
    function($scope, $routeParams, Config, Service) {
        $scope.config = Config.get();

        $scope.service = Service.get({serviceId: $routeParams.serviceId}, function(service) {
            $scope.mainImageUrl = service.imageLogo;
        });

        $scope.setImage = function(imageUrl) {
            $scope.mainImageUrl = imageUrl;
        }
    }]);

soaRepControllers.controller('ServiceCreateCtrl', ['$scope', 'Config', '$http', '$window',
    function($scope, Config, $http, $window) {
        $scope.config = Config.get();

        $scope.createForm = {};
        $scope.createForm.state = "Stateless";
        $scope.createForm.access = "Internal";
        $scope.createForm.type = "Service";
        $scope.createForm.busValue = "Medium";
        $scope.createForm.pii = "None";
        $scope.createForm.status = "Proposed";

        $scope.submitCreateServiceForm = function(item, event) {
            console.log("--> Submitting create form");
            var dataObject = {
                _id: $scope.createForm._id,
                name: $scope.createForm.name,
                team: $scope.createForm.team,
                description: $scope.createForm.description,
                state: $scope.createForm.state,
                access: $scope.createForm.access,
                type: $scope.createForm.type,
                tech: $scope.createForm.tech,
                busValue: $scope.createForm.busValue,
                pii: $scope.createForm.pii,
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

soaRepControllers.controller('ServiceEditCtrl', ['$scope', '$routeParams', 'Config', 'Service', '$http', '$window',
    function($scope, $routeParams, Config, Service, $http, $window) {
        $scope.config = Config.get();
        
        $scope.editForm = {};
        Service.get({serviceId: $routeParams.serviceId}, function(service) {
            $scope.editForm._id = service._id;
            $scope.editForm.name = service.name;
            $scope.editForm.team = service.team;
            $scope.editForm.description = service.description;
            $scope.editForm.state = service.state;
            $scope.editForm.access = service.access;
            $scope.editForm.type = service.type;
            $scope.editForm.tech = service.tech;
            $scope.editForm.busValue = service.busValue;
            $scope.editForm.pii = service.pii;
            $scope.editForm.endpoints = service.endpoints;
            $scope.editForm.endpoints.push({env: "", url: ""});
            $scope.editForm.links = service.links;
            $scope.editForm.links.push({name: "", url: ""});
            $scope.editForm.dataCenters = service.dataCenters;
            $scope.editForm.haRatings = service.haRatings;
        });

        $scope.submitEditServiceForm = function(item, event) {
            console.log("--> Submitting edit form");
            var dataObject = {
                _id: $scope.editForm._id,
                name: $scope.editForm.name,
                team: $scope.editForm.team,
                description: $scope.editForm.description,
                state: $scope.editForm.state,
                access: $scope.editForm.access,
                type: $scope.editForm.type,
                tech: $scope.editForm.tech,
                busValue: $scope.editForm.busValue,
                pii: $scope.editForm.pii,
                endpoints: $scope.editForm.endpoints,
                links: $scope.editForm.links,
                dataCenters: $scope.editForm.dataCenters,
                haRatings: $scope.editForm.haRatings
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

soaRepControllers.controller('ImageAddCtrl', ['$scope', '$routeParams', '$upload', '$http', '$window',
    function($scope, $routeParams, $upload, $http, $window) {
        $scope.serviceId = $routeParams.serviceId;
        $scope.progress = ' ';

        $scope.onFileSelect = function($files) {
            for (var i = 0; i < $files.length; i++) {
                var file = $files[i];
                $scope.upload = $upload.upload({
                    url: '/services/' + $routeParams.serviceId + '/image.json',
                    data: {myObj: $routeParams.serviceId},
                    file: file
                }).progress(function(evt) {
                    $scope.progress = 'Upload process: ' + parseInt(100.0 * evt.loaded / evt.total) + '%';
                }).success(function(data, status, headers, config) {
                    $window.location.href = "#/services/" + $routeParams.serviceId;
                });
            }
        };
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

soaRepControllers.controller('VersionEditCtrl', ['$scope', '$routeParams', 'Service', '$http', '$window',
    function($scope, $routeParams, Service, $http, $window) {
        $scope.editForm = {};
        Service.get({serviceId: $routeParams.serviceId}, function(service) {
            $scope.editForm._id = service._id;
            if ("endpoints" in service) {
                if (service.endpoints.length > 0) {
                    $scope.editForm.envUrl = service.endpoints[0].url;
                }
            }
            service.versions.forEach(function(version) {
                if (version.api === $routeParams.versionId) {
                    $scope.editForm.api = version.api;
                    $scope.editForm.impl = version.impl;
                    $scope.editForm.status = version.status;
                    $scope.editForm.links = version.links;
                    $scope.editForm.links.push({name: "", url: ""});
                    var responsePromise = $http.get("/services/" + $scope.editForm._id + "/" + $scope.editForm.api + "/uses.json", {});
                    responsePromise.success(function(dataFromServer, status, headers, config) {
                        $scope.editForm.uses1 = [];
                        $scope.editForm.uses2 = [];
                        $scope.editForm.uses3 = [];
                        dataFromServer.forEach(function(element, index) {
                            if (index % 3 === 0) {
                                $scope.editForm.uses1.push(element);
                            } else if (index % 3 === 1) {
                                $scope.editForm.uses2.push(element);
                            } else if (index % 3 === 2) {
                                $scope.editForm.uses3.push(element);
                            }
                        });
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
                links: $scope.editForm.links,
                uses1: $scope.editForm.uses1,
                uses2: $scope.editForm.uses2,
                uses3: $scope.editForm.uses3
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

soaRepControllers.controller('ServiceGraphCtrl', ['$scope', 'Graph',
    function($scope, Graph) {
        $scope.data = Graph.query();
        $scope.options = {navigation: true, width: '100%', height: '600px'};
    }]);

soaRepControllers.controller('ServiceHelpCtrl', ['$scope', 'Config',
    function($scope, Config) {
        $scope.config = Config.get();
    }]);


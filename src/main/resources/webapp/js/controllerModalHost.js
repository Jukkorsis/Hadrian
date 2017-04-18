'use strict';

/* Controllers */

hadrianControllers.controller('ModalBackfillHostCtrl', ['$scope', '$http', '$uibModalInstance', '$route', 'config', 'service', 'moduleEnvironment',
    function ($scope, $http, $uibModalInstance, $route, config, service, moduleEnvironment) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.moduleEnvironment = moduleEnvironment;
        $scope.config = config;

        $scope.formBackfillHost = {};
        $scope.formBackfillHost.dataCenter = $scope.config.dataCenters[0];
        $scope.formBackfillHost.platform = $scope.config.platforms[0];
        $scope.formBackfillHost.hosts = "";

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.moduleEnvironment.moduleId,
                dataCenter: $scope.formBackfillHost.dataCenter,
                environment: $scope.moduleEnvironment.environment,
                platform: $scope.formBackfillHost.platform,
                hosts: $scope.formBackfillHost.hosts
            };

            var responsePromise = $http.put("/v1/host/backfill", dataObject, {});
            responsePromise.then(function (response) {
                $uibModalInstance.close();
                $route.reload();
            });
            responsePromise.catch(function (response) {
                $scope.errorMsg = response.data;
            });
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    }]);

hadrianControllers.controller('ModalAddHostCtrl', ['$scope', '$http', '$uibModalInstance', '$route', 'config', 'service', 'moduleEnvironment',
    function ($scope, $http, $uibModalInstance, $route, config, service, moduleEnvironment) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.moduleEnvironment = moduleEnvironment;
        $scope.config = config;

        $scope.configEnvironment = null;
        for (var i = 0; i < config.environments.length; i++) {
            if (config.environments[i].name === moduleEnvironment.environment) {
                $scope.configEnvironment = config.environments[i];
            }
        }

        for (var i = 0; i < service.modules.length; i++) {
            if (service.modules[i].moduleId === moduleEnvironment.moduleId) {
                $scope.module = service.modules[i];
            }
        }

        $scope.modelOptions = {
            debounce: {
                default: 500,
                blur: 250
            },
            getterSetter: true
        };

        $scope.formSaveHost = {};
        $scope.formSaveHost.version = "";
        $scope.formSaveHost.configVersion = "";
        $scope.formSaveHost.counts = {};
        for (var i = 0; i < config.dataCenters.length; i++) {
            $scope.formSaveHost.counts[config.dataCenters[i]] = 0;
        }
        $scope.formSaveHost.specialInstructions = $scope.module.specialInstructions;
        $scope.formSaveHost.reason = "";

        var responsePromise = $http.get(
                "/v1/service/version?serviceId=" +
                $scope.service.serviceId +
                "&moduleId=" +
                $scope.moduleEnvironment.moduleId +
                "&envName=" +
                $scope.configEnvironment.name, {});
        responsePromise.then(function (response) {
            $scope.versions = response.data;
            if ($scope.formSaveHost.version === "") {
                $scope.formSaveHost.version = response.data.artifactVersions[0];
            }
            if ($scope.formSaveHost.configVersion === "") {
                $scope.formSaveHost.configVersion = response.data.configVersions[0];
            }
        });
        
        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.moduleEnvironment.moduleId,
                environment: $scope.moduleEnvironment.environment,
                version: $scope.formSaveHost.version,
                configVersion: $scope.formSaveHost.configVersion,
                counts: $scope.formSaveHost.counts,
                specialInstructions: $scope.formSaveHost.specialInstructions,
                reason: $scope.formSaveHost.reason
            };

            var responsePromise = $http.post("/v1/host/create", dataObject, {});
            responsePromise.then(function (response) {
                $uibModalInstance.close();
                $route.reload();
            });
            responsePromise.catch(function (response) {
                $scope.errorMsg = response.data;
            });
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    }]);

hadrianControllers.controller('ModalDeploySoftwareCtrl', ['$scope', '$http', '$uibModalInstance', '$route', 'config', 'service', 'hostNames', 'moduleEnvironment',
    function ($scope, $http, $uibModalInstance, $route, config, service, hostNames, moduleEnvironment) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.hostNames = hostNames;
        $scope.moduleEnvironment = moduleEnvironment;
        $scope.config = config;

        $scope.configEnvironment = null;
        for (var i = 0; i < config.environments.length; i++) {
            if (config.environments[i].name === moduleEnvironment.environment) {
                $scope.configEnvironment = config.environments[i];
            }
        }

        for (var i = 0; i < service.modules.length; i++) {
            if (service.modules[i].moduleId === moduleEnvironment.moduleId) {
                $scope.module = service.modules[i];
            }
        }

        $scope.modelOptions = {
            debounce: {
                default: 500,
                blur: 250
            },
            getterSetter: true
        };

        $scope.formUpdateHost = {};
        $scope.formUpdateHost.version = "";
        $scope.formUpdateHost.configVersion = "";
        $scope.formUpdateHost.reason = "";

        var responsePromise = $http.get(
                "/v1/service/version?serviceId=" +
                $scope.service.serviceId +
                "&moduleId=" +
                $scope.moduleEnvironment.moduleId +
                "&envName=" +
                $scope.configEnvironment.name, {});
        responsePromise.then(function (response) {
            $scope.versions = data;
            if ($scope.formUpdateHost.version === "") {
                $scope.formUpdateHost.version = response.data.artifactVersions[0];
            }
            if ($scope.formUpdateHost.configVersion === "") {
                $scope.formUpdateHost.configVersion = response.data.configVersions[0];
            }
        });

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.moduleEnvironment.moduleId,
                environment: $scope.moduleEnvironment.environment,
                all: false,
                hostNames: $scope.hostNames,
                version: $scope.formUpdateHost.version,
                configVersion: $scope.formUpdateHost.configVersion,
                reason: $scope.formUpdateHost.reason,
                wait: false
            };

            var responsePromise = $http.put("/v1/host/deploy", dataObject, {});
            responsePromise.then(function (response) {
                $uibModalInstance.close();
                $route.reload();
            });
            responsePromise.catch(function (response) {
                $scope.errorMsg = response.data;
            });
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    }]);

hadrianControllers.controller('ModalRestartHostCtrl', ['$scope', '$http', '$uibModalInstance', '$route', 'config', 'service', 'hostNames', 'moduleEnvironment',
    function ($scope, $http, $uibModalInstance, $route, config, service, hostNames, moduleEnvironment) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.hostNames = hostNames;
        $scope.moduleEnvironment = moduleEnvironment;
        $scope.config = config;

        $scope.configEnvironment = null;
        for (var i = 0; i < config.environments.length; i++) {
            if (config.environments[i].name === moduleEnvironment.environment) {
                $scope.configEnvironment = config.environments[i];
            }
        }

        $scope.formUpdateHost = {};
        $scope.formUpdateHost.doOsUpgrade = false;
        $scope.formUpdateHost.reason = "";

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.moduleEnvironment.moduleId,
                environment: $scope.moduleEnvironment.environment,
                all: false,
                hostNames: $scope.hostNames,
                doOsUpgrade: $scope.formUpdateHost.doOsUpgrade,
                reason: $scope.formUpdateHost.reason,
                wait: false
            };

            var responsePromise = $http.put("/v1/host/restart", dataObject, {});
            responsePromise.then(function (response) {
                $uibModalInstance.close();
                $route.reload();
            });
            responsePromise.catch(function (response) {
                $scope.errorMsg = response.data;
            });
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    }]);

hadrianControllers.controller('ModalRebootHostCtrl', ['$scope', '$http', '$uibModalInstance', '$route', 'config', 'service', 'hostNames', 'moduleEnvironment',
    function ($scope, $http, $uibModalInstance, $route, config, service, hostNames, moduleEnvironment) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.hostNames = hostNames;
        $scope.moduleEnvironment = moduleEnvironment;
        $scope.config = config;

        $scope.configEnvironment = null;
        for (var i = 0; i < config.environments.length; i++) {
            if (config.environments[i].name === moduleEnvironment.environment) {
                $scope.configEnvironment = config.environments[i];
            }
        }

        $scope.formRebootHost = {};
        $scope.formRebootHost.reason = "";

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.moduleEnvironment.moduleId,
                environment: $scope.moduleEnvironment.environment,
                hostNames: $scope.hostNames,
                reason: $scope.formRebootHost.reason
            };

            var responsePromise = $http.post("/v1/host/reboot", dataObject, {});
            responsePromise.then(function (response) {
                $uibModalInstance.close();
                $route.reload();
            });
            responsePromise.catch(function (response) {
                $scope.errorMsg = response.data;
            });
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    }]);

hadrianControllers.controller('ModalDeleteHostCtrl', ['$scope', '$http', '$uibModalInstance', '$route', 'config', 'service', 'hostNames', 'moduleEnvironment',
    function ($scope, $http, $uibModalInstance, $route, config, service, hostNames, moduleEnvironment) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.hostNames = hostNames;
        $scope.moduleEnvironment = moduleEnvironment;
        $scope.config = config;

        $scope.configEnvironment = null;
        for (var i = 0; i < config.environments.length; i++) {
            if (config.environments[i].name === moduleEnvironment.environment) {
                $scope.configEnvironment = config.environments[i];
            }
        }

        $scope.formDeleteHost = {};
        $scope.formDeleteHost.inventoryOnly = false;
        $scope.formDeleteHost.reason = "";

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.moduleEnvironment.moduleId,
                environment: $scope.moduleEnvironment.environment,
                hostNames: $scope.hostNames,
                inventoryOnly: $scope.formDeleteHost.inventoryOnly,
                reason: $scope.formDeleteHost.reason
            };

            var responsePromise = $http.post("/v1/host/delete", dataObject, {});
            responsePromise.then(function (response) {
                $uibModalInstance.close();
                $route.reload();
            });
            responsePromise.catch(function (response) {
                $scope.errorMsg = response.data;
            });
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    }]);

hadrianControllers.controller('ModalSmokeTestCtrl', ['$scope', '$http', '$uibModalInstance', 'config', 'service', 'moduleId', 'endPoint',
    function ($scope, $http, $uibModalInstance, config, service, moduleId, endPoint) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.endPoint = endPoint;
        $scope.config = config;

        $scope.status = "Loading";
        $scope.output = "";
        var responsePromise = $http.get("/v1/st/exec?serviceId=" + service.serviceId + "&moduleId=" + moduleId + "&endPoint=" + endPoint, {});
        responsePromise.then(function (response) {
            $scope.status = response.data.result;
            $scope.output = response.data.output;
        });
        responsePromise.catch(function (response) {
            $scope.status = "Error";
        });

        $scope.cancel = function () {
            $uibModalInstance.close();
        };
    }]);



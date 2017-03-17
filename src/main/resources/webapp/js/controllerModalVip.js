'use strict';

/* Controllers */

hadrianControllers.controller('ModalAddVipCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'service', 'environmentModule',
    function ($scope, $http, $modalInstance, $route, config, service, environmentModule) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.environmentModule = environmentModule;
        $scope.config = config;

        $scope.configEnvironment = null;
        for (var i = 0; i < config.environments.length; i++) {
            if (config.environments[i].name === environmentModule.environment) {
                $scope.configEnvironment = config.environments[i];
            }
        }

        $scope.formSaveVip = {};
        $scope.formSaveVip.dns = "";
        $scope.formSaveVip.domain = $scope.config.domains[0];
        $scope.formSaveVip.inboundProtocol = $scope.config.inboundProtocols[0];
        $scope.formSaveVip.outboundProtocol = $scope.formSaveVip.inboundProtocol.outbound[0];
        $scope.formSaveVip.priorityMode = $scope.config.priorityModes[0];
        $scope.formSaveVip.vipPort = 0;
        $scope.formSaveVip.servicePort = 8080;
        $scope.formSaveVip.external = false;

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.environmentModule.moduleId,
                dns: $scope.formSaveVip.dns,
                domain: $scope.formSaveVip.domain,
                environment: $scope.environmentModule.environment,
                inboundProtocol: $scope.formSaveVip.inboundProtocol.code,
                outboundProtocol: $scope.formSaveVip.outboundProtocol.code,
                priorityMode: $scope.formSaveVip.priorityMode,
                vipPort: $scope.formSaveVip.vipPort,
                servicePort: $scope.formSaveVip.servicePort,
                external: $scope.formSaveVip.external
            };

            var responsePromise = $http.post("/v1/vip/create", dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $modalInstance.close();
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                $scope.errorMsg = data;
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

hadrianControllers.controller('ModalBackfillVipCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'service', 'environmentModule',
    function ($scope, $http, $modalInstance, $route, config, service, environmentModule) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.environmentModule = environmentModule;
        $scope.config = config;

        $scope.configEnvironment = null;
        for (var i = 0; i < config.environments.length; i++) {
            if (config.environments[i].name === environmentModule.environment) {
                $scope.configEnvironment = config.environments[i];
            }
        }

        $scope.formSaveVip = {};
        $scope.formSaveVip.dns = "";
        $scope.formSaveVip.domain = $scope.config.domains[0];
        $scope.formSaveVip.inboundProtocol = $scope.config.inboundProtocols[0];
        $scope.formSaveVip.outboundProtocol = $scope.formSaveVip.inboundProtocol.outbound[0];
        $scope.formSaveVip.priorityMode = $scope.config.priorityModes[0];
        $scope.formSaveVip.vipPort = 0;
        $scope.formSaveVip.servicePort = 8080;
        $scope.formSaveVip.external = false;

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.environmentModule.moduleId,
                dns: $scope.formSaveVip.dns,
                domain: $scope.formSaveVip.domain,
                environment: $scope.environmentModule.environment,
                inboundProtocol: $scope.formSaveVip.inboundProtocol.code,
                outboundProtocol: $scope.formSaveVip.outboundProtocol.code,
                priorityMode: $scope.formSaveVip.priorityMode,
                vipPort: $scope.formSaveVip.vipPort,
                servicePort: $scope.formSaveVip.servicePort,
                external: $scope.formSaveVip.external
            };

            var responsePromise = $http.post("/v1/vip/backfill", dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $modalInstance.close();
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                $scope.errorMsg = data;
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

hadrianControllers.controller('ModalUpdateVipCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'service', 'vip',
    function ($scope, $http, $modalInstance, $route, config, service, vip) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.vip = vip;
        $scope.config = config;

        $scope.configEnvironment = null;
        for (var i = 0; i < config.environments.length; i++) {
            if (config.environments[i].name === vip.environment) {
                $scope.configEnvironment = config.environments[i];
            }
        }

        $scope.formUpdateVip = {};
        $scope.formUpdateVip.priorityMode = vip.priorityMode;
        $scope.formUpdateVip.servicePort = vip.servicePort;

        $scope.save = function () {
            var dataObject = {
                vipId: $scope.vip.vipId,
                serviceId: $scope.service.serviceId,
                priorityMode: $scope.formUpdateVip.priorityMode,
                servicePort: $scope.formUpdateVip.servicePort
            };

            var responsePromise = $http.put("/v1/vip/modify", dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $modalInstance.close();
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                $scope.errorMsg = data;
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

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
        $scope.formSaveVip.external = false;
        $scope.formSaveVip.protocol = $scope.config.protocols[0];
        $scope.formSaveVip.vipPort = 80;
        $scope.formSaveVip.servicePort = 8080;
        $scope.formSaveVip.lbConfig = $scope.config.lbConfigs[0];

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.environmentModule.moduleId,
                dns: $scope.formSaveVip.dns,
                domain: $scope.formSaveVip.domain,
                external: $scope.formSaveVip.external,
                environment: $scope.environmentModule.environment,
                protocol: $scope.formSaveVip.protocol,
                vipPort: $scope.formSaveVip.vipPort,
                servicePort: $scope.formSaveVip.servicePort,
                lbConfig: $scope.formSaveVip.lbConfig
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
        $scope.formSaveVip.external = false;
        $scope.formSaveVip.protocol = $scope.config.protocols[0];
        $scope.formSaveVip.vipPort = 80;
        $scope.formSaveVip.servicePort = 8080;
        $scope.formSaveVip.lbConfig = $scope.config.lbConfigs[0];

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.environmentModule.moduleId,
                dns: $scope.formSaveVip.dns,
                domain: $scope.formSaveVip.domain,
                external: $scope.formSaveVip.external,
                environment: $scope.environmentModule.environment,
                protocol: $scope.formSaveVip.protocol,
                vipPort: $scope.formSaveVip.vipPort,
                servicePort: $scope.formSaveVip.servicePort,
                lbConfig: $scope.formSaveVip.lbConfig
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
        $scope.formUpdateVip.external = vip.external;
        $scope.formUpdateVip.servicePort = vip.servicePort;
        $scope.formUpdateVip.lbConfig = vip.lbConfig;

        $scope.save = function () {
            var dataObject = {
                vipId: $scope.vip.vipId,
                serviceId: $scope.service.serviceId,
                external: $scope.formUpdateVip.external,
                servicePort: $scope.formUpdateVip.servicePort,
                lbConfig: $scope.formUpdateVip.lbConfig
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

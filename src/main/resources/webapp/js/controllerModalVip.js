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
        $scope.formSaveVip.inboundModifiers = null;
        $scope.formSaveVip.outboundProtocol = $scope.formSaveVip.inboundProtocol.outbound[0];
        $scope.formSaveVip.outboundModifiers = null;
        $scope.formSaveVip.priorityMode = $scope.config.priorityModes[0];
        $scope.formSaveVip.vipPort = 0;
        $scope.formSaveVip.servicePort = 8080;
        $scope.formSaveVip.httpCheckPort = 0;
        $scope.formSaveVip.external = false;

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.environmentModule.moduleId,
                dns: $scope.formSaveVip.dns,
                domain: $scope.formSaveVip.domain,
                environment: $scope.environmentModule.environment,
                inboundProtocol: $scope.formSaveVip.inboundProtocol.code,
                inboundModifiers: [],
                outboundProtocol: $scope.formSaveVip.outboundProtocol.code,
                outboundModifiers: [],
                priorityMode: $scope.formSaveVip.priorityMode,
                vipPort: $scope.formSaveVip.vipPort,
                servicePort: $scope.formSaveVip.servicePort,
                httpCheckPort: $scope.formSaveVip.httpCheckPort,
                external: $scope.formSaveVip.external
            };

            if ($scope.formSaveVip.inboundModifiers != null && $scope.formSaveVip.inboundModifiers.length > 0) {
                for (var i = 0; i < $scope.formSaveVip.inboundModifiers.length; i++) {
                    dataObject.inboundModifiers.push($scope.formSaveVip.inboundModifiers[i].code);
                }
            }

            if ($scope.formSaveVip.outboundModifiers != null && $scope.formSaveVip.outboundModifiers.length > 0) {
                for (var i = 0; i < $scope.formSaveVip.outboundModifiers.length; i++) {
                    dataObject.outboundModifiers.push($scope.formSaveVip.outboundModifiers[i].code);
                }
            }

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
        $scope.formSaveVip.inboundModifiers = null;
        $scope.formSaveVip.outboundProtocol = $scope.formSaveVip.inboundProtocol.outbound[0];
        $scope.formSaveVip.outboundModifiers = null;
        $scope.formSaveVip.priorityMode = $scope.config.priorityModes[0];
        $scope.formSaveVip.vipPort = 0;
        $scope.formSaveVip.servicePort = 8080;
        $scope.formSaveVip.httpCheckPort = 0;
        $scope.formSaveVip.external = false;

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.environmentModule.moduleId,
                dns: $scope.formSaveVip.dns,
                domain: $scope.formSaveVip.domain,
                environment: $scope.environmentModule.environment,
                inboundProtocol: $scope.formSaveVip.inboundProtocol.code,
                inboundModifiers: [],
                outboundProtocol: $scope.formSaveVip.outboundProtocol.code,
                outboundModifiers: [],
                priorityMode: $scope.formSaveVip.priorityMode,
                vipPort: $scope.formSaveVip.vipPort,
                servicePort: $scope.formSaveVip.servicePort,
                httpCheckPort: $scope.formSaveVip.httpCheckPort,
                external: $scope.formSaveVip.external
            };

            if ($scope.formSaveVip.inboundModifiers != null && $scope.formSaveVip.inboundModifiers.length > 0) {
                for (var i = 0; i < $scope.formSaveVip.inboundModifiers.length; i++) {
                    dataObject.inboundModifiers.push($scope.formSaveVip.inboundModifiers[i].code);
                }
            }

            if ($scope.formSaveVip.outboundModifiers != null && $scope.formSaveVip.outboundModifiers.length > 0) {
                for (var i = 0; i < $scope.formSaveVip.outboundModifiers.length; i++) {
                    dataObject.outboundModifiers.push($scope.formSaveVip.outboundModifiers[i].code);
                }
            }

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

hadrianControllers.controller('ModalMigrateVipCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'serviceId', 'vip', 'newState',
    function ($scope, $http, $modalInstance, $route, config, serviceId, vip, newState) {
        $scope.errorMsg = null;
        $scope.serviceId = serviceId;
        $scope.vip = vip;
        $scope.newState = newState;
        $scope.formMigrateVip = {};
        $scope.formMigrateVip.specialInstructions = null;
        $scope.config = config;
        
        if (vip.migration === 1 && newState === 2) { 
            $scope.modalTitle = "Migrate VIP Step 1";
            $scope.buttonTitle = "Migrate VIP";
            $scope.showSpecialInstructions = false;
        }
        if (vip.migration === 2 && newState === 3) { 
            $scope.modalTitle = "Migrate VIP Step 2";
            $scope.buttonTitle = "Migrate VIP";
            $scope.showSpecialInstructions = true;
        }
        if (vip.migration === 3 && newState === 2) { 
            $scope.modalTitle = "Rollback VIP Migration";
            $scope.buttonTitle = "Rollback";
            $scope.showSpecialInstructions = true;
        }
        if (vip.migration === 3 && newState === 4) { 
            $scope.modalTitle = "Migrate VIP Step 3";
            $scope.buttonTitle = "Migrate VIP";
            $scope.showSpecialInstructions = false;
        }

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.serviceId,
                vipId: $scope.vip.vipId,
                newState: $scope.newState,
                specialInstructions: $scope.formMigrateVip.specialInstructions
            };

            var responsePromise = $http.post("/v1/vip/migrate", dataObject, {});
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

'use strict';

/* Controllers */

hadrianControllers.controller('ModalAddVipCtrl', ['$scope', '$http', '$uibModalInstance', '$route', 'config', 'service', 'environmentModule',
    function ($scope, $http, $uibModalInstance, $route, config, service, environmentModule) {
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
        $scope.formSaveVip.external = "false";

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

hadrianControllers.controller('ModalBackfillVipCtrl', ['$scope', '$http', '$uibModalInstance', '$route', 'config', 'service', 'environmentModule',
    function ($scope, $http, $uibModalInstance, $route, config, service, environmentModule) {
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
        $scope.formSaveVip.external = "false";

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

hadrianControllers.controller('ModalUpdateVipCtrl', ['$scope', '$http', '$uibModalInstance', '$route', 'config', 'service', 'vip',
    function ($scope, $http, $uibModalInstance, $route, config, service, vip) {
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

hadrianControllers.controller('ModalMigrateVipCtrl', ['$scope', '$http', '$uibModalInstance', '$route', 'config', 'serviceId', 'vip', 'newState',
    function ($scope, $http, $uibModalInstance, $route, config, serviceId, vip, newState) {
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
            $scope.helpText1 = "Once step 1 is complete the F5s will be setup, but requests will still be processed by the A10s.";
            $scope.helpText2 = "Use " + vip.dns + "-f5." + vip.domain + " to test.";
            $scope.showSpecialInstructions = false;
        }
        if (vip.migration === 2 && newState === 3) { 
            $scope.modalTitle = "Migrate VIP Step 2";
            $scope.buttonTitle = "Migrate VIP";
            $scope.helpText1 = "Examples: External IP address needs to be preserved. Contact team to coodinate the execution of step 2. etc.";
            $scope.helpText2 = "Step 2 involves manual tasks. Once step 2 is complete requests will be processed by the F5s.";
            $scope.showSpecialInstructions = true;
        }
        if (vip.migration === 3 && newState === 2) { 
            $scope.modalTitle = "Rollback VIP Migration";
            $scope.buttonTitle = "Rollback";
            $scope.helpText1 = "Rolling back step 2 involves manual tasks.";
            $scope.helpText2 = "Please also escalate to Ops.";
            $scope.showSpecialInstructions = true;
        }
        if (vip.migration === 3 && newState === 4) { 
            $scope.modalTitle = "Migrate VIP Step 3";
            $scope.buttonTitle = "Migrate VIP";
            $scope.helpText1 = "Completely step 3 indicates that the migration to F5 was completed successfully.";
            $scope.helpText2 = " ";
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

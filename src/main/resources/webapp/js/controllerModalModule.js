'use strict';

/* Controllers */

hadrianControllers.controller('ModalAddModuleCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'moduleType', 'team', 'service', 'initialMsg',
    function ($scope, $http, $modalInstance, $route, config, moduleType, team, service, initialMsg) {
        $scope.errorMsg = null;
        $scope.team = team;
        $scope.service = service;
        $scope.initialMsg = initialMsg;
        $scope.config = config;

        $scope.formSaveModule = {};
        $scope.formSaveModule.moduleName = "";
        $scope.formSaveModule.moduleType = moduleType;
        $scope.formSaveModule.gitFolder = "";
        $scope.formSaveModule.mavenArtifactId = "";
        $scope.formSaveModule.artifactType = $scope.config.artifactTypes[0];
        $scope.formSaveModule.artifactSuffix = "";
        $scope.formSaveModule.outbound = "No";
        $scope.formSaveModule.hostAbbr = "";
        $scope.formSaveModule.versionUrl = $scope.config.versionUrl;
        $scope.formSaveModule.availabilityUrl = $scope.config.availabilityUrl;
        $scope.formSaveModule.smokeTestUrl = "";
        $scope.formSaveModule.smokeTestCron = "";
        $scope.formSaveModule.runAs = "";
        $scope.formSaveModule.deploymentFolder = $scope.config.deploymentFolder;
        $scope.formSaveModule.dataFolder = $scope.config.dataFolder;
        $scope.formSaveModule.logsFolder = $scope.config.logsFolder;
        $scope.formSaveModule.logsRetention = 6;
        $scope.formSaveModule.logCollection = "Daily";
        $scope.formSaveModule.startCmdLine = "";
        $scope.formSaveModule.startTimeOut = 60;
        $scope.formSaveModule.stopCmdLine = "";
        $scope.formSaveModule.stopTimeOut = 60;
        $scope.formSaveModule.configName = "";

        $scope.save = function () {
            $scope.initialMsg = null;
            var dataObject = {
                moduleName: $scope.formSaveModule.moduleName,
                serviceId: $scope.service.serviceId,
                moduleType: $scope.formSaveModule.moduleType,
                gitFolder: $scope.formSaveModule.gitFolder,
                mavenArtifactId: $scope.formSaveModule.mavenArtifactId,
                artifactType: $scope.formSaveModule.artifactType,
                artifactSuffix: $scope.formSaveModule.artifactSuffix,
                outbound: $scope.formSaveModule.outbound,
                hostAbbr: $scope.formSaveModule.hostAbbr,
                versionUrl: $scope.formSaveModule.versionUrl,
                availabilityUrl: $scope.formSaveModule.availabilityUrl,
                smokeTestUrl: $scope.formSaveModule.smokeTestUrl,
                smokeTestCron: $scope.formSaveModule.smokeTestCron,
                runAs: $scope.formSaveModule.runAs,
                deploymentFolder: $scope.formSaveModule.deploymentFolder,
                dataFolder: $scope.formSaveModule.dataFolder,
                logsFolder: $scope.formSaveModule.logsFolder,
                logsRetention: $scope.formSaveModule.logsRetention,
                logCollection: $scope.formSaveModule.logCollection,
                startCmdLine: $scope.formSaveModule.startCmdLine,
                startTimeOut: $scope.formSaveModule.startTimeOut,
                stopCmdLine: $scope.formSaveModule.stopCmdLine,
                stopTimeOut: $scope.formSaveModule.stopTimeOut,
                configName: $scope.formSaveModule.configName,
                environmentNames: $scope.formSaveModule.environmentNames
            };

            var responsePromise = $http.post("/v1/module/create", dataObject, {});
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

hadrianControllers.controller('ModalUpdateModuleCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'service', 'module',
    function ($scope, $http, $modalInstance, $route, config, service, module) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.module = module;
        $scope.config = config;

        $scope.formUpdateModule = {};
        $scope.formUpdateModule.moduleName = module.moduleName;
        $scope.formUpdateModule.gitFolder = module.gitFolder;
        $scope.formUpdateModule.mavenArtifactId = module.mavenArtifactId;
        $scope.formUpdateModule.artifactType = module.artifactType;
        $scope.formUpdateModule.artifactSuffix = module.artifactSuffix;
        $scope.formUpdateModule.outbound = module.outbound;
        $scope.formUpdateModule.hostAbbr = module.hostAbbr;
        $scope.formUpdateModule.versionUrl = module.versionUrl;
        $scope.formUpdateModule.availabilityUrl = module.availabilityUrl;
        $scope.formUpdateModule.smokeTestUrl = module.smokeTestUrl;
        $scope.formUpdateModule.smokeTestCron = module.smokeTestCron;
        $scope.formUpdateModule.runAs = module.runAs;
        $scope.formUpdateModule.deploymentFolder = module.deploymentFolder;
        $scope.formUpdateModule.dataFolder = module.dataFolder;
        $scope.formUpdateModule.logsFolder = module.logsFolder;
        $scope.formUpdateModule.logsRetention = module.logsRetention;
        $scope.formUpdateModule.logCollection = module.logCollection;
        $scope.formUpdateModule.startCmdLine = module.startCmdLine;
        $scope.formUpdateModule.startTimeOut = module.startTimeOut;
        $scope.formUpdateModule.stopCmdLine = module.stopCmdLine;
        $scope.formUpdateModule.stopTimeOut = module.stopTimeOut;
        $scope.formUpdateModule.configName = module.configName;
        $scope.formUpdateModule.environmentNames = JSON.parse(JSON.stringify(module.environmentNames))

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.module.moduleId,
                moduleName: $scope.formUpdateModule.moduleName,
                gitFolder: $scope.formUpdateModule.gitFolder,
                mavenArtifactId: $scope.formUpdateModule.mavenArtifactId,
                artifactType: $scope.formUpdateModule.artifactType,
                artifactSuffix: $scope.formUpdateModule.artifactSuffix,
                outbound: $scope.formUpdateModule.outbound,
                hostAbbr: $scope.formUpdateModule.hostAbbr,
                versionUrl: $scope.formUpdateModule.versionUrl,
                availabilityUrl: $scope.formUpdateModule.availabilityUrl,
                smokeTestUrl: $scope.formUpdateModule.smokeTestUrl,
                smokeTestCron: $scope.formUpdateModule.smokeTestCron,
                runAs: $scope.formUpdateModule.runAs,
                deploymentFolder: $scope.formUpdateModule.deploymentFolder,
                dataFolder: $scope.formUpdateModule.dataFolder,
                logsFolder: $scope.formUpdateModule.logsFolder,
                logsRetention: $scope.formUpdateModule.logsRetention,
                logCollection: $scope.formUpdateModule.logCollection,
                startCmdLine: $scope.formUpdateModule.startCmdLine,
                startTimeOut: $scope.formUpdateModule.startTimeOut,
                stopCmdLine: $scope.formUpdateModule.stopCmdLine,
                stopTimeOut: $scope.formUpdateModule.stopTimeOut,
                configName: $scope.formUpdateModule.configName,
                environmentNames: $scope.formUpdateModule.environmentNames
            };

            var responsePromise = $http.put("/v1/module/modify", dataObject, {});
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

hadrianControllers.controller('ModalDeleteModuleCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'service', 'module',
    function ($scope, $http, $modalInstance, $route, config, service, module) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.module = module;
        $scope.config = config;

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.module.moduleId
            };

            var responsePromise = $http.post("/v1/module/delete", dataObject, {});
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

hadrianControllers.controller('ModalModuleFileDeleteCtrl', ['$modalInstance', '$scope', '$http', 'items',
    function ($modalInstance, $scope, $http, items) {
        $scope.fileName = items.fileName;
        $scope.fileNumber = items.fileNumber;
        $scope.service = items.service;
        $scope.moduleId = items.moduleId;
        $scope.environment = items.environment;
        $scope.dataFromServer = items.dataFromServer;

        $scope.delete = function () {
            var responsePromise = $http.delete("/v1/module/file?serviceId=" + $scope.service.serviceId + "&moduleId=" +
                    $scope.moduleId + "&environment=" + $scope.environment + "&fileName=" + $scope.fileName);

            responsePromise.success(function (status) {
                $scope.dataFromServer.splice($scope.fileNumber, 1);
                $modalInstance.dismiss('cancel');
            });
        };

        $scope.close = function () {
            $modalInstance.dismiss('cancel');
        };

    }]);

hadrianControllers.controller('ModalModuleFileCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'service', 'moduleEnvironment', '$uibModal',
    function ($scope, $http, $modalInstance, $route, config, service, moduleEnvironment, $uibModal) {
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

        var responsePromise = $http.get("/v1/module/file?serviceId=" + $scope.service.serviceId + "&moduleId=" + $scope.moduleEnvironment.moduleId + "&environment=" + $scope.moduleEnvironment.environment, {});
        responsePromise.success(function (dataFromServer, status, headers, config) {
            $scope.dataFromServer = dataFromServer;

            angular.forEach($scope.dataFromServer, function (value, index) {
                value.originalName = value.name;
            });

            $scope.openDeleteModal = function (fileName, fileNumber) {
                var modalInstance = $uibModal.open({
                    animation: true,
                    controller: 'ModalModuleFileDeleteCtrl',
                    templateUrl: 'partials/deleteModuleFile.html',
                    size: 'md',
                    resolve: {
                        items: function () {
                            return {
                                fileName: fileName,
                                fileNumber: fileNumber,
                                service: $scope.service,
                                moduleId: $scope.moduleEnvironment.moduleId,
                                environment: $scope.moduleEnvironment.environment,
                                dataFromServer: $scope.dataFromServer
                            };
                        }
                    }
                });
            };
        });

        $scope.save = function (fileNumber) {
            var dataObject = {
                fileNumber: fileNumber,
                serviceId: $scope.service.serviceId,
                moduleId: $scope.moduleEnvironment.moduleId,
                environment: $scope.moduleEnvironment.environment,
                originalName: $scope.dataFromServer[fileNumber].originalName,
                name: $scope.dataFromServer[fileNumber].name,
                contents: $scope.dataFromServer[fileNumber].contents
            };

            var responsePromise = $http.put("/v1/module/file", dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $scope.dataFromServer[fileNumber].originalName = $scope.dataFromServer[fileNumber].name
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                $scope.errorMsg = data;
            });
        };

        $scope.addNewFile = function () {
            $scope.dataFromServer.push({name: "New File", contents: ""});
        };

        $scope.close = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

hadrianControllers.controller('ModalAddUsesCtrl', ['$scope', '$http', '$modalInstance', '$route', 'ServiceNotUses', 'service', 'module',
    function ($scope, $http, $modalInstance, $route, ServiceNotUses, service, module) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.module = module;
        $scope.formSelectUses = {};

        $scope.modelOptions = {
            debounce: {
                default: 500,
                blur: 250
            },
            getterSetter: true
        };

        ServiceNotUses.get({serviceId: service.serviceId, moduleId: module.moduleId}, function (notUses) {
            $scope.notUses = notUses;
        });

        $scope.save = function () {
            var dataObject = {
                clientServiceId: $scope.service.serviceId,
                clientModuleId: $scope.module.moduleId,
                serverServiceId: $scope.formSelectUses.ref.serverServiceId,
                serverModuleId: $scope.formSelectUses.ref.serverModuleId
            };

            var responsePromise = $http.post("/v1/service/createRef", dataObject, {});
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

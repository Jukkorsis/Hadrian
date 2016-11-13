'use strict';

/* Controllers */

hadrianControllers.controller('ServiceCtrl', ['$scope', '$route', '$interval', '$http', '$routeParams', '$sce', '$uibModal', 'filterFilter', 'Config', 'Team', 'Service', 'ServiceRefresh', 'HostDetails', 'VipDetails',
    function ($scope, $route, $interval, $http, $routeParams, $sce, $uibModal, filterFilter, Config, Team, Service, ServiceRefresh, HostDetails, VipDetails) {
        $scope.loading = true;
        $scope.hostSortType = 'hostName';
        $scope.hostSortReverse = false;
        $scope.hostFilter = '';

        $scope.auditFilter = '';

        $scope.formSelectHost = {};

        Service.get({serviceId: $routeParams.serviceId}, function (service) {
            $scope.service = service;
            $scope.loading = false;
            Team.get({teamId: service.teamId}, function (team) {
                $scope.team = team;
            });
        });
        Config.get({}, function (config) {
            $scope.config = config;
        });

        $scope.openUpdateServiceModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/updateService.html',
                controller: 'ModalUpdateServiceCtrl',
                size: 'lg',
                resolve: {
                    service: function () {
                        return $scope.service;
                    },
                    team: function () {
                        return $scope.team;
                    },
                    config: function () {
                        return $scope.config;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.openBuildServiceModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/buildService.html',
                controller: 'ModalBuildServiceCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.openDeleteServiceModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/deleteService.html',
                controller: 'ModalDeleteServiceCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.openAddUsesModal = function (module) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addUses.html',
                controller: 'ModalAddUsesCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    },
                    module: function () {
                        return module;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.deleteServiceRef = function (clientServiceId, clientModuleId, serverServiceId, serverModuleId) {
            var dataObject = {
                clientServiceId: clientServiceId,
                clientModuleId: clientModuleId,
                serverServiceId: serverServiceId,
                serverModuleId: serverModuleId
            };

            var responsePromise = $http.post("/v1/service/deleteRef", dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to remove dependence has failed!");
                $route.reload();
            });
        };

        $scope.openAddDeployableModuleModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addDeployableModule.html',
                controller: 'ModalAddModuleCtrl',
                size: 'lg',
                resolve: {
                    team: function () {
                        return $scope.team;
                    },
                    service: function () {
                        return $scope.service;
                    },
                    config: function () {
                        return $scope.config;
                    },
                    moduleType: function () {
                        return 'Deployable';
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.openAddLibraryModuleModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addLibraryModule.html',
                controller: 'ModalAddModuleCtrl',
                resolve: {
                    team: function () {
                        return $scope.team;
                    },
                    service: function () {
                        return $scope.service;
                    },
                    config: function () {
                        return $scope.config;
                    },
                    moduleType: function () {
                        return 'Library';
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.openAddSimulatorModuleModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addSimulatorModule.html',
                controller: 'ModalAddModuleCtrl',
                size: 'lg',
                resolve: {
                    team: function () {
                        return $scope.team;
                    },
                    service: function () {
                        return $scope.service;
                    },
                    config: function () {
                        return $scope.config;
                    },
                    moduleType: function () {
                        return 'Simulator';
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.openUpdateModuleModal = function (module) {
            if (module.moduleType === 'Deployable') {
                var modalInstance = $uibModal.open({
                    animation: true,
                    templateUrl: 'partials/updateDeployableModule.html',
                    controller: 'ModalUpdateModuleCtrl',
                    size: 'lg',
                    resolve: {
                        service: function () {
                            return $scope.service;
                        },
                        module: function () {
                            return module;
                        },
                        config: function () {
                            return $scope.config;
                        }
                    }
                });
                modalInstance.result.then(function () {
                    $route.reload();
                }, function () {
                });
            }
            if (module.moduleType === 'Library') {
                var modalInstance = $uibModal.open({
                    animation: true,
                    templateUrl: 'partials/updateLibraryModule.html',
                    controller: 'ModalUpdateModuleCtrl',
                    resolve: {
                        service: function () {
                            return $scope.service;
                        },
                        module: function () {
                            return module;
                        },
                        config: function () {
                            return $scope.config;
                        }
                    }
                });
                modalInstance.result.then(function () {
                    $route.reload();
                }, function () {
                });
            }
            if (module.moduleType === 'Simulator') {
                var modalInstance = $uibModal.open({
                    animation: true,
                    templateUrl: 'partials/updateSimulatorModule.html',
                    controller: 'ModalUpdateModuleCtrl',
                    size: 'lg',
                    resolve: {
                        service: function () {
                            return $scope.service;
                        },
                        module: function () {
                            return module;
                        },
                        config: function () {
                            return $scope.config;
                        }
                    }
                });
                modalInstance.result.then(function () {
                    $route.reload();
                }, function () {
                });
            }
        };

        $scope.openModuleFileModal = function (moduleEnvironment) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/editModuleFile.html',
                controller: 'ModalModuleFileCtrl',
                size: 'lg',
                resolve: {
                    service: function () {
                        return $scope.service;
                    },
                    moduleEnvironment: function () {
                        return moduleEnvironment;
                    },
                    config: function () {
                        return $scope.config;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.openBackfillModal = function (moduleEnvironment) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/backfillHost.html',
                controller: 'ModalBackfillHostCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    },
                    moduleEnvironment: function () {
                        return moduleEnvironment;
                    },
                    config: function () {
                        return $scope.config;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.openDeleteModuleModal = function (module) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/deleteModule.html',
                controller: 'ModalDeleteModuleCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    },
                    module: function () {
                        return module;
                    },
                    config: function () {
                        return $scope.config;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.openAddVipModal = function (environmentModule) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addVip.html',
                controller: 'ModalAddVipCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    },
                    environmentModule: function () {
                        return environmentModule;
                    },
                    config: function () {
                        return $scope.config;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.openUpdateVipModal = function (vip) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/updateVip.html',
                controller: 'ModalUpdateVipCtrl',
                resolve: {
                    config: function () {
                        return $scope.config;
                    },
                    service: function () {
                        return $scope.service;
                    },
                    vip: function () {
                        return vip;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.deleteVip = function (vipId) {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                vipId: vipId
            };

            var responsePromise = $http.post("/v1/vip/delete", dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to delete vip has failed!");
                $route.reload();
            });
        };

        $scope.fixVip = function (vipId) {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                vipId: vipId
            };

            var responsePromise = $http.post("/v1/vip/fix", dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to fix vip has failed!");
                $route.reload();
            });
        };

        $scope.openAddHostModal = function (moduleEnvironment) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addHost.html',
                controller: 'ModalAddHostCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    },
                    moduleEnvironment: function () {
                        return moduleEnvironment;
                    },
                    config: function () {
                        return $scope.config;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.openDeploySoftwareHostsModal = function (moduleEnvironment) {
            //TODO
            var filteredArray = filterFilter(moduleEnvironment.hosts, this.hostFilter);
            var hostNames = [];
            for (var i in filteredArray) {
                for (var ii in $scope.formSelectHost) {
                    if (filteredArray[i].hostId === ii && $scope.formSelectHost[ii]) {
                        hostNames.push(filteredArray[i].hostName);
                    }
                }
            }
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/deploySoftware.html',
                controller: 'ModalDeploySoftwareCtrl',
                size: 'lg',
                resolve: {
                    service: function () {
                        return $scope.service;
                    },
                    hostNames: function () {
                        return hostNames;
                    },
                    moduleEnvironment: function () {
                        return moduleEnvironment;
                    },
                    config: function () {
                        return $scope.config;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.openDeploySoftwareHostModal = function (host, moduleEnvironment) {
            var hostNames = [];
            hostNames.push(host.hostName);
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/deploySoftware.html',
                controller: 'ModalDeploySoftwareCtrl',
                size: 'lg',
                resolve: {
                    service: function () {
                        return $scope.service;
                    },
                    hostNames: function () {
                        return hostNames;
                    },
                    moduleEnvironment: function () {
                        return moduleEnvironment;
                    },
                    config: function () {
                        return $scope.config;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.openRestartHostsModal = function (moduleEnvironment) {
            var filteredArray = filterFilter(moduleEnvironment.hosts, this.hostFilter);
            var hostNames = [];
            for (var i in filteredArray) {
                for (var ii in $scope.formSelectHost) {
                    if (filteredArray[i].hostId === ii && $scope.formSelectHost[ii]) {
                        hostNames.push(filteredArray[i].hostName);
                    }
                }
            }
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/restartHost.html',
                controller: 'ModalRestartHostCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    },
                    hostNames: function () {
                        return hostNames;
                    },
                    moduleEnvironment: function () {
                        return moduleEnvironment;
                    },
                    config: function () {
                        return $scope.config;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.openRestartHostModal = function (host, moduleEnvironment) {
            var hostNames = [];
            hostNames.push(host.hostName);
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/restartHost.html',
                controller: 'ModalRestartHostCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    },
                    hostNames: function () {
                        return hostNames;
                    },
                    moduleEnvironment: function () {
                        return moduleEnvironment;
                    },
                    config: function () {
                        return $scope.config;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.openDeleteHostModal = function (host, moduleEnvironment) {
            var hostNames = [];
            hostNames.push(host.hostName);
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/deleteHost.html',
                controller: 'ModalDeleteHostCtrl',
                resolve: {
                    config: function () {
                        return $scope.config;
                    },
                    service: function () {
                        return $scope.service;
                    },
                    hostNames: function () {
                        return hostNames;
                    },
                    moduleEnvironment: function () {
                        return moduleEnvironment;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.getHostDetails = function (host) {
            host.expanded = true;

            HostDetails.get({serviceId: $scope.service.serviceId, hostId: host.hostId}, function (details) {
                host.left = details.left;
                host.right = details.right;
            });
        };

        $scope.openDoSmokeTestHostModal = function (host, mn) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/smokeTest.html',
                controller: 'ModalSmokeTestCtrl',
                resolve: {
                    config: function () {
                        return $scope.config;
                    },
                    service: function () {
                        return $scope.service;
                    },
                    moduleId: function () {
                        return mn.moduleId;
                    },
                    endPoint: function () {
                        return host.hostName;
                    }
                }
            });
            modalInstance.result.then(function () {
            }, function () {
            });
        };

        $scope.getVipDetails = function (vip) {
            vip.loaded = false;
            vip.expanded = true;

            VipDetails.get({serviceId: $scope.service.serviceId, vipId: vip.vipId}, function (details) {
                vip.details = details;
                vip.loaded = true;
            });
        };

        $scope.getVipDetailsName = function (details, dataCenter) {
            if (details && details.name && details.name[dataCenter]) {
                return details.name[dataCenter];
            } else {
                return " ";
            }
        };

        $scope.getVipDetailsAddress = function (details, dataCenter) {
            if (details && details.address && details.address[dataCenter]) {
                return details.address[dataCenter];
            } else {
                return " ";
            }
        };

        $scope.getVipDetailsConections = function (details, dataCenter) {
            if (details && details.connections && details.connections[dataCenter]) {
                return details.connections[dataCenter];
            } else {
                return " ";
            }
        };

        $scope.getVipDetailsPriority = function (details, dataCenter) {
            if (details && details[dataCenter]) {
                return details[dataCenter].priority;
            } else {
                return " ";
            }
        };

        $scope.getVipDetailsOff = function (details, dataCenter) {
            if (details && details[dataCenter]) {
                return details[dataCenter].status === "Off";
            } else {
                return false;
            }
        };

        $scope.getVipDetailsOn = function (details, dataCenter) {
            if (details && details[dataCenter]) {
                return details[dataCenter].status === "On";
            } else {
                return false;
            }
        };

        $scope.getVipDetailsError = function (details, dataCenter) {
            if (details && details[dataCenter]) {
                return details[dataCenter].status === "Error";
            } else {
                return false;
            }
        };

        $scope.getVipDetailsConnections = function (details, dataCenter) {
            if (details && details[dataCenter]) {
                return details[dataCenter].connections;
            } else {
                return " ";
            }
        };

        $scope.openAddCustomFunctionModal = function (module) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addCustomFunction.html',
                controller: 'ModalAddCustomFunctionCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    },
                    module: function () {
                        return module;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.openDoCustomFunctionHostsModal = function (moduleEnvironment, cf) {
            var filteredArray = filterFilter(moduleEnvironment.hosts, this.hostFilter);
            for (var key in filteredArray) {
                var h = filteredArray[key];
                for (var key2 in $scope.formSelectHost) {
                    if (h.hostId === key2 && $scope.formSelectHost[key2]) {
                        window.open("/v1/cf/exec?serviceId=" + $scope.service.serviceId + "&hostName=" + h.hostName + "&cfId=" + cf.customFunctionId, '_blank');
                    }
                }
            }
        };

        $scope.openDoCustomFunctionHostModal = function (host, cf) {
            window.open("/v1/cf/exec?serviceId=" + $scope.service.serviceId + "&hostName=" + host.hostName + "&cfId=" + cf.customFunctionId, '_blank');
        };

        $scope.openUpdateCustomFunctionModal = function (cf) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/updateCustomFunction.html',
                controller: 'ModalUpdateCustomFunctionCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    },
                    cf: function () {
                        return cf;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.deleteCustomFunction = function (cfId) {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                cfId: cfId
            };

            var responsePromise = $http.post("/v1/cf/delete", dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to delete custom function has failed!");
                $route.reload();
            });
        };

        $scope.openAddDocumentModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addDocument.html',
                controller: 'ModalAddDocumentCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.openRemoveDocument = function (doc) {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                docId: doc.docId
            };

            var responsePromise = $http.post("/v1/document/delete", dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to delete vip has failed!");
                $route.reload();
            });
        };

        $scope.openDocument = function (doc) {
            if (doc.documentType === "Link") {
                window.open(doc.link, "_blank");
            } else {
                $scope.service.docType = "Loading";
                var responsePromise = $http.get("/v1/document?serviceId=" + $scope.service.serviceId + "&docId=" + doc.docId, {});
                responsePromise.success(function (output, status, headers, config) {
                    if (doc.documentType === "Text") {
                        $scope.service.docBody = output;
                    }
                    if (doc.documentType === "Markdown") {
                        var converter = new showdown.Converter();
                        var html = converter.makeHtml(output);
                        $scope.service.docBody = $sce.trustAsHtml(html);
                    }
                    $scope.service.docType = doc.documentType;
                });
                responsePromise.error(function (data, status, headers, config) {
                    $scope.service.docBody = data;
                    $scope.service.docType = "Text";
                });
            }
        };

        var dateObj = new Date();
        $scope.search = {
            year: dateObj.getUTCFullYear(),
            month: dateObj.getUTCMonth() + 1,
            start: dateObj.getUTCDate(),
            end: dateObj.getUTCDate()
        };
        $scope.searchAudit = function () {
            var responsePromise = $http.get("/v1/service/audit?serviceId=" + $scope.service.serviceId + "&year=" + $scope.search.year + "&month=" + $scope.search.month + "&start=" + $scope.search.start + "&end=" + $scope.search.end, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $scope.audits = dataFromServer.audits;
                for (var j = 0; j < $scope.audits.length; j++) {
                    var a = $scope.audits[j];
                    if (a.notes !== null && a.notes.length > 2) {
                        var notes = JSON.parse(a.notes);
                        a.left = [];
                        a.right = [];
                        var addLeft = true;
                        for (var key in notes) {
                            if (notes.hasOwnProperty(key)) {
                                var temp = {
                                    label: key.replace("_", " "),
                                    value: notes[key]
                                };
                                if (addLeft) {
                                    a.left.push(temp);
                                    addLeft = false;
                                } else {
                                    a.right.push(temp);
                                    addLeft = true;
                                }
                            }
                        }
                    }
                }
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to search audit records has failed!");
                $route.reload();
            });
        };

        $scope.getAuditOutput = function (audit) {
            audit.expanded = true;

            if (audit.outputDownloaded != true) {
                audit.output = "Loading...";
                var responsePromise = $http.get("/v1/service/auditOutput?serviceId=" + $scope.service.serviceId + "&auditId=" + audit.auditId, {});
                responsePromise.success(function (output, status, headers, config) {
                    audit.output = output;
                    audit.outputDownloaded = true;
                });
            }
        };

        $scope.refreshCount = 0;
        var stopRefresh = $interval(function () {
            if ($scope.refreshCount > 80) {
                return;
            }
            $scope.refreshCount++;
            ServiceRefresh.get({serviceId: $routeParams.serviceId}, function (newService) {
                $scope.service.serviceName = newService.serviceName;
                $scope.service.description = newService.description;
                for (var moduleIndex = 0; moduleIndex < $scope.service.modules.length; moduleIndex++) {
                    var module = $scope.service.modules[moduleIndex];
                    for (var newModuleIndex = 0; newModuleIndex < newService.modules.length; newModuleIndex++) {
                        var newModule = newService.modules[newModuleIndex];
                        if (module.moduleId === newModule.moduleId) {
                            module.moduleName = newModule.moduleName;
                            module.mavenGroupId = newModule.mavenGroupId;
                            module.mavenArtifactId = newModule.mavenArtifactId;
                            module.hostAbbr = newModule.hostAbbr;
                            module.versionUrl = newModule.versionUrl;
                            module.availabilityUrl = newModule.availabilityUrl;
                            module.runAs = newModule.runAs;
                            module.deploymentFolder = newModule.deploymentFolder;
                            module.startCmdLine = newModule.startCmdLine;
                            module.startTimeOut = newModule.startTimeOut;
                            module.stopCmdLine = newModule.stopCmdLine;
                            module.stopTimeOut = newModule.stopTimeOut;
                        }
                    }
                }
                for (var environmentIndex = 0; environmentIndex < $scope.service.environments.length; environmentIndex++) {
                    var environment = $scope.service.environments[environmentIndex];
                    for (var newEnvironmentIndex = 0; newEnvironmentIndex < newService.environments.length; newEnvironmentIndex++) {
                        var newEnvironment = newService.environments[newEnvironmentIndex];
                        if (environment.environment === newEnvironment.environment) {
                            for (var moduleIndex = 0; moduleIndex < environment.modules.length; moduleIndex++) {
                                var module = environment.modules[moduleIndex];
                                for (var newModuleIndex = 0; newModuleIndex < newEnvironment.modules.length; newModuleIndex++) {
                                    var newModule = newEnvironment.modules[newModuleIndex];
                                    if (module.moduleId === newModule.moduleId) {
                                        for (var hostIndex = 0; hostIndex < module.hosts.length; hostIndex++) {
                                            var host = module.hosts[hostIndex];
                                            for (var newHostIndex = 0; newHostIndex < newModule.hosts.length; newHostIndex++) {
                                                var newHost = newModule.hosts[newHostIndex];
                                                if (host.hostId === newHost.hostId) {
                                                    host.busy = newHost.busy;
                                                    host.status = newHost.status;
                                                    host.version = newHost.version;
                                                    host.availability = newHost.availability;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }, 15000);

        $scope.$on('$destroy', function () {
            $interval.cancel(stopRefresh);
            $scope.refreshCount = 0;
        });
    }]);

hadrianControllers.controller('ModalUpdateServiceCtrl', ['$scope', '$route', '$http', '$modalInstance', 'service', 'team', 'config',
    function ($scope, $route, $http, $modalInstance, service, team, config) {
        $scope.team = team;
        $scope.config = config;
        $scope.errorMsg = null;
        $scope.formUpdateService = {};
        $scope.formUpdateService.serviceId = service.serviceId;
        $scope.formUpdateService.serviceName = service.serviceName;
        $scope.formUpdateService.description = service.description;
        $scope.formUpdateService.scope = service.scope;
        $scope.formUpdateService.doBuilds = service.doBuilds;
        $scope.formUpdateService.doDeploys = service.doDeploys;
        $scope.formUpdateService.doManageVip = service.doManageVip;
        $scope.formUpdateService.doCheckJar = service.doCheckJar;
        $scope.formUpdateService.doFindBugsLevel = service.doFindBugsLevel;
        $scope.formUpdateService.gitProject = service.gitProject;
        $scope.formUpdateService.mavenGroupId = service.mavenGroupId;
        $scope.formUpdateService.testStyle = service.testStyle;
        $scope.formUpdateService.testHostname = service.testHostname;
        $scope.formUpdateService.testRunAs = service.testRunAs;
        $scope.formUpdateService.testDeploymentFolder = service.testDeploymentFolder;
        $scope.formUpdateService.testCmdLine = service.testCmdLine;
        $scope.formUpdateService.testTimeOut = service.testTimeOut;
        $scope.formUpdateService.smokeTestCron = service.smokeTestCron;

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.formUpdateService.serviceId,
                serviceName: $scope.formUpdateService.serviceName,
                description: $scope.formUpdateService.description,
                scope: $scope.formUpdateService.scope,
                doBuilds: $scope.formUpdateService.doBuilds,
                doDeploys: $scope.formUpdateService.doDeploys,
                doManageVip: $scope.formUpdateService.doManageVip,
                doCheckJar: $scope.formUpdateService.doCheckJar,
                doFindBugsLevel: $scope.formUpdateService.doFindBugsLevel,
                gitProject: $scope.formUpdateService.gitProject,
                mavenGroupId: $scope.formUpdateService.mavenGroupId,
                testStyle: $scope.formUpdateService.testStyle,
                testHostname: $scope.formUpdateService.testHostname,
                testRunAs: $scope.formUpdateService.testRunAs,
                testDeploymentFolder: $scope.formUpdateService.testDeploymentFolder,
                testCmdLine: $scope.formUpdateService.testCmdLine,
                testTimeOut: $scope.formUpdateService.testTimeOut,
                smokeTestCron: $scope.formUpdateService.smokeTestCron
            };

            var responsePromise = $http.put("/v1/service/modify", dataObject, {});
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

hadrianControllers.controller('ModalBuildServiceCtrl', ['$scope', '$route', '$http', '$modalInstance', 'service',
    function ($scope, $route, $http, $modalInstance, service) {
        $scope.service = service;
        $scope.errorMsg = null;
        $scope.formBuildService = {};
        $scope.formBuildService.branch = "";

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                branch: $scope.formBuildService.branch
            };

            var responsePromise = $http.post("/v1/service/build", dataObject, {});
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

hadrianControllers.controller('ModalDeleteServiceCtrl', ['$scope', '$route', '$http', '$modalInstance', 'service',
    function ($scope, $route, $http, $modalInstance, service) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.formDeleteService = {};
        $scope.formDeleteService.serviceName = service.serviceName;
        $scope.formDeleteService.description = service.description;
        $scope.formDeleteService.reason = "";

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                reason: $scope.formDeleteService.reason
            };

            var responsePromise = $http.put("/v1/service/delete", dataObject, {});
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

hadrianControllers.controller('ModalAddModuleCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'moduleType', 'team', 'service',
    function ($scope, $http, $modalInstance, $route, config, moduleType, team, service) {
        $scope.errorMsg = null;
        $scope.team = team;
        $scope.service = service;
        $scope.config = config;

        $scope.formSaveModule = {};
        $scope.formSaveModule.moduleName = "";
        $scope.formSaveModule.moduleType = moduleType;
        $scope.formSaveModule.deployableTemplate = $scope.config.deployableTemplates[0];
        $scope.formSaveModule.libraryTemplate = $scope.config.libraryTemplates[0];
        $scope.formSaveModule.gitFolder = "";
        $scope.formSaveModule.mavenArtifactId = "";
        $scope.formSaveModule.artifactType = $scope.config.artifactTypes[0];
        $scope.formSaveModule.artifactSuffix = "";
        $scope.formSaveModule.outbound = "No";
        $scope.formSaveModule.hostAbbr = "";
        $scope.formSaveModule.versionUrl = $scope.config.versionUrl;
        $scope.formSaveModule.availabilityUrl = $scope.config.availabilityUrl;
        $scope.formSaveModule.smokeTestUrl = $scope.config.smokeTestUrl;
        $scope.formSaveModule.runAs = "";
        $scope.formSaveModule.deploymentFolder = $scope.config.deploymentFolder;
        $scope.formSaveModule.dataFolder = $scope.config.dataFolder;
        $scope.formSaveModule.logsFolder = $scope.config.logsFolder;
        $scope.formSaveModule.logsRetention = 6;
        $scope.formSaveModule.startCmdLine = "";
        $scope.formSaveModule.startTimeOut = 60;
        $scope.formSaveModule.stopCmdLine = "";
        $scope.formSaveModule.stopTimeOut = 60;
        $scope.formSaveModule.configName = "";

        $scope.save = function () {
            var dataObject = {
                moduleName: $scope.formSaveModule.moduleName,
                serviceId: $scope.service.serviceId,
                moduleType: $scope.formSaveModule.moduleType,
                deployableTemplate: $scope.formSaveModule.deployableTemplate,
                libraryTemplate: $scope.formSaveModule.libraryTemplate,
                gitFolder: $scope.formSaveModule.gitFolder,
                mavenArtifactId: $scope.formSaveModule.mavenArtifactId,
                artifactType: $scope.formSaveModule.artifactType,
                artifactSuffix: $scope.formSaveModule.artifactSuffix,
                outbound: $scope.formSaveModule.outbound,
                hostAbbr: $scope.formSaveModule.hostAbbr,
                versionUrl: $scope.formSaveModule.versionUrl,
                availabilityUrl: $scope.formSaveModule.availabilityUrl,
                smokeTestUrl: $scope.formSaveModule.smokeTestUrl,
                runAs: $scope.formSaveModule.runAs,
                deploymentFolder: $scope.formSaveModule.deploymentFolder,
                dataFolder: $scope.formSaveModule.dataFolder,
                logsFolder: $scope.formSaveModule.logsFolder,
                logsRetention: $scope.formSaveModule.logsRetention,
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
        $scope.formUpdateModule.runAs = module.runAs;
        $scope.formUpdateModule.deploymentFolder = module.deploymentFolder;
        $scope.formUpdateModule.dataFolder = module.dataFolder;
        $scope.formUpdateModule.logsFolder = module.logsFolder;
        $scope.formUpdateModule.logsRetention = module.logsRetention;
        $scope.formUpdateModule.startCmdLine = module.startCmdLine;
        $scope.formUpdateModule.startTimeOut = module.startTimeOut;
        $scope.formUpdateModule.stopCmdLine = module.stopCmdLine;
        $scope.formUpdateModule.stopTimeOut = module.stopTimeOut;
        $scope.formUpdateModule.configName = module.configName;
        $scope.formUpdateModule.environmentNames = module.environmentNames;

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
                runAs: $scope.formUpdateModule.runAs,
                deploymentFolder: $scope.formUpdateModule.deploymentFolder,
                dataFolder: $scope.formUpdateModule.dataFolder,
                logsFolder: $scope.formUpdateModule.logsFolder,
                logsRetention: $scope.formUpdateModule.logsRetention,
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

hadrianControllers.controller('ModalBackfillHostCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'service', 'moduleEnvironment',
    function ($scope, $http, $modalInstance, $route, config, service, moduleEnvironment) {
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
                servicePort: $scope.formSaveVip.servicePort
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

        $scope.save = function () {
            var dataObject = {
                vipId: $scope.vip.vipId,
                serviceId: $scope.service.serviceId,
                external: $scope.formUpdateVip.external,
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

hadrianControllers.controller('ModalAddHostCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'service', 'moduleEnvironment',
    function ($scope, $http, $modalInstance, $route, config, service, moduleEnvironment) {
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

        $scope.modelOptions = {
            debounce: {
                default: 500,
                blur: 250
            },
            getterSetter: true
        };

        $scope.formSaveHost = {};
        $scope.formSaveHost.dataCenter = $scope.config.dataCenters[0];
        $scope.formSaveHost.platform = $scope.config.platforms[0];
        $scope.formSaveHost.sizeCpu = $scope.config.minCpu;
        $scope.formSaveHost.sizeMemory = $scope.config.minMemory;
        $scope.formSaveHost.sizeStorage = $scope.config.minStorage;
        $scope.formSaveHost.version = "";
        $scope.formSaveHost.configVersion = "";
        $scope.formSaveHost.count = 1;
        $scope.formSaveHost.reason = "";

        var responsePromise = $http.get("/v1/service/version?serviceId=" + $scope.service.serviceId + "&moduleId=" + $scope.moduleEnvironment.moduleId, {});
        responsePromise.success(function (data, status, headers, config) {
            $scope.versions = data;
            if ($scope.formSaveHost.version === "") {
                $scope.formSaveHost.version = data.artifactVersions[0];
            }
            if ($scope.formSaveHost.configVersion === "") {
                $scope.formSaveHost.configVersion = data.configVersions[0];
            }
        });

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.moduleEnvironment.moduleId,
                dataCenter: $scope.formSaveHost.dataCenter,
                environment: $scope.moduleEnvironment.environment,
                platform: $scope.formSaveHost.platform,
                sizeCpu: $scope.formSaveHost.sizeCpu,
                sizeMemory: $scope.formSaveHost.sizeMemory,
                sizeStorage: $scope.formSaveHost.sizeStorage,
                version: $scope.formSaveHost.version,
                configVersion: $scope.formSaveHost.configVersion,
                count: $scope.formSaveHost.count,
                reason: $scope.formSaveHost.reason
            };

            var responsePromise = $http.post("/v1/host/create", dataObject, {});
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

hadrianControllers.controller('ModalDeploySoftwareCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'Calendar', 'service', 'hostNames', 'moduleEnvironment',
    function ($scope, $http, $modalInstance, $route, config, Calendar, service, hostNames, moduleEnvironment) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.hostNames = hostNames;
        $scope.moduleEnvironment = moduleEnvironment;
        $scope.config = config;
        $scope.calendar = Calendar.get({serviceId: $scope.service.serviceId});

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

        var responsePromise = $http.get("/v1/service/version?serviceId=" + $scope.service.serviceId + "&moduleId=" + $scope.moduleEnvironment.moduleId, {});
        responsePromise.success(function (data, status, headers, config) {
            $scope.versions = data;
            if ($scope.formUpdateHost.version === "") {
                $scope.formUpdateHost.version = data.artifactVersions[0];
            }
            if ($scope.formUpdateHost.configVersion === "") {
                $scope.formUpdateHost.configVersion = data.configVersions[0];
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

hadrianControllers.controller('ModalRestartHostCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'service', 'hostNames', 'moduleEnvironment',
    function ($scope, $http, $modalInstance, $route, config, service, hostNames, moduleEnvironment) {
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

hadrianControllers.controller('ModalDeleteHostCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'service', 'hostNames', 'moduleEnvironment',
    function ($scope, $http, $modalInstance, $route, config, service, hostNames, moduleEnvironment) {
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
        $scope.formDeleteHost.reason = "";

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.moduleEnvironment.moduleId,
                environment: $scope.moduleEnvironment.environment,
                hostNames: $scope.hostNames,
                reason: $scope.formDeleteHost.reason
            };

            var responsePromise = $http.post("/v1/host/delete", dataObject, {});
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

hadrianControllers.controller('ModalSmokeTestCtrl', ['$scope', '$http', '$modalInstance', 'config', 'service', 'moduleId', 'endPoint',
    function ($scope, $http, $modalInstance, config, service, moduleId, endPoint) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.endPoint = endPoint;
        $scope.config = config;

        $scope.status = "Loading";
        $scope.output = "";
        var responsePromise = $http.get("/v1/st/exec?serviceId=" + service.serviceId + "&moduleId=" + moduleId + "&endPoint=" + endPoint, {});
        responsePromise.success(function (data, status, headers, config) {
            $scope.status = data.result;
            $scope.output = data.output;
        });
        responsePromise.error(function (data, status, headers, config) {
            $scope.status = "Error";
        });
                
        $scope.cancel = function () {
            $modalInstance.close();
        };
    }]);

hadrianControllers.controller('ModalAddCustomFunctionCtrl', ['$scope', '$http', '$modalInstance', '$route', 'service', 'module',
    function ($scope, $http, $modalInstance, $route, service, module) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.module = module;

        $scope.formSaveCF = {};

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.module.moduleId,
                name: $scope.formSaveCF.name,
                method: $scope.formSaveCF.method,
                url: $scope.formSaveCF.url,
                teamOnly: $scope.formSaveCF.teamOnly
            };

            var responsePromise = $http.post("/v1/cf/create", dataObject, {});
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

hadrianControllers.controller('ModalUpdateCustomFunctionCtrl', ['$scope', '$http', '$modalInstance', '$route', 'service', 'cf',
    function ($scope, $http, $modalInstance, $route, service, cf) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.cf = cf;

        $scope.formUpdateCF = {};
        $scope.formUpdateCF.name = cf.name;
        $scope.formUpdateCF.method = cf.method;
        $scope.formUpdateCF.url = cf.url;
        $scope.formUpdateCF.teamOnly = cf.teamOnly;

        $scope.save = function () {
            var dataObject = {
                cfId: $scope.cf.customFunctionId,
                serviceId: $scope.service.serviceId,
                name: $scope.formUpdateCF.name,
                method: $scope.formUpdateCF.method,
                url: $scope.formUpdateCF.url,
                teamOnly: $scope.formUpdateCF.teamOnly
            };

            var responsePromise = $http.put("/v1/cf/modify", dataObject, {});
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

hadrianControllers.controller('ModalAddDocumentCtrl', ['$scope', '$http', '$modalInstance', '$route', 'service',
    function ($scope, $http, $modalInstance, $route, service) {
        $scope.errorMsg = null;
        $scope.service = service;

        $scope.formSaveDocument = {
            documentType: "Markdown",
            title: "",
            link: ""
        };

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                documentType: $scope.formSaveDocument.documentType,
                title: $scope.formSaveDocument.title,
                link: $scope.formSaveDocument.link
            };

            var responsePromise = $http.post("/v1/document/create", dataObject, {});
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



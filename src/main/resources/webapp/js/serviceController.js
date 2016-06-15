'use strict';

/* Controllers */

hadrianControllers.controller('ServiceCtrl', ['$scope', '$route', '$interval', '$http', '$routeParams', '$uibModal', 'filterFilter', 'Config', 'Team', 'Service', 'ServiceRefresh', 'HostDetails', 'VipDetails',
    function ($scope, $route, $interval, $http, $routeParams, $uibModal, filterFilter, Config, Team, Service, ServiceRefresh, HostDetails, VipDetails) {
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
            if (service.active) {
                selectTreeNode($routeParams.serviceId);
            }
        });
        Config.get({}, function (config) {
            $scope.config = config;
        });

        $scope.openUpdateServiceModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/updateService.html',
                controller: 'ModalUpdateServiceCtrl',
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

        $scope.openAddTestModuleModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addTestModule.html',
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
                        return 'Test';
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
            if (module.moduleType === 'Test') {
                var modalInstance = $uibModal.open({
                    animation: true,
                    templateUrl: 'partials/updateTestModule.html',
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
        };

        $scope.openModuleFileModal = function (module, network) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/editModuleFile.html',
                controller: 'ModalModuleFileCtrl',
                size: 'lg',
                resolve: {
                    service: function () {
                        return $scope.service;
                    },
                    module: function () {
                        return module;
                    },
                    network: function () {
                        return network;
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

        $scope.openBackfillModal = function (network, module) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/backfillHost.html',
                controller: 'ModalBackfillModuleCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    },
                    network: function () {
                        return network;
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

        $scope.openAddVipModal = function (network, module) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addVip.html',
                controller: 'ModalAddVipCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    },
                    network: function () {
                        return network;
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

        $scope.openUpdateVipModal = function (vip) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/updateVip.html',
                controller: 'ModalUpdateVipCtrl',
                resolve: {
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

        $scope.openAddHostModal = function (network, module) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addHost.html',
                controller: 'ModalAddHostCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    },
                    network: function () {
                        return network;
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

        $scope.openDeploySoftwareHostsModal = function (moduleNetwork, module) {
            var filteredArray = filterFilter(moduleNetwork.hosts, this.hostFilter);
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
                    network: function () {
                        return moduleNetwork.network;
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

        $scope.openDeploySoftwareHostModal = function (host, moduleNetwork, module) {
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
                    network: function () {
                        return moduleNetwork.network;
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

        $scope.openRestartHostsModal = function (moduleNetwork, module) {
            var filteredArray = filterFilter(moduleNetwork.hosts, this.hostFilter);
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
                    network: function () {
                        return moduleNetwork.network;
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

        $scope.openRestartHostModal = function (host, moduleNetwork, module) {
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
                    network: function () {
                        return moduleNetwork.network;
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

        $scope.openDeleteHostModal = function (host, moduleNetwork, module) {
            var hostNames = [];
            hostNames.push(host.hostName);
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/deleteHost.html',
                controller: 'ModalDeleteHostCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    },
                    hostNames: function () {
                        return hostNames;
                    },
                    network: function () {
                        return moduleNetwork.network;
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

        $scope.getHostDetails = function (host) {
            host.expanded = true;

            HostDetails.get({serviceId: $scope.service.serviceId, hostId: host.hostId}, function (details) {
                host.left = details.left;
                host.right = details.right;
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
                return details[dataCenter].status=== "Off";
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
                return details[dataCenter].status=== "Error";
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

        $scope.openDoCustomFunctionHostsModal = function (moduleNetwork, cf) {
            var filteredArray = filterFilter(moduleNetwork.hosts, this.hostFilter);
            for (var key in filteredArray) {
                var h = filteredArray[key];
                for (var key2 in $scope.formSelectHost) {
                    if (h.hostId === key2 && $scope.formSelectHost[key2]) {
                        window.open("/v1/cf/exec?serviceAbbr=" + $scope.service.serviceAbbr + "&hostName=" + h.hostName + "&cfId=" + cf.customFunctionId, '_blank');
                    }
                }
            }
        };

        $scope.openDoCustomFunctionHostModal = function (host, cf) {
            window.open("/v1/cf/exec?serviceAbbr=" + $scope.service.serviceAbbr + "&hostName=" + host.hostName + "&cfId=" + cf.customFunctionId, '_blank');
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

        $scope.search = {};
        $scope.searchAudit = function () {
            var responsePromise = $http.get("/v1/service/audit?serviceId=" + $scope.service.serviceId + "&start=" + $scope.search.start + "&end=" + $scope.search.end, {});
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

        var stopRefresh = $interval(function () {
            ServiceRefresh.get({serviceId: $routeParams.serviceId}, function (newService) {
                $scope.service.serviceAbbr = newService.serviceAbbr;
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
                            for (var networkIndex = 0; networkIndex < module.networks.length; networkIndex++) {
                                var network = module.networks[networkIndex];
                                for (var newNetworkIndex = 0; newNetworkIndex < newModule.networks.length; newNetworkIndex++) {
                                    var newNetwork = newModule.networks[newNetworkIndex];
                                    if (network.network === newNetwork.network) {
                                        for (var hostIndex = 0; hostIndex < network.hosts.length; hostIndex++) {
                                            var host = network.hosts[hostIndex];
                                            for (var newHostIndex = 0; newHostIndex < newNetwork.hosts.length; newHostIndex++) {
                                                var newHost = newNetwork.hosts[newHostIndex];
                                                if (host.hostId === newHost.hostId) {
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
        });
    }]);

hadrianControllers.controller('ModalUpdateServiceCtrl', ['$scope', '$route', '$http', '$modalInstance', 'service',
    function ($scope, $route, $http, $modalInstance, service) {
        $scope.errorMsg = null;
        $scope.formUpdateService = {};
        $scope.formUpdateService.serviceId = service.serviceId;
        $scope.formUpdateService.serviceAbbr = service.serviceAbbr;
        $scope.formUpdateService.serviceName = service.serviceName;
        $scope.formUpdateService.description = service.description;

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.formUpdateService.serviceId,
                serviceAbbr: $scope.formUpdateService.serviceAbbr,
                serviceName: $scope.formUpdateService.serviceName,
                description: $scope.formUpdateService.description
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

hadrianControllers.controller('ModalDeleteServiceCtrl', ['$scope', '$route', '$http', '$modalInstance', 'service',
    function ($scope, $route, $http, $modalInstance, service) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.formDeleteService = {};
        $scope.formDeleteService.serviceAbbr = service.serviceAbbr;
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
        $scope.formSaveModule.moduleName = service.serviceAbbr + "-";
        $scope.formSaveModule.order = 1;
        $scope.formSaveModule.moduleType = moduleType;
        $scope.formSaveModule.deployableTemplate = $scope.config.deployableTemplates[0];
        $scope.formSaveModule.libraryTemplate = $scope.config.libraryTemplates[0];
        $scope.formSaveModule.testTemplate = $scope.config.testTemplates[0];
        $scope.formSaveModule.gitProject = "";
        $scope.formSaveModule.gitFolder = "";
        $scope.formSaveModule.mavenGroupId = $scope.config.mavenGroupId;
        $scope.formSaveModule.mavenArtifactId = "";
        $scope.formSaveModule.artifactType = $scope.config.artifactTypes[0];
        $scope.formSaveModule.artifactSuffix = "";
        $scope.formSaveModule.hostAbbr = "";
        $scope.formSaveModule.hostname = "";
        $scope.formSaveModule.versionUrl = $scope.config.versionUrl;
        $scope.formSaveModule.availabilityUrl = $scope.config.availabilityUrl;
        $scope.formSaveModule.runAs = "";
        $scope.formSaveModule.deploymentFolder = $scope.config.deploymentFolder;
        $scope.formSaveModule.startCmdLine = "";
        $scope.formSaveModule.startTimeOut = 60;
        $scope.formSaveModule.stopCmdLine = "";
        $scope.formSaveModule.stopTimeOut = 60;
        $scope.formSaveModule.configName = "";

        $scope.save = function () {
            var dataObject = {
                moduleName: $scope.formSaveModule.moduleName,
                serviceId: $scope.service.serviceId,
                order: $scope.formSaveModule.order,
                moduleType: $scope.formSaveModule.moduleType,
                deployableTemplate: $scope.formSaveModule.deployableTemplate,
                libraryTemplate: $scope.formSaveModule.libraryTemplate,
                testTemplate: $scope.formSaveModule.testTemplate,
                gitProject: $scope.formSaveModule.gitProject,
                gitFolder: $scope.formSaveModule.gitFolder,
                mavenGroupId: $scope.formSaveModule.mavenGroupId,
                mavenArtifactId: $scope.formSaveModule.mavenArtifactId,
                artifactType: $scope.formSaveModule.artifactType,
                artifactSuffix: $scope.formSaveModule.artifactSuffix,
                hostAbbr: $scope.formSaveModule.hostAbbr,
                hostname: $scope.formSaveModule.hostname,
                versionUrl: $scope.formSaveModule.versionUrl,
                availabilityUrl: $scope.formSaveModule.availabilityUrl,
                runAs: $scope.formSaveModule.runAs,
                deploymentFolder: $scope.formSaveModule.deploymentFolder,
                startCmdLine: $scope.formSaveModule.startCmdLine,
                startTimeOut: $scope.formSaveModule.startTimeOut,
                stopCmdLine: $scope.formSaveModule.stopCmdLine,
                stopTimeOut: $scope.formSaveModule.stopTimeOut,
                configName: $scope.formSaveModule.configName,
                networkNames: $scope.formSaveModule.networkNames
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
        $scope.formUpdateModule.order = module.order;
        $scope.formUpdateModule.mavenGroupId = module.mavenGroupId;
        $scope.formUpdateModule.mavenArtifactId = module.mavenArtifactId;
        $scope.formUpdateModule.artifactType = module.artifactType;
        $scope.formUpdateModule.artifactSuffix = module.artifactSuffix;
        $scope.formUpdateModule.hostAbbr = module.hostAbbr;
        $scope.formUpdateModule.hostname = module.hostname;
        $scope.formUpdateModule.versionUrl = module.versionUrl;
        $scope.formUpdateModule.availabilityUrl = module.availabilityUrl;
        $scope.formUpdateModule.runAs = module.runAs;
        $scope.formUpdateModule.deploymentFolder = module.deploymentFolder;
        $scope.formUpdateModule.startCmdLine = module.startCmdLine;
        $scope.formUpdateModule.startTimeOut = module.startTimeOut;
        $scope.formUpdateModule.stopCmdLine = module.stopCmdLine;
        $scope.formUpdateModule.stopTimeOut = module.stopTimeOut;
        $scope.formUpdateModule.configName = module.configName;
        $scope.formUpdateModule.networkNames = module.networkNames;

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.module.moduleId,
                moduleName: $scope.formUpdateModule.moduleName,
                order: $scope.formUpdateModule.order,
                mavenGroupId: $scope.formUpdateModule.mavenGroupId,
                mavenArtifactId: $scope.formUpdateModule.mavenArtifactId,
                artifactType: $scope.formUpdateModule.artifactType,
                artifactSuffix: $scope.formUpdateModule.artifactSuffix,
                hostAbbr: $scope.formUpdateModule.hostAbbr,
                hostname: $scope.formUpdateModule.hostname,
                versionUrl: $scope.formUpdateModule.versionUrl,
                availabilityUrl: $scope.formUpdateModule.availabilityUrl,
                runAs: $scope.formUpdateModule.runAs,
                deploymentFolder: $scope.formUpdateModule.deploymentFolder,
                startCmdLine: $scope.formUpdateModule.startCmdLine,
                startTimeOut: $scope.formUpdateModule.startTimeOut,
                stopCmdLine: $scope.formUpdateModule.stopCmdLine,
                stopTimeOut: $scope.formUpdateModule.stopTimeOut,
                configName: $scope.formUpdateModule.configName,
                networkNames: $scope.formUpdateModule.networkNames
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

hadrianControllers.controller('ModalModuleFileCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'service', 'module', 'network',
    function ($scope, $http, $modalInstance, $route, config, service, module, network) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.module = module;
        $scope.network = network;
        $scope.config = config;

        $scope.formFile = {};
        $scope.formFile.name = "";
        $scope.formFile.contents = "";

        $scope.loading = true;

        var responsePromise = $http.get("/v1/module/file?serviceId=" + $scope.service.serviceId + "&moduleId=" + $scope.module.moduleId + "&network=" + $scope.network, {});
        responsePromise.success(function (dataFromServer, status, headers, config) {
            $scope.formFile.name = dataFromServer.name;
            $scope.formFile.contents = dataFromServer.contents;
            $scope.loading = false;
        });

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.module.moduleId,
                network: $scope.network,
                name: $scope.formFile.name,
                contents: $scope.formFile.contents
            };

            var responsePromise = $http.put("/v1/module/file", dataObject, {});
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

hadrianControllers.controller('ModalBackfillModuleCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'service', 'network', 'module',
    function ($scope, $http, $modalInstance, $route, config, service, network, module) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.network = network;
        $scope.module = module;
        $scope.config = config;

        $scope.formBackfillHost = {};
        $scope.formBackfillHost.dataCenter = $scope.config.dataCenters[0];
        $scope.formBackfillHost.env = $scope.config.envs[0];
        $scope.formBackfillHost.size = $scope.config.sizes[0];
        $scope.formBackfillHost.hosts = "";

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.module.moduleId,
                dataCenter: $scope.formBackfillHost.dataCenter,
                network: $scope.network,
                env: $scope.formBackfillHost.env,
                size: $scope.formBackfillHost.size,
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

hadrianControllers.controller('ModalAddVipCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'service', 'network', 'module',
    function ($scope, $http, $modalInstance, $route, config, service, network, module) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.network = network;
        $scope.module = module;
        $scope.config = config;

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
                moduleId: $scope.module.moduleId,
                dns: $scope.formSaveVip.dns,
                domain: $scope.formSaveVip.domain,
                external: $scope.formSaveVip.external,
                network: $scope.network,
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

hadrianControllers.controller('ModalUpdateVipCtrl', ['$scope', '$http', '$modalInstance', '$route', 'service', 'vip',
    function ($scope, $http, $modalInstance, $route, service, vip) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.vip = vip;

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

hadrianControllers.controller('ModalAddHostCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'service', 'network', 'module',
    function ($scope, $http, $modalInstance, $route, config, service, network, module) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.network = network;
        $scope.module = module;
        $scope.config = config;

        $scope.modelOptions = {
            debounce: {
                default: 500,
                blur: 250
            },
            getterSetter: true
        };

        $scope.formSaveHost = {};
        $scope.formSaveHost.dataCenter = $scope.config.dataCenters[0];
        $scope.formSaveHost.env = $scope.config.envs[0];
        $scope.formSaveHost.size = $scope.config.sizes[0];
        $scope.formSaveHost.version = "";
        $scope.formSaveHost.configVersion = "";
        $scope.formSaveHost.count = 1;
        $scope.formSaveHost.reason = "";

        var responsePromise = $http.get("/v1/service/version?serviceId=" + $scope.service.serviceId + "&moduleId=" + $scope.module.moduleId, {});
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
                moduleId: $scope.module.moduleId,
                dataCenter: $scope.formSaveHost.dataCenter,
                network: $scope.network,
                env: $scope.formSaveHost.env,
                size: $scope.formSaveHost.size,
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

hadrianControllers.controller('ModalDeploySoftwareCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'Calendar', 'service', 'hostNames', 'network', 'module',
    function ($scope, $http, $modalInstance, $route, config, Calendar, service, hostNames, network, module) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.hostNames = hostNames;
        $scope.network = network;
        $scope.module = module;
        $scope.config = config;
        $scope.calendar = Calendar.get({serviceId: $scope.service.serviceId});

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

        var responsePromise = $http.get("/v1/service/version?serviceId=" + $scope.service.serviceId + "&moduleId=" + $scope.module.moduleId, {});
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
                moduleId: $scope.module.moduleId,
                network: $scope.network,
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

hadrianControllers.controller('ModalRestartHostCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'service', 'hostNames', 'network', 'module',
    function ($scope, $http, $modalInstance, $route, config, service, hostNames, network, module) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.hostNames = hostNames;
        $scope.network = network;
        $scope.module = module;
        $scope.config = config;

        $scope.formUpdateHost = {};
        $scope.formUpdateHost.reason = "";

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.module.moduleId,
                network: $scope.network,
                all: false,
                hostNames: $scope.hostNames,
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

hadrianControllers.controller('ModalDeleteHostCtrl', ['$scope', '$http', '$modalInstance', '$route', 'service', 'hostNames', 'network', 'module',
    function ($scope, $http, $modalInstance, $route, service, hostNames, network, module) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.hostNames = hostNames;
        $scope.network = network;
        $scope.module = module;

        $scope.formDeleteHost = {};
        $scope.formDeleteHost.reason = "";

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.module.moduleId,
                network: $scope.network,
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

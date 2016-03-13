'use strict';

/* Controllers */

hadrianControllers.controller('ServiceCtrl', ['$scope', '$route', '$http', '$routeParams', '$uibModal', 'Config', 'Team', 'Service', 'HostDetails',
    function ($scope, $route, $http, $routeParams, $uibModal, Config, Team, Service, HostDetails) {
        selectTreeNode($routeParams.serviceId);

        $scope.hostSortType = 'hostName';
        $scope.hostSortReverse = false;
        $scope.hostFilter = '';

        $scope.auditFilter = '';

        $scope.formSelectHost = {};

        Service.get({serviceId: $routeParams.serviceId}, function (service) {
            $scope.service = service;
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

        $scope.openAddUsesModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addUses.html',
                controller: 'ModalAddUsesCtrl',
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

        $scope.deleteServiceRef = function (clientId, serviceId) {
            var responsePromise = $http.delete("/v1/service/" + clientId + "/uses/" + serviceId, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to remove dependence has failed!");
                $route.reload();
            });
        };

        $scope.openAddModuleModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addModule.html',
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
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.openUpdateModuleModal = function (module) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/updateModule.html',
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
        };

        $scope.deleteModule = function (id) {
            var responsePromise = $http.delete("/v1/module/" + $scope.service.serviceId + "/" + id, {});
            responsePromise.success(function () {
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to delete module has failed!");
                $route.reload();
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

        $scope.deleteVip = function (id) {
            var responsePromise = $http.delete("/v1/vip/" + $scope.service.serviceId + "/" + id, {});
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

        $scope.openDeploySoftwareModal = function (network, module) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/deploySoftware.html',
                controller: 'ModalDeploySoftwareCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    },
                    hosts: function () {
                        return $scope.formSelectHost;
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

        $scope.restartHost = function (network, module) {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: module.moduleId,
                network: network,
                hosts: $scope.formSelectHost
            };

            var responsePromise = $http.put("/v1/host/restart", dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to restart hosts has failed!");
                $route.reload();
            });
        };

        $scope.deleteHost = function (id) {
            var responsePromise = $http.delete("/v1/host/" + $scope.service.serviceId + "/" + id, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to delete host has failed!");
                $route.reload();
            });
        };

        $scope.openAddHostToVipModal = function (moduleNetwork) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addHostToVip.html',
                controller: 'ModalAddHostToVipCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    },
                    moduleNetwork: function () {
                        return moduleNetwork;
                    },
                    hosts: function () {
                        return $scope.formSelectHost;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.deleteHostFromVip = function (hostId, vipId) {
            var responsePromise = $http.delete("/v1/host/" + $scope.service.serviceId + "/" + hostId + "/" + vipId, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to remove host from Vip has failed!");
                $route.reload();
            });
        };

        $scope.getHostDetails = function (host) {
            host.expanded = true;

            HostDetails.get({serviceId: $scope.service.serviceId, hostId: host.hostId}, function (details) {
                host.left = details.left;
                host.right = details.right;
            });
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

        $scope.openDoCustomFunctionModal = function (mn, cf) {
            for (var key in mn.hosts) {
                var h = mn.hosts[key];
                for (var key2 in $scope.formSelectHost) {
                    if (h.hostId === key2 && $scope.formSelectHost[key2]) {
                        window.open("/v1/cf/" + $scope.service.serviceId + "/" + cf.customFunctionId + "/" + h.hostId, '_blank');
                    }
                }
            }
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

        $scope.deleteCustomFunction = function (id) {
            var responsePromise = $http.delete("/v1/cf/" + $scope.service.serviceId + "/" + id, {});
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
            var responsePromise = $http.get("/v1/service/" + $scope.service.serviceId + "/audit?start=" + $scope.search.start + "&end=" + $scope.search.end, {});
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
    }]);

hadrianControllers.controller('ModalUpdateServiceCtrl', ['$scope', '$route', '$http', '$modalInstance', 'service',
    function ($scope, $route, $http, $modalInstance, service) {
        $scope.formUpdateService = {};
        $scope.formUpdateService.serviceId = service.serviceId;
        $scope.formUpdateService.serviceAbbr = service.serviceAbbr;
        $scope.formUpdateService.serviceName = service.serviceName;
        $scope.formUpdateService.description = service.description;

        $scope.save = function () {
            var dataObject = {
                serviceAbbr: $scope.formUpdateService.serviceAbbr,
                serviceName: $scope.formUpdateService.serviceName,
                description: $scope.formUpdateService.description
            };

            var responsePromise = $http.put("/v1/service/" + $scope.formUpdateService.serviceId, dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $modalInstance.close();
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to update service has failed!");
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

hadrianControllers.controller('ModalAddUsesCtrl', ['$scope', '$http', '$modalInstance', '$route', 'ServiceNotUses', 'service',
    function ($scope, $http, $modalInstance, $route, ServiceNotUses, service) {
        $scope.service = service;
        $scope.formSelectUses = {};

        ServiceNotUses.get({serviceId: service.serviceId}, function (notUses) {
            $scope.notUses = notUses;
        });

        $scope.save = function () {
            var dataObject = {
                uses: $scope.formSelectUses
            };

            var responsePromise = $http.post("/v1/service/" + $scope.service.serviceId + "/ref", dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $modalInstance.close();
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to add dependence has failed!");
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

hadrianControllers.controller('ModalAddModuleCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'team', 'service',
    function ($scope, $http, $modalInstance, $route, config, team, service) {
        $scope.team = team;
        $scope.service = service;
        $scope.config = config;

        $scope.formSaveModule = {};
        $scope.formSaveModule.moduleName = "";
        $scope.formSaveModule.order = 1;
        if (service.serviceType === 'Service') {
            $scope.formSaveModule.moduleType = $scope.config.moduleTypes[0];
        } else {
            $scope.formSaveModule.moduleType = 'Library';
        }
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
        $scope.formSaveModule.versionUrl = $scope.config.versionUrl;
        $scope.formSaveModule.availabilityUrl = $scope.config.availabilityUrl;
        $scope.formSaveModule.runAs = "";
        $scope.formSaveModule.deploymentFolder = $scope.config.deploymentFolder;
        $scope.formSaveModule.startCmdLine = $scope.config.startCmd;
        $scope.formSaveModule.startTimeOut = 60;
        $scope.formSaveModule.stopCmdLine = $scope.config.stopCmd;
        $scope.formSaveModule.stopTimeOut = 60;

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
                versionUrl: $scope.formSaveModule.versionUrl,
                availabilityUrl: $scope.formSaveModule.availabilityUrl,
                runAs: $scope.formSaveModule.runAs,
                deploymentFolder: $scope.formSaveModule.deploymentFolder,
                startCmdLine: $scope.formSaveModule.startCmdLine,
                startTimeOut: $scope.formSaveModule.startTimeOut,
                stopCmdLine: $scope.formSaveModule.stopCmdLine,
                stopTimeOut: $scope.formSaveModule.stopTimeOut
            };

            var responsePromise = $http.post("/v1/module", dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $modalInstance.close();
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to create new module has failed!");
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

hadrianControllers.controller('ModalUpdateModuleCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'service', 'module',
    function ($scope, $http, $modalInstance, $route, config, service, module) {
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
        $scope.formUpdateModule.versionUrl = module.versionUrl;
        $scope.formUpdateModule.availabilityUrl = module.availabilityUrl;
        $scope.formUpdateModule.runAs = module.runAs;
        $scope.formUpdateModule.deploymentFolder = module.deploymentFolder;
        $scope.formUpdateModule.startCmdLine = module.startCmdLine;
        $scope.formUpdateModule.startTimeOut = module.startTimeOut;
        $scope.formUpdateModule.stopCmdLine = module.stopCmdLine;
        $scope.formUpdateModule.stopTimeOut = module.stopTimeOut;

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleName: $scope.formUpdateModule.moduleName,
                order: $scope.formUpdateModule.order,
                mavenGroupId: $scope.formUpdateModule.mavenGroupId,
                mavenArtifactId: $scope.formUpdateModule.mavenArtifactId,
                artifactType: $scope.formUpdateModule.artifactType,
                artifactSuffix: $scope.formUpdateModule.artifactSuffix,
                hostAbbr: $scope.formUpdateModule.hostAbbr,
                versionUrl: $scope.formUpdateModule.versionUrl,
                availabilityUrl: $scope.formUpdateModule.availabilityUrl,
                runAs: $scope.formUpdateModule.runAs,
                deploymentFolder: $scope.formUpdateModule.deploymentFolder,
                startCmdLine: $scope.formUpdateModule.startCmdLine,
                startTimeOut: $scope.formUpdateModule.startTimeOut,
                stopCmdLine: $scope.formUpdateModule.stopCmdLine,
                stopTimeOut: $scope.formUpdateModule.stopTimeOut
            };

            var responsePromise = $http.put("/v1/module/" + $scope.service.serviceId + "/" + $scope.module.moduleId, dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $modalInstance.close();
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to update module has failed!");
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

hadrianControllers.controller('ModalAddVipCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'service', 'network', 'module',
    function ($scope, $http, $modalInstance, $route, config, service, network, module) {
        $scope.service = service;
        $scope.network = network;
        $scope.module = module;
        $scope.config = config;

        $scope.formSaveVip = {};
        $scope.formSaveVip.vipName = "";
        $scope.formSaveVip.dns = "";
        $scope.formSaveVip.domain = $scope.config.domains[0];
        $scope.formSaveVip.external = false;
        $scope.formSaveVip.protocol = $scope.config.protocols[0];
        $scope.formSaveVip.vipPort = 80;
        $scope.formSaveVip.servicePort = 8080;

        $scope.save = function () {
            var dataObject = {
                vipName: $scope.formSaveVip.vipName,
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

            var responsePromise = $http.post("/v1/vip/vip", dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $modalInstance.close();
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to create new vip has failed!");
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

hadrianControllers.controller('ModalUpdateVipCtrl', ['$scope', '$http', '$modalInstance', '$route', 'service', 'vip',
    function ($scope, $http, $modalInstance, $route, service, vip) {
        $scope.service = service;
        $scope.vip = vip;

        $scope.formUpdateVip = {};
        $scope.formUpdateVip.external = vip.external;
        $scope.formUpdateVip.servicePort = vip.servicePort;

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                external: $scope.formUpdateVip.external,
                servicePort: $scope.formUpdateVip.servicePort
            };

            var responsePromise = $http.put("/v1/vip/" + $scope.vip.vipId, dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $modalInstance.close();
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to update vip has failed!");
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

hadrianControllers.controller('ModalAddHostCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'service', 'network', 'module',
    function ($scope, $http, $modalInstance, $route, config, service, network, module) {
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
        $scope.formSaveHost.version = $scope.module.versions[0];
        $scope.formSaveHost.count = 1;
        $scope.formSaveHost.reason = "";

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.module.moduleId,
                dataCenter: $scope.formSaveHost.dataCenter,
                network: $scope.network,
                env: $scope.formSaveHost.env,
                size: $scope.formSaveHost.size,
                version: $scope.formSaveHost.version,
                count: $scope.formSaveHost.count,
                reason: $scope.formSaveHost.reason
            };

            var responsePromise = $http.post("/v1/host/host", dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $modalInstance.close();
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to create new host has failed!");
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

hadrianControllers.controller('ModalDeploySoftwareCtrl', ['$scope', '$http', '$modalInstance', '$route', 'config', 'Calendar', 'service', 'hosts', 'network', 'module',
    function ($scope, $http, $modalInstance, $route, config, Calendar, service, hosts, network, module) {
        $scope.service = service;
        $scope.hosts = hosts;
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
        $scope.formUpdateHost.version = $scope.module.versions[0];
        $scope.formUpdateHost.reason = "";

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                moduleId: $scope.module.moduleId,
                network: $scope.network,
                all: false,
                hosts: $scope.hosts,
                version: $scope.formUpdateHost.version,
                reason: $scope.formUpdateHost.reason,
                wait: false
            };

            var responsePromise = $http.put("/v1/host/deploy", dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $modalInstance.close();
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to deploy software to hosts has failed!");
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

hadrianControllers.controller('ModalAddHostToVipCtrl', ['$scope', '$http', '$modalInstance', '$route', 'service', 'moduleNetwork', 'hosts',
    function ($scope, $http, $modalInstance, $route, service, moduleNetwork, hosts) {
        $scope.service = service;
        $scope.moduleNetwork = moduleNetwork;
        $scope.hosts = hosts;

        $scope.formSelectVip = {};

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                vips: $scope.formSelectVip,
                hosts: $scope.hosts
            };

            var responsePromise = $http.post("/v1/host/vips", dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $modalInstance.close();
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to add hosts to Vip has failed!");
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }
]);

hadrianControllers.controller('ModalAddCustomFunctionCtrl', ['$scope', '$http', '$modalInstance', '$route', 'service', 'module',
    function ($scope, $http, $modalInstance, $route, service, module) {
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

            var responsePromise = $http.post("/v1/cf/cf", dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $modalInstance.close();
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to create custom function has failed!");
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

hadrianControllers.controller('ModalUpdateCustomFunctionCtrl', ['$scope', '$http', '$modalInstance', '$route', 'service', 'cf',
    function ($scope, $http, $modalInstance, $route, service, cf) {
        $scope.service = service;
        $scope.cf = cf;

        $scope.formUpdateCF = {};
        $scope.formUpdateCF.name = cf.name;
        $scope.formUpdateCF.method = cf.method;
        $scope.formUpdateCF.url = cf.url;
        $scope.formUpdateCF.teamOnly = cf.teamOnly;

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                name: $scope.formUpdateCF.name,
                method: $scope.formUpdateCF.method,
                url: $scope.formUpdateCF.url,
                teamOnly: $scope.formUpdateCF.teamOnly
            };

            var responsePromise = $http.put("/v1/cf/" + $scope.cf.customFunctionId, dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $modalInstance.close();
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to update custom function has failed!");
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

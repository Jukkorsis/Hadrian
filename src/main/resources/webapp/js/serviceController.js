'use strict';

/* Controllers */

soaRepControllers.controller('ServiceCtrl', ['$scope', '$http', '$routeParams', '$uibModal', 'Service', 'HostDetails',
    function ($scope, $http, $routeParams, $uibModal, Service, HostDetails) {
        selectTreeNode($routeParams.serviceId);

        $scope.hostSortType = 'hostName';
        $scope.hostSortReverse = false;
        $scope.hostSearch = '';

        $scope.formSelectHost = {};

        Service.get({serviceId: $routeParams.serviceId}, function (service) {
            $scope.service = service;
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
                $scope.my_tree_handler(tree.get_selected_branch());
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
                $scope.my_tree_handler(tree.get_selected_branch());
            }, function () {
            });
        };

        $scope.deleteServiceRef = function (clientId, serviceId) {
            var responsePromise = $http.delete("/v1/service/" + clientId + "/uses/" + serviceId, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $scope.my_tree_handler(tree.get_selected_branch());
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to delete host has failed!");
                $scope.my_tree_handler(tree.get_selected_branch());
            });
        };

        $scope.openAddVipModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addVip.html',
                controller: 'ModalAddVipCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    }
                }
            });
            modalInstance.result.then(function () {
                $scope.my_tree_handler(tree.get_selected_branch());
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
                $scope.my_tree_handler(tree.get_selected_branch());
            }, function () {
            });
        };

        $scope.deleteVip = function (id) {
            var responsePromise = $http.delete("/v1/vip/" + $scope.service.serviceId + "/" + id, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $scope.my_tree_handler(tree.get_selected_branch());
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to delete host has failed!");
                $scope.my_tree_handler(tree.get_selected_branch());
            });
        };

        $scope.openAddHostModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addHost.html',
                controller: 'ModalAddHostCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    }
                }
            });
            modalInstance.result.then(function () {
                $scope.my_tree_handler(tree.get_selected_branch());
            }, function () {
            });
        };

        $scope.openUpdateHostModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/updateHost.html',
                controller: 'ModalUpdateHostCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    },
                    hosts: function () {
                        return $scope.formSelectHost;
                    }
                }
            });
            modalInstance.result.then(function () {
                $scope.my_tree_handler(tree.get_selected_branch());
            }, function () {
            });
        };

        $scope.restartHost = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                hosts: $scope.formSelectHost
            };

            var responsePromise = $http.put("/v1/host/restart", dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $scope.my_tree_handler(tree.get_selected_branch());
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to create new host has failed!");
                $scope.my_tree_handler(tree.get_selected_branch());
            });
        }

        $scope.deleteHost = function (id) {
            var responsePromise = $http.delete("/v1/host/" + $scope.service.serviceId + "/" + id, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $scope.my_tree_handler(tree.get_selected_branch());
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to delete host has failed!");
                $scope.my_tree_handler(tree.get_selected_branch());
            });
        };

        $scope.openAddHostToVipModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addHostToVip.html',
                controller: 'ModalAddHostToVipCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    },
                    hosts: function () {
                        return $scope.formSelectHost;
                    }
                }
            });
            modalInstance.result.then(function () {
                $scope.my_tree_handler(tree.get_selected_branch());
            }, function () {
            });
        }

        $scope.deleteHostFromVip = function (hostId, vipId) {
            var responsePromise = $http.delete("/v1/host/" + $scope.service.serviceId + "/" + hostId + "/" + vipId, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $scope.my_tree_handler(tree.get_selected_branch());
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to delete host has failed!");
                $scope.my_tree_handler(tree.get_selected_branch());
            });
        }

        $scope.getHostDetails = function (host) {
            host.expanded = true;

            HostDetails.get({serviceId: $scope.service.serviceId, hostId: host.hostId}, function (details) {
                host.left = details.left;
                host.right = details.right;
            });
        }

        $scope.openAddCustomFunctionModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addCustomFunction.html',
                controller: 'ModalAddCustomFunctionCtrl',
                resolve: {
                    service: function () {
                        return $scope.service;
                    }
                }
            });
            modalInstance.result.then(function () {
                $scope.my_tree_handler(tree.get_selected_branch());
            }, function () {
            });
        }

        $scope.openDoCustomFunctionModal = function (cf) {
            for (var key in $scope.service.hosts) {
                var h = $scope.service.hosts[key];
                for (var key2 in $scope.formSelectHost) {
                    if (h.hostId == key2 && $scope.formSelectHost[key2]) {
                        window.open("/v1/cf/" + $scope.service.serviceId + "/" + cf.customFunctionId + "/" + h.hostId, '_blank');
                    }
                }
            }
        }

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
                $scope.my_tree_handler(tree.get_selected_branch());
            }, function () {
            });
        };

        $scope.deleteCustomFunction = function (id) {
            var responsePromise = $http.delete("/v1/cf/" + $scope.service.serviceId + "/" + id, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $scope.my_tree_handler(tree.get_selected_branch());
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to delete host has failed!");
                $scope.my_tree_handler(tree.get_selected_branch());
            });
        };
    }]);

soaRepControllers.controller('ModalUpdateServiceCtrl',
        function ($scope, $http, $modalInstance, $route, Config, service) {
            Config.get({}, function (config) {
                $scope.config = config;

                $scope.formUpdateService = {};
                $scope.formUpdateService.serviceId = service.serviceId;
                $scope.formUpdateService.serviceAbbr = service.serviceAbbr;
                $scope.formUpdateService.serviceName = service.serviceName;
                $scope.formUpdateService.description = service.description;
                $scope.formUpdateService.runAs = service.runAs;
                $scope.formUpdateService.mavenGroupId = service.mavenGroupId;
                $scope.formUpdateService.mavenArtifactId = service.mavenArtifactId;
                $scope.formUpdateService.artifactType = service.artifactType;
                $scope.formUpdateService.artifactSuffix = service.artifactSuffix;
                $scope.formUpdateService.versionUrl = service.versionUrl;
                $scope.formUpdateService.availabilityUrl = service.availabilityUrl;
                $scope.formUpdateService.startCmdLine = service.startCmdLine;
                $scope.formUpdateService.stopCmdLine = service.stopCmdLine;

                $scope.save = function () {
                    var dataObject = {
                        serviceAbbr: $scope.formUpdateService.serviceAbbr,
                        serviceName: $scope.formUpdateService.serviceName,
                        description: $scope.formUpdateService.description,
                        runAs: $scope.formUpdateService.runAs,
                        mavenGroupId: $scope.formUpdateService.mavenGroupId,
                        mavenArtifactId: $scope.formUpdateService.mavenArtifactId,
                        artifactType: $scope.formUpdateService.artifactType,
                        artifactSuffix: $scope.formUpdateService.artifactSuffix,
                        versionUrl: $scope.formUpdateService.versionUrl,
                        availabilityUrl: $scope.formUpdateService.availabilityUrl,
                        startCmdLine: $scope.formUpdateService.startCmdLine,
                        stopCmdLine: $scope.formUpdateService.stopCmdLine
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
            });
        });

soaRepControllers.controller('ModalAddUsesCtrl', ['$scope', '$http', '$modalInstance', '$route', 'ServiceNotUses', 'service',
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
                alert("Request to create new vip has failed!");
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

soaRepControllers.controller('ModalAddVipCtrl', ['$scope', '$http', '$modalInstance', '$route', 'Config', 'service',
    function ($scope, $http, $modalInstance, $route, Config, service) {
        $scope.service = service;
        Config.get({}, function (config) {
            $scope.config = config;

            $scope.formSaveVip = {};
            $scope.formSaveVip.vipName = "";
            $scope.formSaveVip.dns = "";
            $scope.formSaveVip.domain = $scope.config.domains[0];
            $scope.formSaveVip.external = false;
            $scope.formSaveVip.network = $scope.config.networks[0];
            $scope.formSaveVip.protocol = $scope.config.protocols[0];
            $scope.formSaveVip.vipPort = 80;
            $scope.formSaveVip.servicePort = 8080;

            $scope.save = function () {
                var dataObject = {
                    vipName: $scope.formSaveVip.vipName,
                    serviceId: $scope.service.serviceId,
                    dns: $scope.formSaveVip.dns,
                    domain: $scope.formSaveVip.domain,
                    external: $scope.formSaveVip.external,
                    network: $scope.formSaveVip.network,
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
        });
    }]);

soaRepControllers.controller('ModalUpdateVipCtrl', ['$scope', '$http', '$modalInstance', '$route', 'service', 'vip',
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

soaRepControllers.controller('ModalAddHostCtrl', ['$scope', '$http', '$modalInstance', '$route', 'Config', 'service',
    function ($scope, $http, $modalInstance, $route, Config, service) {
        $scope.service = service;
        Config.get({}, function (config) {
            $scope.config = config;

            $scope.formSaveHost = {};
            $scope.formSaveHost.dataCenter = $scope.config.dataCenters[0];
            $scope.formSaveHost.network = $scope.config.networks[0];
            $scope.formSaveHost.env = $scope.config.envs[0];
            $scope.formSaveHost.size = $scope.config.sizes[0];
            $scope.formSaveHost.version = $scope.service.versions[0];
            $scope.formSaveHost.count = 1;

            $scope.save = function () {
                var dataObject = {
                    serviceId: $scope.service.serviceId,
                    dataCenter: $scope.formSaveHost.dataCenter,
                    network: $scope.formSaveHost.network,
                    env: $scope.formSaveHost.env,
                    size: $scope.formSaveHost.size,
                    version: $scope.formSaveHost.version,
                    count: $scope.formSaveHost.count
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
        });
    }]);

soaRepControllers.controller('ModalUpdateHostCtrl', ['$scope', '$http', '$modalInstance', '$route', 'Config', 'service', 'hosts',
    function ($scope, $http, $modalInstance, $route, Config, service, hosts) {
        $scope.config = Config.get();
        $scope.service = service;
        $scope.hosts = hosts;

        $scope.formUpdateHost = {};
        $scope.formUpdateHost.env = "";
        $scope.formUpdateHost.size = "";
        $scope.formUpdateHost.version = "";

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                env: $scope.formUpdateHost.env,
                size: $scope.formUpdateHost.size,
                version: $scope.formUpdateHost.version,
                hosts: $scope.hosts
            };

            var responsePromise = $http.put("/v1/host/host", dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $modalInstance.close();
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to update hosts has failed!");
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

soaRepControllers.controller('ModalAddHostToVipCtrl',
        function ($scope, $http, $modalInstance, $route, service, hosts) {
            $scope.service = service;
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
                    alert("Request to update hosts has failed!");
                });
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        });

soaRepControllers.controller('ModalAddCustomFunctionCtrl',
        function ($scope, $http, $modalInstance, $route, service) {
            $scope.service = service;

            $scope.formSaveCF = {};

            $scope.save = function () {
                var dataObject = {
                    serviceId: $scope.service.serviceId,
                    name: $scope.formSaveCF.name,
                    method: $scope.formSaveCF.method,
                    url: $scope.formSaveCF.url,
                    helpText: $scope.formSaveCF.helpText,
                    teamOnly: $scope.formSaveCF.teamOnly
                };

                var responsePromise = $http.post("/v1/cf/cf", dataObject, {});
                responsePromise.success(function (dataFromServer, status, headers, config) {
                    $modalInstance.close();
                    $route.reload();
                });
                responsePromise.error(function (data, status, headers, config) {
                    alert("Request to update hosts has failed!");
                });
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        });

soaRepControllers.controller('ModalUpdateCustomFunctionCtrl',
        function ($scope, $http, $modalInstance, $route, service, cf) {
            $scope.service = service;
            $scope.cf = cf;

            $scope.formUpdateCF = {};
            $scope.formUpdateCF.name = cf.name;
            $scope.formUpdateCF.method = cf.method;
            $scope.formUpdateCF.url = cf.url;
            $scope.formUpdateCF.helpText = cf.helpText;
            $scope.formUpdateCF.teamOnly = cf.teamOnly;

            $scope.save = function () {
                var dataObject = {
                    serviceId: $scope.service.serviceId,
                    name: $scope.formUpdateCF.name,
                    method: $scope.formUpdateCF.method,
                    url: $scope.formUpdateCF.url,
                    helpText: $scope.formUpdateCF.helpText,
                    teamOnly: $scope.formUpdateCF.teamOnly
                };

                var responsePromise = $http.put("/v1/cf/" + $scope.cf.customFunctionId, dataObject, {});
                responsePromise.success(function (dataFromServer, status, headers, config) {
                    $modalInstance.close();
                    $route.reload();
                });
                responsePromise.error(function (data, status, headers, config) {
                    alert("Request to update hosts has failed!");
                });
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        });

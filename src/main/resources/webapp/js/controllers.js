'use strict';

/* Controllers */

var soaRepControllers = angular.module('soaRepControllers', []);

soaRepControllers.controller('MenuCtrl', ['$scope', '$location', 'Tree',
    function ($scope, $location, Tree) {
        $scope.my_tree_handler = function (branch) {
            $location.path(branch.data.type + '/' + branch.data.id);
        };
        $scope.my_data = Tree.query();
        var tree;
        $scope.my_tree = tree = {};
    }]);

soaRepControllers.controller('HomeCtrl', ['$scope',
    function ($scope) {
    }]);

soaRepControllers.controller('TeamCtrl', ['$scope', '$routeParams', 'Team',
    function ($scope, $routeParams, Team) {
        Team.get({teamId: $routeParams.teamId}, function (team) {
            $scope.team = team;
        });

        $scope.openAddServiceModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addService.html',
                controller: 'ModalAddServiceCtrl',
                resolve: {
                    team: function () {
                        return $scope.team;
                    }
                }
            });
            modalInstance.result.then(function () {
                $location.path('/ui/#/tree', true);
            }, function () {
            });
        };
    }]);

soaRepControllers.controller('ServiceCtrl', ['$scope', '$routeParams', 'Service',
    function ($scope, $routeParams, Service) {
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
                        return $scope.labelService;
                    }
                }
            });
            modalInstance.result.then(function () {
                $scope.my_tree_handler(tree.get_selected_branch());
            }, function () {
            });
        };

        $scope.formSelectHost = {};

        $scope.openAddUsesModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addUses.html',
                controller: 'ModalAddUsesCtrl',
                resolve: {
                    service: function () {
                        return $scope.labelService;
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
                        return $scope.labelService;
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
                        return $scope.labelService;
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
            var responsePromise = $http.delete("/v1/vip/" + id, {});
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
                        return $scope.labelService;
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
                        return $scope.labelService;
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
                serviceId: $scope.labelService.serviceId,
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
            var responsePromise = $http.delete("/v1/host/" + id, {});
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
                        return $scope.labelService;
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
            var responsePromise = $http.delete("/v1/host/" + hostId + "/" + vipId, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $scope.my_tree_handler(tree.get_selected_branch());
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to delete host has failed!");
                $scope.my_tree_handler(tree.get_selected_branch());
            });
        }

        $scope.openAddCustomFunctionModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addCustomFunction.html',
                controller: 'ModalAddCustomFunctionCtrl',
                resolve: {
                    service: function () {
                        return $scope.labelService;
                    }
                }
            });
            modalInstance.result.then(function () {
                $scope.my_tree_handler(tree.get_selected_branch());
            }, function () {
            });
        }

        $scope.openDoCustomFunctionModal = function (cf) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/doCustomFunction.html',
                controller: 'ModalDoCustomFunctionCtrl',
                size: 'lg',
                resolve: {
                    service: function () {
                        return $scope.labelService;
                    },
                    cf: function () {
                        return cf;
                    },
                    selectedHosts: function () {
                        return $scope.formSelectHost;
                    }
                }
            });
            modalInstance.result.then(function () {
                $scope.my_tree_handler(tree.get_selected_branch());
            }, function () {
            });
        }

        $scope.openUpdateCustomFunctionModal = function (cf) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/updateCustomFunction.html',
                controller: 'ModalUpdateCustomFunctionCtrl',
                resolve: {
                    service: function () {
                        return $scope.labelService;
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
            var responsePromise = $http.delete("/v1/cf/" + id, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $scope.my_tree_handler(tree.get_selected_branch());
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to delete host has failed!");
                $scope.my_tree_handler(tree.get_selected_branch());
            });
        };
    }]);

soaRepControllers.controller('TreeCtrl', ['$scope', '$http', '$location', '$uibModal', 'Tree', 'Team', 'Service',
    function ($scope, $http, $location, $uibModal, Tree, Team, Service) {
        $scope.my_tree_handler = function (branch) {
            $scope.displayTeam = false;
            $scope.displayService = false;
            $scope.displayLoading = true;
            if (branch.data.type === "Team") {
                Team.get({teamId: branch.data.id}, function (team) {
                    $scope.team = team;
                    $scope.displayLoading = false;
                    $scope.displayTeam = true;
                });
            } else if (branch.data.type === "Service") {
                Service.get({serviceId: branch.data.id}, function (service) {
                    $scope.labelService = service;
                    $scope.displayLoading = false;
                    $scope.displayService = true;
                });
            } else {
                $scope.displayLoading = false;
            }
            $scope.formSelectVip = {};
            $scope.formSelectHost = {};
        };

        $scope.my_data = Tree.query();

        var tree;
        $scope.my_tree = tree = {};

        $scope.selectTreeNode = function (id) {
            var n;
            n = tree.get_first_branch();
            while (true) {
                if (n === null) {
                    return;
                }
                if (n.data.id === id) {
                    tree.select_branch(n);
                    return;
                }
                n = tree.get_next_branch(n);
            }
        };

        $scope.refresh = function () {
            $scope.my_tree_handler(tree.get_selected_branch());
        };

    }]);

soaRepControllers.controller('ModalAddServiceCtrl',
        function ($scope, $http, $modalInstance, team) {
            $scope.team = team;

            $scope.formSaveService = {};
            $scope.formSaveService.serviceAbbr = "";
            $scope.formSaveService.serviceName = "";
            $scope.formSaveService.description = "";
            $scope.formSaveService.mavenGroupId = "";
            $scope.formSaveService.mavenArtifactId = "";
            $scope.formSaveService.versionUrl = "{host}.mydomain.com:9090/version";
            $scope.formSaveService.availabilityUrl = "{host}.mydomain:9090/availability";

            $scope.save = function () {
                var dataObject = {
                    serviceAbbr: $scope.formSaveService.serviceAbbr,
                    serviceName: $scope.formSaveService.serviceName,
                    teamId: $scope.team.teamId,
                    description: $scope.formSaveService.description,
                    mavenGroupId: $scope.formSaveService.mavenGroupId,
                    mavenArtifactId: $scope.formSaveService.mavenArtifactId,
                    versionUrl: $scope.formSaveService.versionUrl,
                    availabilityUrl: $scope.formSaveService.availabilityUrl
                };

                var responsePromise = $http.post("/v1/service/service", dataObject, {});
                responsePromise.success(function (dataFromServer, status, headers, config) {
                    $modalInstance.close();
                });
                responsePromise.error(function (data, status, headers, config) {
                    alert("Request to create new service has failed!");
                });
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        });

soaRepControllers.controller('ModalUpdateServiceCtrl',
        function ($scope, $http, $modalInstance, service) {

            $scope.formUpdateService = {};
            $scope.formUpdateService.serviceId = service.serviceId;
            $scope.formUpdateService.serviceAbbr = service.serviceAbbr;
            $scope.formUpdateService.serviceName = service.serviceName;
            $scope.formUpdateService.description = service.description;
            $scope.formUpdateService.mavenGroupId = service.mavenGroupId;
            $scope.formUpdateService.mavenArtifactId = service.mavenArtifactId;
            $scope.formUpdateService.versionUrl = service.versionUrl;
            $scope.formUpdateService.availabilityUrl = service.availabilityUrl;

            $scope.save = function () {
                var dataObject = {
                    serviceAbbr: $scope.formUpdateService.serviceAbbr,
                    serviceName: $scope.formUpdateService.serviceName,
                    description: $scope.formUpdateService.description,
                    mavenGroupId: $scope.formUpdateService.mavenGroupId,
                    mavenArtifactId: $scope.formUpdateService.mavenArtifactId,
                    versionUrl: $scope.formUpdateService.versionUrl,
                    availabilityUrl: $scope.formUpdateService.availabilityUrl
                };

                var responsePromise = $http.put("/v1/service/" + $scope.formUpdateService.serviceId, dataObject, {});
                responsePromise.success(function (dataFromServer, status, headers, config) {
                    $modalInstance.close();
                });
                responsePromise.error(function (data, status, headers, config) {
                    alert("Request to update service has failed!");
                });
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        });

soaRepControllers.controller('ModalAddUsesCtrl', ['$scope', '$http', '$modalInstance', 'ServiceNotUses', 'service',
    function ($scope, $http, $modalInstance, ServiceNotUses, service) {
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
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to create new vip has failed!");
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

soaRepControllers.controller('ModalAddVipCtrl', ['$scope', '$http', '$modalInstance', 'Config', 'service',
    function ($scope, $http, $modalInstance, Config, service) {
        $scope.service = service;
        Config.get({}, function (config) {
            $scope.config = config;

            $scope.formSaveVip = {};
            $scope.formSaveVip.vipName = "";
            $scope.formSaveVip.dns = "";
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
                    external: $scope.formSaveVip.external,
                    network: $scope.formSaveVip.network,
                    protocol: $scope.formSaveVip.protocol,
                    vipPort: $scope.formSaveVip.vipPort,
                    servicePort: $scope.formSaveVip.servicePort
                };

                var responsePromise = $http.post("/v1/vip/vip", dataObject, {});
                responsePromise.success(function (dataFromServer, status, headers, config) {
                    $modalInstance.close();
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

soaRepControllers.controller('ModalUpdateVipCtrl', ['$scope', '$http', '$modalInstance', 'service', 'vip',
    function ($scope, $http, $modalInstance, service, vip) {
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
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to update vip has failed!");
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

soaRepControllers.controller('ModalAddHostCtrl', ['$scope', '$http', '$modalInstance', 'Config', 'service',
    function ($scope, $http, $modalInstance, Config, service) {
        $scope.service = service;
        Config.get({}, function (config) {
            $scope.config = config;

            $scope.formSaveHost = {};
            $scope.formSaveHost.dataCenter = $scope.config.dataCenters[0];
            $scope.formSaveHost.network = $scope.config.networks[0];
            $scope.formSaveHost.env = $scope.config.envs[0];
            $scope.formSaveHost.size = $scope.config.sizes[0];
            $scope.formSaveHost.version = $scope.service.versions[0];

            $scope.save = function () {
                var dataObject = {
                    serviceId: $scope.service.serviceId,
                    dataCenter: $scope.formSaveHost.dataCenter,
                    network: $scope.formSaveHost.network,
                    env: $scope.formSaveHost.env,
                    size: $scope.formSaveHost.size,
                    version: $scope.formSaveHost.version
                };

                var responsePromise = $http.post("/v1/host/host", dataObject, {});
                responsePromise.success(function (dataFromServer, status, headers, config) {
                    $modalInstance.close();
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

soaRepControllers.controller('ModalUpdateHostCtrl', ['$scope', '$http', '$modalInstance', 'Config', 'service', 'hosts',
    function ($scope, $http, $modalInstance, Config, service, hosts) {
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
        function ($scope, $http, $modalInstance, service, hosts) {
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
        function ($scope, $http, $modalInstance, service) {
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
                });
                responsePromise.error(function (data, status, headers, config) {
                    alert("Request to update hosts has failed!");
                });
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        });

soaRepControllers.controller('ModalDoCustomFunctionCtrl',
        function ($scope, $modalInstance, service, cf, selectedHosts) {
            $scope.service = service;
            $scope.cf = cf;

            var hosts = [];
            for (var key in service.hosts) {
                var h = service.hosts[key];
                for (var key2 in selectedHosts) {
                    if (h.hostId == key2 && selectedHosts[key2]) {
                        var obj = {
                            hostName: h.hostName,
                            url: cf.url.replace("{host}", h.hostName),
                            realUrl: "/v1/cf/" + cf.customFunctionId + "/" + h.hostId
                        }
                        hosts.push(obj);
                    }
                }
            }
            $scope.hosts = hosts;

            $scope.ok = function () {
                $modalInstance.close();
            };
        });

soaRepControllers.controller('ModalUpdateCustomFunctionCtrl',
        function ($scope, $http, $modalInstance, service, cf) {
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
                });
                responsePromise.error(function (data, status, headers, config) {
                    alert("Request to update hosts has failed!");
                });
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        });

soaRepControllers.controller('GraphCtrl', ['$scope', 'Graph',
    function ($scope, Graph) {
        $scope.data = Graph.query();
        $scope.options = {navigation: true, width: '100%', height: '600px'};
    }]);

soaRepControllers.controller('AdminCtrl', ['$scope', 'Config',
    function ($scope, Config) {
        $scope.config = Config.get();
    }]);

soaRepControllers.controller('HelpCtrl', ['$scope', 'Config',
    function ($scope, Config) {
        $scope.config = Config.get();
    }]);


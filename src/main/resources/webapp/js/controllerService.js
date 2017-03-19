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
                backdrop: 'static',
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

        $scope.openTransferServiceModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                backdrop: 'static',
                templateUrl: 'partials/transferService.html',
                controller: 'ModalTransferServiceCtrl',
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
                backdrop: 'static',
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
                backdrop: 'static',
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
                backdrop: 'static',
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
                backdrop: 'static',
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
                    },
                    initialMsg: function () {
                        return null;
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
                backdrop: 'static',
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
                    },
                    initialMsg: function () {
                        return null;
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
                backdrop: 'static',
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
                    },
                    initialMsg: function () {
                        return null;
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
                    backdrop: 'static',
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
                    backdrop: 'static',
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
                    backdrop: 'static',
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
                backdrop: 'static',
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

        $scope.openDeleteModuleModal = function (module) {
            var modalInstance = $uibModal.open({
                animation: true,
                backdrop: 'static',
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
                backdrop: 'static',
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

        $scope.openBackfillVipModal = function (environmentModule) {
            var modalInstance = $uibModal.open({
                animation: true,
                backdrop: 'static',
                templateUrl: 'partials/backfillVip.html',
                controller: 'ModalBackfillVipCtrl',
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
                backdrop: 'static',
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

        $scope.migrateVip = function (vipId) {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                vipId: vipId
            };

            var responsePromise = $http.post("/v1/vip/migrate", dataObject, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to migrate vip has failed!");
                $route.reload();
            });
        };

        $scope.openAddHostModal = function (moduleEnvironment) {
            var modalInstance = $uibModal.open({
                animation: true,
                backdrop: 'static',
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

        $scope.openBackfillHostModal = function (moduleEnvironment) {
            var modalInstance = $uibModal.open({
                animation: true,
                backdrop: 'static',
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
                backdrop: 'static',
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
                backdrop: 'static',
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
                backdrop: 'static',
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
                backdrop: 'static',
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

        $scope.openRebootHostModal = function (host, moduleEnvironment) {
            var hostNames = [];
            hostNames.push(host.hostName);
            var modalInstance = $uibModal.open({
                animation: true,
                backdrop: 'static',
                templateUrl: 'partials/rebootHost.html',
                controller: 'ModalRebootHostCtrl',
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

        $scope.openDeleteHostModal = function (host, moduleEnvironment) {
            var hostNames = [];
            hostNames.push(host.hostName);
            var modalInstance = $uibModal.open({
                animation: true,
                backdrop: 'static',
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
                backdrop: 'static',
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
                backdrop: 'static',
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
                backdrop: 'static',
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
                backdrop: 'static',
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
                                                    host.statusCode = newHost.statusCode;
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
        }, 20000);

        $scope.$on('$destroy', function () {
            $interval.cancel(stopRefresh);
            $scope.refreshCount = 0;
        });
    }]);

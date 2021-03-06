'use strict';

/* Controllers */

hadrianControllers.controller('TeamCtrl', ['$scope', '$route', '$routeParams', '$uibModal', '$http', 'Config', 'Team',
    function ($scope, $route, $routeParams, $uibModal, $http, Config, Team) {
        $scope.loading = true;

        Config.get({}, function (config) {
            $scope.config = config;
        });

        Team.get({teamId: $routeParams.teamId}, function (team) {
            $scope.team = team;
            $scope.loading = false;
        });

        $scope.openUpdateTeamModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                backdrop: 'static',
                templateUrl: 'partials/updateTeam.html',
                controller: 'ModalUpdateTeamCtrl',
                resolve: {
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

        $scope.openSshAccessModal = function (team) {
            var modalInstance = $uibModal.open({
                animation: true,
                backdrop: 'static',
                templateUrl: 'partials/ssh.html',
                controller: 'ModalSshCtrl',
                resolve: {
                    team: function () {
                        return team;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.openAddServiceModal = function (check) {
            var modalInstance = $uibModal.open({
                animation: true,
                backdrop: 'static',
                templateUrl: 'partials/addService.html',
                controller: 'ModalAddServiceCtrl',
                size: 'lg',
                resolve: {
                    team: function () {
                        return $scope.team;
                    },
                    check: function () {
                        return check;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };
    }]);

hadrianControllers.controller('ModalUpdateTeamCtrl', ['$scope', '$http', '$uibModalInstance', '$route', 'team', 'config',
    function ($scope, $http, $uibModalInstance, $route, team, config) {
        $scope.errorMsg = null;
        $scope.config = config;
        $scope.formUpdateTeam = {};
        $scope.formUpdateTeam.name = team.teamName;
        $scope.formUpdateTeam.email = team.teamEmail;
        $scope.formUpdateTeam.slack = team.teamSlack;
        $scope.formUpdateTeam.gitGroup = team.gitGroup;
        $scope.formUpdateTeam.teamPage = team.teamPage;
        $scope.formUpdateTeam.securityGroupName = team.securityGroupName;
        $scope.formUpdateTeam.colour = team.colour;

        $scope.save = function () {
            var dataObject = {
                teamId: team.teamId,
                teamName: $scope.formUpdateTeam.name,
                teamEmail: $scope.formUpdateTeam.email,
                teamSlack: $scope.formUpdateTeam.slack,
                gitGroup: $scope.formUpdateTeam.gitGroup,
                teamPage: $scope.formUpdateTeam.teamPage,
                securityGroupName: $scope.formUpdateTeam.securityGroupName,
                colour: $scope.formUpdateTeam.colour
            };

            var responsePromise = $http.put("/v1/team/modify", dataObject, {});
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

hadrianControllers.controller('ModalAddServiceCtrl', ['$scope', '$http', '$uibModalInstance', '$window', '$uibModal', 'Config', 'team', 'check',
    function ($scope, $http, $uibModalInstance, $window, $uibModal, Config, team, check) {
        $scope.team = team;
        $scope.errorMsg = null;
        Config.get({}, function (config) {
            $scope.config = config;

            $scope.team = team;

            $scope.formSaveService = {};
            $scope.formSaveService.serviceName = "";
            $scope.formSaveService.description = "";
            $scope.formSaveService.serviceType = "Service";
            $scope.formSaveService.gitProject = "";
            $scope.formSaveService.scope = $scope.config.scopes[0];
            $scope.formSaveService.haFunctionality = false;
            $scope.formSaveService.haPerformance = false;
            $scope.formSaveService.haData = false;
            $scope.formSaveService.haNotes = "";
            $scope.formSaveService.mavenGroupId = $scope.config.mavenGroupId;
            $scope.formSaveService.doBuilds = check;
            $scope.formSaveService.doDeploys = check;
            $scope.formSaveService.doManageVip = true;
            $scope.formSaveService.doCheckJar = true;
            $scope.formSaveService.doFindBugsLevel = "report";
            $scope.formSaveService.testStyle = "Maven";
            $scope.formSaveService.testHostname = "";
            $scope.formSaveService.testRunAs = "";
            $scope.formSaveService.testDeploymentFolder = "";
            $scope.formSaveService.testCmdLine = "";
            $scope.formSaveService.testTimeOut = 300;

            $scope.save = function () {
                var dataObject = {
                    serviceName: $scope.formSaveService.serviceName,
                    teamId: $scope.team.teamId,
                    description: $scope.formSaveService.description,
                    serviceType: $scope.formSaveService.serviceType,
                    gitProject: $scope.formSaveService.gitProject,
                    doBuilds: $scope.formSaveService.doBuilds,
                    doDeploys: $scope.formSaveService.doDeploys,
                    doManageVip: $scope.formSaveService.doManageVip,
                    doCheckJar: $scope.formSaveService.doCheckJar,
                    doFindBugsLevel: $scope.formSaveService.doFindBugsLevel,
                    scope: $scope.formSaveService.scope,
                    haFunctionality: $scope.formSaveService.haFunctionality,
                    haPerformance: $scope.formSaveService.haPerformance,
                    haData: $scope.formSaveService.haData,
                    haNotes: $scope.formSaveService.haNotes,
                    mavenGroupId: $scope.formSaveService.mavenGroupId,
                    testStyle: $scope.formSaveService.testStyle,
                    testHostname: $scope.formSaveService.testHostname,
                    testRunAs: $scope.formSaveService.testRunAs,
                    testDeploymentFolder: $scope.formSaveService.testDeploymentFolder,
                    testCmdLine: $scope.formSaveService.testCmdLine,
                    testTimeOut: $scope.formSaveService.testTimeOut
                };

                var responsePromise = $http.post("/v1/service/create", dataObject, {});
                responsePromise.then(function (response) {
                    $uibModalInstance.close();
                    
                    let templateUrl = "partials/addDeployableModule.html";
                    let moduleType = "Deployable";
                    if (response.data.serviceType === "Shared Library") {
                        templateUrl = "partials/addLibraryModule.html";
                        moduleType = "Library";
                    }
                    
                    var modalInstance = $uibModal.open({
                        animation: true,
                        backdrop: 'static',
                        templateUrl: templateUrl,
                        controller: 'ModalAddModuleCtrl',
                        size: 'lg',
                        resolve: {
                            team: function () {
                                return $scope.team;
                            },
                            service: function () {
                                return response.data;
                            },
                            config: function () {
                                return $scope.config;
                            },
                            moduleType: function () {
                                return moduleType;
                            },
                            initialMsg: function () {
                                return 'Service ' + response.data.serviceName + ' has been saved.';
                            }
                        }
                    });
                    modalInstance.result.then(function () {
                        $window.location.reload();
                    }, function () {
                    });
                });
                responsePromise.catch(function (response) {
                    $scope.errorMsg = response.data;
                });
            };

            $scope.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        });
    }]);

hadrianControllers.controller('TeamDashboardCtrl', ['$scope', '$routeParams', 'Config', 'Dashboard',
    function ($scope, $routeParams, Config, Dashboard) {
        $scope.loading = true;
        $scope.env = $routeParams.env;

        Config.get({}, function (config) {
            $scope.config = config;
        });

        Dashboard.get({teamId: $routeParams.teamId, env: $routeParams.env}, function (dashboard) {
            $scope.dashboard = dashboard;
            $scope.loading = false;
        });

    }]);

hadrianControllers.controller('AllDashboardCtrl', ['$scope', '$routeParams', 'Config', 'DashboardAll',
    function ($scope, $routeParams, Config, DashboardAll) {
        $scope.loading = true;
        $scope.env = $routeParams.env;

        Config.get({}, function (config) {
            $scope.config = config;
        });

        DashboardAll.get({env: $routeParams.env}, function (dashboard) {
            $scope.dashboard = dashboard;
            $scope.loading = false;
        });

    }]);


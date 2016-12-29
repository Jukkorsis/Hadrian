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
                templateUrl: 'partials/updateTeam.html',
                controller: 'ModalUpdateTeamCtrl',
                resolve: {
                    team: function () {
                        return $scope.team;
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

hadrianControllers.controller('ModalUpdateTeamCtrl', ['$scope', '$http', '$modalInstance', '$route', 'team',
    function ($scope, $http, $modalInstance, $route, team) {
        $scope.errorMsg = null;
        $scope.formUpdateTeam = {};
        $scope.formUpdateTeam.name = team.teamName;
        $scope.formUpdateTeam.email = team.teamEmail;
        $scope.formUpdateTeam.slack = team.teamSlack;
        $scope.formUpdateTeam.gitGroup = team.gitGroup;
        $scope.formUpdateTeam.teamPage = team.teamPage;
        $scope.formUpdateTeam.calendarId = team.calendarId;
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
                calendarId: $scope.formUpdateTeam.calendarId,
                securityGroupName: $scope.formUpdateTeam.securityGroupName,
                colour: $scope.formUpdateTeam.colour
            };

            var responsePromise = $http.put("/v1/team/modify", dataObject, {});
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

hadrianControllers.controller('ModalAddServiceCtrl', ['$scope', '$http', '$modalInstance', '$window', 'Config', 'team', 'check',
    function ($scope, $http, $modalInstance, $window, Config, team, check) {
        $scope.team = team;
        $scope.errorMsg = null;
        Config.get({}, function (config) {
            $scope.config = config;

            $scope.team = team;

            $scope.formSaveService = {};
            $scope.formSaveService.serviceName = "";
            $scope.formSaveService.description = "";
            $scope.formSaveService.serviceType = $scope.config.serviceTypes[0];
            $scope.formSaveService.gitProject = "";
            $scope.formSaveService.scope = $scope.config.scopes[0];
            $scope.formSaveService.haFunctionality = false;
            $scope.formSaveService.haPerformance = false;
            $scope.formSaveService.haData = false;
            $scope.formSaveService.haNotes = "";
            $scope.formSaveService.mavenGroupId =  $scope.config.mavenGroupId;
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
                responsePromise.success(function (dataFromServer, status, headers, config) {
                    $modalInstance.close();
                    $window.location.reload();
                });
                responsePromise.error(function (data, status, headers, config) {
                    $scope.errorMsg = data;
                });
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
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


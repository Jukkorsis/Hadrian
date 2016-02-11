'use strict';

/* Controllers */

soaRepControllers.controller('TeamCtrl', ['$scope', '$routeParams', '$uibModal', '$http', 'User', 'Team',
    function ($scope, $routeParams, $uibModal, $http, User, Team) {
        selectTreeNode($routeParams.teamId);

        $scope.users = User.get();

        Team.get({teamId: $routeParams.teamId}, function (team) {
            $scope.team = team;
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
                $location.path('/ui/#/tree', true);
            }, function () {
            });
        };

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

        $scope.openAddUserModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addUserToTeam.html',
                controller: 'ModalAddUserToTeamCtrl',
                resolve: {
                    users: function () {
                        return $scope.users;
                    },
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

        $scope.removeUserFromTeam = function (username) {
            var responsePromise = $http.delete("/v1/team/" + $scope.team.teamId + "/" + username, {}, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $modalInstance.close();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to create new team has failed!");
            });
        };
    }]);

soaRepControllers.controller('ModalUpdateTeamCtrl',
        function ($scope, $http, $modalInstance, $route, team) {
            $scope.formUpdateTeam = {};
            $scope.formUpdateTeam.name = team.teamName;
            $scope.formUpdateTeam.email = team.teamEmail;
            $scope.formUpdateTeam.irc = team.teamIrc;
            $scope.formUpdateTeam.calendarId = team.calendarId;

            $scope.save = function () {
                var dataObject = {
                    teamName: $scope.formUpdateTeam.name,
                    teamEmail: $scope.formUpdateTeam.email,
                    teamIrc: $scope.formUpdateTeam.irc,
                    calendarId: $scope.formUpdateTeam.calendarId
                };

                var responsePromise = $http.put("/v1/team/" + tesm.teamId, dataObject, {});
                responsePromise.success(function (dataFromServer, status, headers, config) {
                    $modalInstance.close();
                    $route.reload();
                });
                responsePromise.error(function (data, status, headers, config) {
                    alert("Request to update team has failed!");
                });
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        });

soaRepControllers.controller('ModalAddServiceCtrl', ['$scope', '$http', '$modalInstance', '$window', 'Config', 'team',
    function ($scope, $http, $modalInstance, $window, Config, team) {
        $scope.team = team;
        Config.get({}, function (config) {
            $scope.config = config;

            $scope.team = team;

            $scope.formSaveService = {};
            $scope.formSaveService.serviceAbbr = "";
            $scope.formSaveService.serviceName = "";
            $scope.formSaveService.description = "";
            $scope.formSaveService.template = $scope.config.templates[0];
            $scope.formSaveService.businessImpact = $scope.config.businessImpacts[0];
            $scope.formSaveService.piiUsage = $scope.config.piiUsages[0];
            $scope.formSaveService.gitPath = "";
            $scope.formSaveService.mavenGroupId = "";
            $scope.formSaveService.mavenArtifactId = "";
            $scope.formSaveService.artifactType = $scope.config.artifactTypes[0];
            $scope.formSaveService.artifactSuffix = "";
            $scope.formSaveService.versionUrl = $scope.config.versionUrl;
            $scope.formSaveService.availabilityUrl = $scope.config.availabilityUrl;
            $scope.formSaveService.runAs = "";
            $scope.formSaveService.startCmdLine = $scope.config.startCmd;
            $scope.formSaveService.stopCmdLine = $scope.config.stopCmd;
            $scope.formSaveService.cmdLineTimeOut = 60;

            $scope.save = function () {
                var dataObject = {
                    serviceAbbr: $scope.formSaveService.serviceAbbr,
                    serviceName: $scope.formSaveService.serviceName,
                    teamId: $scope.team.teamId,
                    description: $scope.formSaveService.description,
                    template: $scope.formSaveService.template,
                    businessImpact: $scope.formSaveService.businessImpact,
                    piiUsage: $scope.formSaveService.piiUsage,
                    gitPath: $scope.formSaveService.gitPath,
                    mavenGroupId: $scope.formSaveService.mavenGroupId,
                    mavenArtifactId: $scope.formSaveService.mavenArtifactId,
                    artifactType: $scope.formSaveService.artifactType,
                    artifactSuffix: $scope.formSaveService.artifactSuffix,
                    versionUrl: $scope.formSaveService.versionUrl,
                    availabilityUrl: $scope.formSaveService.availabilityUrl,
                    runAs: $scope.formSaveService.runAs,
                    startCmdLine: $scope.formSaveService.startCmdLine,
                    stopCmdLine: $scope.formSaveService.stopCmdLine,
                    cmdLineTimeOut: $scope.formSaveService.cmdLineTimeOut
                };

                var responsePromise = $http.post("/v1/service/service", dataObject, {});
                responsePromise.success(function (dataFromServer, status, headers, config) {
                    $modalInstance.close();
                    $window.location.reload();
                });
                responsePromise.error(function (data, status, headers, config) {
                    alert("Request to create new service has failed!");
                });
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        });
    }]);

soaRepControllers.controller('ModalAddUserToTeamCtrl',
        function ($scope, $http, $modalInstance, $route, users, team) {
            $scope.users = users;
            $scope.team = team;

            $scope.formAddUserToTeam = {};
            $scope.formAddUserToTeam.user = users.users[0];

            $scope.save = function () {
                var responsePromise = $http.put("/v1/team/" + $scope.team.teamId + "/" + $scope.formAddUserToTeam.user.username, {}, {});
                responsePromise.success(function (dataFromServer, status, headers, config) {
                    $modalInstance.close();
                    $route.reload();
                });
                responsePromise.error(function (data, status, headers, config) {
                    alert("Request to create new team has failed!");
                });
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        });


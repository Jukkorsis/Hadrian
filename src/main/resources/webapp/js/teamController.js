'use strict';

/* Controllers */

hadrianControllers.controller('TeamCtrl', ['$scope', '$route', '$routeParams', '$uibModal', '$http', 'User', 'Team',
    function ($scope, $route, $routeParams, $uibModal, $http, User, Team) {
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
                $route.reload();
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
                $route.reload();
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
                $route.reload();
            }, function () {
            });
        };

        $scope.removeUserFromTeam = function (username) {
            var responsePromise = $http.delete("/v1/team/" + $scope.team.teamId + "/" + username, {}, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $route.reload();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to create new team has failed!");
                $route.reload();
            });
        };
    }]);

hadrianControllers.controller('ModalUpdateTeamCtrl',
        function ($scope, $http, $modalInstance, $route, team) {
            $scope.formUpdateTeam = {};
            $scope.formUpdateTeam.name = team.teamName;
            $scope.formUpdateTeam.email = team.teamEmail;
            $scope.formUpdateTeam.irc = team.teamIrc;
            $scope.formUpdateTeam.gitRepo = team.gitRepo;
            $scope.formUpdateTeam.calendarId = team.calendarId;

            $scope.save = function () {
                var dataObject = {
                    teamName: $scope.formUpdateTeam.name,
                    teamEmail: $scope.formUpdateTeam.email,
                    teamIrc: $scope.formUpdateTeam.irc,
                    gitRepo: $scope.formUpdateTeam.gitRepo,
                    calendarId: $scope.formUpdateTeam.calendarId
                };

                var responsePromise = $http.put("/v1/team/" + team.teamId, dataObject, {});
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

hadrianControllers.controller('ModalAddServiceCtrl', ['$scope', '$http', '$modalInstance', '$window', 'Config', 'team',
    function ($scope, $http, $modalInstance, $window, Config, team) {
        $scope.team = team;
        Config.get({}, function (config) {
            $scope.config = config;

            $scope.team = team;

            $scope.formSaveService = {};
            $scope.formSaveService.serviceAbbr = "";
            $scope.formSaveService.serviceName = "";
            $scope.formSaveService.description = "";
            $scope.formSaveService.serviceType = $scope.config.serviceTypes[0];
            $scope.formSaveService.gitMode = $scope.config.gitModes[0];
            $scope.formSaveService.gitProject = "";

            $scope.save = function () {
                var dataObject = {
                    serviceAbbr: $scope.formSaveService.serviceAbbr,
                    serviceName: $scope.formSaveService.serviceName,
                    teamId: $scope.team.teamId,
                    description: $scope.formSaveService.description,
                    serviceType: $scope.formSaveService.serviceType,
                    gitMode: $scope.formSaveService.gitMode,
                    gitProject: $scope.formSaveService.gitProject
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

hadrianControllers.controller('ModalAddUserToTeamCtrl',
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


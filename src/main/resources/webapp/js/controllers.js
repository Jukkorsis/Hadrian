'use strict';

/* Controllers */

var hadrianControllers = angular.module('hadrianControllers', []);

hadrianControllers.controller('MenuCtrl', ['$scope', '$location', 'Tree',
    function ($scope, $location, Tree) {
        $scope.selectActivity = function () {
            $location.path("Activty");
            $scope.menuMode = "home";
        }
        $scope.selectDevTeam = function (team) {
            $location.path("Team/" + team.teamId);
            $scope.team = team;
            $scope.menuMode = "devTeam";
        }
        $scope.selectService = function (service) {
            $location.path("Service/" + service.serviceId);
        }
        $scope.selectGraphs = function () {
            $location.path("Graph");
        }
        $scope.selectFindHost = function () {
            $location.path("FindHost");
        }
        $scope.selectWorkItems = function () {
            $location.path("WorkItems");
        }
        $scope.selectUsers = function () {
            $location.path("Users");
        }
        $scope.selectParameters = function () {
            $location.path("Parameters");
        }
        $scope.selectHelp = function () {
            $location.path("Help");
        }
        $scope.treeData = Tree.query();
        $scope.selectActivity();
    }]);

hadrianControllers.controller('ActivityCtrl', ['$scope',
    function ($scope) {
    }]);

hadrianControllers.controller('GraphCtrl', ['$scope', '$http', 'Services',
    function ($scope, $http, Services) {
        $scope.services = Services.get();

        $scope.formGraph = {};
        $scope.formGraph.style = "all";
        $scope.formGraph.service = null;

        $scope.doGraph = function () {
            if ($scope.formGraph.style == "all") {
                var responsePromise = $http.get("/v1/graph/all", {});
                responsePromise.success(function (dot, status, headers, config) {
                    document.getElementById("viz").innerHTML = Viz(dot);
                });
            } else if ($scope.formGraph.style == "fanIn") {
                var responsePromise = $http.get("/v1/graph/fanin/" + $scope.formGraph.service.serviceId, {});
                responsePromise.success(function (dot, status, headers, config) {
                    document.getElementById("viz").innerHTML = Viz(dot);
                });
            } else if ($scope.formGraph.style == "fanOut") {
                var responsePromise = $http.get("/v1/graph/fanout/" + $scope.formGraph.service.serviceId, {});
                responsePromise.success(function (dot, status, headers, config) {
                    document.getElementById("viz").innerHTML = Viz(dot);
                });
            }
        }
    }]);

hadrianControllers.controller('FindHostCtrl', ['$scope', '$http',
    function ($scope, $http) {
        $scope.findHostName = "";
        $scope.findStatus = "";
        $scope.findHost = null;

        $scope.doFindHost = function () {
            $scope.findStatus = "Searching...";
            $scope.findHost = null;
            var responsePromise = $http.get("/v1/host/find?hostName=" + $scope.findHostName, {});
            responsePromise.success(function (data, status, headers, config) {
                $scope.findStatus = "";
                $scope.findHost = data;
            });
            responsePromise.error(function (data, status, headers, config) {
                $scope.findStatus = "Could not find host " + $scope.findHostName;
                $scope.findHost = null;
            });
        }
    }]);

hadrianControllers.controller('ParametersCtrl', ['$scope', 'Config',
    function ($scope, Config) {
        $scope.config = Config.get();
    }]);

hadrianControllers.controller('WorkItemsCtrl', ['$scope', '$http', '$route', 'WorkItem',
    function ($scope, $http, $route, WorkItem) {
        $scope.workItemSortType = 'requestDate';
        $scope.workItemSortReverse = false;
        $scope.workItemSearch = '';

        $scope.formSelectWorkItem = {};

        $scope.workItems = WorkItem.get();

        $scope.completeWorkItem = function () {
            for (var key in $scope.workItems.workItems) {
                var wi = $scope.workItems.workItems[key];
                for (var key2 in $scope.formSelectWorkItem) {
                    if (wi.id == key2 && $scope.formSelectWorkItem[key2]) {
                        var dataObject = {
                            requestId: wi.id,
                            status: "success",
                            errorCode: 0,
                            errorDescription: " ",
                            output: "Manually performed"
                        };
                        $http.post("/webhook/callback", dataObject, {});
                    }
                }
            }
            $route.reload();
        };

        $scope.cancelWorkItem = function () {
            for (var key in $scope.workItems.workItems) {
                var wi = $scope.workItems.workItems[key];
                for (var key2 in $scope.formSelectWorkItem) {
                    if (wi.id == key2 && $scope.formSelectWorkItem[key2]) {
                        var dataObject = {
                            requestId: wi.id,
                            status: "error",
                            errorCode: 0,
                            errorDescription: " ",
                            output: "Manually performed"
                        };
                        $http.post("/webhook/callback", dataObject, {});
                    }
                }
            }
            $route.reload();
        };

    }]);

hadrianControllers.controller('UsersCtrl', ['$scope', '$route', '$uibModal', 'User',
    function ($scope, $route, $uibModal, User) {
        $scope.userSortType = 'username';
        $scope.userSortReverse = false;

        $scope.users = User.get();

        $scope.openAddTeamModal = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/addTeam.html',
                controller: 'ModalAddTeamCtrl',
                resolve: {
                    users: function () {
                        return $scope.users;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };

        $scope.openUpdateUserModal = function (user) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'partials/updateUser.html',
                controller: 'ModalUpdateUserCtrl',
                resolve: {
                    user: function () {
                        return user;
                    }
                }
            });
            modalInstance.result.then(function () {
                $route.reload();
            }, function () {
            });
        };
    }]);

hadrianControllers.controller('ModalAddTeamCtrl', ['$scope', '$http', '$modalInstance', '$window', 'users',
    function ($scope, $http, $modalInstance, $window, users) {
        $scope.users = users;
        $scope.errorMsg = null;

        $scope.formSaveTeam = {};
        $scope.formSaveTeam.name = "";
        $scope.formSaveTeam.email = "";
        $scope.formSaveTeam.irc = "";
        $scope.formSaveTeam.slack = "";
        $scope.formSaveTeam.gitGroup = "";
        $scope.formSaveTeam.teamPage = "";
        $scope.formSaveTeam.calendarId = "";
        $scope.formSaveTeam.user = users.users[0];

        $scope.save = function () {
            var dataObject = {
                teamName: $scope.formSaveTeam.name,
                teamEmail: $scope.formSaveTeam.email,
                teamIrc: $scope.formSaveTeam.irc,
                teamSlack: $scope.formSaveTeam.slack,
                gitGroup: $scope.formSaveTeam.gitGroup,
                teamPage: $scope.formSaveTeam.teamPage,
                calendarId: $scope.formSaveTeam.calendarId,
                user: $scope.formSaveTeam.user
            };

            var responsePromise = $http.post("/v1/team/create", dataObject, {});
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
    }]);

hadrianControllers.controller('ModalUpdateUserCtrl', ['$scope', '$http', '$modalInstance', '$route', 'user',
    function ($scope, $http, $modalInstance, $route, user) {
        $scope.user = user;
        $scope.errorMsg = null;

        $scope.formUpdateUser = {};
        $scope.formUpdateUser.username = user.username;
        $scope.formUpdateUser.fullName = user.fullName;
        $scope.formUpdateUser.admin = user.admin;
        $scope.formUpdateUser.deploy = user.deploy;
        $scope.formUpdateUser.audit = user.audit;

        $scope.save = function () {
            var dataObject = {
                username: $scope.user.username,
                fullName: $scope.formUpdateUser.fullName,
                admin: $scope.formUpdateUser.admin,
                deploy: $scope.formUpdateUser.deploy,
                audit: $scope.formUpdateUser.audit
            };

            var responsePromise = $http.put("/v1/user/modify", dataObject, {});
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

hadrianControllers.controller('HelpCtrl', ['$scope',
    function ($scope) {
    }]);

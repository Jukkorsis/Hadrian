'use strict';

/* Controllers */

var hadrianControllers = angular.module('hadrianControllers', []);

hadrianControllers.controller('MenuCtrl', ['$scope', '$location', 'Tree',
    function ($scope, $location, Tree) {
        $scope.menuMode = "home";
        $scope.selectDevTeam = function (team, reset) {
            $location.path("Team/" + team.teamId);
            $scope.team = team;
            $scope.menuMode = "devTeam";
            if (reset) {
                $scope.showInactive = false;
            }
        }
        $scope.selectShowActive = function (state) {
            $scope.showInactive = state;
        }
        $scope.selectService = function (service) {
            $location.path("Service/" + service.serviceId);
        }
        $scope.selectCatalog = function () {
            $location.path("Catalog");
            $scope.menuMode = "home";
        }
        $scope.selectGraphs = function () {
            $location.path("Graph");
            $scope.menuMode = "home";
        }
        $scope.selectFindHost = function () {
            $location.path("FindHost");
            $scope.menuMode = "home";
        }
        $scope.selectWorkItems = function () {
            $location.path("WorkItems");
            $scope.menuMode = "home";
        }
        $scope.selectUsers = function () {
            $location.path("Users");
            $scope.menuMode = "home";
        }
        $scope.selectParameters = function () {
            $location.path("Parameters");
            $scope.menuMode = "home";
        }
        $scope.selectHelp = function () {
            $location.path("Help");
            $scope.menuMode = "home";
        }
        $scope.treeData = Tree.query();
        //$scope.selectCatalog();
    }]);

hadrianControllers.controller('CatalogCtrl', ['$scope', '$http',
    function ($scope, $http) {
        $scope.catalogLoading = true;
        $scope.catalog = null;
        var responsePromise = $http.get("/v1/catalog", {});
        responsePromise.success(function (data, status, headers, config) {
            $scope.catalogSortType = 'teamName';
            $scope.catalogSortReverse = false;
            $scope.catalogFilter = null;
            $scope.catalog = data;
            $scope.catalogLoading = false;
        });
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

hadrianControllers.controller('ParametersCtrl', ['$scope', '$http', 'Config',
    function ($scope, $http, Config) {
        $scope.config = Config.get();
        
        $scope.formEnvironmentConvert = {};
        $scope.formEnvironmentConvert.oldValue = "";
        $scope.formEnvironmentConvert.newValue = "";
        $scope.formEnvironmentConvert.result = "";
        
        $scope.formPlatfromConvert = {};
        $scope.formPlatfromConvert.oldValue = "";
        $scope.formPlatfromConvert.newValue = "";
        $scope.formPlatfromConvert.result = "";
        
        $scope.convertEnvironment = function () {
            $scope.formEnvironmentConvert.result = "Converting";
            var responsePromise = $http.post("/v1/convert?attr=environment&old=" + $scope.formEnvironmentConvert.oldValue + "&new=" + $scope.formEnvironmentConvert.newValue, {});
            responsePromise.success(function (data, status, headers, config) {
                $scope.formEnvironmentConvert.result = "Done";
            });
            responsePromise.error(function (data, status, headers, config) {
                $scope.formEnvironmentConvert.result = data;
            });
        }

        $scope.convertPlatform = function () {
            $scope.formPlatfromConvert.result = "Converting";
            var responsePromise = $http.post("/v1/convert?attr=platform&old=" + $scope.formPlatfromConvert.oldValue + "&new=" + $scope.formPlatfromConvert.newValue, {});
            responsePromise.success(function (data, status, headers, config) {
                $scope.formPlatfromConvert.result = "Done";
            });
            responsePromise.error(function (data, status, headers, config) {
                $scope.formPlatfromConvert.result = data;
            });
        }

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

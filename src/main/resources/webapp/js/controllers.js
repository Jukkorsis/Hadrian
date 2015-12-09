'use strict';

/* Controllers */

var soaRepControllers = angular.module('soaRepControllers', []);

var tree;
var selectTreeNode = function (id) {
    var n = tree.get_first_branch();
    if (n) {
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
    } else {
        window.setTimeout(function () {
            selectTreeNode(id)
        }, 100);
    }
};

soaRepControllers.controller('MenuCtrl', ['$scope', '$location', 'Tree',
    function ($scope, $location, Tree) {
        $scope.my_tree_handler = function (branch) {
            if (branch.data.id < 0) {
                $location.path(branch.data.type);
            } else {
                $location.path(branch.data.type + '/' + branch.data.id);
            }
        };
        $scope.my_data = Tree.query();
        $scope.my_tree = tree = {};
    }]);

soaRepControllers.controller('HomeCtrl', ['$scope',
    function ($scope) {
    }]);

soaRepControllers.controller('GraphCtrl', ['$scope', '$http', 'Services',
    function ($scope, $http, Services) {
        selectTreeNode("-2");
        
        $scope.services = Services.get();
        
        $scope.formGraph = {};
        $scope.formGraph.style = "all";
        $scope.formGraph.service = null;
        
        $scope.doGraph = function() {
            if ($scope.formGraph.style == "all") {
                var responsePromise = $http.get("/v1/graph/all", {});
                responsePromise.success(function (dot, status, headers, config) {
                    document.getElementById("viz").innerHTML = Viz(dot);
                });
            } else if ($scope.formGraph.style == "fanIn") {
                var responsePromise = $http.get("/v1/graph/fanin/"+$scope.formGraph.service.serviceId, {});
                responsePromise.success(function (dot, status, headers, config) {
                    document.getElementById("viz").innerHTML = Viz(dot);
                });
            } else if ($scope.formGraph.style == "fanOut") {
                var responsePromise = $http.get("/v1/graph/fanout/"+$scope.formGraph.service.serviceId, {});
                responsePromise.success(function (dot, status, headers, config) {
                    document.getElementById("viz").innerHTML = Viz(dot);
                });
            }
        }
    }]);

soaRepControllers.controller('ProxyCtrl', ['$scope', 'Portal',
    function ($scope, Portal) {
        selectTreeNode("-3");

        $scope.portal = Portal.query();
    }]);

soaRepControllers.controller('ParametersCtrl', ['$scope', 'Config',
    function ($scope, Config) {
        $scope.config = Config.get();
        selectTreeNode("-8");
    }]);

soaRepControllers.controller('CrossServiceCtrl', ['$scope',
    function ($scope) {
        selectTreeNode("-5");
    }]);

soaRepControllers.controller('WorkItemsCtrl', ['$scope', '$http', '$route', 'WorkItem',
    function ($scope, $http, $route, WorkItem) {
        selectTreeNode("-6");

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
                        $http.post(wi.callbackUrl, "200", {});
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
                        $http.post(wi.callbackUrl, "400", {});
                    }
                }
            }
            $route.reload();
        };

    }]);

soaRepControllers.controller('BackfillCtrl', ['$scope', '$http',
    function ($scope, $http) {
        selectTreeNode("-7");

        $scope.backfillTextarea;
        $scope.submitBackfill = function () {
            var responsePromise = $http.post("/v1/host/backfill", $scope.backfillTextarea, {});
            responsePromise.success(function (dataFromServer, status, headers, config) {
                $modalInstance.close();
            });
            responsePromise.error(function (data, status, headers, config) {
                alert("Request to update hosts has failed!");
            });
        };
    }]);

soaRepControllers.controller('AdminCtrl', ['$scope', '$uibModal', 'User',
    function ($scope, $uibModal, User) {
        selectTreeNode("-9");

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
                $scope.my_tree_handler(tree.get_selected_branch());
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
                $scope.my_tree_handler(tree.get_selected_branch());
            }, function () {
            });
        };
    }]);

soaRepControllers.controller('ModalAddTeamCtrl',
        function ($scope, $http, $modalInstance, $window, users) {
            $scope.users = users;

            $scope.formSaveTeam = {};
            $scope.formSaveTeam.name = "";
            $scope.formSaveTeam.user = "";

            $scope.save = function () {
                var dataObject = {
                    name: $scope.formSaveTeam.name,
                    user: $scope.formSaveTeam.user
                };

                var responsePromise = $http.post("/v1/team", dataObject, {});
                responsePromise.success(function (dataFromServer, status, headers, config) {
                    $modalInstance.close();
                    $window.location.reload();
                });
                responsePromise.error(function (data, status, headers, config) {
                    alert("Request to create new team has failed!");
                });
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        });

soaRepControllers.controller('ModalUpdateUserCtrl',
        function ($scope, $http, $modalInstance, $route, user) {
            $scope.user = user;

            $scope.formUpdateUser = {};
            $scope.formUpdateUser.username = user.username;
            $scope.formUpdateUser.fullName = user.fullName;
            $scope.formUpdateUser.ops = user.ops;
            $scope.formUpdateUser.admin = user.admin;

            $scope.save = function () {
                var dataObject = {
                    username: $scope.user.username,
                    fullName: $scope.formUpdateUser.fullName,
                    ops: $scope.formUpdateUser.ops,
                    admin: $scope.formUpdateUser.admin
                };

                var responsePromise = $http.put("/v1/user/" + $scope.user.username, dataObject, {});
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

soaRepControllers.controller('HelpCtrl', ['$scope',
    function ($scope) {
    }]);

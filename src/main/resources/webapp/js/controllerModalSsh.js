'use strict';

/* Controllers */

hadrianControllers.controller('ModalSshCtrl', ['$scope', '$http', '$uibModalInstance', '$route', 'team',
    function ($scope, $http, $uibModalInstance, $route, team) {
        $scope.errorMsg = null;
        $scope.team = team;
        $scope.title = "";
        $scope.sshGrant = "";
        
        $scope.newEntries = [];
        let len = $scope.team.sshEntries.length;
        let i = 0;
        for (i = 0; i < len; i++) {
            $scope.newEntries.push($scope.team.sshEntries[i]);
        }

        $scope.modelOptions = {
            debounce: {
                default: 500,
                blur: 250
            },
            getterSetter: true
        };

        var responsePromise = $http.get("/v1/ssh?teamId=" + team.teamId, {});
        responsePromise.then(function (response) {
            $scope.title = response.data.title;
            $scope.sshEntries = response.data.sshEntries;
        });

        $scope.add = function () {
            $scope.newEntries.push($scope.sshGrant);
            $scope.sshGrant = "";
        };

        $scope.remove = function (entry) {
            let len = $scope.newEntries.length;
            let i = 0;
            for (i = 0; i < len; i++) {
                let e = $scope.newEntries[i];
                if (e.title == entry.title) {
                    $scope.newEntries.splice(i, 1);
                }
            }
        };

        $scope.save = function () {
            var dataObject = {
                sshGrants: $scope.newEntries
            };

            var responsePromise = $http.put("/v1/ssh?teamId=" + $scope.team.teamId, dataObject, {});
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

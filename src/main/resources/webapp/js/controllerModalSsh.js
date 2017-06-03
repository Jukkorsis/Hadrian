'use strict';

/* Controllers */

hadrianControllers.controller('ModalSshCtrl', ['$scope', '$http', '$uibModalInstance', '$route', 'teamId',
    function ($scope, $http, $uibModalInstance, $route, teamId) {
        $scope.errorMsg = null;
        $scope.teamId = teamId;
        $scope.title = "";

        $scope.formSsh = {};
        $scope.formSsh.sshGrants = null;

        var responsePromise = $http.get("/v1/ssh?teamId=" + teamId, {});
        responsePromise.then(function (response) {
            $scope.title = response.data.title;
            $scope.sshEntries = response.data.sshEntries;
            $scope.formSsh.sshGrants = response.data.sshGrants;
        });

        $scope.save = function () {
            var dataObject = {
                sshGrants: $scope.formSsh.sshGrants
            };

            var responsePromise = $http.put("/v1/ssh?teamId=" + $scope.teamId, dataObject, {});
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

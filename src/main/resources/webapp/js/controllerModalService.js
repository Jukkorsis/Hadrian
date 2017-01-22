'use strict';

/* Controllers */

hadrianControllers.controller('ModalUpdateServiceCtrl', ['$scope', '$route', '$http', '$modalInstance', 'service', 'team', 'config',
    function ($scope, $route, $http, $modalInstance, service, team, config) {
        $scope.team = team;
        $scope.config = config;
        $scope.service = service;
        $scope.errorMsg = null;
        $scope.formUpdateService = {};
        $scope.formUpdateService.serviceId = service.serviceId;
        $scope.formUpdateService.serviceName = service.serviceName;
        $scope.formUpdateService.description = service.description;
        $scope.formUpdateService.scope = service.scope;
        $scope.formUpdateService.haFunctionality = service.haFunctionality;
        $scope.formUpdateService.haPerformance = service.haPerformance;
        $scope.formUpdateService.haData = service.haData;
        $scope.formUpdateService.haNotes = service.haNotes;
        $scope.formUpdateService.doBuilds = service.doBuilds;
        $scope.formUpdateService.doDeploys = service.doDeploys;
        $scope.formUpdateService.doManageVip = service.doManageVip;
        $scope.formUpdateService.doCheckJar = service.doCheckJar;
        $scope.formUpdateService.doFindBugsLevel = service.doFindBugsLevel;
        $scope.formUpdateService.gitProject = service.gitProject;
        $scope.formUpdateService.mavenGroupId = service.mavenGroupId;
        $scope.formUpdateService.testStyle = service.testStyle;
        $scope.formUpdateService.testHostname = service.testHostname;
        $scope.formUpdateService.testRunAs = service.testRunAs;
        $scope.formUpdateService.testDeploymentFolder = service.testDeploymentFolder;
        $scope.formUpdateService.testCmdLine = service.testCmdLine;
        $scope.formUpdateService.testTimeOut = service.testTimeOut;

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.formUpdateService.serviceId,
                serviceName: $scope.formUpdateService.serviceName,
                description: $scope.formUpdateService.description,
                scope: $scope.formUpdateService.scope,
                haFunctionality: $scope.formUpdateService.haFunctionality,
                haPerformance: $scope.formUpdateService.haPerformance,
                haData: $scope.formUpdateService.haData,
                haNotes: $scope.formUpdateService.haNotes,
                doBuilds: $scope.formUpdateService.doBuilds,
                doDeploys: $scope.formUpdateService.doDeploys,
                doManageVip: $scope.formUpdateService.doManageVip,
                doCheckJar: $scope.formUpdateService.doCheckJar,
                doFindBugsLevel: $scope.formUpdateService.doFindBugsLevel,
                gitProject: $scope.formUpdateService.gitProject,
                mavenGroupId: $scope.formUpdateService.mavenGroupId,
                testStyle: $scope.formUpdateService.testStyle,
                testHostname: $scope.formUpdateService.testHostname,
                testRunAs: $scope.formUpdateService.testRunAs,
                testDeploymentFolder: $scope.formUpdateService.testDeploymentFolder,
                testCmdLine: $scope.formUpdateService.testCmdLine,
                testTimeOut: $scope.formUpdateService.testTimeOut
            };

            var responsePromise = $http.put("/v1/service/modify", dataObject, {});
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

hadrianControllers.controller('ModalTransferServiceCtrl', ['$scope', '$route', '$http', '$modalInstance', 'Teams', 'service', 'team', 'config',
    function ($scope, $route, $http, $modalInstance, Teams, service, team, config) {
        $scope.team = team;
        $scope.config = config;
        $scope.service = service;
        $scope.errorMsg = null;

        Teams.get({}, function (teams) {
            $scope.teams = teams;
        });

        $scope.formTransferService = {};
        $scope.formTransferService.team = "";
        $scope.formTransferService.reason = "";

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                teamId: $scope.formTransferService.team.teamId,
                reason: $scope.formTransferService.reason
            };

            var responsePromise = $http.put("/v1/service/transfer", dataObject, {});
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

hadrianControllers.controller('ModalBuildServiceCtrl', ['$scope', '$route', '$http', '$modalInstance', 'service',
    function ($scope, $route, $http, $modalInstance, service) {
        $scope.service = service;
        $scope.errorMsg = null;
        $scope.formBuildService = {};
        $scope.formBuildService.branch = "";

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                branch: $scope.formBuildService.branch
            };

            var responsePromise = $http.post("/v1/service/build", dataObject, {});
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

hadrianControllers.controller('ModalDeleteServiceCtrl', ['$scope', '$route', '$http', '$modalInstance', 'service',
    function ($scope, $route, $http, $modalInstance, service) {
        $scope.errorMsg = null;
        $scope.service = service;
        $scope.formDeleteService = {};
        $scope.formDeleteService.serviceName = service.serviceName;
        $scope.formDeleteService.description = service.description;
        $scope.formDeleteService.reason = "";

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                reason: $scope.formDeleteService.reason
            };

            var responsePromise = $http.put("/v1/service/delete", dataObject, {});
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


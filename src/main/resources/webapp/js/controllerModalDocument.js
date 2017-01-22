'use strict';

/* Controllers */

hadrianControllers.controller('ModalAddDocumentCtrl', ['$scope', '$http', '$modalInstance', '$route', 'service',
    function ($scope, $http, $modalInstance, $route, service) {
        $scope.errorMsg = null;
        $scope.service = service;

        $scope.formSaveDocument = {
            documentType: "Markdown",
            title: "",
            link: ""
        };

        $scope.save = function () {
            var dataObject = {
                serviceId: $scope.service.serviceId,
                documentType: $scope.formSaveDocument.documentType,
                title: $scope.formSaveDocument.title,
                link: $scope.formSaveDocument.link
            };

            var responsePromise = $http.post("/v1/document/create", dataObject, {});
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



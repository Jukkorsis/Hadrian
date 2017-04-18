'use strict';

/* Controllers */

hadrianControllers.controller('ModalAddDocumentCtrl', ['$scope', '$http', '$uibModalInstance', '$route', 'service',
    function ($scope, $http, $uibModalInstance, $route, service) {
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



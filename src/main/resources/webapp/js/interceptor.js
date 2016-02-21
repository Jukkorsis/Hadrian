hadrianApp.factory('redirectInterceptor', function ($q, $location, $window) {
    return {
        'response': function (response) {
            if (typeof response.data === 'string' && response.headers("X-Login-Request")) {
                $window.location.href = "/ui/login.html";
                return $q.reject(response);
            } else {
                return response;
            }
        }
    }

});

hadrianApp.config(['$httpProvider', 
    function ($httpProvider) {
        $httpProvider.interceptors.push('redirectInterceptor');
    }
]); 
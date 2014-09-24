define(['angular', 'greeter'], function(angular, greeter) {
    return angular.module("greeter", []).factory("greeterService", function() {
        return greeter;
    });
});
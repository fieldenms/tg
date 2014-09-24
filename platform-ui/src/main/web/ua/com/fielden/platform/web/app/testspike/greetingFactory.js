define(['angular', 'ua/com/fielden/platform/web/app/testspike/greeter'], function(angular, greeter) {
    return angular.module("greeter", []).factory("greeterService", function() {
        return greeter;
    });
});

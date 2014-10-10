require.config({
	baseUrl: '/app/spike' // the baseUrl is set to correspond to server configuration, which states that App Spike feature is located in 'localhost:1692/app/spike'
});

require(['/config.js'], function() {
	require(['angular', 'angular.route', 'myModule'], function(angular, angularRoute, myModule) {
		var myApp = angular.module("my-app", ["ngRoute", "myModule"]);
		
		myApp.config(["$routeProvider",
			function($routeProvider) {
				$routeProvider.
				when('/', {
					templateUrl: 'simpleTemplate.html'
				}).
				when('/hello', {
					templateUrl: 'helloTemplate.html'
				}).
				otherwise({
					redirectTo: '/'
				});
			}
		]);
		angular.bootstrap(document.getElementsByTagName('body')[0], ["my-app"]); // boot angular application with main module
	});
});

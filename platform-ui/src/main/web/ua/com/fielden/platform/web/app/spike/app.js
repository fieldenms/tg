require.config({
	paths: {
		'angular': 'angular', // why angular.min does not work?
		'angular.route': 'angular-route' // why angular.min does not work?
	},
	shim: { // used for non-AMD modules
		'angular': {
			deps: [],
			exports: 'angular'
		},

		'angular.route': {
			deps: ['angular'],
			exports: 'angularRoute'
		}
	}
});

require(['angular', 'angular.route', 'myModule'], function(angular, angularRoute, myModule) {

	var myApp = angular.module("my-app", ["ngRoute", "myModule"]);

	myApp.config(["$routeProvider", function($routeProvider) {

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

	}]);

	angular.bootstrap(document.getElementsByTagName('body')[0], ["my-app"]); // boot angular application with main module
});



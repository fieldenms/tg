require.config({
	paths: {
		'angular': 'angular' // why angular.min does not work?
	},
	shim: { // used for non-AMD modules
		'angular': {
			deps: [],
			exports: 'angular'
		}
	}
});

require(['angular', 'myModule'], function(angular, myModule) {

	angular.module("my-app", ["myModule"]);
	angular.bootstrap(document.getElementsByTagName('body')[0], ["my-app"]); // boot angular application with main module
});



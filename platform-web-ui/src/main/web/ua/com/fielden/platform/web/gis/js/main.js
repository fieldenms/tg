require(['../../config'], function() {
	require(['log', 'angular', 'GisModule'], function(log, angular, GisModule) {
		angular.module('app', ['GisModule']); // create main angular application module which depends on GisModule
		angular.bootstrap(document.getElementsByTagName('body')[0], ['app']); // boot angular application with main module
	});
});
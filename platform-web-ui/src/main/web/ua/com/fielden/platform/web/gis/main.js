require.config({
	baseUrl: '/gis' // the baseUrl is set to correspond to server configuration, which states that GIS feature is located in 'localhost:1692/gis'
});

require(['/config.js'], function() {
	require(['log', 'angular', 'GisModule'], function(log, angular, GisModule) {
		angular.module('app', ['GisModule']); // create main angular application module which depends on GisModule
		angular.bootstrap(document.getElementsByTagName('body')[0], ['app']); // boot angular application with main module
	});
});
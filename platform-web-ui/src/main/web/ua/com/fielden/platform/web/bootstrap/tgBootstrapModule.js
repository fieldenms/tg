define(['log', 'angular', 'bootstrap.ui'], function(log, angular, bootstrapUi) {
	var tgBootstrapModule = angular.module("tgBootstrapModule", ["ui.bootstrap"]);

	tgBootstrapModule.controller('navCtrl', ['$scope', '$location',
		function($scope, $location) {
			$scope.navClass = function(page) {
				var currentRoute = $location.path().substring(1) || '';
				return page === currentRoute ? 'active' : '';
			};
		}
	]);

	tgBootstrapModule.controller("TaskBarController", ["$scope",
		function($scope) {
			$scope.status = {
				isOpen: true
			};
		}
	]);

	return tgBootstrapModule;
});
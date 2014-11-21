define(['angular'], function(angular) {

	var myModule = angular.module("myModule", []);

	myModule.directive("hello", function() {
		return {
			restrict: "E",
			templateUrl: "hello.html",
			replace: true,
			link: function(scope) {
				scope.name = 'Oleh';

				require(['heavy'], function(heavy) {
					scope.$apply(function() {
						scope.name = heavy;	
					});
				});
			}
		};
	});
	return myModule;
});
define([], function() {

	var myModule = angular.module("myModule", []);

	myModule.directive("hello", function() {
		return {
			restrict: "E",
			templateUrl: "hello.html",
			replace: true
		};
	});
	return myModule;
});
define(['angular', 'log'], function(angular, log) {
	var gisModule = angular.module('GisModule', []);

	gisModule.directive('tgMap', function() {
		return {
			restrict: 'E',
			template: '<div class="map-container"><div class="progress"><div class="progress-bar"></div></div><div class="map"></div></div>',
			replace: true,
			scope: {
				gisCreator: '='
			},
			link: function(scope, iElement, iAttrs) {
				var gisComponent = scope.gisCreator(
					iElement[0].lastElementChild, /*map*/
					iElement[0].firstElementChild, /*progress*/
					iElement[0].firstElementChild.firstElementChild /*progressBar*/ );
			}
		};
	});

	return gisModule;
});
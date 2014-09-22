define(['angular', 'log'], function(angular, log) {
	var gisModule = angular.module('GisModule', []);

	gisModule.directive('tgMap', function() {
		return {
			restrict: 'E',
			template: '<div class="map-container"><div class="progress"><div class="progress-bar"></div></div><div class="map"></div></div>',
			replace: true,
			scope: {
				dependency: '@'
			},
			link: function(scope, iElement, iAttrs) {
				require([scope.dependency], function(GisComponentClass) {
					var _gisComponent = new GisComponentClass(iElement[0].lastElementChild, /*map*/
															iElement[0].firstElementChild, /*progress*/
															iElement[0].firstElementChild.firstElementChild /*progressBar*/);
					if (typeof java !== 'undefined') {
						window.gisComponent = _gisComponent;
					}
					return _gisComponent;
				});
			}
		};
	});

	return gisModule;
});
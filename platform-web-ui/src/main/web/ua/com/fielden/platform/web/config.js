require.config({
	// baseUrl: should be set at the earlier stage
	paths: {
		// external library modules:
		'jquery': 'resources/jquery/jquery',// 2.1.1
		'text': 'resources/require/text', // AMD-compliant
		'async': 'resources/require/async', // AMD-compliant
		'css': 'resources/require/css', // AMD-compliant
		'angular': 'resources/angular/angular', // 1.3.0-beta.19
		'angular.resource': 'resources/angular/angular-resource', // 1.3.0-beta.19
		'angular.route': 'resources/angular/angular-route', // 1.3.0-beta.19
		'bootstrap.ui': 'resources/angular/ui-bootstrap-tpls', // 0.11.2
		'css.bootstrap': 'resources/bootstrap/css/bootstrap', // 3.2.0

		// internal modules:
		'log': 'resources/logging/log'
	},
	shim: { // used for non-AMD modules
		'jquery': {
			deps: [],
			exports: '$'
		},

		'angular': {
			deps: ['jquery'],
			exports: 'angular'
		},

		'angular.resource': {
			deps: ['angular'],
			exports: 'angularResource'
		},

		'angular.route': {
			deps: ['angular'],
			exports: 'angularRoute'
		},

		'bootstrap.ui': {
			deps: ['angular', 'css!css.bootstrap'],
			exports: 'bootstrapUi'
		}
	}
});

require.config({
	// baseUrl: should be set at the earlier stage
	paths: {
		// external library modules:
		'jquery': 'vendor/jquery/jquery',// 2.1.1
		'text': 'vendor/require/text', // AMD-compliant
		'async': 'vendor/require/async', // AMD-compliant
		'css': 'vendor/require/css', // AMD-compliant
		'angular': 'vendor/angular/angular', // 1.3.0-beta.19
		'angular.resource': 'vendor/angular/angular-resource', // 1.3.0-beta.19
		'angular.route': 'vendor/angular/angular-route', // 1.3.0-beta.19
		'bootstrap.ui': 'vendor/angular/ui-bootstrap-tpls', // 0.11.2
		'css.bootstrap': 'vendor/bootstrap/css/bootstrap', // 3.2.0

		// internal modules:
		'log': 'vendor/logging/log'
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

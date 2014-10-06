require.config({
	// baseUrl: should be set at the earlier stage
	paths: {
		// external library modules:
		'jquery': '/jquery/jquery',// 2.1.1
		'text': '/require/text', // AMD-compliant
		'async': '/require/async', // AMD-compliant
		'css': '/require/css', // AMD-compliant
		'angular': '/angular/angular', // 1.3.0-beta.19
		'angular.resource': '/angular/angular-resource', // 1.3.0-beta.19
		'angular.route': '/angular/angular-route', // 1.3.0-beta.19
		'bootstrap.ui': '/angular/angular-route', // 1.3.0-beta.19
		'css.bootstrap': '/bootstrap/bootstrap', // 3.2.0

		// internal modules:
		'log': '/logging/log'
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
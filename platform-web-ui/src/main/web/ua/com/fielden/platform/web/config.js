require.config({
	// baseUrl: should be set at the earlier stage
	paths: {
		// external library modules:
		'text': '/require/text', // AMD-compliant
		'async': '/require/async', // AMD-compliant
		'css': '/require/css', // AMD-compliant
		'angular': '/angular/angular', // why angular.min does not work?
		'angular.route': '/angular/angular-route',

		// internal modules:
		'log': '/logging/log'
	},
	shim: { // used for non-AMD modules
		'angular': {
			deps: [],
			exports: 'angular'
		},

		'angular.route': {
			deps: ['angular'],
			exports: 'angularRoute'
		}
	}
});
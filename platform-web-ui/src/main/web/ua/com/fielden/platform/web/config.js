require.config({
	// baseUrl: should be set at the earlier stage
	paths: {
		// external library modules:
		'text': '/require/text', // AMD-compliant
		'async': '/require/async', // AMD-compliant
		'angular': '/angular/angular', // why angular.min does not work?
		// internal modules:
		'log': '/logging/log'
	},
	shim: { // used for non-AMD modules
		'angular': {
			deps: [],
			exports: 'angular'
		}
	}
});
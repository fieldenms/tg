require.config({
	// baseUrl: should be set at the earlier stage
	paths: {
		// external library modules:
		'text': 'resources/require/text', // AMD-compliant
		'async': 'resources/require/async', // AMD-compliant
		'css': 'resources/require/css', // AMD-compliant

		// internal modules:
		'log': 'resources/logging/log'
	},
	shim: { // used for non-AMD modules
	}
});

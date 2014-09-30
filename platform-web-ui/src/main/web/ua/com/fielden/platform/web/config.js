(function() {
	var prepender0 = function(baseUrl, acc) {
		if (baseUrl.lastIndexOf("/") > -1) {
			return prepender0(baseUrl.substr(0, baseUrl.lastIndexOf("/")), ("../" + acc));
		} else {
			return acc;
		}
	};

	var prepender = function(baseUrl) {
		return prepender0(baseUrl, "");
	};

	var removeTrailingSlash = function(str) {
		if (str.lastIndexOf("/") > -1) {
			return str.substr(0, str.lastIndexOf("/"));
		} else {
			throw "Base url string (from requirejs) [" + str + "] should have one additional trailing slash.";
		}
	};

	var base = removeTrailingSlash(requirejs.s.contexts._.config.baseUrl); // require.toUrl();
	var prepender = prepender(base);

	// console.log('!config', requirejs.s.contexts._.config);
	// console.log('!base', base);
	// console.log('!prepender(base)', prepender);

	require.config({
		// baseUrl: '/',
		// waitSeconds : 120,
		paths: {
			// external library modules:
			'text': prepender + 'require/text', // AMD-compliant     ../../
			'async': prepender + 'require/async', // AMD-compliant    ../../
			'angular': prepender + 'angular/angular' // why angular.min does not work?   ../../
			// 'jQuery': 'vendor/jquery-1.9.0.min',
			// 'underscore': 'vendor/underscore-1.9.min',
		},
		shim: { // used for non-AMD modules
			'angular': {
				deps: [],
				exports: 'angular'
			}

			// 'jQuery': {
			//     exports: '$'
			// },
			// 'underscore': {
			//     exports: '_'
			// },
		}
	});
})();
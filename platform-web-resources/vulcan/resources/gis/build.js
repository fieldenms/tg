({
	baseUrl : 'js',
	mainConfigFile: 'js/main.js',

	out: 'dist/main.min.js',
    optimize: 'uglify2',

	removeCombined: true,
	name: 'main', // http://www.justinmccandless.com/blog/Building+a+Requirejs+Project+to+a+Single,+Reusable+File
	include: [] // potentially other requirejs main files
})

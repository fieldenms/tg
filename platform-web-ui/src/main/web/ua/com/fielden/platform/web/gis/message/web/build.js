({
	baseUrl : 'temp/js',
	mainConfigFile: 'temp/js/message.js',

	out: 'js/message.min.js',
    optimize: 'uglify2',

	removeCombined: true,
	name: 'message', // http://www.justinmccandless.com/blog/Building+a+Requirejs+Project+to+a+Single,+Reusable+File
	include: [] // potentially other requirejs main files
})

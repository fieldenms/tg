define([], function() {

	return function(object) {
		if (typeof java !== 'undefined') {
			java.info(object);
			// java.println(object);
		}		
		console.log(object);
		// console.trace();
	};

});
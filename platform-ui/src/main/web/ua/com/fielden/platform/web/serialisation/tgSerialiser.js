require(['angular'], function(angular) {
    return angular.module('tgSerialiser').factory('serialiser', function() {
        return {
            serialise: function(_) {

            },

            deserialise: function(_) {
                var objectMap = {},
                    objectWithRefId = [],
                    tracer = function(prop) {
                        if (typeof prop === 'object') {
                            deserialiseObject(prop);
                        }
                    },
                    deserialiseObject = function(prop) {
                        if (prop['@id']) {

                        }
                    };
                if (typeof _ === 'object')
            }
        };
    });
});
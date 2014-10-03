define(['angular'], function (angular) {
    'use strict';
    return angular.module('tgSerialiser').factory('serialiser', function () {
        return {
            serialise: function (envelop) {

            },

            deserialise: function (envelop) {
                var objectMap = {},
                    objectWithRefId = [],
                    deserialiseObject = function (obj, propName) {
                        var idType;
                        if (obj['@id']) {
                            objectMap[obj['@id']] = obj;
                            idType = obj['@id'].split('#');
                            if (idType.length !== 2) {
                                idType = obj['@id'].split('#');
                            }
                        }
                    };
                if (Array.isArray(envelop)) {
                    envelop.forEach(function (elem) {
                        this.deserialise(elem);
                    });
                    return envelop;
                } else if (typeof envelop === 'object') {
                    return deserialiseObject(envelop);
                }
            }
        };
    });
});
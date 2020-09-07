import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/app/tg-reflector.js'

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const template = html`
    <tg-reflector id="reflector"></tg-reflector>`;

template.setAttribute('strip-whitespace', '');

const convertToType = function (value) {
    const v = Number(value);
    return !isNaN(v) ? v :
        value === "undefined" ? undefined : value === "null" ? null : value === "true" ? true : value === "false" ? false : value;
};
export const TgSerialiser = Polymer({
    _template: template,

    is: 'tg-serialiser',

    serialise: function (envelope) {
        var entitiesByType = {}, // represents the array of serialised entities of concrete type (firstly serialised entities in the head of list)
            getId = function (entity) {
                var array = entitiesByType[entity.constructor.prototype.type.call(entity).fullClassName()];
                return array ? array.indexOf(entity) : -1;
            },
            putId = function (entity) {
                var array = entitiesByType[entity.constructor.prototype.type.call(entity).fullClassName()];
                if (!array) {
                    array = [];
                    entitiesByType[entity.constructor.prototype.type.call(entity).fullClassName()] = array;
                }
                array.push(entity);
                return array.length - 1;
            },

            serialise = function (obj) {
                var serialisedObj,
                    property,
                    index;
                // Determine the type of the obj to serilise.
                if (Array.isArray(obj)) {
                    // If the type of obj is array then create new array and push into it each deserialised element.
                    serialisedObj = [];
                    obj.forEach(function (elem) {
                        serialisedObj.push(serialise(elem));
                    });
                } else if (obj && typeof obj === 'object') {
                    serialisedObj = {};
                    // If the type is an object and if it has _type property then it is an entity.
                    if (obj.hasOwnProperty("_type")) {
                        // Find the index of the obj in 'array of serialised objects'. If the index equals -1 -- the object was not serialised yet
                        index = getId(obj);
                        if (index >= 0) {
                            serialisedObj["@id_ref"] = obj.constructor.prototype.type.call(obj).identifier() + "#" + (index + 1); // use the current number
                            return serialisedObj;
                        } else {
                            // If obj wasn't found in object map then
                            // create object and add property "@id" to that object, also add created object to object map with key "@id".
                            // That object map contains entity references in order to use it later for initialising cross referenced objects. 
                            serialisedObj["@id"] = obj.constructor.prototype.type.call(obj).identifier() + "#" + (putId(obj) + 1);
                        }
                    }
                    // All object properties will be copied to new object. Just skip "_type" property.
                    for (property in obj) {
                        if (obj.hasOwnProperty(property)) {
                            if (property !== "_type") {
                                serialisedObj[property] = serialise(obj[property]);
                            }
                        }
                    }
                } else {
                    // All other type of objects will be just copied.
                    serialisedObj = obj;
                }
                return serialisedObj;
            };
        return serialise(envelope);
    },
    reflector: function () {
        return this.$.reflector;
    },
    deserialise: function (envelope) {
        var objectMap = {},
            self = this,
            deserialiseMetaProperty = function (obj) {
                var deserialisedObject = self.$.reflector.newEntityInstancePropEmpty();

                // var deserialisedObject = {};

                // Copy all properties from obj to new deserialised object. Skip "@id" proeprty.
                copyProperties(deserialisedObject, obj);
                return deserialisedObject;
            },
            copyProperties = function (deserialisedObject, obj) {
                const exceptionalNames = ['@pdString', '@_i', '@id', '@instanceTypes', '@instanceType', '@resultType', '@id_ref']; // reserved names that can not represent metaProperty name
                for (let property in obj) {
                    if (property[0] === "@" && exceptionalNames.indexOf(property) === -1 && obj[property] !== null && typeof obj[property] === 'object' && !Array.isArray(obj[property])) { // starts with @, non-null object value (perhaps empty)
                        deserialisedObject[property] = deserialiseMetaProperty(obj[property]);
                    } else if (property === "@instanceTypes" && Array.isArray(obj[property])) {
                        console.log("!!!New Types!!!", obj[property]);
                        var types = obj[property];
                        // new types have appeared -- need to be registered inside serialiser!
                        for (var i = 0; i < types.length; i++) {
                            self.$.reflector.registerEntityType(types[i]);
                        }
                        // deserialisedObject["@instanceType"] = 'java.util.ArrayList';
                    } else if (property === "@instanceType" && typeof obj[property] === 'object') {
                        console.log("!!!New Type!!!", obj[property]);
                        self.$.reflector.registerEntityType(obj[property]);
                        // new type has appeared -- need to be registered inside serialiser!
                        deserialisedObject["@instanceType"] = deserialise(obj[property].key);
                    } else if (property !== "@id") {
                        deserialisedObject[property] = deserialise(obj[property]);
                    }
                }
            },
            deserialise = function (obj) {
                var deserialisedObject;
                // Determine the type of object to deserialise.
                if (Array.isArray(obj)) {
                    // If it is an array then create new array and add deserialised eleemnts to that array.
                    deserialisedObject = [];
                    obj.forEach(function (elem) {
                        deserialisedObject.push(deserialise(elem));
                    });
                } else if (obj && typeof obj === 'object') {
                    // If it is an object with "@id_ref" property then find it in object map and return.
                    // Object map contains previously deserialised objects. This map provides the support for cross reference.
                    if (obj.hasOwnProperty("@id_ref")) {
                        return objectMap[obj["@id_ref"]];
                    } else if (obj.hasOwnProperty("@id")) {
                        // If it is an object with "@id" property then create new object with "_id" and "_type" properties and add to the object map.
                        var typeIdentifier = obj["@id"].split("#")[0];
                        var typeName = typeIdentifier.split(":")[0];

                        deserialisedObject = self.$.reflector.newEntityEmpty();
                        // deserialisedObject = {};

                        var foundType = self.$.reflector.getType(typeName);
                        if (!foundType) {
                            throw "Can not find the type with name [" + typeName + "]";
                        }
                        deserialisedObject["_type"] = foundType;
                        objectMap[obj["@id"]] = deserialisedObject;
                    } else {
                        deserialisedObject = {};
                    }
                    // Copy all properties from obj to new deserialised object. Skip "@id" proeprty.
                    copyProperties(deserialisedObject, obj);
                } else {
                    deserialisedObject = obj;
                }
                return deserialisedObject;
            };
        var deserialisedEnvelope = deserialise(envelope);
        //console.log("deserialisedEnvelope = ", deserialisedEnvelope);

        // var entities = deserialisedEnvelope.instance;
        // console.log("deserialisedEnvelope.instance[6]['@prop'].isEditable() = ", deserialisedEnvelope.instance[6]["@prop"].isEditable());
        // console.log("deserialisedEnvelope.instance[7]['@prop'].isEditable() = ", deserialisedEnvelope.instance[7]["@prop"].isEditable());
        // console.log("deserialisedEnvelope.instance[8]['@prop'].isEditable() = ", deserialisedEnvelope.instance[8]["@prop"].isEditable());

        // console.log("entities[1].type().entityTitle() = ", entities[1].type().entityTitle());
        // console.log("entities[1].type().entityDesc() = ", entities[1].type().entityDesc());
        return deserialisedEnvelope;
    }

});
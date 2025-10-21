import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';

import { _millisDateRepresentation } from '/resources/reflection/tg-date-utils.js';
import { resultMessages } from '/resources/reflection/tg-polymer-utils.js';

/**
 * Used for decimal and money formatting. If the scale value for formatting wasn't specified then the default one is used.
 */
const DEFAULT_SCALE = 2;
/**
 * If the precion for entity type property wasn't defined then the default one should be used.
 */
const DEFAULT_PRECISION = 18;
/**
 * If the string property length wasn't defined then the default one should be used.
 */
const DEFAULT_LENGTH = 0;
/**
 * Used for decimal nad money formatting. If trailing Zeros property wasn't defined
 */
const DEFAULT_TRAILING_ZEROS = true;

const DEFAULT_DISPLAY_AS = "";

const _UNDEFINED_CONFIG_TITLE = '_______________________undefined';
const _LINK_CONFIG_TITLE = '_______________________link';
const KEY_NOT_ASSIGNED = "[key is not assigned]"; // closely resembles AbstractEntity.KEY_NOT_ASSIGNED

const STANDARD_COLLECTION_SEPARATOR = ', ';

const VALIDATION_RESULT = '_validationResult';

// A variable that defines a currency symbol, used to represent monetary values as strings.
// This variable is assigned only once.
let currencySymbol = null;

// A space used to separate a currency symbol from a numeric part of  when representing monetary value as strings
export const CURRENCY_SYMBOL_SPACE = '\u200A';

/**
 * Determines whether the result represents the error.
 */
var _isError0 = function (result) {
    return result !== null
        && (result["@resultType"] === "ua.com.fielden.platform.error.Result" || result["@resultType"] === "ua.com.fielden.platform.web.utils.PropertyConflict")
        && (typeof result.ex !== 'undefined');
};

const _simpleClassName = function (fullClassName) {
    const index = fullClassName.lastIndexOf('.') + 1;
    return fullClassName.substring(index);
};

var _isContinuationError0 = function (result) {
    return _isError0(result) && (typeof result.ex.continuationTypeStr !== 'undefined');
}

/**
 * Determines whether the result represents the warning.
 */
var _isWarning0 = function (result) {
    return result !== null && result["@resultType"] === "ua.com.fielden.platform.error.Warning";
};

/**
 * Determines whether the result is informative.
 */
var _isInformative0 = function (result) {
    return result !== null && result["@resultType"] === "ua.com.fielden.platform.error.Informative";
};

/**
 * 'EntityTypeProp' creator. Dependencies: none.
 */
var _createEntityTypePropPrototype = function () {
    ////////////////////////////////////////// THE PROTOTYPE FOR EntityTypeProp ////////////////////////////////////////// 
    var EntityTypeProp = function (name, rawObject) {
        Object.call(this);
        //Set the name of property
        this._name = name;
        // copy all properties from rawObject after deserialisation
        for (var prop in rawObject) {
            this[prop] = rawObject[prop];
        }
    };
    EntityTypeProp.prototype = Object.create(Object.prototype);
    EntityTypeProp.prototype.constructor = EntityTypeProp;

    /**
     * Returns the name of a property in the entity type.
     */
    EntityTypeProp.prototype.name = function () {
        return this._name;
    }

    /**
     * Returns property type.
     */
    EntityTypeProp.prototype.type = function () {
        if (typeof this._typeName === 'undefined') {
            return null; // the type is unknown; collectional properties are the example
        }
        return this._typeName.indexOf(':') > -1 ? _typeTable[this._typeName.substring(1)] : this._typeName;
    }

    /**
     * Returns short collection's key for the property of "short collection" type.
     */
    EntityTypeProp.prototype.shortCollectionKey = function () {
        return typeof this._shortCollectionKey === 'undefined' ? null : this._shortCollectionKey;
    }

    /**
     * Returns specific time-zone for the property of type date.
     *
     * IMPORTANT: do not use '_timeZone' field directly!
     */
    EntityTypeProp.prototype.timeZone = function () {
        return typeof this._timeZone === 'undefined' ? null : this._timeZone;
    }

    /**
     * Returns 'true' when the type property is secrete, false otherwise.
     *
     * IMPORTANT: do not use '_secrete' field directly!
     */
    EntityTypeProp.prototype.isSecrete = function () {
        return typeof this._secrete === 'undefined' ? false : this._secrete;
    }

    /**
     * Returns 'true' when the type property is upperCase, false otherwise.
     *
     * IMPORTANT: do not use '_upperCase' field directly!
     */
    EntityTypeProp.prototype.isUpperCase = function () {
        return typeof this._upperCase === 'undefined' ? false : this._upperCase;
    }

    /**
     * Returns 'true' when the property should be displayed only with date portion, false otherwise.
     *
     * IMPORTANT: do not use '_date' field directly!
     */
    EntityTypeProp.prototype.isDate = function () {
        return typeof this._date === 'undefined' ? false : this._date;
    }

    /**
     * Returns 'true' when the property should be displayed only with time portion, false otherwise.
     *
     * IMPORTANT: do not use '_time' field directly!
     */
    EntityTypeProp.prototype.isTime = function () {
        return typeof this._time === 'undefined' ? false : this._time;
    }

    /**
     * Returns 'true' when the property carries entity type information for the master to be opened.
     *
     * IMPORTANT: do not use '_entityTypeCarrier' field directly!
     */
    EntityTypeProp.prototype.isEntityTypeCarrier = function () {
        return typeof this._entityTypeCarrier === 'undefined' ? false : this._entityTypeCarrier;
    }

    /**
     * Returns 'DATE', 'TIME' or null (means both) for the portion to be displayed for this property.
     */
    EntityTypeProp.prototype.datePortion = function () {
        return this.isDate() ? 'DATE' : (this.isTime() ? 'TIME' : null);
    }

    /** 
     * Returns entity type prop title.
     */
    EntityTypeProp.prototype.title = function () {
        return this._title;
    }

    /** 
     * Returns entity type prop description.
     */
    EntityTypeProp.prototype.desc = function () {
        return this._desc;
    }

    /**
     * Returns 'true' when the type property is critOnly, false otherwise.
     *
     * IMPORTANT: do not use '_critOnly' field directly!
     */
    EntityTypeProp.prototype.isCritOnly = function () {
        return typeof this._critOnly === 'undefined' ? false : this._critOnly;
    }

    /**
     * Returns 'true' when the type property is resultOnly, false otherwise.
     *
     * IMPORTANT: do not use '_resultOnly' field directly!
     */
    EntityTypeProp.prototype.isResultOnly = function () {
        return typeof this._resultOnly === 'undefined' ? false : this._resultOnly;
    }

    /**
     * Returns 'true' when the type property is 'ignore', false otherwise.
     *
     * IMPORTANT: do not use '_ignore' field directly!
     */
    EntityTypeProp.prototype.isIgnore = function () {
        return typeof this._ignore === 'undefined' ? false : this._ignore;
    }

    /** 
     * Returns entity type prop length.
     */
    EntityTypeProp.prototype.length = function () {
        return typeof this._length === 'undefined' ? DEFAULT_LENGTH : this._length;
    }

    /** 
     * Returns entity type prop precision.
     */
    EntityTypeProp.prototype.precision = function () {
        return typeof this._precision === 'undefined' ? DEFAULT_PRECISION : this._precision;
    }

    /** 
     * Returns entity type prop scale.
     */
    EntityTypeProp.prototype.scale = function () {
        return typeof this._scale === 'undefined' ? DEFAULT_SCALE : this._scale;
    }

    /** 
     * Returns entity type prop trailingZeros value.
     */
    EntityTypeProp.prototype.trailingZeros = function () {
        return typeof this._trailingZeros === 'undefined' ? DEFAULT_TRAILING_ZEROS : this._trailingZeros;
    }

    /** 
     * Returns entity type prop scale.
     */
    EntityTypeProp.prototype.displayAs = function () {
        return typeof this._displayAs === 'undefined' ? DEFAULT_DISPLAY_AS : this._displayAs;
    }

    return EntityTypeProp;
};

/**
 * 'EntityInstanceProp' creator. Dependencies: none.
 */
var _createEntityInstancePropPrototype = function () {
    ////////////////////////////////////////// THE PROTOTYPE FOR EntityInstanceProp ////////////////////////////////////////// 
    var EntityInstanceProp = function () {
        Object.call(this);
    };
    EntityInstanceProp.prototype = Object.create(Object.prototype);
    EntityInstanceProp.prototype.constructor = EntityInstanceProp;

    /**
     * Returns 'true' when the instance property is editable, false otherwise.
     *
     * IMPORTANT: do not use '_editable' field directly!
     */
    EntityInstanceProp.prototype.isEditable = function () {
        return typeof this._editable === 'undefined' ? true : this._editable;
    }

    /**
     * Returns 'true' when the instance property is changed from original, false otherwise.
     *
     * IMPORTANT: do not use '_cfo' field directly!
     */
    EntityInstanceProp.prototype.isChangedFromOriginal = function () {
        return typeof this._cfo === 'undefined' ? false : this._cfo;
    }

    /**
     * Returns original value in case where the property is changed from original and the entity is persisted.
     *
     * IMPORTANT: do not use '_originalVal' field directly!
     */
    EntityInstanceProp.prototype.originalValue = function () {
        if (!this.isChangedFromOriginal()) {
            throw "No one should access originalValue for not changed from original property.";
        }

        if (typeof this['_originalVal'] === 'undefined') {
            throw "instanceMetaProperty has no _originalVal when it is crucial!";
        }

        return this._originalVal;
    }

    /**
     * Returns 'true' when the instance property is required, false otherwise.
     *
     * IMPORTANT: do not use '_required' field directly!
     */
    EntityInstanceProp.prototype.isRequired = function () {
        return typeof this._required === 'undefined' ? false : this._required;
    }

    /**
     * Returns 'true' when the instance property is visible, false otherwise.
     *
     * IMPORTANT: do not use '_visible' field directly!
     */
    EntityInstanceProp.prototype.isVisible = function () {
        return typeof this._visible === 'undefined' ? true : this._visible;
    }

    /**
     * Returns validation result (failure or warning) for the instance property or 'null' if successful without warnings.
     *
     * IMPORTANT: do not use '_validationResult' field directly!
     */
    EntityInstanceProp.prototype.validationResult = function () {
        return typeof this[VALIDATION_RESULT] === 'undefined' ? null : this[VALIDATION_RESULT];
    }

    /**
     * Returns last invalid value for the instance property.
     *
     * IMPORTANT: do not use '_lastInvalidValue' field directly!
     */
    EntityInstanceProp.prototype.lastInvalidValue = function () {
        return typeof this._lastInvalidValue === 'undefined' ? null : this._lastInvalidValue;
    }

    /**
     * Returns the max possible length in case of string property, 'undefined' otherwise.
     *
     * IMPORTANT: do not use '_max' field directly!
     */
    EntityInstanceProp.prototype.stringLength = function () {
        // if (this._isString()) { 
        return typeof this._max === 'undefined' ? 0 : this._max;
        // }
        // return undefined;
    }

    /**
     * Returns the max possible integer value in case of integer property (null means unlimited), 'undefined' otherwise.
     *
     * IMPORTANT: do not use '_max' field directly!
     */
    EntityInstanceProp.prototype.integerMax = function () {
        // if (!this._isString()) {
        return typeof this._max === 'undefined' ? null : this._max;
        // }
        // return undefined;
    }

    /**
     * Returns the min possible integer value in case of integer property (null means unlimited), 'undefined' otherwise.
     *
     * IMPORTANT: do not use '_min' field directly!
     */
    EntityInstanceProp.prototype.integerMin = function () {
        // if (!this._isString()) {
        return typeof this._min === 'undefined' ? null : this._min;
        // }
        // return undefined;
    }

    return EntityInstanceProp;
};

/**
 * 'Entity' creator. Dependencies: 'EntityInstanceProp'.
 */
const _createEntityPrototype = function (EntityInstanceProp, StrictProxyException, _isError0, _isWarning0, DynamicEntityKey) {
    ////////////////////////////////////////// THE PROTOTYPE FOR Entity ////////////////////////////////////////// 
    var Entity = function (rawObject) { // rawObject
        Object.call(this);

        if (rawObject) {
            // copy all properties from rawObject if it is not empty
            for (var prop in rawObject) {
                this[prop] = rawObject[prop];
            }
        }
    };
    Entity.prototype = Object.create(Object.prototype);
    Entity.prototype.constructor = Entity;

    /**
     * Returns the type for the entity.
     *
     * IMPORTANT: do not use '_type' field directly!
     */
    Entity.prototype.type = function () {
        return this._type;
    }

    /**
     * Returns the instance prop for the entity.
     *
     * IMPORTANT: do not use '@prop' field directly!
     */
    Entity.prototype.prop = function (name) {
        this.get(name); // ensures that the instance prop of the 'fetched' property is accessed
        if (this._isObjectUndefined("@" + name)) {
            this["@" + name] = new EntityInstanceProp(); // lazily initialise entity instance prop in case where it was not JSON-serialised (all information was 'default')
        }
        return this["@" + name];
    }

    /**
     * Returns the property value for the entity.
     *
     * IMPORTANT: do not use property field directly!
     */
    Entity.prototype.get = function (name) {
        if (name === '') { // empty property name means 'entity itself'
            return this;
        }
        const dotIndex = name.indexOf(".");
        if (dotIndex > -1) {
            const first = name.slice(0, dotIndex);
            const rest = name.slice(dotIndex + 1);
            const firstVal = this.get(first);
            if (firstVal === null) {
                return null;
            } else if (_isEntity(firstVal)) {
                return firstVal.get(rest);
            } else if (firstVal instanceof Array) {
                const internalList = [];
                for (let index = 0; index < firstVal.length; index++) {
                    internalList.push(firstVal[index].get(rest));
                }
                return internalList;
            } else {
                throw 'Unsupported dot-notation [' + name + '] in type [' + this.constructor.prototype.type.call(this).fullClassName() + '].';
            }
        } else {
            const type = this.constructor.prototype.type.call(this);
            if ('key' === name && type.isCompositeEntity()) {
                const dynamicKey = new DynamicEntityKey();
                dynamicKey._entity = this;
                return dynamicKey;
            } else if (type.isUnionEntity() && type.unionCommonProps().concat(['key', 'desc', 'id']).includes(name)) { // common property (perhaps with KEY / DESC) or non-common KEY, DESC and ID for which the value can still be taken; this is to be in sync with Finder.getAbstractUnionEntityFieldValue
                // In case of union entity, its [key / desc / id / common_prop] should return the [key / desc / id / common_prop] of corresponding 'active entity'.
                // This slightly deviates from Java 'AbstractUnionEntity' logic in two aspects:
                // 1) Here the key is exactly equal to key of active entity, but in Java the key is equal to String representation of the key of active entity -- see AbstractUnionEntity.getKey(); however in AbstractEntity.get() in Java there is no conversion to String (see Finder.getAbstractUnionEntityFieldValue)
                // 2) In case where [key / desc / id] is accessed from empty union entity -- here empty values (aka nulls) are returned, but in Java -- exception is thrown. 
                const activeEntity = this._activeEntity();
                return activeEntity === null ? null : activeEntity.get(name);
            } else if (this._isObjectUndefined(name)) {
                if ('desc' === name) { // undefined 'desc' most likely means that it is not a real property and null value can be returned
                    return null;
                } else { // otherwise, need to raise a strict proxy exception
                    throw new StrictProxyException(name, type._simpleClassName());
                }
            } else if (this._isIdOnlyProxy(name)) {
                throw new StrictProxyException(name, type._simpleClassName(), true);
            }
            return this[name];
        }
    }

    /**
     * Returns 'active entity' in this union entity.
     * 
     * This method closely resembles method 'AbstractUnionEntity.activeEntity'.
     */
    Entity.prototype._activeEntity = function () {
        const activeProperty = this._activeProperty();
        return activeProperty !== null ? this.get(activeProperty) : null;
    }

     /**
      * Returns 'active property' in this union entity. Active property is a property among union properties that is not null.
      * If there are no such property then null is returned.
      *
      * This method closely resembles methods 'AbstractUnionEntity.getNameOfAssignedUnionProperty'.
      */
    Entity.prototype._activeProperty = function () {
        const type = this.constructor.prototype.type.call(this);
        return type.unionProps().find(prop => this.get(prop) !== null) || null;
    }

    /**
     * Returns the original property value for the entity.
     *
     */
    Entity.prototype.getOriginal = function (propName) {
        var value = this.get(propName);
        var instanceMetaProperty = this.constructor.prototype.prop.call(this, propName);
        if (instanceMetaProperty.isChangedFromOriginal()) {
            return instanceMetaProperty.originalValue();
        } else {
            return value;
        }
    }

    /**
     * Returns 'true' if there is an object member defined (not a function!) with a specified name, 'false' otherwise.
     *
     */
    Entity.prototype._isObjectUndefined = function (name) {
        return (typeof this[name] === 'undefined') || (typeof this[name] === 'function');
    }

    /**
     * Returns 'true' if property represents id-only proxy instance, 'false' otherwise.
     *
     */
    Entity.prototype._isIdOnlyProxy = function (name) {
        return typeof this[name] === 'string' && (this[name].lastIndexOf('_______id_only_proxy_______', 0) === 0); // starts with 'id-only' prefix
    }

    /**
     * Sets the property value for the entity.
     *
     * IMPORTANT: do not use property field directly!
     */
    Entity.prototype.set = function (name, value) {
        this.get(name); // ensures that the instance prop of the 'fetched' property is accessed
        return this[name] = value;
    }

    /**
     * Sets the property value for the [binding!]entity and registers property 'touch'.
     *
     * In case where user interaction takes place, this method registers such 'property touch' for the purposes 
     * of collecting the queue of 'touched' properties.
     *
     * The property that was touched last will reside last in that queue. Also the count of 'touches' is recorded
     * for each property (mainly for logging purposes).
     *
     * Please, note that 'touched' property does not mean 'modified' from technical perspective.
     * But, even if it is not modified -- such property will be forced to be mutated on server (with its origVal) 
     * to have properly invoked its ACE handlers.
     *
     * IMPORTANT: this method is applicable only to binding entities (not fully-fledged)!
     */
    Entity.prototype.setAndRegisterPropertyTouch = function (propertyName, value) {
        const result = this.set(propertyName, value);

        const touched = this["@@touchedProps"];
        const names = touched.names;
        const values = touched.values;
        const counts = touched.counts;
        const index = names.indexOf(propertyName);
        if (index > -1) {
            const prevCount = counts[index];
            names.splice(index, 1);
            values.splice(index, 1);
            counts.splice(index, 1);
            names.push(propertyName);
            values.push(value);
            counts.push(prevCount + 1);
        } else {
            names.push(propertyName);
            values.push(value);
            counts.push(1);
        }
        // need to reset previously cached ID after the property was modified (touched) by the user -- the cached ID becomes stale in that case, and server-side reconstruction of entity-typed property should be KEY-based instead of ID-based. 
        if (typeof this['@' + propertyName + '_id'] !== 'undefined') {
            delete this['@' + propertyName + '_id'];
        }
        console.debug('Just TOUCHED', propertyName, '(', counts[counts.length - 1], ' time). Names =', names.slice(), 'Values =', values.slice(), 'Counts =', counts.slice());

        return result;
    }

    /**
     * Traverses all fetched properties in entity. It does not include 'id', 'version', '_type' and '@prop' instance meta-props.
     * 
     * Proxy: 
     *    a) proxied properties are missing in serialised entity graph -- this method disregards such properties;
     *    b) id-only proxy properties exist (foe e.g. '_______id_only_proxy_______673') -- this method disregards such properties too.
     *
     * @param propertyCallback -- function(propertyName) to be called on each property
     */
    Entity.prototype.traverseProperties = function (propertyCallback) {
        var entity = this;
        for (var membName in entity) {
            if (entity.hasOwnProperty(membName) && membName[0] !== "@" && membName !== "_type" && membName !== "id" && membName !== "version") {
                if (!entity._isObjectUndefined(membName) && !entity._isIdOnlyProxy(membName)) {
                    propertyCallback(membName);
                }
            }
        }
    }

    /**
     * Determines whether the entity is valid, which means that there are no invalid properties.
     *
     */
    Entity.prototype.isValid = function () {
        return this.firstFailure() === null;
    }

    /**
     * Determines whether the entity is valid (which means that there are no invalid properties) and no exception has been occurred during some server-side process behind the entity (master entity saving, centre selection-crit entity running etc.).
     *
     */
    Entity.prototype.isValidWithoutException = function () {
        return this.isValid() && !this.exceptionOccurred();
    }

    /**
     * Returns preferred property for the entity (non-empty string property name) or 'null' if preferred property is not defined.
     */
    Entity.prototype.preferredProperty = function () {
        return (typeof this['@_pp'] === 'undefined') ? null : this['@_pp'];
    }

    /**
     * Sets preferred property for the entity (non-empty string property name) or 'null' if preferred property should not be defined for the entity.
     */
    Entity.prototype._setPreferredProperty = function (preferredProperty) {
        return this['@_pp'] = preferredProperty;
    }

    /**
     * Determines whether the top-level result, that wraps this entity was invalid, which means that some exception on server has been occurred (e.g. saving exception).
     *
     */
    Entity.prototype.exceptionOccurred = function () {
        return (typeof this['@@___exception-occurred'] === 'undefined') ? null : this['@@___exception-occurred'];
    }

    /**
     * Provides a value 'exceptionOccurred' flag, which determines whether the top-level result, that wraps this entity was invalid, which means that some exception on server has been occurred (e.g. saving exception).
     *
     */
    Entity.prototype._setExceptionOccurred = function (exceptionOccurred) {
        return this['@@___exception-occurred'] = exceptionOccurred;
    }

    /**
     * Finds the first failure for the properties of this entity, if any.
     *
     */
    Entity.prototype.firstFailure = function () {
        var self = this;
        var first = null;
        self.traverseProperties(function (propName) {
            if (_isError0(self.prop(propName).validationResult())) {
                first = self.prop(propName).validationResult();
                return;
            }
        });
        return first;
    }

    /**
     * Determines whether the entity is valid with warning, which means that there are no invalid properties but the properties with warnings exist.
     *
     */
    Entity.prototype.isValidWithWarning = function () {
        return this.firstFailure() === null && this.firstWarning() !== null;
    }

    /**
     * Finds the first warning for the properties of this entity, if any.
     *
     */
    Entity.prototype.firstWarning = function () {
        var self = this;
        var first = null;
        self.traverseProperties(function (propName) {
            if (_isWarning0(self.prop(propName).validationResult())) {
                first = self.prop(propName).validationResult();
                return;
            }
        });
        return first;
    }

    /**
     * Returns 'true' if the entity was persisted before and 'false' otherwise.
     */
    Entity.prototype.isPersisted = function () {
        return this.get('id') !== null;
    }
    
    /**
     * Returns string representation of entity.
     *
     * Note: this method closely resembles AbstractEntity.toString method.
     */
    Entity.prototype.toString = function () {
        const convertedKey = _toString(_convert(this.get('key')), this.constructor.prototype.type.call(this), 'key');
        return convertedKey === '' ? KEY_NOT_ASSIGNED : convertedKey;
    }
    
    return Entity;
};

/**
 * 'DynamicEntityKey' creator.
 */
const _createDynamicEntityKeyPrototype = function () {
    ////////////////////////////////////////// THE PROTOTYPE FOR DynamicEntityKey ////////////////////////////////////////// 
    const DynamicEntityKey = function () {
        Object.call(this);
    };
    DynamicEntityKey.prototype = Object.create(Object.prototype);
    DynamicEntityKey.prototype.constructor = DynamicEntityKey;

    /**
     * The method to convert dynamic entity key (key of composite entity) to String.
     *
     * IMPORTANT: this is the mirror of the java method DynamicEntityKey.toString(). So, please be carefull and maintain it
     * in accordance with java counterpart.
     */
    DynamicEntityKey.prototype._convertDynamicEntityKey = function () {
        const compositeEntity = this._entity;
        const type = compositeEntity.constructor.prototype.type.call(compositeEntity);
        return _toStringForKeys(type.compositeKeyNames().map(name => [name, compositeEntity.get(name)]), type, type.compositeKeySeparator());
    };

    /**
     * Returns 'true' if this equals to dynamicEntityKey2, 'false' otherwise.
     *
     * IMPORTANT: this is the mirror of the java method DynamicEntityKey.compareTo(). So, please be carefull and maintain it
     * in accordance with java counterparts.
     */
    DynamicEntityKey.prototype._dynamicEntityKeyEqualsTo = function (dynamicEntityKey2) {
        if (this === dynamicEntityKey2) {
            return true;
        }
        if (!_isDynamicEntityKey(dynamicEntityKey2)) {
            return false;
        }
        const entity1 = this._entity;
        const entity2 = dynamicEntityKey2._entity;
        const compositeKeyNames = entity1.constructor.prototype.type.call(entity1).compositeKeyNames();
        for (let i = 0; i < compositeKeyNames.length; i++) {
            const compositePartName = compositeKeyNames[i];
            let compositePart1, compositePart2;
            try {
                compositePart1 = entity1.get(compositePartName);
            } catch (strictProxyEx1) {
                throw 'Comparison of entities [' + entity1 + ', ' + entity2 + '] failed. Composite key part [' + compositePartName + '] was not fetched in first entity, please check fetching strategy.' + strictProxyEx1;
            }
            try {
                compositePart2 = entity2.get(compositePartName);
            } catch (strictProxyEx2) {
                throw 'Comparison of entities [' + entity1 + ', ' + entity2 + '] failed. Composite key part [' + compositePartName + '] was not fetched in second entity, please check fetching strategy. ' + strictProxyEx2;
            }

            if (!_equalsEx(compositePart1, compositePart2)) {
                return false;
            }
        }
        return true;
    };

    return DynamicEntityKey;
};

/**
 * 'EntityType' creator. Dependencies: 'EntityTypeProp'.
 */
var _createEntityTypePrototype = function (EntityTypeProp) {
    ////////////////////////////////////////// THE PROTOTYPE FOR EntityType ////////////////////////////////////////// 
    var EntityType = function (rawObject) {
        Object.call(this);
        // copy all properties from rawObject after deserialisation
        for (var prop in rawObject) {
            if (prop === "_props") {
                var _props = rawObject[prop];

                for (var p in _props) {
                    if (_props.hasOwnProperty(p)) {
                        var val = _props[p];
                        _props[p] = new EntityTypeProp(p, val);
                        if (_props[p].isEntityTypeCarrier() && !this["#entityTypeCarrirerPropName#"]) {
                            this["#entityTypeCarrirerPropName#"] = p;
                        }
                    }
                }

                this[prop] = _props;
            } else {
                this[prop] = rawObject[prop];
            }
        }
    };
    EntityType.prototype = Object.create(Object.prototype);
    EntityType.prototype.constructor = EntityType;

    /**
     * Returns entity type carrier property name if exists. Otherwise returns null
     *
     */
    EntityType.prototype.entityTypeCarrierName = function () {
        return this["#entityTypeCarrirerPropName#"] || null;
    }

    /**
     * Returns full Java class name for the entity type.
     *
     */
    EntityType.prototype.fullClassName = function () {
        return this.key;
    }

    /**
     * Returns full not enhanced Java class name for the entity type.
     */
    EntityType.prototype.notEnhancedFullClassName = function () {
        const fullClassName = this.fullClassName();
        const enhancedIndex = fullClassName.indexOf("$$TgEntity");
        if (enhancedIndex >= 0) {
            return fullClassName.substring(0, enhancedIndex);
        }
        return fullClassName;
    }

    /**
     * Returns simple Java class name for the entity type.
     *
     */
    EntityType.prototype._simpleClassName = function () {
        return _simpleClassName(this.fullClassName());
    }

    /**
     * Returns not enhanced simple Java class name for the entity type.
     *
     */
    EntityType.prototype._notEnhancedSimpleClassName = function () {
        var ind = this.fullClassName().lastIndexOf(".") + 1,
            simpleClassName = this.fullClassName().substring(ind),
            enhancedIndex = simpleClassName.indexOf("$$TgEntity");
        if (enhancedIndex >= 0) {
            return simpleClassName.substring(0, enhancedIndex);
        }
        return simpleClassName;
    }

    /**
     * Returns 'true' when the entity type represents composite entity, 'false' otherwise.
     *
     * Use compositeKeyNames() function to determine property names for the key members.
     */
    EntityType.prototype.isCompositeEntity = function () {
        return typeof this._compositeKeyNames !== 'undefined' && this._compositeKeyNames && this._compositeKeyNames.length > 0;
    }

    /**
     * Returns 'true' if the entity type represents union entity type, 'false' otherwise.
     *
     */
    EntityType.prototype.isUnionEntity = function () {
        return typeof this['_unionCommonProps'] !== 'undefined' && typeof this['_unionProps'] !== 'undefined';
    }

    /** 
     * Returns the property names for common properties (can be empty) in case of union entity; not defined otherwise.
     */
    EntityType.prototype.unionCommonProps = function () {
        return this._unionCommonProps;
    }

    /** 
     * Returns the property names for union properties in case of union entity; not defined otherwise.
     */
     EntityType.prototype.unionProps = function () {
        return this._unionProps;
    }

    /**
     * Returns full class name for main persistent type for this compound master opener (if it is of such kind, empty otherwise).
     */
    EntityType.prototype.compoundOpenerType = function () {
        return typeof this['_compoundOpenerType'] === 'undefined' ? null : this['_compoundOpenerType'];
    }

    /**
     * Returns full class name for base type for this synthetic-based-on-persistent / single-entity-key type (if it is of such kind, the full class name of the type itself otherwise).
     */
    EntityType.prototype.baseType = function () {
        return typeof this['_baseType'] === 'undefined' ? this.fullClassName() : this['_baseType'];
    }

    /**
     * Returns 'true' if the entity type represents menu item entity in compound master.
     *
     */
    EntityType.prototype.isCompoundMenuItem = function () {
        return typeof this['_compoundMenuItem'] === 'undefined' ? false : this['_compoundMenuItem'];
    }

    /** 
     * Returns the property names for the key members in case of composite entity, 'undefined' otherwise.
     */
    EntityType.prototype.compositeKeyNames = function () {
        return this._compositeKeyNames;
    }

    /** 
     * Returns the key member separator in case of composite entity, 'undefined' otherwise.
     */
    EntityType.prototype.compositeKeySeparator = function () {
        return typeof this._compositeKeySeparator === 'undefined' ? " " : this._compositeKeySeparator;
    }

    /**
     * Returns key title in case of composite / union entity type, 'undefined' otherwise.
     */
    EntityType.prototype.keyTitle = function () {
        return this._keyTitle;
    }

    /**
     * Returns key description in case of composite / union entity type, 'undefined' otherwise.
     */
    EntityType.prototype.keyDesc = function () {
        return this._keyDesc;
    }

    /**
     * Returns 'true' if the entity type represents a persistent entity.
     *
     */
    EntityType.prototype.isPersistent = function () {
        return typeof this['_persistent'] === 'undefined' ? false : this['_persistent'];
    }

    /**
     * Returns 'true' if the entity type represents a persistent entity and contains versioning information like created/updated, version, etc.
     *
     */
    EntityType.prototype.isPersistentWithAuditData = function () {
        return typeof this['_persistentWithAudit'] === 'undefined' ? false : this['_persistentWithAudit'];
    }

    /**
     * Returns 'true' if the entity type represents a continuation entity.
     *
     */
    EntityType.prototype.isContinuation = function () {
        return typeof this['_continuation'] === 'undefined' ? false : this['_continuation'];
    }

    /**
     * Returns 'true' if editors for this entity type should display description of its values when not focused, 'false' otherwise.
     *
     */
    EntityType.prototype.shouldDisplayDescription = function () {
        return typeof this['_displayDesc'] === 'undefined' ? false : this['_displayDesc'];
    }

    /** 
     * Returns entity title.
     */
    EntityType.prototype.entityTitle = function () {
        return typeof this._entityTitle === 'undefined' ? this._entityTitleDefault() : this._entityTitle;
    }

    /** 
     * Finds EntityTypeProp meta-info instance from specified 'name'. Returns 'null' for property '' meaning there is no EntityTypeProp for "entity itself".
     * 
     * @param name -- property name; can be dot-notated
     */
    EntityType.prototype.prop = function (name) {
        const dotIndex = name.indexOf('.');
        if (dotIndex > -1) {
            const first = name.slice(0, dotIndex);
            const rest = name.slice(dotIndex + 1);
            return this.prop(first).type().prop(rest);
        } else {
            const prop = typeof this._props !== 'undefined' && this._props && this._props[name];
            if (!prop && name === 'key') {
                // Composite / union entity type serialisation excludes KEY property in case if it is of composite / union nature.
                // See `Finder.streamRealProperties` and `EntitySerialiser.createCachedProperties` methods.
                // That's why we provide "virtual" implementation for EntityTypeProp here.
                if (this.isCompositeEntity()) {
                    return {
                        type: () => 'DynamicEntityKey',
                        title: this.keyTitle.bind(this),
                        desc: this.keyDesc.bind(this)
                    };
                } else if (this.isUnionEntity()) { // the key type for union entities at the Java level is "String", but for JS its actual type is determined at runtime base on the active property
                    return {
                        type: () => 'String',
                        title: this.keyTitle.bind(this),
                        desc: this.keyDesc.bind(this)
                    }
                }
            } else if (!prop && name === 'desc' && this.isUnionEntity()) { // the 'desc' type for union entities always return "String", even if there is no @DescTitle annotation on union type
                return { type: function () { return 'String'; } }
            }
            return prop ? prop : null;
        }
    }

    /** 
     * Returns an array of EntityTypeProp objects for this EntityType instance.
     */
    EntityType.prototype.props = function () {
        return typeof this._props !== 'undefined' && this._props ? Object.values(this._props) : [];
    }

    /** 
     * Returns entity description.
     */
    EntityType.prototype.entityDesc = function () {
        return typeof this._entityDesc === 'undefined' ? this._entityDescDefault() : this._entityDesc;
    }

    /** 
     * Returns default entity title.
     */
    EntityType.prototype._entityTitleDefault = function () {
        var title = this._breakToWords(this._simpleClassName());
        return title;
    }

    /** 
     * Returns default entity desc.
     */
    EntityType.prototype._entityDescDefault = function () {
        var title = this._breakToWords(this._simpleClassName());
        return title + " entity";
    }

    /** 
     * Breaks camelCased string onto words and separates it with ' '.
     */
    EntityType.prototype._breakToWords = function (str) { // see http://stackoverflow.com/questions/10425287/convert-string-to-camelcase-with-regular-expression
        return str.replace(/([A-Z])/g, function (match, group) {
            return " " + group;
        }).trim();
    }

    /**
     * Returns associated entity master informational object for this entity type.
     * Returns 'null' if there is no entity master for this type.
     */
    EntityType.prototype.entityMaster = function () {
        return _typeTable[this.notEnhancedFullClassName()]._entityMaster;
    }
    
    /**
     * Returns associated entity master for new entity informational object for this entity type.
     * Returns 'null' if there is no entity master for this type.
     */
    EntityType.prototype.newEntityMaster = function () {
        return _typeTable[this.notEnhancedFullClassName()]._newEntityMaster;
    }

    return EntityType;
};

//////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////// EXCEPTIONS /////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////

/**
 * 'StrictProxyException' creator.
 */
var _createStrictProxyExceptionPrototype = function () {
    /**
   * Exception prototype for strict proxy exceptions.
   */
    var StrictProxyException = function (propName, simpleClassName, isIdOnlyProxy) { // rawObject
        Object.call(this);

        this.message = "Strict proxy exception: property [" + propName + "] " + (isIdOnlyProxy === true ? "is id-only proxy" : "does not exist") + " in the entity of type [" + simpleClassName + "]. Please, check the fetch strategy or construction strategy of the entity object.";
    };
    StrictProxyException.prototype = Object.create(Object.prototype);
    StrictProxyException.prototype.constructor = StrictProxyException;

    /**
     * Overridden toString method to represent this exception more meaningfully than '[Object object]'.
     *
     */
    StrictProxyException.prototype.toString = function () {
        return this.message;
    }
    return StrictProxyException;
}

/**
 * 'UnsupportedConversionException' creator.
 */
var _createUnsupportedConversionExceptionPrototype = function () {
    /**
     * Exception prototype for unsupported conversions.
     */
    var UnsupportedConversionException = function (value) {
        Object.call(this);

        this.message = "Unsupported conversion exception: the conversion for value [" + value + "] is unsupported at this stage. Value typeof === " + (typeof value) + ".";
    };
    UnsupportedConversionException.prototype = Object.create(Object.prototype);
    UnsupportedConversionException.prototype.constructor = UnsupportedConversionException;

    /**
     * Overridden toString method to represent this exception more meaningfully than '[Object object]'.
     *
     */
    UnsupportedConversionException.prototype.toString = function () {
        return this.message;
    }
    return UnsupportedConversionException;
}

//////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// EXCEPTIONS [END] /////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////

const _SPEPrototype = _createStrictProxyExceptionPrototype();
const _UCEPrototype = _createUnsupportedConversionExceptionPrototype();

const _ETPPrototype = _createEntityTypePropPrototype();
const _ETPrototype = _createEntityTypePrototype(_ETPPrototype);
const _DEKPrototype = _createDynamicEntityKeyPrototype();

/**
 * The prototype for entity's instance-scoped property. Can be reached by 'entity.prop("name")' call.
 */
const _EIPPrototype = _createEntityInstancePropPrototype();
/**
 * The prototype for entities.
 */
const _EPrototype = _createEntityPrototype(_EIPPrototype, _SPEPrototype, _isError0, _isWarning0, _DEKPrototype);

/**
 * Determines whether the specified object represents the entity.
 */
export const _isEntity = function (obj) {
    return obj !== null && (obj instanceof _EPrototype);
};

/**
 * Determines whether the specified object represents dynamic entity key.
 */
const _isDynamicEntityKey = function (obj) {
    return obj && (obj instanceof _DEKPrototype);
};

const _isPropertyValueObject = function (value, subValueName) {
    return typeof value === 'object' && typeof value[subValueName] !== 'undefined';
};

/**
 * Checks whether non-null 'value' represents money value.
 */
const _isMoney = function (value) {
    return _isPropertyValueObject(value, 'amount');
};
const _moneyVal = function (value) {
    return value['amount'];
};
/**
 * Checks whether non-null 'value' represents rich text value.
 */
const _isRichText = function (value) {
    return _isPropertyValueObject(value, 'formattedText');
};
const _richTextVal = function (value) {
    return value['formattedText'];
};

/**
 * Checks whether non-null 'value' represents colour value.
 */
const _isColour = function (value) {
    return _isPropertyValueObject(value, 'hashlessUppercasedColourValue');
};
const _colourVal = function (value) {
    return value['hashlessUppercasedColourValue'];
};
/**
 * Checks whether non-null 'value' represents hyperlink value.
 */
const _isHyperlink = function (value) {
    return _isPropertyValueObject(value, 'value');
};
const _hyperlinkVal = function (value) {
    return value['value'];
};

/**
 * Returns 'true' if the regular values are equal, 'false' otherwise.
 */
const _equalsEx = function (value1, value2) {
    // if (value1) {
    //     if (!value2) {
    //         return false;
    //     } else {
    //         return ...
    //     }
    // }

    // TODO 1: rectify whether this implementation is good
    // TODO 2: potentially, extend implementation to support composite types: objects?
    if (_isDynamicEntityKey(value1)) {
        return value1._dynamicEntityKeyEqualsTo(value2);
    } else if (_isEntity(value1)) {
        return _entitiesEqualsEx(value1, value2, false);
    } else if (Array.isArray(value1)) {
        return _arraysEqualsEx(value1, value2);
    } else if (value1 !== null && _isMoney(value1)) {
        return value2 !== null && _isMoney(value2) && _equalsEx(_moneyVal(value1), _moneyVal(value2));
    } else if (value1 !== null && _isColour(value1)) {
        return value2 !== null && _isColour(value2) && _equalsEx(_colourVal(value1), _colourVal(value2));
    } else if (value1 !== null && _isHyperlink(value1)) {
        return value2 !== null && _isHyperlink(value2) && _equalsEx(_hyperlinkVal(value1), _hyperlinkVal(value2));
    } else if (value1 !== null && _isRichText(value1)) {
        return value2 !== null && _isRichText(value2) && _equalsEx(_richTextVal(value1), _richTextVal(value2));
    }
    return value1 === value2;
};

/**
 * Returns 'true' if arrays are equal, 'false' otherwise.
 */
var _arraysEqualsEx = function (array1, array2) {
    if (array1 === array2) {
        return true;
    }
    if (!Array.isArray(array2)) {
        return false;
    }

    if (array1.length !== array2.length) {
        return false;
    }

    // now can compare items
    for (var index = 0; index < array1.length; index++) {
        if (!_equalsEx(array1[index], array2[index])) {
            return false;
        }
    }
    return true;
};

const _type = entity => entity.constructor.prototype.type.call(entity);

/**
 * Returns 'true' if the entities are equal, 'false' otherwise.
 *
 * IMPORTANT: this is the mirror of the java methods AbstractEntity.equals() and DynamicEntityKey.compareTo(). So, please be carefull and maintain it
 * in accordance with java counterparts.
 */
const _entitiesEqualsEx = function (entity1, entity2, inHierarchy) {
    if (entity1 === entity2) {
        return true;
    }
    if (!_isEntity(entity2)) {
        return false;
    }
    // Let's ensure that types match.
    const entity1Type = _type(entity1);
    const entity2Type = _type(entity2);
    // In most cases, two entities of the same type will be compared -- their types will be equal by reference.
    // However, generated types re-register on each centre run / refresh
    // This is why we need to compare their base types (this also covers the case where multiple server nodes are used and different nodes generate different types from the same base type).
    if (entity1Type !== entity2Type &&
        entity1Type.notEnhancedFullClassName() !== entity2Type.notEnhancedFullClassName() &&
        (inHierarchy === false || _typeTable[entity1Type.notEnhancedFullClassName()].baseType() !== _typeTable[entity2Type.notEnhancedFullClassName()].baseType()))
    {
        return false;
    }
    // Now can compare key values.
    let key1, key2;

    try {
        key1 = entity1.get('key');
    } catch (strictProxyEx1) {
        throw 'Comparison of entities [' + entity1 + ', ' + entity2 + '] failed. Property \'key\' was not fetched in first entity, please check fetching strategy.' + strictProxyEx1;
    }

    try {
        key2 = entity2.get('key');
    } catch (strictProxyEx2) {
        throw 'Comparison of entities [' + entity1 + ', ' + entity2 + '] failed. Property \'key\' was not fetched in second entity, please check fetching strategy.' + strictProxyEx2;
    }
    return _equalsEx(key1, key2);
};

/**
 * Converts the property value, that has got from deserialised entity instance, to the form, that is suitable for editors binding.
 */
const _convert = function (value) {
    if (value === null) { // 'null' is the missing value representation for TG web editors
        return null;
    } else if (value instanceof _DEKPrototype) {
        return value._convertDynamicEntityKey();
    } else if (value instanceof _EPrototype) {
        return value.toString();
    } else if (typeof value === 'number') { // for number value -- return the same value for editors (includes date, integer, decimal number editors)
        return value;
    } else if (typeof value === 'boolean') { // for boolean value -- return the same value for editors
        return value;
    } else if (typeof value === 'object' && value.hasOwnProperty('amount') && value.hasOwnProperty('currency') && value.hasOwnProperty('taxPercent')) { // for money related value -- return the same value for editors
        return value;
    } else if (typeof value === 'string') { // for string value -- return the same value for editors
        return value;
    } else if (Array.isArray(value)) { // for Array value -- return the same value for tg-entity-editor and tg-collectional-representor
        return value.slice(); // binding value must return a new shallow array copy to get distinct instances for _currBindingEntity and _originalBindingEntity; this is because binding value may be altered directly inside some editors (e.g. tg-collectional-editor)
    } else if (typeof value === 'object' && (value.hasOwnProperty('hashlessUppercasedColourValue') || value.hasOwnProperty('value'))) {
        return value;
    } else if (typeof value === 'object' && Object.getOwnPropertyNames(value).length === 0) {
        // Some functional actions have properties with type Map<String, ...>.
        // Initial value of such properties, retrieved from server, is always {}.
        // That's because they are processed manually on client and only gets back to server with some data.
        // Here we allows such processing instead of throwing 'unsupported type' exception.
        return value;
    } else if (typeof value === 'object' && value.hasOwnProperty('coreText') && value.hasOwnProperty('formattedText')) { // for rich text type
        return value;
    } else {
        throw new _UCEPrototype(value);
    }
};

/**
 * Converts property's 'fullValue' into binding entity value representation. Takes care of id-based value conversion for entity-typed properties. Also takes care about entity's description.
 */
const _convertFullPropertyValue = function (bindingView, propertyName, fullValue) {
    bindingView[propertyName] = _convert(fullValue);
    if (_isEntity(fullValue)) {
        if (fullValue.get('id') !== null) {
            bindingView['@' + propertyName + '_id'] = fullValue.get('id');
        }
        if (fullValue.type().isUnionEntity()) {
            bindingView[`@${propertyName}_activeProperty`] = fullValue._activeProperty(); 
        }
        try {
            const desc = fullValue.get('desc');
            bindingView['@' + propertyName + '_desc'] = _convert(desc);
        } catch (strictProxyError) {
            console.warn('Extend fetch provider to see description in tooltip of entity-typed value. Original error: [' + strictProxyError + '].');
        }
    }
};

/**
 * Determines the type of property from 'entityType' and 'property'.
 * 
 * Returns 'DynamicEntityKey' for 'key' property of composite 'entityType'.
 * 
 * @param entityType -- entity type
 * @param property -- property name; can be dot-notated or '' meaning "entity itself"
 */
const _determinePropertyType = function (entityType, property) {
    return '' === property ? entityType : entityType.prop(property).type();
};

/**
 * Converts property value, converted to editor binding representation ('bindingValue'), to string.
 * 
 * @param bindingValue -- binding representation of property value; for entity-typed property it is string; for array it is shallow array copy; for all other values -- it is the same value
 * @param rootEntityType -- the type of entity holding this property
 * @param property -- property name of the property; can be dot-notated or '' meaning "entity itself"
 */
const _toString = function (bindingValue, rootEntityType, property) {
    const propertyType = _determinePropertyType(rootEntityType, property);
    if (propertyType === 'boolean') {
        return bindingValue === null ? 'false' : '' + bindingValue;
    } else if (bindingValue === null) {
        return '';
    } else if (typeof bindingValue === 'string') {
        return bindingValue; // this covers converted entity-typed properties, DynamicEntityKey instances and string properties -- no further conversion required
    } else if (typeof bindingValue === 'number') {
        if (propertyType === 'Date') {
            const prop = rootEntityType.prop(property);
            return _millisDateRepresentation(bindingValue, prop.timeZone(), prop.datePortion());
        } else {
            return '' + bindingValue; // Integer value (or Long, but very rare) and BigDecimal value
        }
    } else if (_isMoney(bindingValue)) {
        return '' + _moneyVal(bindingValue); // represents number, so needs "'' +" conversion prefix
    } else if (Array.isArray(bindingValue)) {
        // Here we have standard logic of converting collections using the most common ', ' separator.
        // To apply custom separator please use _toStringForCollection method (see tg-entity-editor.convertToString).
        return _toStringForCollection(bindingValue, rootEntityType, property, STANDARD_COLLECTION_SEPARATOR);
    } else if (_isColour(bindingValue)) {
        return _colourVal(bindingValue); // represents string -- no conversion required
    } else if (_isHyperlink(bindingValue)) {
        return _hyperlinkVal(bindingValue); // represents string -- no conversion required
    } else if (_isRichText(bindingValue)) {
        return _richTextVal(bindingValue); // represents string -- no conversion required
    } else if (typeof bindingValue === 'object' && Object.getOwnPropertyNames(bindingValue).length === 0) {
        // See method _convert that explains the use case of the properties with {} value.
        // We provide ability to even convert binding representation of these properties to string, which is, naturally, ''.
        // This is done to provide consistency between supported types of properties in both _convert and _toString functions.
        // The main case, however, does not require _toString support -- this is because such properties do not bind to any existing editor.
        return '';
    } else {
        throw new _UCEPrototype(bindingValue);
    }
};

/**
 * Converts property value, converted to editor binding representation ('bindingValue'), to string for display purposes.
 * 
 * @param bindingValue -- binding representation of property value; for entity-typed property it is string; for array it is shallow array copy; for all other values -- it is the same value
 * @param rootEntityType -- the type of entity holding this property
 * @param property -- property name of the property; can be dot-notated or '' meaning "entity itself"
 * @param locale -- optional; application-wide server-driven locale; this is to be used for number properties conversion (BigDecimal, Integer / Long, Money) and can be omitted for other types
 */
const _toStringForDisplay = function (bindingValue, rootEntityType, property, locale) {
    const propertyType = _determinePropertyType(rootEntityType, property);
    const prop = rootEntityType.prop(property);
    // for all numeric types and Colour we have non-standard display formatting; all other types will be displayed the same fashion as it is in standard conversion
    if (propertyType === 'Colour') {
        return bindingValue === null ? '' : '#' + _toString(bindingValue, rootEntityType, property);
    } else if (propertyType === 'BigDecimal') {
        return _formatDecimal(bindingValue, locale, prop.scale(), prop.trailingZeros());
    } else if (propertyType === 'Integer' || propertyType === 'Long') {
        return _formatInteger(bindingValue, locale);
    } else if (propertyType === 'Money') {
        return _formatMoney(bindingValue, locale, prop.scale(), prop.trailingZeros());
    } else {
        return _toString(bindingValue, rootEntityType, property);
    }
};

/**
 * Converts collectional property value, converted to editor binding representation ('bindingValue'), to string.
 * 
 * @param bindingValue -- binding representation of property value that is the same as fully-fledged value; this can be null or array of entities or numbers or strings etc.
 * @param rootEntityType -- the type of entity holding this property
 * @param property -- property name of the property; can be dot-notated
 * @param separator -- string value to glue string representations of values with
 * @param mappingFunction -- maps resulting elements before actual element-by-element toString conversion and glueing them all together; this is optional
 */
const _toStringForCollection = function (bindingValue, rootEntityType, property, separator, mappingFunction) {
    if (bindingValue === null || bindingValue.length === 0) {
        return '';
    } else {
        let resultingCollection = bindingValue;
        const entityTypeProp = rootEntityType.prop(property);
        const shortCollectionKey = entityTypeProp.shortCollectionKey();
        if (shortCollectionKey) { // existence of shortCollectionKey indicates that the property is indeed "short collection"
            resultingCollection = bindingValue.map(entity => entity ? entity.get(shortCollectionKey) : entity);
        }
        return resultingCollection
            .map(element => _toString(_convert(mappingFunction ? mappingFunction(element) : element), rootEntityType, property)) // note that collection of 'boolean'/'Date' values are not [yet] supported due to non-existence of collection element type on the client EntityTypeProp for collectional property (see _toString method); this looks like artificial collections to be supported; however they can be, if needed 
            .filter(str => str !== '') // filter out empty strings not to include them into resulting string (especially important for functions that use 'mappingFunction')
            .join(separator);
    }
};

/**
 * Converts composite entity's keyNamesAndValues to string.
 * 
 * @param keyNamesAndValues -- non-empty array of elements (also arrays) consisting on [0] index of composite key property name and on [1] index of actual value of that composite key
 * @param entityType -- the type of composite entity
 * @param separator -- string value to glue string representations of values with
 * @param mappingFunction -- maps resulting elements before actual element-by-element toString conversion and glueing them all together; this is optional
 */
const _toStringForKeys = function (keyNamesAndValues, entityType, separator, mappingFunction) {
    return keyNamesAndValues
        .map(keyNameAndValue => _toString(_convert(mappingFunction ? mappingFunction(keyNameAndValue[1]) : keyNameAndValue[1]), entityType, keyNameAndValue[0]))
        .filter(str => str !== '') // filter out empty strings not to include them into resulting string (especially important for functions that use 'mappingFunction')
        .join(separator);
};

/**
 * Converts collectional property value, converted to editor binding representation ('bindingValue'), to tooltip's string representation.
 * 
 * @param bindingValue -- binding representation of property value that is the same as fully-fledged value; this can be null or array of entities or numbers or strings etc.
 * @param rootEntityType -- the type of entity holding this property
 * @param property -- property name of the property; can be dot-notated
 */
const _toStringForCollectionAsTooltip = function (bindingValue, rootEntityType, property) {
    const convertedCollection = _toString(bindingValue, rootEntityType, property);
    if (convertedCollection === '') {
        return '';
    }
    const desc = _toStringForCollection(bindingValue, rootEntityType, property, STANDARD_COLLECTION_SEPARATOR, entity => entity ? entity.get('desc') : entity); // maps entity descriptions; this includes descs from short collection sub-keys
    return '<b>' + convertedCollection + '</b>' + (desc !== '' ? '<br>' + desc : '');
};

/**
 * Formats integer number in to string based on locale. If the value is null then returns empty string.
 */
const _formatInteger = function (value, locale) {
    if (value !== null) {
        return value.toLocaleString(locale);
    }
    return '';
};

/**
 * Formats number with floating point in to string based on locale. If the value is null then returns empty string.
 */
const _formatDecimal = function (value, locale, scale, trailingZeros) {
    if (value !== null) {
        const definedScale = typeof scale === 'undefined' || scale === null || scale < 0 || scale > 20 /* 0 and 20 are allowed bounds for scale*/ ? DEFAULT_SCALE : scale;
        const options = { maximumFractionDigits: definedScale };
        if (trailingZeros !== false) {
            options.minimumFractionDigits = definedScale;
        }
        return value.toLocaleString(locale, options);
    }
    return '';
};

const _getCurrencySymbol = function() {
    return currencySymbol || '$';
}

/**
 * Formats money number in to string based on locale. If the value is null then returns empty string.
 */
const _formatMoney = function (value, locale, scale, trailingZeros) {
    if (value !== null) {
        const strValue = _formatDecimal(Math.abs(value.amount), locale, scale, trailingZeros);
        return (value.amount < 0 ? `-${_getCurrencySymbol()}` : `${_getCurrencySymbol()}`) + CURRENCY_SYMBOL_SPACE + strValue;
    }
    return '';
};

/**
 * Completes the process of type table preparation -- creates instances of EntityType objects for each entity type in type table.
 */
var _providePrototypes = function (typeTable, EntityType) {
    for (var key in typeTable) {
        if (typeTable.hasOwnProperty(key)) {
            var entityType = typeTable[key];
            typeTable[key] = new EntityType(entityType);
            // entityType.prototype = EntityType.prototype;
            // entityType.__proto__ = EntityType.prototype;
        }
    }
    console.log("typeTable =", typeTable);
    return typeTable;
};

/**
 * The table for entity types.
 *
 * NOTE: 'typeTable' part will be generated by server to provide current state for the entity types table.
 */
var _typeTable = _providePrototypes(@typeTable, _ETPrototype);

export const TgReflector = Polymer({
    is: 'tg-reflector',

    getType: function (typeName) {
        return _typeTable[typeName];
    },

    /**
     * Registers new entity type inside the type table.
     */
    registerEntityType: function (newType) {
        var EntityType = this._getEntityTypePrototype();
        var registeredType = new EntityType(newType);
        _typeTable[registeredType.fullClassName()] = registeredType;
        console.log("Registering new entity type ", registeredType);
        return registeredType;
    },

    /**
     * Returns the entity type property instance for specified entity and dot notation property name.
     */
    getEntityTypeProp: function (entity, dotNotatedProp) {
        const lastDotIndex = dotNotatedProp.lastIndexOf(".");
        const rest = lastDotIndex > -1 ? dotNotatedProp.slice(lastDotIndex + 1) : dotNotatedProp;
        const firstVal = lastDotIndex > -1 ? entity.get(dotNotatedProp.slice(0, lastDotIndex)) : entity;
        return firstVal && rest.length > 0 ? firstVal.constructor.prototype.type.call(firstVal).prop(rest) || undefined : undefined;
    },

    /**
     * Finds the entity type by its full class name. Returns 'null' if no registered entity type for such 'typeName' exists.
     */
    findTypeByName: function (typeName) {
        var type = _typeTable[typeName];
        return type ? type : null;
    },

    /**
     * Returns the prototype for 'EntityInstanceProp'.
     *
     * Please, do not use it directly, only, perhaps, in tests.
     */
    getEntityInstancePropPrototype: function () {
        return _EIPPrototype;
    },

    /**
     * Returns the prototype for 'Entity'.
     *
     * Please, do not use it directly, only, perhaps, in tests.
     */
    getEntityPrototype: function () {
        return _EPrototype;
    },

    /**
     * Returns the prototype for 'EntityType'.
     *
     * Please, do not use it directly, only, perhaps, in tests.
     */
    _getEntityTypePrototype: function () {
        return _ETPrototype;
    },

    /**
     * Returns the prototype for 'DynamicEntityKey'.
     *
     * Please, do not use it directly, only, perhaps, in tests.
     */
    getDynamicEntityKeyPrototype: function () {
        return _DEKPrototype;
    },

    /**
     * Returns the prototype for 'StrictProxyException'.
     *
     * Please, do not use it directly, only, perhaps, in tests.
     */
    getStrictProxyExceptionPrototype: function () {
        return _SPEPrototype;
    },

    /**
     * Returns 'true' if the regular values are equal, 'false' otherwise.
     */
    equalsEx: function (value1, value2) {
        return _equalsEx(value1, value2);
    },

    /**
     * Returns 'true' if the entity values are equal disregarding type hierarchy (synthetic-based-on-persistent / single-entity-key), 'false' otherwise.
     */
    equalsExInHierarchy: function (entity1, entity2) {
        return _entitiesEqualsEx(entity1, entity2, true);
    },

    /**
     * Determines whether result represents the error.
     */
    isError: function (result) {
        return _isError0(result);
    },

    /**
     * Determines whether result represents an error that indicates continuation.
     */
    isContinuationError: function (result) {
        return _isContinuationError0(result);
    },

    /**
     * Determines whether result represents the warning.
     */
    isWarning: function (result) {
        return _isWarning0(result);
    },

    /**
     * Determines whether result is informative.
     */
    isInformative: function (result) {
        return _isInformative0(result);
    },

    //////////////////// SERVER EXCEPTIONS UTILS ////////////////////

    /**
     * Returns html representation for the specified exception trace (including 'cause' expanded, if any).
     */
    stackTrace: function (ex) {
        // collects error cause by traversing the stack into an ordered list
        const causeCollector = function (ex, causes) {
            if (ex) {
                causes = causes + "<li>" + resultMessages(ex).extended + "</li>";
                printStackTrace(ex);
                if (ex.cause !== null) {
                    causes = causeCollector(ex.cause, causes);
                }
            }
            return causes + "</ol>";
        };

        // outputs the exception stack trace into the console as warning
        const printStackTrace = function (ex) {
            let msg = "No cause and stack trace.";
            if (ex) {
                msg = resultMessages(ex).short + '\n';
                if (Array.isArray(ex.stackTrace)) {
                    for (let i = 0; i < ex.stackTrace.length; i += 1) {
                        const st = ex.stackTrace[i];
                        msg = msg + st.className + '.java:' + st.lineNumber + ':' + st.methodName + ';\n';
                    }
                }
            }
            console.warn(msg);
        };

        if (ex) {
            let causes = "<b>" + resultMessages(ex).extended + "</b>";
            printStackTrace(ex);
            // Result 'cause' is not serialised and that's why it is 'undefined'.
            // Allow proper stack traces (empty) for such Results (e.g. see NeedMoreDataException).
            if (ex.cause) {
                causes = causeCollector(ex.cause, causes + "<br><br>Cause(s):<br><ol>")
            }
            return causes;
        }
        return '';
    },

    //////////////////// SERVER EXCEPTIONS UTILS [END] //////////////

    /**
     * Determines whether the specified object represents the entity.
     */
    isEntity: function (obj) {
        return _isEntity(obj);
    },

    /**
     * Creates the 'entity' without concrete type specified.
     */
    newEntityEmpty: function () {
        var Entity = this.getEntityPrototype();
        return new Entity();
    },

    /**
     * Creates the 'entity instance prop'.
     */
    newEntityInstancePropEmpty: function () {
        var EntityInstanceProp = this.getEntityInstancePropPrototype();
        return new EntityInstanceProp();
    },

    /**
     * Creates the 'entity' with concrete type, specified as 'typeName' string.
     */
    newEntity: function (typeName) {
        var newOne = this.newEntityEmpty();

        newOne["_type"] = this.findTypeByName(typeName);
        newOne["id"] = null;
        newOne["version"] = 0;
        return newOne;
    },

    /**
     * Converts the property value, that has got from deserialised entity instance, to the form, that is suitable for editors binding.
     */
    tg_convert: function (value) {
        return _convert(value);
    },
    
    /**
     * Determines the type of property from 'entityType' and 'property'.
     * 
     * Returns 'DynamicEntityKey' for 'key' property of composite 'entityType'.
     * 
     * @param entityType -- entity type
     * @param property -- property name; can be dot-notated or '' meaning "entity itself"
     */
    tg_determinePropertyType: function (entityType, property) {
        return _determinePropertyType(entityType, property);
    },
    
    /**
     * Converts property value to string.
     * 
     * Terms:
     * binding value -- binding representation of fully-fledged property value; for entity-typed property it is string; for array it is shallow array copy; for all other values -- it is the same value.
     * 
     * @param value -- the value of property; can be fully-fledged (by default) or binding value (if opts.bindingValue === true)
     * @param rootEntityType -- the type of entity holding this property
     * @param property -- property name of the property; can be dot-notated
     * @param opts.bindingValue -- if true then 'value' represents binding value representation, otherwise -- fully-fledged value
     * 
     * @param opts.display -- if true then 'value' will be converted to "display" string representation, aka #F1F1F1 for colour or 10,000.50 and $37.7878 for numeric props, otherwise -- to editing value representation (F1F1F1 or 10000.5 and 37.7878)
     * @param   opts.locale -- optional; works only for opts.display === true; application-wide server-driven locale; this is to be used for number properties conversion (BigDecimal, Integer / Long, Money) and can be omitted for other types
     *    otherwise
     * @param opts.collection -- true if 'value' represents collection, false otherwise; this is to be used with custom parameters for collection conversion:
     * @param   opts.asTooltip -- if true then collection of entities will be converted to standard tooltip representation
     *          or
     * @param   opts.separator -- string value to glue string representations of collectional values with; ', ' by default
     * @param   opts.mappingFunction -- maps resulting collectional elements before actual element-by-element toString conversion and glueing them all together; optional
     *    otherwise
     *        standard toString conversion
     */
    tg_toString: function (value, rootEntityType, property, opts) {
        const isBindingValue = opts && opts.bindingValue;
        if (!isBindingValue) {
            return this.tg_toString(_convert(value), rootEntityType, property, Object.assign({}, opts, { bindingValue: true })); // copy opts with bindingValue assigned as true
        } else {
            if (opts && opts.display) {
                return _toStringForDisplay(value, rootEntityType, property, opts.locale);
            } else if (opts && opts.collection) {
                if (opts.asTooltip) {
                    return _toStringForCollectionAsTooltip(value, rootEntityType, property);
                } else {
                    return _toStringForCollection(value, rootEntityType, property, opts.separator || STANDARD_COLLECTION_SEPARATOR, opts.mappingFunction);
                }
            } else {
                return _toString(value, rootEntityType, property);
            }
        }
    },
    
    /**
     * Returns indication whether 'obj' represents 'mock not found entity'. Synced with 'EntityResourceUtils.isMockNotFoundEntity' method.
     */
    isMockNotFoundEntity: function (obj) {
       return this.isEntity(obj) /* obj can be empty and will return false as a result */
            && obj.get('id') === null
            && KEY_NOT_ASSIGNED === obj.toString()
            && obj.get('desc'); // 'desc' is not empty and contains string representaion of 'not found' entity
    },
    
    /**
     * Converts the value of property with 'propertyName' name from fully-fledged entity 'entity' into the 'bindingView' binding entity.
     *
     * This implementation takes care of the aspect of property validity and, in case where the property is invalid, then the values are taken from previously bound property ('previousModifiedPropertiesHolder').
     * This ensures that corresponding editor will show invalid value, that was edited by the user and did not pass server-side validation (fully fledged entity contains previous valid value in this case + validation error).
     */
    tg_convertPropertyValue: function (bindingView, propertyName, entity, previousModifiedPropertiesHolder) {
        if (this.isError(entity.prop(propertyName).validationResult())) {
            if (previousModifiedPropertiesHolder === null) { // is a brand new instance just received from server?
                // bind the received from server property value
                _convertFullPropertyValue(bindingView, propertyName, this._getErroneousFullPropertyValue(entity, propertyName));
            } else { // otherwise, this entity instance has already been received before and should be handled accordingly
                if (typeof previousModifiedPropertiesHolder[propertyName].val === 'undefined') {
                    // EDGE-CASE: if the value becomes invalid not because the action done upon this property -- 
                    //   but because the action on other property -- the previous version of modifiedPropsHolder will not hold
                    //   invalid 'attempted value' -- but originalVal exists and should be used in this case!
                    bindingView[propertyName] = previousModifiedPropertiesHolder[propertyName].origVal;
                    if (typeof previousModifiedPropertiesHolder[propertyName].origValId !== 'undefined') {
                        bindingView['@' + propertyName + '_id'] = previousModifiedPropertiesHolder[propertyName].origValId;
                    }
                } else {
                    bindingView[propertyName] = previousModifiedPropertiesHolder[propertyName].val;
                    if (typeof previousModifiedPropertiesHolder[propertyName].valId !== 'undefined') {
                        bindingView['@' + propertyName + '_id'] = previousModifiedPropertiesHolder[propertyName].valId;
                    }
                }
            }
        } else {
            var fullValue = entity.get(propertyName);
            _convertFullPropertyValue(bindingView, propertyName, fullValue);

            const touchedProps = bindingView['@@touchedProps'];
            const touchedPropIndex = touchedProps.names.indexOf(propertyName);
            // #1992 reset @@touchedProps only for non-compound-master-opener types, because opener's 'key' property needs to remain touched
            // this ensures correct server-side restoration of the opener in cases where its produced 'key' (no id) equals to the saved version of the 'key' (with id)
            if (touchedPropIndex > -1 && !this.equalsEx(bindingView.get(propertyName), touchedProps.values[touchedPropIndex]) && !entity.type().compoundOpenerType()) {
                // make the property untouched in cases where its value was successfully mutated through definer of another property (it means that the value is valid and different to the value originated from the user's touch)
                touchedProps.names.splice(touchedPropIndex, 1);
                touchedProps.counts.splice(touchedPropIndex, 1);
                touchedProps.values.splice(touchedPropIndex, 1);
            }
        }
        if (typeof bindingView[propertyName] === 'undefined' || bindingView[propertyName] === undefined) {
            throw "Illegal value exception: the property [" + propertyName + "] can not be assigned as [" + bindingView[propertyName] + "].";
        }
    },

    /**
     * Converts original value of property with 'propertyName' name from fully-fledged entity 'entity' into the 'originalBindingView' binding entity.
     */
    tg_convertOriginalPropertyValue: function (originalBindingView, propertyName, entity) {
        _convertFullPropertyValue(originalBindingView, propertyName, entity.getOriginal(propertyName));
    },

    /**
     * Returns property's value knowing that it is erroneous. This is to be used for futher conversion into binding value representaion by 'convert' method and its derivatives.
     */
    _getErroneousFullPropertyValue: function (entity, propertyName) {
        const lastInvalidValue = entity.prop(propertyName).lastInvalidValue();
        return this.isMockNotFoundEntity(lastInvalidValue) ? lastInvalidValue.get('desc') : lastInvalidValue;
    },

    /**
     * Formats integer number in to string based on locale. If the value is null then returns empty string.
     */
    tg_formatInteger: function (value, locale) {
        return _formatInteger(value, locale);
    },

    /**
     * Formats number with floating point in to string based on locale. If the value is null then returns empty string.
     */
    tg_formatDecimal: function (value, locale, scale, trailingZeros) {
        return _formatDecimal(value, locale, scale, trailingZeros);
    },

    /**
     * Formats money number in to string based on locale. If the value is null then returns empty string.
     */
    tg_formatMoney: function (value, locale, scale, trailingZeros) {
        return _formatMoney(value, locale, scale, trailingZeros);
    },

    /**
     * Returns the binding value for the specified 'bindingEntity' and 'dotNotatedName' of the property.
     *
     * This supports the retrieval of binding value for dot-notation properties with the use of bindingEntity's '@@origin'.
     */
    tg_getBindingValue: function (bindingEntity, dotNotatedName) {
        return this.isDotNotated(dotNotatedName) ? this.tg_convert(this.tg_getFullValue(bindingEntity, dotNotatedName)) : bindingEntity.get(dotNotatedName);
    },

    /**
     * Returns the binding value for the specified 'fullyFledgedEntity' and 'dotNotatedName' of the property.
     *
     * This supports the retrieval of binding value for dot-notation properties.
     */
    tg_getBindingValueFromFullEntity: function (fullyFledgedEntity, dotNotatedName) {
        return this.tg_convert(fullyFledgedEntity.get(dotNotatedName));
    },

    /**
     * Returns fully-fledged entity from which 'bindingEntity' has been originated.
     */
    tg_getFullEntity: function (bindingEntity) {
        return bindingEntity['@@origin'];
    },

    /**
     * Returns the full value for the specified 'bindingEntity' and 'dotNotatedName' of the property.
     *
     * This method does no conversion of the value to 'binding' representation.
     */
    tg_getFullValue: function (bindingEntity, dotNotatedName) {
        return this.tg_getFullEntity(bindingEntity).get(dotNotatedName);
    },

    /**
     * Convenient method for retrieving of 'customObject' from deserialised array.
     */
    customObject: function (arrayOfEntityAndCustomObject) {
        if (arrayOfEntityAndCustomObject.length >= 2) {
            return arrayOfEntityAndCustomObject[1];
        } else {
            return null;
        }
    },

    /**
     * Set the provided currency symbol if previous was empty and provided one is not empty. It means that currency symbol can be set only once. 
     * 
     * @param {String} newCurrencySymbol - currency symbol to set
     */
    setCurrencySymbol: function (newCurrencySymbol) {
        if (!currencySymbol && newCurrencySymbol) {
            currencySymbol = newCurrencySymbol;
        }
    },

    /**
     * Fills in the centre context holder with 'master entity' based on whether should 'requireMasterEntity'.
     */
    provideMasterEntity: function (requireMasterEntity, centreContextHolder, getMasterEntity) {
        if (requireMasterEntity === "true") {
            centreContextHolder["masterEntity"] = getMasterEntity();
        } else if (requireMasterEntity === "false") { // 'masterEntity' will be proxied after server-side deserialisation
        } else {
            throw "Unknown value for attribute 'requireMasterEntity': " + requireMasterEntity;
        }
    },

    /**
     * Fills in the centre context holder with 'selected entities' based on whether should 'requireSelectedEntities' (ALL, ONE or NONE).
     */
    provideSelectedEntities: function (requireSelectedEntities, centreContextHolder, getSelectedEntities) {
        if (requireSelectedEntities === "ALL") {
            centreContextHolder["selectedEntities"] = getSelectedEntities();
        } else if (requireSelectedEntities === "ONE") {
            centreContextHolder["selectedEntities"] = getSelectedEntities().length > 0 ? [getSelectedEntities()[0]] : [];
        } else if (requireSelectedEntities === "NONE") { // 'selectedEntities' will be proxied after server-side deserialisation
            centreContextHolder["selectedEntities"] = [];
        } else {
            throw "Unknown value for attribute 'requireSelectedEntities': " + requireSelectedEntities;
        }
    },

    /**
     * Returns 'true' if the propertyName is specified in 'dot-notation' syntax, otherwise 'false'.
     */
    isDotNotated: function (propertyName) {
        return propertyName.indexOf(".") > -1;
    },

    /**
     * Returns an entity instance that is the value of the property refered to by 'dotNotatedPropertyName' without the last propert part.
     * Effectively, the returned values if super-property value. 
     * For example, for 'entity.entityValuedProp.someProp' the returned value should an entity referenced by 'entity.entityValuedProp'. 
     */
    entityPropOwner: function (entity, dotNotatedPropertyName) {
        if (this.isDotNotated(dotNotatedPropertyName) === true) {
            const lastDotIndex = dotNotatedPropertyName.lastIndexOf(".");
            const propName = dotNotatedPropertyName.substring(0, lastDotIndex);
            return entity.get(propName);
        }
        return undefined;
    },

    /**
     * Extracts simple class name from full class name (removes package from it).
     */
    simpleClassName: function (fullClassName) {
        return _simpleClassName(fullClassName);
    },

    /**
     * Creates the context holder to be transferred with actions, centre autocompletion process, query enhancing process etc.
     *
     * @param originallyProducedEntity -- in case if new entity is operated on, this instance holds an original fully-fledged contextually produced entity.
     */
    createContextHolder: function (
        requireSelectionCriteria, requireSelectedEntities, requireMasterEntity,
        createModifiedPropertiesHolder, getSelectedEntities, getMasterEntity,
        originallyProducedEntity
    ) {
        var centreContextHolder = this.newEntity("ua.com.fielden.platform.entity.functional.centre.CentreContextHolder");
        centreContextHolder.id = null;
        centreContextHolder['customObject'] = {}; // should always exist, potentially empty
        centreContextHolder['key'] = 'centreContextHolder_key';
        centreContextHolder['desc'] = 'centreContextHolder description';

        if (requireSelectionCriteria !== null) {
            this.provideSelectionCriteria(requireSelectionCriteria, centreContextHolder, createModifiedPropertiesHolder);
            if (originallyProducedEntity) {
                centreContextHolder['originallyProducedEntity'] = originallyProducedEntity;
            }
        }
        if (requireSelectedEntities !== null) {
            this.provideSelectedEntities(requireSelectedEntities, centreContextHolder, getSelectedEntities);
        }
        if (requireMasterEntity !== null) {
            this.provideMasterEntity(requireMasterEntity, centreContextHolder, getMasterEntity);
        }
        return centreContextHolder;
    },

    /**
     * Fills in the centre context holder with 'selection criteria modified props holder' based on whether should 'requireSelectionCriteria'.
     */
    provideSelectionCriteria: function (requireSelectionCriteria, centreContextHolder, createModifiedPropertiesHolder) {
        if (requireSelectionCriteria === "true") {
            centreContextHolder["modifHolder"] = createModifiedPropertiesHolder();
        } else if (requireSelectionCriteria === "false") { // 'modifHolder' will be proxied after server-side deserialisation
        } else {
            throw "Unknown value for attribute 'requireSelectionCriteria': " + requireSelectionCriteria;
        }
    },

    /**
     * Creates the holder of modified properties, originallyProducedEntity and savingContext.
     *
     * There are three cases:
     *    1) modifiedPropertiesHolder.id !== null and the entity will be fetched from persistent storage (in this case originallyProducedEntity is always null, savingContext is not applicable)
     *    2) modifiedPropertiesHolder.id === null && originallyProducedEntity !== null and the entity will be deserialised from originallyProducedEntity and modifHolder applied (in this case savingContext is not applicable)
     *    3) otherwise the entity will be produced through savingContext-dependent producer (only in this case savingContext is applicable)
     *
     * @param originallyProducedEntity -- in case if new entity is operated on, this instance holds an original fully-fledged contextually produced entity.
     */
    createSavingInfoHolder: function (originallyProducedEntity, modifiedPropertiesHolder, savingContext, continuationsMap) {
        const savingInfoHolder = this.newEntity("ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder");
        savingInfoHolder.id = null;
        savingInfoHolder['key'] = 'NO_KEY';
        savingInfoHolder['desc'] = 'savingInfoHolder description';
        savingInfoHolder['modifHolder'] = modifiedPropertiesHolder;
        savingInfoHolder['originallyProducedEntity'] = originallyProducedEntity;

        if (savingContext) { // if saving context was defined (not 'undefined'):
            savingInfoHolder['centreContextHolder'] = savingContext;
        }

        if (typeof continuationsMap !== 'undefined') {
            var continuations = [];
            var continuationProperties = [];
            for (var continuationProperty in continuationsMap) {
                if (continuationsMap.hasOwnProperty(continuationProperty)) {
                    continuations.push(continuationsMap[continuationProperty]);
                    continuationProperties.push(continuationProperty);
                }
            }
            savingInfoHolder['continuations'] = continuations;
            savingInfoHolder['continuationProperties'] = continuationProperties;
        }
        return savingInfoHolder;
    },

    /**
     * Provides custom property into 'centreContextHolder' internals.
     */
    setCustomProperty: function (centreContextHolder, name, value) {
        centreContextHolder["customObject"][name] = value;
        return centreContextHolder;
    },

    /**
     * Removes custom property from 'centreContextHolder' internals, if exists.
     */
    removeCustomProperty: function (centreContextHolder, name) {
        if (typeof (centreContextHolder["customObject"])[name] !== 'undefined') {
            delete (centreContextHolder["customObject"])[name];
        }
    },

    /**
     * Discards all requests of 'ajaxElement' if any.
     *
     * @param exceptLastOne -- if 'true' disacrds all requests except the last one and returns that last request
     * Returns the number of aborted requests (or last undiscarded request in case of exceptLastOne === 'true')
     */
    discardAllRequests: function (ajaxElement, exceptLastOne) {
        var number = 0;
        if (ajaxElement.loading && ajaxElement.activeRequests.length > 0) { // need to ensure that activeRequests are not empty; if they are empty, 'loading' property of 'ajaxElement' can still be true.
            if (exceptLastOne === true && ajaxElement.activeRequests.length === 1) {
                return number;
            } else {
                // get oldest request and discard it
                var oldestRequest = ajaxElement.activeRequests[0];
                // there is a need to explicitly abort iron-request instance since _discardRequest() does not perform that action:
                oldestRequest.abort();
                number = number + 1;

                // discards oldest request in terms of 'iron-ajax' element -- after that the request is not 'active' 
                //   and is removed from consideration (for e.g., 'loading' property could be recalculated) -- but this
                //   request is not aborted!
                // TODO maybe, the private API usage should be removed, and 'debouncing' should be used instead -- please, consider
                ajaxElement._discardRequest(oldestRequest);

                // discard all other requests if there are any:
                number = number + this.discardAllRequests(ajaxElement, exceptLastOne);
            }
        }
        return number;
    },

    /**
     * Cancels any unfinished request that was actioned earlier (if any) except the last one and returns corresponding promise.
     * 
     * @param ajaxSender -- iron-ajax instance containing requests
     * @param operationDesc -- description of iron-ajax operation to make warnings / errors more specific
     */
    abortRequestsExceptLastOne: function (ajaxSender, operationDesc) {
        const numberOfAbortedRequests = this.discardAllRequests(ajaxSender, true);
        if (numberOfAbortedRequests > 0) {
            console.warn(`abortRequestsExceptLastOne: number of aborted ${operationDesc} requests = ${numberOfAbortedRequests}`);
        }
        if (ajaxSender.activeRequests.length > 0) {
            if (ajaxSender.activeRequests.length > 1) {
                throw new Error(`At this stage only one ${operationDesc} request should exist.`);
            }
            return ajaxSender.activeRequests[0].completes;
        } else {
            if (numberOfAbortedRequests > 0) {
                throw new Error(`There were aborted ${operationDesc} requests, however the last one was needed to be NOT ABORTED, but it was.`);
            }
            return null;
        }
    },

    /**
     * Cancels any unfinished request that was actioned earlier (if any).
     * 
     * @param ajaxSender -- iron-ajax instance containing requests
     * @param operationDesc -- description of iron-ajax operation to make warning more specific
     */
    abortRequestsIfAny: function (ajaxSender, operationDesc) {
        const numberOfAbortedRequests = this.discardAllRequests(ajaxSender);
        if (numberOfAbortedRequests > 0) {
            console.warn(`abortRequestsIfAny: number of aborted ${operationDesc} requests = ${numberOfAbortedRequests}`);
        }
    },

    /**
     * Validates the presence of originallyProducedEntity based on number representation of entity id.
     */
    _validateOriginallyProducedEntity: function (originallyProducedEntity, idNumber) {
        if (idNumber === null) {
            if (!_isEntity(originallyProducedEntity)) {
                throw 'For new entities (null id) originallyProducedEntity should always exist.';
            }
        } else if (Number.isInteger(idNumber)) {
            if (_isEntity(originallyProducedEntity)) {
                throw 'For existing entities (id exists) originallyProducedEntity should always be empty.';
            }
        } else {
            throw 'Unknown id number [' + idNumber + ']';
        }
        return originallyProducedEntity;
    },

    /**
     * Validates the context based on string representation of entity id during retrieval process.
     * 
     * For non-empty context:
     *  1) if 'new' entity is retrieved then full context (CentreContextHolder) is necessary to be able to contextually restore the entity.
     *  2) if 'find_or_new' entity is retrieved then only master functional entity (SavingInfoHolder) is necessary to be able to contextually restore the entity -- empty context (CentreContextHolder)
     *     will be created on server (see EntityResource.retrieve method) and master functional entity will be set into it. This is most likely the situation of embedded master inside other master.
     */
    _validateRetrievalContext: function (context, idString) {
        if (context) {
            if (idString === 'new') {
                if (!_isEntity(context) || context.type()._simpleClassName() !== 'CentreContextHolder') {
                    throw 'Non-empty context for "new" entity during retrieval should be of type CentreContextHolder. Context = ' + context;
                }
            } else if (idString === 'find_or_new') {
                if (!_isEntity(context) || context.type()._simpleClassName() !== 'SavingInfoHolder') {
                    throw 'Non-empty context for "find_or_new" entity during retrieval should be of type SavingInfoHolder. Context = ' + context;
                }
            } else {
                throw 'Incorrect id string [' + idString + '] for non-empty context. Context = ' + context;
            }
        } else {
            if (idString === 'find_or_new') { // this occurs, for example, when Cancel button has been pressed -- context is undefined. 'new' or '830' idString is applicable.
                throw 'Incorrect id string [' + idString + '] for empty context. Context = ' + context;
            }
        }
        return context;
    },

    /**
     * Loads all union subtypes in the system. Sort them descendingly by frequency of usages in union types.
     */
    loadUnionSubtypesAndSortByUsageFrequency: function() {
        // Load all union subtypes from union types.
        // These can have duplicates.
        const allUnionSubtypes = Object.values(_typeTable)
            .filter(type => type.isUnionEntity())
            .flatMap(type => type.unionProps().map(unionProp => type.prop(unionProp).type()));
        // Create a frequency of usage of those subtypes in union types.
        const freqMap = new Map();
        allUnionSubtypes.forEach(unionSubtype => {
            freqMap.set(unionSubtype, freqMap.get(unionSubtype) ? freqMap.get(unionSubtype) + 1 : 1);
        });
        // Sort subtypes descendingly by frequency of usage; return array of subtypes only.
        return Array.from(freqMap)
            .toSorted((pair1, pair2) => pair2[1] - pair1[1])
            .map(pair => pair[0]);
    },

    /**
     * Returns URI-fashined identification key for the centre.
     */
    _centreKey: function (miType, saveAsName) {
        return miType + '/default' + saveAsName;
    },

    /**
     * The surrogate title of not yet known configuration. This is used during first time centre loading.
     */
    get UNDEFINED_CONFIG_TITLE() {
        return _UNDEFINED_CONFIG_TITLE;
    },

    /**
     * The surrogate title of centre 'link' configuration. This is used when link with centre parameters opens.
     */
    get LINK_CONFIG_TITLE() {
        return _LINK_CONFIG_TITLE;
    },

    get KEY_NOT_ASSIGNED() {
        return KEY_NOT_ASSIGNED;
    }

});
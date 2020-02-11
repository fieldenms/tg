import { generateShortCollection } from '/resources/reflection/tg-polymer-utils.js';
import { _millisDateRepresentation } from '/resources/reflection/tg-date-utils.js';
import { TgReflector } from '/app/tg-reflector.js';
import { TgAppConfig } from '/app/tg-app-config.js';

export const TgEgiDataRetrievalBehavior = {

    created: function () {
        this._reflector = new TgReflector();
        this._appConfig = new TgAppConfig();
    },

    isHyperlinkProp: function (entity, column) {
        return column.type === 'Hyperlink' && this.getValueFromEntity(entity, column) !== null
    },

    isBooleanProp: function (entity, column) {
        return column.type === 'Boolean' && this.getValueFromEntity(entity, column) !== null
    },

    isNotBooleanOrHyperlinkProp: function (entity, column) {
        return !(this.isBooleanProp(entity, column) || this.isHyperlinkProp(entity, column));
    },

    getAttachmentIfPossible: function (entity, column) {
        const valueFromEntity = this.getValueFromEntity(entity, column);
        if (entity.type && entity.constructor.prototype.type.call(entity).notEnhancedFullClassName() === "ua.com.fielden.platform.attachment.Attachment") {
            return entity;
        } else if (valueFromEntity && valueFromEntity.type &&
            valueFromEntity.constructor.prototype.type.call(valueFromEntity).notEnhancedFullClassName() === "ua.com.fielden.platform.attachment.Attachment") {
            return valueFromEntity;
        } else if (this._reflector.entityPropOwner(this.getRealEntity(entity, column), this.getRealProperty(column))) {
            const owner = this._reflector.entityPropOwner(this.getRealEntity(entity, column), this.getRealProperty(column));
            if (owner.type().notEnhancedFullClassName() === "ua.com.fielden.platform.attachment.Attachment") {
                return owner;
            }
            return null;
        } else {
            return null;
        }
    },

    getRealEntity: function (entity, column) {
        return entity && column.collectionalProperty ? this.getCollectionalItem(entity, column) : entity;
    },

    getRealProperty: function (column) {
        return column.collectionalProperty ? column.valueProperty : column.property;
    },

    getCollectionalItem: function (entity, column) {
        if (column.collectionalProperty) {
            const collection = entity.get(column.collectionalProperty);
            const item = collection.find(cItem => column.property === cItem[column.keyProperty]);
            return item || null;
        }
    },

    getValueFromEntity: function (entity, column) {
        const realEntity = this.getRealEntity(entity, column);
        const realProperty = this.getRealProperty(column);
        return realEntity && realEntity.get(realProperty);
    },

    getBindedValue: function (entity, column) {
        return this.getValue(this.getRealEntity(entity, column), this.getRealProperty(column), column.type);
    },

    getValue: function (entity, property, type) {
        if (entity === null || property === null || type === null || this.getValueFromEntity(entity, {property: property}) === null) {
            return "";
        } else if (this._reflector.findTypeByName(type)) {
            var propertyValue = this.getValueFromEntity(entity, {property: property});
            if (Array.isArray(propertyValue)) {
                propertyValue = generateShortCollection(entity, property, this._reflector.findTypeByName(type));
            }
            return Array.isArray(propertyValue) ? this._reflector.convert(propertyValue).join(", ") : this._reflector.convert(propertyValue);
        } else if (type.lastIndexOf('Date', 0) === 0) { // check whether type startsWith 'Date'. Type can be like 'Date', 'Date:UTC:' or 'Date:Europe/London:'
            var splitedType = type.split(':');
            return _millisDateRepresentation(entity.get(property), splitedType[1] || null, splitedType[2] || null);
        } else if (typeof entity.get(property) === 'number') {
            if (type === 'BigDecimal') {
                const metaProp = this._reflector.getEntityTypeProp(entity, property);
                return this._reflector.formatDecimal(entity.get(property), this._appConfig.locale, metaProp && metaProp.scale(), metaProp && metaProp.trailingZeros());
            } else {
                return this._reflector.formatNumber(entity.get(property), this._appConfig.locale);
            }
        } else if (type === 'Money') {
            const metaProp = this._reflector.getEntityTypeProp(entity, property);
            return this._reflector.formatMoney(entity.get(property), this._appConfig.locale, metaProp && metaProp.scale(), metaProp && metaProp.trailingZeros());
        } else if (type === 'Colour') {
            return '#' + entity.get(property)['hashlessUppercasedColourValue'];
        } else if (type === 'Hyperlink') {
            return entity.get(property)['value'];
        } else {
            return entity.get(property);
        }
    },
}
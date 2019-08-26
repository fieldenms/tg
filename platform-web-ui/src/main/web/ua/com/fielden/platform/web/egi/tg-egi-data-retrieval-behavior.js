import { generateShortCollection } from '/resources/reflection/tg-polymer-utils.js';
import { _millisDateRepresentation } from '/resources/reflection/tg-date-utils.js';
import { TgReflector } from '/app/tg-reflector.js';
import { TgAppConfig } from '/app/tg-app-config.js';

export const TgEgiDataRetrievalBehavior = {

    created: function () {
        this._reflector = new TgReflector();
        this._appConfig = new TgAppConfig();
    },

    isHyperlinkProp: function (entity, property, type) {
        return type === 'Hyperlink' && this.getValueFromEntity(entity, property) !== null
    },

    isBooleanProp: function (entity, property, type) {
        return type === 'Boolean' && this.getValueFromEntity(entity, property) !== null
    },

    isNotBooleanOrHyperlinkProp: function (entity, property, type) {
        return !(this.isBooleanProp(entity, property, type) || this.isHyperlinkProp(entity, property, type));
    },

    getAttachmentIfPossible: function (entity, property) {
        if (entity.type && entity.constructor.prototype.type.call(entity).notEnhancedFullClassName() === "ua.com.fielden.platform.attachment.Attachment") {
            return entity;
        } else if (this.getValueFromEntity(entity, property) && this.getValueFromEntity(entity, property).type &&
            this.getValueFromEntity(entity, property).type().notEnhancedFullClassName() === "ua.com.fielden.platform.attachment.Attachment") {
            return this.getValueFromEntity(entity, property);
        } else if (this._reflector.entityPropOwner(entity, property)) {
            const owner = this._reflector.entityPropOwner(entity, property);
            if (owner.type().notEnhancedFullClassName() === "ua.com.fielden.platform.attachment.Attachment") {
                return owner;
            }
            return null;
        } else {
            return null;
        }
    },

    getValueFromEntity: function (entity, property) {
        return entity && entity.get(property);
    },

    getBindedValue: function (entity, property, type) {
        return this.getValue(entity, property, type);
    },

    getValue: function (entity, property, type) {
        if (entity === null || property === null || type === null || this.getValueFromEntity(entity, property) === null) {
            return "";
        } else if (this._reflector.findTypeByName(type)) {
            var propertyValue = this.getValueFromEntity(entity, property);
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
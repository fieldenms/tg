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
            valueFromEntity.type().notEnhancedFullClassName() === "ua.com.fielden.platform.attachment.Attachment") {
            return valueFromEntity;
        } else if (valueFromEntity) {
            const entityPropOwner = column.collectionalProperty ? 
                this._reflector.entityPropOwner(valueFromEntity, column.valueProperty) : 
                this._reflector.entityPropOwner(entity, column.property);
            if (entityPropOwner) {
                if (owner.type().notEnhancedFullClassName() === "ua.com.fielden.platform.attachment.Attachment") {
                    return owner;
                }
            }
            return null;
        } else {
            return null;
        }
    },

    getCollectionalItem: function (entity, column) {
        if (column.collectionalProperty) {
            const collection = entity.get(column.collectionalProperty);
            const item = collection.find(cItem => column.property === cItem[column.keyProperty]);
            return item;
        }
    },

    getValueFromEntity: function (entity, column) {
        if (entity) {
            if (!column.collectionalProperty) {
                return  entity.get(column.property);
            } else {
                const item = this.getCollectionalItem(entity, column);
                return item && item.get(column.valueProperty);
            }
        }
        return entity;
    },

    getBindedValue: function (entity, column) {
        if (!column.collectionalProperty) {
            return this.getValue(entity, column.property, column.type);
        } 
        return this.getValue(this.getCollectionalItem(entity, column), column.valueProperty, column.type);
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
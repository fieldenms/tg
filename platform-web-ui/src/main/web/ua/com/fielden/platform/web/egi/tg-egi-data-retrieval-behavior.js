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
        if (entity === null || property === null || type === null) {
            return '';
        } else {
            return this._reflector.tg_toString(entity.get(property), entity.type(), property, { display: true, locale: this._appConfig.locale });
        }
    }

};
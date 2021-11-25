import { _millisDateRepresentation } from '/resources/reflection/tg-date-utils.js';
import { TgReflector } from '/app/tg-reflector.js';
import { TgAppConfig } from '/app/tg-app-config.js';

export const TgEgiDataRetrievalBehavior = {

    created: function () {
        this._reflector = new TgReflector();
        this._appConfig = new TgAppConfig();
    },

    isHyperlinkProp: function (entity, column) {
        return column.type === 'Hyperlink' && this.getValueFromEntity(entity, column) !== null;
    },

    isBooleanProp: function (entity, column) {
        return column.type === 'Boolean' && ['false', 'true'].includes(this.getBindedValue(entity, column)); // getBindedValue always returns 'true' or 'false' even if actual boolean value is null; this situation is possible for properties like 'lastUpdatedBy.base' where 'lastUpdatedBy' is null itself
    },

    isNotBooleanOrHyperlinkProp: function (entity, column) {
        return !(this.isBooleanProp(entity, column) || this.isHyperlinkProp(entity, column));
    },

    getAttachmentIfPossible: function (entity, column) {
        const valueFromEntity = this.getValueFromEntity(entity, column);
        const isAttachmentType = entityObj => {
            const entityTypeFunction = entityObj && entityObj.constructor.prototype.type;
            return entityTypeFunction
                && entityTypeFunction.call(entityObj)
                && entityTypeFunction.call(entityObj).notEnhancedFullClassName
                && entityTypeFunction.call(entityObj).notEnhancedFullClassName() === 'ua.com.fielden.platform.attachment.Attachment';
        };
        if (isAttachmentType(entity)) {
            return entity;
        } else if (isAttachmentType(valueFromEntity)) {
            return valueFromEntity;
        } else {
            const owner = this._reflector.entityPropOwner(this.getRealEntity(entity, column), this.getRealProperty(column));
            if (isAttachmentType(owner)) {
                return owner;
            }
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
            return this._reflector.tg_toString(entity.get(property), entity.constructor.prototype.type.call(entity), property, { display: true, locale: this._appConfig.locale });
        }
    }

};
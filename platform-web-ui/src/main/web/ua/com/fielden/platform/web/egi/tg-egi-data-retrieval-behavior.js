import { _millisDateRepresentation } from '/resources/reflection/tg-date-utils.js';
import { TgReflector } from '/app/tg-reflector.js';
import { TgAppConfig } from '/app/tg-app-config.js';

function moveChildren(from, to) {
    while(from.hasChildNodes()) {
        to.appendChild(from.firstChild);
    }
    return to;
}

function convertHeader(header) {
    const bold = document.createElement('b');
    return moveChildren(header, bold);
}

function convertNode(root) {
    let nextNode = root.firstChild;
    while (nextNode) {
        if (nextNode.tagName) {
            const nodeToReplace = nextNode;
            const convertedNode = RichTextConverter[nextNode.tagName](nodeToReplace);
            nextNode = nodeToReplace.nextSibling;
            if (convertedNode) {
                root.replaceChild(convertedNode, nodeToReplace);
            } else {
                root.removeChild(nodeToReplace);
            }
        } else {
            nextNode = nextNode.nextSibling;
        }
    }
    [...root.childNodes].forEach(child => convertNode(child));
    return root;
}

function inlineRichText(richText) {
    const root = document.createElement('div');
    root.innerHTML = richText;
    return convertNode(root).innerHTML;
}

RichTextConverter = {
    'H1': convertHeader,
    'H2': convertHeader,
    'H3': convertHeader,
}

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

    isRichTextProp: function (entity, column) {
        return column.type === 'RichText' && this.getValueFromEntity(entity, column) !== null;
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


    /**
     * Should return real property name which is an entity property name that has value.
     * If specified column is for collectional property then real property - is a property name of collectional entity that has a value. 
     * 
     * @param {Object} column - property column for which real property name should be returned.
     * @returns 
     */
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
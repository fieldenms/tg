import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { TgEditorBehavior, TgEditorBehaviorImpl, createEditorTemplate} from '/resources/editors/tg-editor-behavior.js';
import { generateShortCollection, allDefined } from '/resources/reflection/tg-polymer-utils.js';

const additionalTemplate = html`
    <style>
        #input[disabled] {
            cursor: text;
        }
    </style>`;
const customInputTemplate = html`
    <iron-input bind-value="{{_editingValue}}" class="custom-input-wrapper collectional-representor-input">
        <input
            id="input"
            class="custom-input"
            on-change="_onChange"
            on-input="_onInput"
            on-tap="_onTap"
            on-mousedown="_onTap"
            on-keydown="_onKeydown"
            disabled$="[[_disabled]]"
            tooltip-text$="[[_getTooltip(_editingValue, entity)]]"
            autocomplete="off"/>
    </iron-input>`;
const propertyActionTemplate = html`<slot name="property-action"></slot>`;

Polymer({
    _template: createEditorTemplate(additionalTemplate, html``, customInputTemplate, html``, html``, propertyActionTemplate),

    is: 'tg-collectional-representor',
    
    behaviors: [ TgEditorBehavior ],
    
    /**
     * Converts the value into string representation (which is used in edititing / comm values).
     */
    convertToString: function(value) {
        if (value === null) {
            return "";
        }

        if (value.constructor !== Array) {
            throw 'Unsupported value has appeared inside collectional representor: ' + value;
        }

        const fullEntity = this.reflector()._getValueFor(this.entity, "");
        const originValue = fullEntity.get(this.propertyName);
        if (originValue.length === 0 || !originValue[0].type().isCompositeEntity()) {
            return value.map(v => this.reflector().convert(v)).join(", "); // assumes that value is array of entities
        } else {
            return this.reflector().convert(generateShortCollection(fullEntity, this.propertyName, originValue[0].type())).join(", ");
        }
    },

    /**
     * This method promotes 'IRRELEVANT' into _acceptedValue which should not be a problem, since this 'representor' is not editable at all.
     */
    convertFromString: function(strValue) {
        return 'IRRELEVANT';
    },

    /**
     * This 'representor' is disabled for editing (just gives a view of the entity collection).
     */
    _isDisabled: function(currentState, bindingEntity, propertyName) {
        return true;
    },
    
    _getTooltip: function(_editingValue, entity) {
        if (!allDefined(arguments)) {
            return "";
        }
        if (entity !== null) {
            const fullEntity = this.reflector()._getValueFor(entity, "");
            let valueToFormat = "";
            if (this.reflector().isError(fullEntity.prop(this.propertyName).validationResult())) {
                valueToFormat = _editingValue;
            } else {
                valueToFormat = this.reflector()._getValueFor(entity, this.propertyName);
            }
            return TgEditorBehaviorImpl._getTooltip.call(this, valueToFormat);
        }
        return "";
    },
    
    _formatTooltipText: function (valueToFormat) {
        if (valueToFormat !== null) {
            if (Array.isArray(valueToFormat)) {
                if (valueToFormat.length === 0 || !valueToFormat[0].type().isCompositeEntity()) {
                    return valueToFormat.length > 0 ? ("<b>" + valueToFormat.join(", ") + "</b>") : '';
                } else {
                    const collection = generateShortCollection(this.reflector()._getValueFor(this.entity, ""), this.propertyName, valueToFormat[0].type());
                    const title = this.reflector().convert(collection).join(", ");
                    const desc = collection.reduce((curr, next) => this.reflector().isEntity(next) && next.get("desc") ? curr + (curr ? ", " : "") + next.get("desc") : curr, "");
                    return "<b>" + title + "</b>" + (desc ? "<br>" + desc : "");
                }
            } else {
                throw 'Unsupported value has appeared inside collectional representor: ' + valueToFormat;
            }
        }
        return '';
    }
});
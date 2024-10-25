import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import {html} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import { TgEditor, createEditorTemplate} from '/resources/editors/tg-editor.js';
import { allDefined } from '/resources/reflection/tg-polymer-utils.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';

const additionalTemplate = html`
    <style>
        #input[disabled] {
            cursor: text;
        }
    </style>`;
const customLabelTemplate = html`
    <label style$="[[_calcLabelStyle(_editorKind, _disabled)]]" disabled$="[[_disabled]]" tooltip-text$="[[_getTooltip(_editingValue, entity)]]" slot="label">
        <span>[[propTitle]]</span>
        <iron-icon hidden$="[[noLabelFloat]]" id="copyIcon" icon="icons:content-copy" on-tap="_copyTap"></iron-icon>
    </label>`;

const customInputTemplate = html`
    <iron-input bind-value="{{_editingValue}}" class="custom-input-wrapper collectional-representor-input">
        <input
            id="input"
            class="custom-input"
            on-change="_onChange"
            on-input="_onInput"
            on-mouseup="_onMouseUp" 
            on-mousedown="_onMouseDown"
            on-keydown="_onKeydown"
            disabled$="[[_disabled]]"
            tooltip-text$="[[_getTooltip(_editingValue, entity)]]"
            autocomplete="off"/>
    </iron-input>`;
const propertyActionTemplate = html`<slot id="actionSlot" name="property-action"></slot>`;

export class TgCollectionalRepresentor extends TgEditor {

    static get template() { 
        return createEditorTemplate(additionalTemplate, html``, customInputTemplate, html``, html``, propertyActionTemplate, customLabelTemplate);
    }
    
    /**
     * Converts the value into string representation (which is used in edititing / comm values).
     */
    convertToString (value) {
        if (value && !Array.isArray(value)) {
            throw 'Unsupported value has appeared inside collectional representor: ' + value;
        }
        return super.convertToString(value);
    }

    /**
     * This method promotes 'IRRELEVANT' into _acceptedValue which should not be a problem, since this 'representor' is not editable at all.
     */
    convertFromString (strValue) {
        return 'IRRELEVANT';
    }

    /**
     * This 'representor' is disabled for editing (just gives a view of the entity collection).
     */
    _isDisabled (currentState, bindingEntity, propertyName) {
        return true;
    }
    
    _getTooltip (_editingValue, entity) {
        if (!allDefined(arguments)) {
            return "";
        }
        if (entity !== null) {
            const fullEntity = this.reflector().tg_getFullEntity(entity);
            let valueToFormat = "";
            if (this.reflector().isError(fullEntity.prop(this.propertyName).validationResult())) {
                valueToFormat = _editingValue;
            } else {
                valueToFormat = fullEntity.get(this.propertyName);
            }
            return super._getTooltip(valueToFormat);
        }
        return "";
    }
    
    _formatTooltipText (valueToFormat) {
        if (valueToFormat !== null) {
            if (Array.isArray(valueToFormat)) {
                return this.reflector().tg_toString(valueToFormat, this.entity.type(), this.propertyName, { collection: true, asTooltip: true });
            } else {
                throw 'Unsupported value has appeared inside collectional representor: ' + valueToFormat;
            }
        }
        return '';
    }
}

customElements.define('tg-collectional-representor', TgCollectionalRepresentor);
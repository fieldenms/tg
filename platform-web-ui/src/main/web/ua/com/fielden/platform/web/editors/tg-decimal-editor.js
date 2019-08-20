import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-input/iron-input.js'

import '/app/tg-app-config.js'

import {html} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import { TgEditor,  createEditorTemplate} from '/resources/editors/tg-editor.js';
import { truncateInsignificantZeros } from '/resources/reflection/tg-numeric-utils.js'

const additionalTemplate = html`
    <style>
        /* Styles for integer and decimal property editors. */
        input[type=number]::-webkit-outer-spin-button,
        input[type=number]::-webkit-inner-spin-button {
            -webkit-appearance: none;
            margin: 0;
        }
        input[type=number] {
            -moz-appearance: textfield;
        }
        .input-layer {
            cursor: text;
            text-align: right;
            text-overflow: ellipsis;
            white-space: nowrap;
            overflow: hidden;
        }
    </style>
    <tg-app-config id="appConfig"></tg-app-config>`;
const customInputTemplate = html`
    <iron-input bind-value="{{_editingValue}}" class="custom-input-wrapper">
        <input
            id="input"
            class="custom-input decimal-input"
            type="number"
            step="any"
            bind-value="{{_editingValue}}"
            on-change="_onChange"
            on-input="_onInput"
            on-keydown="_onKeydown"
            on-mouseup="_onMouseUp" 
            on-mousedown="_onMouseDown"
            on-focus="_onFocus"
            on-blur="_outFocus"
            disabled$="[[_disabled}}"
            tooltip-text$="[[_getTooltip(_editingValue)]]"
            autocomplete="off"/>
    </iron-input>`;
const inputLayerTemplate = html`<div class="input-layer" tooltip-text$="[[_getTooltip(_editingValue)]]">[[_formatText(_editingValue)]]</div>`;
const propertyActionTemplate = html`<slot name="property-action"></slot>`;

export class TgDecimalEditor extends TgEditor {

    static get template() { 
        return createEditorTemplate(additionalTemplate, html``, customInputTemplate, inputLayerTemplate, html``, propertyActionTemplate);
    }

    constructor () {
        super();
        this._hasLayer = true;
    }
    
    /**
     * Converts the value into string representation (which is used in editing / comm values).
     */
    convertToString (value) {
        return value === null ? "" : "" + value;
    }

    /**
     * Converts the value from string representation (which is used in editing / comm values) into concrete type of this editor component (Number).
     */
    convertFromString (strValue) {
        if (strValue === '') {
            return null;
        }
        // var convertedNumber = (+strValue);
        if (isNaN(strValue)) {
            throw "The entered number is incorrect.";
        }
        return (+strValue);
    }

    _formatText (valueToFormat) {
        var value = this.convertFromString(valueToFormat);
        if (value !== null) {
            const metaProp = this.reflector().getEntityTypeProp(this.reflector()._getValueFor(this.entity, ''), this.propertyName);
            return this.reflector().formatDecimal(value, this.$.appConfig.locale, metaProp && metaProp.scale(), metaProp && metaProp.trailingZeros());
        }
        return '';
    }

    /**
     * Overridden to provide value corrections.
     */
    _commitForDescendants () {
        const correctedValue = truncateInsignificantZeros(this._editingValue);
        if (!this.reflector().equalsEx(correctedValue, this._editingValue)) {
            this._editingValue = correctedValue;
        }
    }
}

customElements.define('tg-decimal-editor', TgDecimalEditor);
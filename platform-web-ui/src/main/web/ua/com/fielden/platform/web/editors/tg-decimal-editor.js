import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-input/iron-input.js'

import {html} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import { createEditorTemplate } from '/resources/editors/tg-editor.js';
import { TgNumericEditor } from '/resources/editors/tg-numeric-editor.js';

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
    </style>`;
const customInputTemplate = html`
    <iron-input bind-value="{{_editingValue}}" class="custom-input-wrapper">
        <input
            id="input"
            class="custom-input decimal-input"
            type="number"
            title=""
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
            tooltip-text$="[[_getTooltip(_editingValue, _scanAvailable)]]"
            autocomplete="off"/>
    </iron-input>`;
const inputLayerTemplate = html`<div id="inputLayer" class="input-layer" tooltip-text$="[[_getTooltip(_editingValue, _scanAvailable)]]">[[_formatText(_editingValue)]]</div>`;
const propertyActionTemplate = html`<slot id="actionSlot" name="property-action"></slot>`;

export class TgDecimalEditor extends TgNumericEditor {

    static get template() { 
        return createEditorTemplate(additionalTemplate, html``, customInputTemplate, inputLayerTemplate, html``, propertyActionTemplate);
    }

    constructor () {
        super();
        this.builtInValidationMessage = 'The value entered is not a number.';
    }

    /**
     * Converts the value from string representation (which is used in editing / comm values) into concrete type of this editor component (Number).
     */
    convertFromString (strValue) {
        if (strValue === '') {
            return null;
        }
        if (isNaN(strValue)) {
            throw this.builtInValidationMessage;
        }
        return (+strValue);
    }

}

customElements.define('tg-decimal-editor', TgDecimalEditor);
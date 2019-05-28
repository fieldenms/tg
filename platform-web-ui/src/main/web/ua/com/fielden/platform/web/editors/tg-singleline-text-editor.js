import '/resources/polymer/@polymer/iron-input/iron-input.js';

import {html} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import { TgEditorBehavior,  createEditorTemplate} from '/resources/editors/tg-editor-behavior.js';

const additionalTemplate = html`
    <style>
        #input[disabled] {
            cursor: text;
        }
        #input.upper-case {
            text-transform: uppercase;
        }
    </style>`;
const customInputTemplate = html`
    <iron-input bind-value="{{_editingValue}}" class="custom-input-wrapper">
        <input
            id="input"
            class="custom-input singleline-text-input"
            on-change="_onChange"
            on-input="_onInput"
            on-tap="_onTap"
            on-mousedown="_onTap"
            on-keydown="_onKeydown"
            disabled$="[[_disabled]]"
            tooltip-text$="[[_getTooltip(_editingValue)]]"
            autocomplete="off"/>
    </iron-input>`;
const propertyActionTemplate = html`<slot name="property-action"></slot>`;

class TgSinglelineTextEditor extends TgEditorBehavior {

    static get template() { 
        return createEditorTemplate(additionalTemplate, html``, customInputTemplate, html``, html``, propertyActionTemplate);
    }
    
    /**
     * Converts the value into string representation (which is used in edititing / comm values).
     */
    convertToString (value) {
        return value === null ? "" : "" + value;
    }
    
    /**
     * Converts the value from string representation (which is used in edititing / comm values) into concrete type of this editor component (String).
     */
    convertFromString (strValue) {
        return strValue === '' ? null : strValue;
    }
}

customElements.define('tg-singleline-text-editor', TgSinglelineTextEditor);
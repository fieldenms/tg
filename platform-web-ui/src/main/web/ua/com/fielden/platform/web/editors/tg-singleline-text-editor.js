import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-input/iron-input.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

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
    <iron-input bind-value="{{_editingValue}}" class="custom-input singleline-text-input">
        <input
            id="input"
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

Polymer({
    _template: createEditorTemplate(additionalTemplate, html``, customInputTemplate, html``, html``, propertyActionTemplate),

    is: 'tg-singleline-text-editor',
    
    behaviors: [ TgEditorBehavior ],
    
    /**
     * Converts the value into string representation (which is used in edititing / comm values).
     */
    convertToString: function (value) {
        return value === null ? "" : "" + value;
    },
    
    /**
     * Converts the value from string representation (which is used in edititing / comm values) into concrete type of this editor component (String).
     */
    convertFromString: function (strValue) {
        return strValue === '' ? null : strValue;
    }
});
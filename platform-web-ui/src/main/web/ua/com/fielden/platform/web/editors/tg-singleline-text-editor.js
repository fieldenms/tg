import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-input/iron-input.js';

import '/resources/editors/tg-editor.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { TgEditorBehavior } from '/resources/editors/tg-editor-behavior.js';

const template = html`
    <style>
        #input[disabled] {
            cursor: text;
        }
        #input.upper-case {
            text-transform: uppercase;
        }
    </style>
    <tg-editor 
        id="editorDom" 
        prop-title="[[propTitle]]"
        _disabled="[[_disabled]]" 
        _editing-value="{{_editingValue}}" 
        action="[[action]]" 
        _error="[[_error]]" 
        _comm-value="[[_commValue]]" 
        _accepted-value="[[_acceptedValue]]" 
        debug="[[debug]]">
        <input
            id="input"
            class="custom-input singleline-text-input"
            is="iron-input"
            bind-value="{{_editingValue}}"
            on-change="_onChange"
            on-input="_onInput"
            on-tap="_onTap"
            on-mousedown="_onTap"
            on-keydown="_onKeydown"
            disabled$="[[_disabled]]"
            tooltip-text$="[[_getTooltip(_editingValue)]]"/>
        <slot name="property-action"></slot>
    </tg-editor>`;

Polymer({
    _template: template,

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
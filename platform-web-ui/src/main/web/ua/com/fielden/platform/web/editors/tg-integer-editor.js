import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-input/iron-input.js';

import '/resources/editors/tg-editor.js';

import '/app/tg-app-config.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { TgEditorBehavior } from '/resources/editors/tg-editor-behavior.js';
import { truncateInsignificantZeros } from '/resources/reflection/tg-numeric-utils.js';

const template = html`
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
    <tg-app-config id="appConfig"></tg-app-config>
    <tg-editor 
        id="editorDom" 
        prop-title="[[propTitle]]"
        _disabled="[[_disabled]]" 
        _editing-value="{{_editingValue}}" 
        action="[[action]]" 
        _error="[[_error]]" 
        _comm-value="[[_commValue]]" 
        _accepted-value="[[_acceptedValue]]"
        _focused="[[focused]]"
        debug="[[debug]]">
        <input
            id="input"
            class="custom-input integer-input"
            is="iron-input"
            type="number"
            step="1"
            prevent-invalid-input
            bind-value="{{_editingValue}}"
            on-change="_onChange"
            on-input="_onInput"
            on-keydown="_onKeydown"
            on-tap="_onTap"
            on-mousedown="_onTap"
            on-focus="_onFocus"
            on-blur="_outFocus"
            tooltip-text$="[[_getTooltip(_editingValue)]]"
            disabled$="[[_disabled]]"/>
        <div class="input-layer" tooltip-text$="[[_getTooltip(_editingValue)]]">[[_formatText(_editingValue)]]</div>
        <slot name="property-action"></slot>
    </tg-editor>`;

Polymer({
    _template: template,

    is: 'tg-integer-editor',

    behaviors: [ TgEditorBehavior ],
    
    /**
     * Converts the value into string representation (which is used in edititing / comm values).
     */
    convertToString: function (value) {
        // NOTE: consider the follwing example, of how 'super' method can be invoked.
        //   Just use concrete name of the 'super' behavior and call the function excplicitly:            		
        // Polymer.TgBehaviors.TgEditorBehavior.convertToString(value);
        
        return value === null ? "" : "" + value;
    },

    /**
     * Converts the value from string representation (which is used in edititing / comm values) into concrete type of this editor component (Number).
     */
    convertFromString: function (strValue) {
        return strValue === '' ? null : parseInt(strValue);
    },
    
    _formatText: function(valueToFormat) {
        var value = this.convertFromString(valueToFormat);
        if (value !== null) {
            return this.reflector().formatNumber(value, this.$.appConfig.locale);
        }
        return '';
    },
    
    /**
     * Overridden to provide value corrections.
     */
    _commitForDescendants: function () {
        const correctedValue = truncateInsignificantZeros(this._editingValue);
        if (!this.reflector().equalsEx(correctedValue, this._editingValue)) {
            this._editingValue = correctedValue;
        }
    }
});
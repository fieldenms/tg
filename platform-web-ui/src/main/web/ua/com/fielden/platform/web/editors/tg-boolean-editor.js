import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/paper-checkbox/paper-checkbox.js'

import {html} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import {TgEditor, createEditorTemplate} from '/resources/editors/tg-editor.js'

const customLableTemplate = html`
    <label style$="[[_calcLabelStyle(_editorKind, _disabled)]]" disabled$="[[_disabled]]" tooltip-text$="[[_getTooltip(_editingValue, _scanAvailable)]]" slot="label">[[propTitle]]</label>`;

const additionalTemplate = html`
    <style>
        /* Styles for boolean property editors. */
        paper-checkbox {
            -moz-user-select: none;
            -webkit-user-select: none;
            -ms-user-select: none;
            user-select: none;
            -o-user-select: none;
            font-family: 'Roboto', 'Noto', sans-serif;
            --paper-checkbox-unchecked-color: var(--paper-grey-900);
            --paper-checkbox-checked-color: var(--paper-light-blue-700);
            --paper-checkbox-checked-ink-color: var(--paper-light-blue-700);
            --paper-checkbox-label-color: #757575;
            height: 24px;
            --paper-checkbox-label: {
                display:grid;
            };
        }

        .label {
            transform:scale(0.75);
            transform-origin: left;
            width:130%;
            font-weight: 400;
            -webkit-font-smoothing: antialiased;
            text-rendering: optimizeLegibility;
        }
        
        .truncate {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        #decorator.required paper-checkbox {
            --paper-checkbox-unchecked-color: #03A9F4;
            --paper-checkbox-checked-color: #03A9F4;
            --paper-checkbox-checked-ink-color: #03A9F4;
            --paper-checkbox-label-color: #03A9F4;
        }

        #decorator[is-invalid].warning paper-checkbox {
            --paper-checkbox-unchecked-color: #FFA000;
            --paper-checkbox-checked-color: #FFA000;
            --paper-checkbox-checked-ink-color: #FFA000;
            --paper-checkbox-label-color: #FFA000;
        }

        #decorator[is-invalid].informative paper-checkbox {
            --paper-checkbox-unchecked-color: #8E24AA;
            --paper-checkbox-checked-color: #8E24AA;
            --paper-checkbox-checked-ink-color: #8E24AA;
            --paper-checkbox-label-color: #8E24AA;
        }

        #decorator[is-invalid]:not(.warning):not(.informative) paper-checkbox {
            --paper-checkbox-unchecked-color: var(--google-red-500);
            --paper-checkbox-checked-color: var(--google-red-500);
            --paper-checkbox-checked-ink-color: var(--google-red-500);
            --paper-checkbox-label-color:var(--google-red-500);
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>`;
const customInputTemplate = html`
    <paper-checkbox
            id="input"
            class="paper-input-input custom-input boolean-input layout horizontal center"
            checked="[[_isBooleanChecked(_editingValue)]]"
            disabled$="[[_disabled]]"
            on-change="_onChange"
            on-focus="_onFocus"
            on-blur="_outFocus"
            tooltip-text$="[[_getTooltip(_editingValue, _scanAvailable)]]"><span class="label truncate">[[propTitle]]</span></paper-checkbox>`;
const propertyActionTemplate = html`<slot id="actionSlot" name="property-action"></slot>`;

export class TgBooleanEditor extends TgEditor {

    static get template() { 
        return createEditorTemplate(additionalTemplate, html``, customInputTemplate, html``, html``, propertyActionTemplate, customLableTemplate);
    }

    static get properties() {
        return {
            _onChange: {
                type: Function
            },
            
            _isBooleanChecked: {
                type: Function
            }
        }
    }
    
    constructor () {
        super();
        this._editorKind = "BOOLEAN";

        this._onChange = (function (e) {
            console.log("_onChange:", e);
            this._editingValue = this.convertToString(e.target.checked);
            
            const parentFunction = TgEditor.properties._onChange.value.call(this);
            parentFunction.call(this, e);
        }).bind(this);
        
        this._isBooleanChecked = (function (editingValue) {
            return editingValue === 'true';
        }).bind(this);
    }

    /**
     * This function returns the tooltip for this editor.
     */
    _getTooltip (value) {
        let tooltip = this._formatTooltipText(value);
        tooltip += this.propDesc && (tooltip ? '<br><br>' : '') + this.propDesc;
        return tooltip;
    }
    
    /**
     * This method returns a default value for '_editingValue', which is used 
     *  for representing the value when no entity was bound to this editor yet.
     *
     * Overriden to return 'false' as the value that will be used when no entity is bound to this editor yet.
     */
    _defaultEditingValue () {
        return 'false';
    }
    
    /**
     * Converts the value from string representation (which is used in edititing / comm values) into concrete type of this editor component (String).
     */
    convertFromString (strValue) {
        if (strValue !== "false" && strValue !== "true") {
            throw "The entered check value is incorrect [" + strValue + "].";
        }
        return strValue === "true" ? true : false;
    }
}

customElements.define('tg-boolean-editor', TgBooleanEditor);
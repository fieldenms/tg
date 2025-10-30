import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-input/iron-input.js'
import '/resources/polymer/@polymer/iron-icons/iron-icons.js'
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js'

import {html} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import { TgEditor, createEditorTemplate} from '/resources/editors/tg-editor.js'

import { checkLinkAndOpen, MAILTO_PROTOCOL, SUPPORTED_PROTOCOLS, ERR_UNSUPPORTED_PROTOCOL } from '/resources/components/tg-link-opener.js';

const additionalTemplate = html`
    <style>
        #input[disabled] {
            cursor: text;
        }
		.open-button {
            display: flex;
            width: 24px;
            height: 24px;
            padding: 4px;
        }
    </style>`;
const customInputTemplate = html`
    <iron-input bind-value="{{_editingValue}}" class="custom-input-wrapper hyperlink-input">
        <input
            id="input"
            class="custom-input"
            on-change="_onChange"
            on-input="_onInput"
            on-mouseup="_onMouseUp" 
            on-mousedown="_onMouseDown"
            on-keydown="_onKeydown"
            on-focus="_onFocus"
            on-blur="_outFocus"
            disabled$="[[_disabled]]"
            tooltip-text$="[[_getTooltip(_editingValue, _scanAvailable)]]"
            autocomplete="off"/>
    </iron-input>`;
const customIconButtonsTemplate = html`<paper-icon-button on-tap="_openLink" icon="open-in-browser" class="open-button custom-icon-buttons" tabIndex="-1" tooltip-text="Open link"></paper-icon-button>`;
const propertyActionTemplate = html`<slot id="actionSlot" name="property-action"></slot>`;

export class TgHyperlinkEditor extends TgEditor {

    static get template() { 
        return createEditorTemplate(additionalTemplate, html``, customInputTemplate, html``, customIconButtonsTemplate, propertyActionTemplate);
    }

    /**
     * Converts the value from string representation into a JSON object that is used for representing value of Java type Hyperlink.
     */
    convertFromString (strValue) {
        if (strValue === '') {
            return null;
        } else {
            if (!strValue.startsWith(MAILTO_PROTOCOL) && SUPPORTED_PROTOCOLS.every(p => !strValue.startsWith(p + '//'))) {
                throw ERR_UNSUPPORTED_PROTOCOL;
            }

            return {
                value: strValue
            };
        }
    }

    /**
     * A handler to open a linked resource in browser
     */
    _openLink () {
        if (this._acceptedValue) {
            checkLinkAndOpen(this._acceptedValue.value);
        }
    }

    /**
     * Overridden to provide value approximations.
     */
    _commitForDescendants () {
        const trimmedValWithProtocol = this._trimAndPrependProtocol(this._editingValue);
        if (!this.reflector().equalsEx(trimmedValWithProtocol, this._editingValue)) {
            this._editingValue = trimmedValWithProtocol;
        }
    }

    _trimAndPrependProtocol (editingValue) {
        const strValue = editingValue.trim();
        if (strValue === '' || strValue.includes('://') || strValue.includes(MAILTO_PROTOCOL)) {
            return strValue;
        } else {
            return `https://${strValue}`;
        }
    }

}

customElements.define('tg-hyperlink-editor', TgHyperlinkEditor);
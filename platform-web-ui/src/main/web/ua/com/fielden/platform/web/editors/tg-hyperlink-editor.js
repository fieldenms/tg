import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-input/iron-input.js'
import '/resources/polymer/@polymer/iron-icons/iron-icons.js'
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js'

import {html} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import { TgEditor, createEditorTemplate} from '/resources/editors/tg-editor.js'

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
            on-tap="_onTap"
            on-mousedown="_onTap"
            on-keydown="_onKeydown"
            disabled$="[[_disabled]]"
            tooltip-text$="[[_getTooltip(_editingValue)]]"
            autocomplete="off"/>
    </iron-input>`;
const customIconButtonsTemplate = html`<paper-icon-button on-tap="_openLink" icon="open-in-browser" class="open-button custom-icon-buttons" tabIndex="-1" tooltip-text="Open link"></paper-icon-button>`;
const propertyActionTemplate = html`<slot name="property-action"></slot>`;

export class TgHyperlinkEditor extends TgEditor {

    static get template() { 
        return createEditorTemplate(additionalTemplate, html``, customInputTemplate, html``, customIconButtonsTemplate, propertyActionTemplate);
    }
    
    convertToString (link) {
        return link === null ? "" : link.value;
    }

    /**
     * Converts the value from string representation into a JSON object that is used for representing value of Java type Hyperlink.
     */
    convertFromString (value) {
        var strValue = value.trim();
        if (strValue === '') {
            return null;
        } else {
            if ((strValue.startsWith('https://') || strValue.startsWith('http://') ||
                    strValue.startsWith('ftp://') || strValue.startsWith('ftps://') ||
                    strValue.startsWith('mailto:')) === false) {
                throw "One of http, https, ftp, ftps or mailto hyperlink protocols is expected.";
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
            var win = window.open(this._acceptedValue.value, '_blank');
            win.focus();
        }
    }
}

customElements.define('tg-hyperlink-editor', TgHyperlinkEditor);
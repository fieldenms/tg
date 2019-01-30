import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-input/iron-input.js'
import '/resources/polymer/@polymer/iron-icons/iron-icons.js'
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js'

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { TgEditorBehavior, createEditorTemplate} from '/resources/editors/tg-editor-behavior.js'

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
    <iron-input slot="input" bind-value="{{_editingValue}}" class="custom-input hyperlink-input">
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
const customIconButtonsTemplate = html`<paper-icon-button slot="suffix" on-tap="_openLink" icon="open-in-browser" class="open-button custom-icon-buttons" tabIndex="-1" tooltip-text="Open link"></paper-icon-button>`;
const propertyActionTemplate = html`<slot slot="suffix" name="property-action"></slot>`;

Polymer({
    _template: createEditorTemplate(additionalTemplate, html``, customInputTemplate, html``, customIconButtonsTemplate, propertyActionTemplate),

    is: 'tg-hyperlink-editor',

    behaviors: [ TgEditorBehavior ],

    /**
     * Converts a JSON object represneting a value of Java type Hyperlink into a string.
     */
    convertToString: function (link) {
        return link === null ? "" : link.value;
    },

    /**
     * Converts the value from string representation into a JSON object that is used for representing value of Java type Hyperlink.
     */
    convertFromString: function (value) {
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
    },

    /**
     * A handler to open a linked resource in browser
     */
    _openLink: function () {
        if (this._acceptedValue) {
            var win = window.open(this._acceptedValue.value, '_blank');
            win.focus();
        }
    }
});
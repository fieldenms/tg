import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/components/tg-rich-text-input.js';

import {html} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import { TgEditor, createEditorTemplate} from '/resources/editors/tg-editor.js';
import { allDefined } from '/resources/reflection/tg-polymer-utils.js';

const additionalTemplate = html`
    <style>
        #input {
            cursor: text;
        }
    </style>`;
const customLabelTemplate = html`
    <label style$="[[_calcLabelStyle(_editorKind, _disabled)]]" disabled$="[[_disabled]]" tooltip-text$="[[_getTooltip(_editingValue, entity)]]" slot="label">
        <span>[[propTitle]]</span>
        <iron-icon hidden$="[[noLabelFloat]]" id="copyIcon" icon="icons:content-copy" on-tap="_copyTap"></iron-icon>
    </label>`;

const customInputTemplate = html`
    <tg-rich-text-input id="input" 
        class="custom-input paper-input-input"
        tooltip-text$="[[_getTooltip(_editingValue, entity)]]"
        disabled$="[[_disabled]]" markdown-text="[[_editingValue]]"></tg-rich-text-input>`;
const propertyActionTemplate = html`<slot id="actionSlot" name="property-action"></slot>`;

export class TgRichTextEditor extends TgEditor {

    static get template() { 
        return createEditorTemplate(additionalTemplate, html``, customInputTemplate, html``, html``, propertyActionTemplate, customLabelTemplate);
    }
    
    /**
     * Converts the value into string representation (which is used in edititing / comm values).
     */
    convertToString (value) {
        return value.formattedText || "";
    }

    /**
     * This method converts 
     */
    convertFromString (strValue) {
        return {formattedText: strValue};
    }
}

customElements.define('tg-rich-text-editor', TgRichTextEditor);
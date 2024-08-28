import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/components/tg-rich-text-input.js';

import {html} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import { TgEditor, createEditorTemplate} from '/resources/editors/tg-editor.js';

const additionalTemplate = html`
    <style>
        #input {
            cursor: text;
        }
        .title-action {
            display: none;
            width: 18px;
            height: 18px;
            margin-left: 4px;
        }
        label .title-action {
            cursor: pointer;
        }
        :host(:hover) .title-action,#decorator[focused] .title-action {
            display: unset;
        }
    </style>`;
const customLabelTemplate = html`
    <label id="editorLabel" style$="[[_calcLabelStyle(_editorKind, _disabled)]]" disabled$="[[_disabled]]" tooltip-text$="[[_getTooltip(_editingValue, entity)]]" slot="label">
        <span>[[propTitle]]</span>
        <iron-icon hidden$="[[noLabelFloat]]" id="copyIcon" class="title-action" icon="icons:content-copy" action-title="Copy" tooltip-text="Copy content" on-tap="_copyTap"></iron-icon>
        <!-- <iron-icon hidden$="[[noLabelFloat]]" id="markdownIcon" icon="editor:mode-edit" on-tap="_switchToMarkdownMode"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" id="htmlIcon" icon="icons:visibility" on-tap="_switchToWysiwyg"></iron-icon> -->
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:format-bold" action-title="Bold" tooltip-text="Make your text bold" on-tap="_makeBold"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:format-italic" action-title="Italic" tooltip-text="Italicize yor text" on-tap="_makeItalic"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:strikethrough-s" action-title="Strikethrough" tooltip-text="Cross text out by drawing a line through it" on-tap="_makeStrike"></iron-icon>
    </label>`;

const customInputTemplate = html`
    <tg-rich-text-input id="input" 
        class="custom-input paper-input-input"
        tooltip-text$="[[_getTooltip(_editingValue, entity)]]"
        disabled$="[[_disabled]]" 
        value="{{_editingValue}}"
        change-event-handler="[[_onChange]]"
        entity-type="[[entityType]]"
        property-name="[[propertyName]]"
        min-height="[[minHeight]]"
        height="[[height]]">
    </tg-rich-text-input>`;
const propertyActionTemplate = html`<slot id="actionSlot" name="property-action"></slot>`;

export class TgRichTextEditor extends TgEditor {

    static get template() { 
        return createEditorTemplate(additionalTemplate, html``, customInputTemplate, html``, html``, propertyActionTemplate, customLabelTemplate);
    }

    static get properties() {
        return {

            entityType: {
                type: String
            },

            minHeight: {
                type: String,
                value: "100px"
            },

            height: {
                type: String,
                value: "100px"
            }
        }
    }

    /**
     * This method converts string value to rich test object
     */
    convertFromString (strValue) {
        if (strValue === '') {
            return null;
        }

        return {'formattedText': strValue};
    }

    _switchToMarkdownMode(e) {
        this.$.input.switchToMarkdownMode();
    }

    _switchToWysiwyg(e) {
        this.$.input.switchToWysiwyg();
    }

    _makeBold(e) {
        this.$.input.applyBold();
    }
    
    _makeItalic(e) {
        this.$.input.applyItalic();
    }

    _makeStrike(e) {
        this.$.input.applyStrikethough();
    }

    /**
     * Returns tooltip for action
     */
    _getActionTooltip () {
        const actions = [...this.$.editorLabel.children].slice(1, 2);//remove first child that is lable title.
        const actionStr = actions.map(action => `<b>${action.getAttribute("action-title")}</b><br>${action.getAttribute("tooltip-text")}`);

        return `<div style='display:flex;'>
            <div style='margin-right:10px;'>With action: </div>
            <div style='flex-grow:1;'>${actionStr.join('<br><br>')}</div>
            </div>`
    }
}

customElements.define('tg-rich-text-editor', TgRichTextEditor);
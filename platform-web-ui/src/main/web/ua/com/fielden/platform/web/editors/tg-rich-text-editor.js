import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/components/tg-rich-text-input.js';

import { html } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import {GestureEventListeners} from '/resources/polymer/@polymer/polymer/lib/mixins/gesture-event-listeners.js';

import { TgEditor, createEditorTemplate } from '/resources/editors/tg-editor.js';
import { tearDownEvent, localStorageKey } from '/resources/reflection/tg-polymer-utils.js';

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
        #resizer {
            position: absolute;
            bottom: 0;
            right: 0;
            z-index: 21;
            --iron-icon-fill-color: var(--paper-grey-600);
        }
        #resizer:hover {
            cursor: ns-resize;
        }
        .noselect {
            -webkit-touch-callout: none;
            /* iOS Safari */
            -webkit-user-select: none;
            /* Safari */
            -khtml-user-select: none;
            /* Konqueror HTML */
            -moz-user-select: none;
            /* Firefox */
            -ms-user-select: none;
            /* Internet Explorer/Edge */
            user-select: none;
            /* Non-prefixed version, currently
               supported by Chrome and Opera */
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
        min-height="[[minHeight]]"
        height="[[_calcHeight(height, entityType, propertyName)]]">
    </tg-rich-text-input>
    <iron-icon id="resizer" icon="tg-icons:resize-bottom-right" on-tap="_resetHeight" on-down="_makeInputUnselectable" on-up="_makeInputSelectable" on-track="_resizeInput" tooltip-text="Drag to resize<br>Double tap to reset height"></iron-icon>`;
const propertyActionTemplate = html`<slot id="actionSlot" name="property-action"></slot>`;

export class TgRichTextEditor extends GestureEventListeners(TgEditor) {

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
            },

            withoutResizing: {
                type: Boolean,
                value: false
            }
        }
    }

    convertToString (value) {
        return (value && value.formattedText) || '';
    }
    /**
     * This method converts string value to rich test object
     */
    convertFromString (strValue) {
        if (strValue === '') {
            return null;
        }

        return {coreText: '', 'formattedText': strValue};
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

    _calcHeight(height, entityType, propertyName) {
        if (height && entityType && propertyName) {
            return this._readHeight() || height;
        }
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

    _resizeInput (event) {
        const target = event.target || event.srcElement;
        if (target === this.$.resizer && !this.withoutResizing) {
            switch (event.detail.state) {
                case 'start':
                    break;
                case 'track':
                    let newHeight = this.$.input.offsetHeight + event.detail.ddy;
                    if (newHeight < parseInt(this.minHeight)) {
                        newHeight = parseInt(this.minHeight);
                    }
                    this.$.input.height = newHeight + "px";
                    break;
                case 'end':
                    this._saveHeight(this.$.input.getHeight());
                    break;
            }
        }
        tearDownEvent(event);  
    }

    _makeInputUnselectable () {
        this.$.input.classList.toggle("noselect", true);
        this.$.input.makeReadOnly();
        document.styleSheets[0].insertRule('* { cursor: ns-resize !important; }', 0); // override custom cursors in all application with resizing cursor
    }

    _makeInputSelectable () {
        this.$.input.classList.toggle("noselect", false);
        this.$.input.makeEditable();
        if (document.styleSheets.length > 0 && document.styleSheets[0].cssRules.length > 0) {
            document.styleSheets[0].deleteRule(0);
        }
    }

    _resetHeight(e) {
        console.log("Height was reset");
    }

    _saveHeight(height) {
        localStorage.setItem(this._generateKey(), height);
    }

    _readHeight() {
        return localStorage.getItem(this._generateKey());
    }

    _generateKey() {
        return localStorageKey(`${this.entityType}_${this.propertyName}_height`);
    }
}

customElements.define('tg-rich-text-editor', TgRichTextEditor);
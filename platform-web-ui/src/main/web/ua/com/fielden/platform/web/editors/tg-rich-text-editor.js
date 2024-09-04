import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/images/tg-rich-text-editor-icons.js';
import '/resources/components/tg-rich-text-input.js';
import '/resources/components/tg-link-dialog.js';

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
        .dropdown-content {
            background-color: white;
            box-shadow: 0px 2px 6px #ccc;
        }
    </style>
    <iron-dropdown id="linkDropdown" vertical-align="top" horizontal-align="left" vertical-offset="13.5" always-on-top>
        <tg-link-dialog id="linkDialog" class="dropdown-content" slot="dropdown-content" cancel-callback="[[_cancelLinkInsertion]]" ok-callback="[[_acceptLink]]"></tg-link-dialog>
    </iron-dropdown>`;
const customLabelTemplate = html`
    <label id="editorLabel" style$="[[_calcLabelStyle(_editorKind, _disabled)]]" disabled$="[[_disabled]]" tooltip-text$="[[_getTooltip(_editingValue, entity)]]" slot="label">
        <span>[[propTitle]]</span>
        <iron-icon hidden$="[[noLabelFloat]]" id="copyIcon" class="title-action" icon="icons:content-copy" action-title="Copy" tooltip-text="Copy content" on-tap="_copyTap"></iron-icon>
        <!-- <iron-icon hidden$="[[noLabelFloat]]" id="markdownIcon" icon="editor:mode-edit" on-tap="_switchToMarkdownMode"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" id="htmlIcon" icon="icons:visibility" on-tap="_switchToWysiwyg"></iron-icon> -->
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="tg-rich-text-editor:header-1" action-title="Heading 1" tooltip-text="Make your text header 1" on-tap="_makeHeader1"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="tg-rich-text-editor:header-2" action-title="Heading 2" tooltip-text="Make your text header 2" on-tap="_makeHeader2"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="tg-rich-text-editor:header-3" action-title="Heading 3" tooltip-text="Make your text header 3" on-tap="_makeHeader3"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="tg-rich-text-editor:format-paragraph" action-title="Paragraph" tooltip-text="Make your text paragraph" on-tap="_makeParagraph"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:format-bold" action-title="Bold" tooltip-text="Make your text bold" on-tap="_makeBold"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:format-italic" action-title="Italic" tooltip-text="Italicize yor text" on-tap="_makeItalic"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:strikethrough-s" action-title="Strikethrough" tooltip-text="Cross text out by drawing a line through it" on-tap="_makeStrike"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:format-color-text" action-title="Font Color" tooltip-text="Change the color of your text" on-tap="_changeTextColor"></iron-icon>
        <iron-icon id="linkAction" hidden$="[[noLabelFloat]]" class="title-action" icon="editor:insert-link" action-title="Insert Link" tooltip-text="Insert link into your text" on-tap="_insertLink"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:format-indent-increase" action-title="Increase Indent" tooltip-text="Move your paragraph further away from the margin" on-tap="_indent"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:format-indent-decrease" action-title="Decrease Indent" tooltip-text="Move your paragraph closer to the margin" on-tap="_outdent"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:format-list-bulleted" action-title="Bullets" tooltip-text="Create a bulleted list" on-tap="_createBulletedList"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:format-list-numbered" action-title="Numbering" tooltip-text="Create a numbered list" on-tap="_createNumberedList"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="tg-rich-text-editor:list-checkbox" action-title="Task List" tooltip-text="Create a task list" on-tap="_createTaskList"></iron-icon>
    </label>`;

const customInputTemplate = html`
    <tg-rich-text-input id="input" 
        class="custom-input paper-input-input"
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
            },

            _cancelLinkInsertion: Function,
            _acceptLink: Function
        }
    }

    ready() {
        super.ready();
        this.$.linkDropdown.positionTarget = this.$.linkAction;
        this._cancelLinkInsertion = function () {
            this.$.linkDropdown.cancel();
        }.bind(this);
        this._acceptLink = function () {
            this.$.input.insertLink(this.$.linkDialog.url, this.$.linkDialog.linkText);
            this.$.linkDropdown.close();
        }.bind(this);
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

    _makeHeader1(e) {
        this.$.input.applyHeader1();
    }

    _makeHeader2(e) {
        this.$.input.applyHeader2();
    }

    _makeHeader3(e) {
        this.$.input.applyHeader3();
    }

    _makeParagraph(e) {
        this.$.input.applyParagraph();
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

    _changeTextColor(e) {
        this.$.input.applyColor('red');
    }

    _insertLink(e) {
        this.$.linkDropdown.open();
    }

    _indent(e) {
        this.$.input.applyIndent();
    }

    _outdent(e) {
        this.$.input.applyOutdent();
    }

    _createBulletedList(e) {
        this.$.input.createBulletList();
    }

    _createNumberedList(e) {
        this.$.input.createOrderedList();
    }

    _createTaskList(e) {
        this.$.input.createTaskList();
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
import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/images/tg-rich-text-editor-icons.js';
import '/resources/components/tg-rich-text-input.js';
import '/resources/components/tg-link-dialog.js';
import '/resources/components/tg-color-picker-dialog.js';

import { html } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import {GestureEventListeners} from '/resources/polymer/@polymer/polymer/lib/mixins/gesture-event-listeners.js';

import { TgEditor, createEditorTemplate } from '/resources/editors/tg-editor.js';
import { tearDownEvent, localStorageKey, getRelativePos } from '/resources/reflection/tg-polymer-utils.js';

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
    </iron-dropdown>
    <iron-dropdown id="colorDropdown" vertical-align="top" horizontal-align="left" vertical-offset="13.5" always-on-top>
        <tg-color-picker-dialog id="colorDialog" class="dropdown-content" slot="dropdown-content" cancel-callback="[[_cancelColorAction]]" ok-callback="[[_acceptColor]]"></tg-color-picker-dialog>
    </iron-dropdown>`;
const customLabelTemplate = html`
    <label id="editorLabel" style$="[[_calcLabelStyle(_editorKind, _disabled)]]" disabled$="[[_disabled]]" tooltip-text$="[[_getTooltip(_editingValue, entity)]]" slot="label">
        <span>[[propTitle]]</span>
        <iron-icon hidden$="[[noLabelFloat]]" id="copyIcon" class="title-action" icon="icons:content-copy" action-title="Copy" tooltip-text="Copy content" on-tap="_copyTap"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="tg-rich-text-editor:header-1" action-title="Heading 1" tooltip-text="Make your text header 1" on-tap="_makeHeader1"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="tg-rich-text-editor:header-2" action-title="Heading 2" tooltip-text="Make your text header 2" on-tap="_makeHeader2"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="tg-rich-text-editor:header-3" action-title="Heading 3" tooltip-text="Make your text header 3" on-tap="_makeHeader3"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="tg-rich-text-editor:format-paragraph" action-title="Paragraph" tooltip-text="Make your text paragraph" on-tap="_makeParagraph"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:format-bold" action-title="Bold" tooltip-text="Make your text bold, Ctrl+B, Meta+B" on-tap="_makeBold"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:format-italic" action-title="Italic" tooltip-text="Italicize yor text, Ctrl+I, Meta+I" on-tap="_makeItalic"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:strikethrough-s" action-title="Strikethrough" tooltip-text="Cross text out by drawing a line through it, Ctrl+S, Meta+S" on-tap="_makeStrike"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="icons:undo" action-title="Undo" tooltip-text="Undo last action, Ctrl+Z, Meta+Z" on-tap="_undo"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="icons:redo" action-title="Redo" tooltip-text="Redo last action, Ctrl+Shift+Z, Meta+Shift+Z" on-tap="_redo"></iron-icon>
        <iron-icon id="colorAction" hidden$="[[noLabelFloat]]" class="title-action" icon="editor:format-color-text" action-title="Font Color" tooltip-text="Change the color of your text" on-tap="_changeTextColor"></iron-icon>
        <iron-icon id="linkAction" hidden$="[[noLabelFloat]]" class="title-action" icon="editor:insert-link" action-title="Insert Link" tooltip-text="Insert link into your text" on-tap="_insertLink"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:format-indent-increase" action-title="Increase Indent" tooltip-text="Move your paragraph further away from the margin, Tab" on-tap="_indent"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:format-indent-decrease" action-title="Decrease Indent" tooltip-text="Move your paragraph closer to the margin, Shift+Tab" on-tap="_outdent"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:format-list-bulleted" action-title="Bullets" tooltip-text="Create a bulleted list, Ctrl+U, Meta+U" on-tap="_createBulletedList"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:format-list-numbered" action-title="Numbering" tooltip-text="Create a numbered list, Ctrl+O, Meta+O" on-tap="_createNumberedList"></iron-icon>
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
                value: "16px"
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
            _acceptLink: Function,

            _cancelColorAction: Function,
            _acceptColor: Function
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
        this.$.colorDropdown.positionTarget = this.$.colorAction;
        this._cancelColorAction = function() {
            this.$.colorDropdown.cancel();
        }.bind(this);
        this._acceptColor = function() {
            this.$.input.applyColor(this.$.colorDialog.color);
            this.$.colorDropdown.close();
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

    _undo() {
        this.$.input.undo();
    }

    _redo() {
        this.$.input.redo();
    }

    _changeTextColor(e) {
        // const element = this.$.input.getDomAtCaretPosition();
        // if (element.style.color) {
        //     this.$.colorDialog.color = element.style.color;
        // }
        if (this.$.input._prevSelection[0] != this.$.input._prevSelection[1]) {
            this.$.colorDropdown.open();
        }
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
                    const prevHeight = this.$.input.offsetHeight;
                    let newHeight = this.$.input.offsetHeight + event.detail.ddy;
                    //Adjust height if mouse is out of the scroll container
                    const scrollContainer = this._getScrollingParent();
                    const mousePos = scrollContainer && getRelativePos(event.detail.x, event.detail.y, scrollContainer);
                    if (scrollContainer && mousePos) {
                        if (mousePos.y > scrollContainer.offsetHeight) {
                            newHeight += mousePos.y - scrollContainer.offsetHeight;
                        } else if (mousePos.y < 0) {
                            newHeight += mousePos.y;
                        }
                    }
                    //Adjust new height if it less then resizer icon or min height of this editor
                    if (newHeight < this.$.resizer.offsetHeight) {
                        newHeight = this.$.resizer.offsetHeight;
                    }
                    if (newHeight < parseInt(this.minHeight)) {
                        newHeight = parseInt(this.minHeight);
                    }
                    this.$.input.height = newHeight + "px";
                    //scroll if needed
                    if (scrollContainer && mousePos) {
                        if (mousePos.y > scrollContainer.offsetHeight || mousePos.y < 0) {
                            scrollContainer.scrollTop += newHeight - prevHeight;
                        }
                    }
                    break;
                case 'end':
                    this._saveHeight(this.$.input.getHeight());
                    break;
            }
        }
        tearDownEvent(event);  
    }

    _getScrollingParent() {
        let parent = this;
        while (parent && parent.offsetHeight === parent.scrollHeight) {
            // go through parent elements (including going out from shadow DOM)
            parent = parent.assignedSlot || parent.parentElement || parent.getRootNode().host;
        }
        return parent;
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
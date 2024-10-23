import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/images/tg-rich-text-editor-icons.js';
import '/resources/components/rich-text/tg-rich-text-input.js';
import '/resources/components/tg-link-dialog.js';
import '/resources/components/tg-color-picker-dialog.js';

import { html } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import {GestureEventListeners} from '/resources/polymer/@polymer/polymer/lib/mixins/gesture-event-listeners.js';

import { TgEditor, createEditorTemplate } from '/resources/editors/tg-editor.js';
import { tearDownEvent, localStorageKey, getRelativePos, isMobileApp  } from '/resources/reflection/tg-polymer-utils.js';

function setDialogPosition(dialog, pos) {
    const dialogWidth = parseInt(dialog.style.width);
    const dialogHeight = parseInt(dialog.style.height);
    let x = (pos[0].left + pos[1].left) / 2 - dialogWidth / 2;
    let y = Math.max(pos[0].bottom, pos[1].bottom);

    const wWidth = getWindowWidth();
    const wHeight = getWindowHeight();

    if (x < 0) {
        x = 0; 
    } else if (x + dialogWidth > wWidth) {
        x = wWidth - dialogWidth;
    }

    if (y < 0) {
        y = 0;
    } else if (y + dialogHeight > wHeight) {
        const yAboveTheText = Math.min(pos[0].top, pos[1].top) - dialogHeight;
        y = Math.min(wHeight - dialogHeight, yAboveTheText);
    }

    dialog.horizontalOffset =  x;
    dialog.verticalOffset = y;
}

function getWindowWidth () {
    return window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
}

function getWindowHeight () {
    return window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
}

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
    <iron-dropdown id="linkDropdown" style="width:300px;height:160px;" vertical-align="top" horizontal-align="left" always-on-top on-iron-overlay-closed="_dialogClosed" on-iron-overlay-opened="_dialogOpened">
        <tg-link-dialog id="linkDialog" class="dropdown-content" slot="dropdown-content" cancel-callback="[[_cancelLinkInsertion]]" ok-callback="[[_acceptLink]]"></tg-link-dialog>
    </iron-dropdown>
    <iron-dropdown id="colorDropdown" style="width:300px;height:160px;" vertical-align="top" horizontal-align="left" always-on-top on-iron-overlay-closed="_dialogClosed" on-iron-overlay-opened="_dialogOpened">
        <tg-color-picker-dialog id="colorDialog" class="dropdown-content" slot="dropdown-content" cancel-callback="[[_cancelColorAction]]" ok-callback="[[_acceptColor]]"></tg-color-picker-dialog>
    </iron-dropdown>`;
const customLabelTemplate = html`
    <label id="editorLabel" style$="[[_calcLabelStyle(_editorKind, _disabled)]]" disabled$="[[_disabled]]" tooltip-text$="[[_getTooltip(_editingValue, entity)]]" slot="label">
        <span>[[propTitle]]</span>
        <iron-icon hidden$="[[noLabelFloat]]" id="copyIcon" class="title-action" icon="icons:content-copy" action-title="Copy" tooltip-text="Copy content" on-down="_preventEvent" on-tap="_copyTap"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="tg-rich-text-editor:header-1" action-title="Heading 1" tooltip-text="Make your text header 1" on-down="_preventEvent" on-tap="_makeHeader1"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="tg-rich-text-editor:header-2" action-title="Heading 2" tooltip-text="Make your text header 2" on-down="_preventEvent" on-tap="_makeHeader2"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="tg-rich-text-editor:header-3" action-title="Heading 3" tooltip-text="Make your text header 3" on-down="_preventEvent" on-tap="_makeHeader3"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="tg-rich-text-editor:format-paragraph" action-title="Paragraph" tooltip-text="Make your text paragraph" on-down="_preventEvent" on-tap="_makeParagraph"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:format-bold" action-title="Bold" tooltip-text="Make your text bold, Ctrl+B, &#x2318;+B" on-down="_preventEvent" on-tap="_makeBold"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:format-italic" action-title="Italic" tooltip-text="Italicize yor text, Ctrl+I, &#x2318;+I" on-down="_preventEvent" on-tap="_makeItalic"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:strikethrough-s" action-title="Strikethrough" tooltip-text="Cross text out by drawing a line through it, Ctrl+S, &#x2318;+S" on-down="_preventEvent" on-tap="_makeStrike"></iron-icon>
        <iron-icon id="colorAction" hidden$="[[noLabelFloat]]" class="title-action" icon="editor:format-color-text" action-title="Font Color" tooltip-text="Change the color of your text" on-down="_applyFakeSelect" on-tap="_changeTextColor"></iron-icon>
        <iron-icon id="linkAction" hidden$="[[noLabelFloat]]" class="title-action" icon="editor:insert-link" action-title="Insert Link" tooltip-text="Insert link into your text" on-down="_applyFakeSelect" on-tap="_toggleLink"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:format-list-bulleted" action-title="Bullets" tooltip-text="Create a bulleted list, Ctrl+U, &#x2318;+U" on-down="_preventEvent" on-tap="_createBulletedList"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="editor:format-list-numbered" action-title="Numbering" tooltip-text="Create a numbered list, Ctrl+O, &#x2318;+O" on-down="_preventEvent" on-tap="_createNumberedList"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="tg-rich-text-editor:list-checkbox" action-title="Task List" tooltip-text="Create a task list" on-down="_preventEvent" on-tap="_createTaskList"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="icons:undo" action-title="Undo" tooltip-text="Undo last action, Ctrl+Z, &#x2318;+Z" on-down="_preventEvent" on-tap="_undo"></iron-icon>
        <iron-icon hidden$="[[noLabelFloat]]" class="title-action" icon="icons:redo" action-title="Redo" tooltip-text="Redo last action, Ctrl+Y, &#x2318;+Y" on-down="_preventEvent" on-tap="_redo"></iron-icon>
    </label>`;

const customInputTemplate = html`
    <tg-rich-text-input id="input" 
        class="custom-input paper-input-input"
        disabled="[[_disabled]]" 
        value="{{_editingValue}}"
        change-event-handler="[[_onChange]]"
        key-down-handler="[[_onKeydown]]" 
        on-focus="_onFocus"
        on-blur="_outFocus"
        min-height="[[minHeight]]"
        height="[[_calcHeight(height, entityType, propertyName)]]"
        tabindex='0'>
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

            /**
             * OVERRIDDEN FROM TgEditor: this specific event is invoked after some key has been pressed.
             *
             */
            _onKeydown: {
                type: Function,
                value: function () {
                    return this._handleCopy.bind(this);
                }
            },

            _cancelLinkInsertion: Function,
            _acceptLink: Function,

            _cancelColorAction: Function,
            _acceptColor: Function
        }
    }

    ready() {
        super.ready();
        this.$.linkDropdown.positionTarget = document.body;
        this._cancelLinkInsertion = function () {
            this.$.linkDropdown.cancel();
            this.$.input.focusEditor();
        }.bind(this);
        this._acceptLink = function () {
            this.$.input.toggleLink(this.$.linkDialog.url, this.$.linkDialog.linkText || this.$.linkDialog.url);
            this.$.linkDropdown.close();
        }.bind(this);
        this.$.colorDropdown.positionTarget = document.body;
        this._cancelColorAction = function() {
            this.$.colorDropdown.cancel();
            this.$.input.focusEditor();
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

    _preventEvent(e) {
        e.preventDefault();
    }

    _applyFakeSelect(e) {
        this.$.input.fakeSelect();
    }

    _makeHeader1(e) {
        this.$.input.applyHeader1(e);
    }

    _makeHeader2(e) {
        this.$.input.applyHeader2(e);
    }

    _makeHeader3(e) {
        this.$.input.applyHeader3(e);
    }

    _makeParagraph(e) {
        this.$.input.applyParagraph(e);
    }

    _makeBold(e) {
        this.$.input.applyBold(e);
    }
    
    _makeItalic(e) {
        this.$.input.applyItalic(e);
    }

    _makeStrike(e) {
        this.$.input.applyStrikethough(e);
    }

    _undo(e) {
        this.$.input.undo(e);
    }

    _redo(e) {
        this.$.input.redo(e);
    }

    _changeTextColor(e) {
        this.$.input.scrollIntoView();
        const textColorObj = this.$.input.initColorEditing();
        this.$.input.fakeSelect();
        if (textColorObj) {
            this.$.colorDialog.color = textColorObj.detail;
        }
        setDialogPosition(this.$.colorDropdown, this.$.input.getSelectionCoordinates());
        this.$.colorDropdown.open();
    }

    _toggleLink(e) {
        this.$.input.scrollIntoView();
        const link = this.$.input.initLinkEditing();
        this.$.input.fakeSelect();
        if (link) {
            this.$.linkDialog.url = link.detail;
            this.$.linkDialog.linkText = link.text;
        }
        setDialogPosition(this.$.linkDropdown, this.$.input.getSelectionCoordinates());
        this.$.linkDropdown.open();
    }

    _createBulletedList(e) {
        this.$.input.createBulletList(e);
        this.$.input.scrollIntoView();
    }

    _createNumberedList(e) {
        this.$.input.createOrderedList(e);
        this.$.input.scrollIntoView();
    }

    _createTaskList(e) {
        this.$.input.createTaskList(e);
        this.$.input.scrollIntoView();
    }

    _calcHeight(height, entityType, propertyName) {
        if (height && entityType && propertyName) {
            return this._readHeight() || height;
        }
    }

    _formatTooltipText(value) {
        return '';
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
        this.$.input.makeEditable(false);
        document.styleSheets[0].insertRule('* { cursor: ns-resize !important; }', 0); // override custom cursors in all application with resizing cursor
    }

    _makeInputSelectable () {
        this.$.input.classList.toggle("noselect", false);
        this.$.input.makeEditable(!this._disabled);
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

    _dialogClosed(e) {
        if (e.composedPath()[0].tagName == "IRON-DROPDOWN") {
            const dropDownContent = e.composedPath()[0].$.content.assignedNodes()[0];
            if (dropDownContent && dropDownContent.resetState) {
                dropDownContent.resetState();
                this.$.input.fakeUnselect();
            }
        }
    }

    _dialogOpened(e) {
        if (e.composedPath()[0].tagName == "IRON-DROPDOWN") {
            const dropDownContent = e.composedPath()[0].$.content.assignedNodes()[0];
            if (dropDownContent && dropDownContent.focusDefaultEditor && !isMobileApp()) {
                dropDownContent.focusDefaultEditor();
            }
        }
    }

    _copyTap() {
        // copy to clipboard should happen only if there is something to copy
        if (navigator.clipboard && this._editingValue) {
            navigator.clipboard.writeText(this._editingValue);
            this._showCheckIconAndToast(`<div class="toastui-editor-contents">${this._editingValue}</div>`);
        } else if (this.toaster) {
            this.toaster.openToastWithoutEntity("Nothing to copy", true, "There was nothing to copy.", false);
        }
    }
}

customElements.define('tg-rich-text-editor', TgRichTextEditor);
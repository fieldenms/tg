
import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import {mixinBehaviors} from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';
import { IronA11yKeysBehavior } from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';

import { TgTooltipBehavior } from '/resources/components/tg-tooltip-behavior.js';
import { tearDownEvent, isMobileApp } from '/resources/reflection/tg-polymer-utils.js';
import { excludeErrors } from '/resources/components/tg-global-error-handler.js';

import '/resources/toastui-editor/toastui-editor-all.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-icons/editor-icons.js';
import '/resources/polymer/@polymer/iron-dropdown/iron-dropdown.js';

import '/resources/polymer/@polymer/paper-styles/paper-styles.js';

import '/resources/components/rich-text/tg-rich-text-input-enhanced-styles.js';
import '/resources/images/tg-rich-text-editor-icons.js';
import '/resources/egi/tg-responsive-toolbar.js';

excludeErrors( e => e.filename && e.filename.includes("toastui-editor-all") && e.error && e.error.name === 'TransformError');

function fakeSelection(context, options) {

    return {
        wysiwygCommands: {
            fakeSelect: ({from, to}, { tr, schema }, dispatch) => {
                const mark = schema.marks.mark.create();
                tr.addMark(from, to, mark).setMeta("addToHistory", false);
                dispatch(tr);
                return true;
            },
            fakeUnselect: ({from, to}, state, dispatch) => {
                const markType = state.schema.marks.mark;
                if (dispatch) {
                    let tr = state.tr;
                    const has = state.doc.rangeHasMark(from, to, markType);
                    if (has) {
                        tr.removeMark(from, to, markType).setMeta("addToHistory", false);
                    }
                    dispatch(tr);
                }
                return true;
            },
        },
        toHTMLRenderers: {
            htmlInline: {
                mark(node, { entering }) {
                    return entering
                        ? { type: 'openTag', tagName: 'mark', attributes: node.attrs }
                        : { type: 'closeTag', tagName: 'mark' };
                },
            },
        },
    }
}

function colorTextPlugin(context, options) {
    //The following method was copied from prosemirror-commands module
    function markApplies(doc, ranges, type) {
        for (let i = 0; i < ranges.length; i++) {
            let { $from, $to } = ranges[i];
            let can = $from.depth == 0 ? doc.type.allowsMarkType(type) : false;
            doc.nodesBetween($from.pos, $to.pos, node => {
                if (can) {
                    return false;
                }
                can = node.inlineContent && node.type.allowsMarkType(type);
            });
            if (can) {
                return true;
            }
        }
        return false
    }

    return {
        wysiwygCommands: {
            color: ({ selectedColor }, { tr, selection, schema }, dispatch) => {
                if (selectedColor) {
                    const { from, to } = selection;
                    const attrs = { htmlAttrs: { style: `color: ${selectedColor} !important` } };
                    const mark = schema.marks.span.create(attrs);

                    tr.addMark(from, to, mark);
                    dispatch(tr.scrollIntoView());

                    return true;
                }
                return false;
            },
            clearColor: (payload, state, dispatch) => {
                //The following logic was copied from prosemirror-commands module and tuned to requirements of this method
                const markType = state.schema.marks.span;
                let { empty, $cursor, ranges } = state.selection;
                if ((empty && !$cursor) || !markApplies(state.doc, ranges, markType)) {
                    return false;
                } 
                if (dispatch) {
                    if ($cursor) {
                        if (markType.isInSet(state.storedMarks || $cursor.marks())) {
                            dispatch(state.tr.removeStoredMark(markType));
                        } 
                    } else {
                        let has = false, tr = state.tr;
                        for (let i = 0; !has && i < ranges.length; i++) {
                            let { $from, $to } = ranges[i];
                            has = state.doc.rangeHasMark($from.pos, $to.pos, markType);
                        }
                        for (let i = 0; i < ranges.length; i++) {
                            let { $from, $to } = ranges[i]
                            if (has) {
                                tr.removeMark($from.pos, $to.pos, markType);
                            }
                        }
                        dispatch(tr);
                    }
                }
                return true;
            },
        },
        toHTMLRenderers: {
            htmlInline: {
                span(node, { entering }) {
                    return entering
                        ? { type: 'openTag', tagName: 'span', attributes: node.attrs }
                        : { type: 'closeTag', tagName: 'span' };
                },
            },
        },
    };
}

let currentTooltipElement = null;
function mouseOverHandler(e) {
    const a = findParentBy.bind(this)(e.target, isLink);
    if (a) {
        if (currentTooltipElement !== a) {
            this._hideTooltip();
        }
        this.showTooltip(`${a.getAttribute('href')}<br>Ctrl+Click, &#x2318;+Click or long touch to open link`);
    } else {
        this._hideTooltip();
    }
    currentTooltipElement = a;
};

function isLink(element) {
    return element.hasAttribute && element.hasAttribute('href')
}

function isColoredSpan(element) {
    return element.tagName && element.tagName === 'SPAN' && element.style.color
}

function findParentBy(element, predicate) {
    let parent = element;
    while (parent && !predicate(parent) && parent !== this._getEditableContent() ) {
        parent = parent.parentElement;
    }
    return parent && predicate(parent) ? parent : null;
}

let mouseTimer = null;
let longPress = false;
let shortPress = false;

function runLinkIfPossible(el) {
    const a = findParentBy.bind(this)(el, isLink);
    if (a) {
        const w = window.open(a.getAttribute('href'));
        w.focus();
    }
}

function mouseDownHandler(e) {
    if ((e.button == 0 && (e.ctrlKey || e.metaKey)) || e.type.startsWith("touch")) {
        longPress = false;
        shortPress = true;
        const el = e.target;
        mouseTimer = setTimeout(() => {
            longPress = true;
            shortPress = false;
            setTimeout( () => {runLinkIfPossible.bind(this)(el)}, 1);
        }, 1000);
    }
}

function mouseUpHandler(e) {
    if (e.button == 0 || e.type.startsWith("touch")) {
        if (mouseTimer) {
            clearTimeout(mouseTimer);
        }
        if (shortPress && !longPress && (e.ctrlKey || e.metaKey)) {
            const el = e.target;
            setTimeout( () => {runLinkIfPossible.bind(this)(el)}, 150);
        }
        longPress = false;
        shortPress = false;
        mouseTimer = null;
    }
}

function getElementToEdit(predicate, extractor) {
    const selection = this._editor.getSelection();
    if (selection) {
        if (selection[0] === selection[1]) {
            //It means that only caret postion was set (no selection). Then take text and url from dom at caret position if it exists
            const node = this._editor.wwEditor.view.domAtPos(selection[0], 1).node;
            const element = findParentBy.bind(this)(node, predicate);
            if (element && element.pmViewDesc) {
                const text = this._editor.getSelectedText(element.pmViewDesc.posAtStart, element.pmViewDesc.posAtEnd);
                return {pos: [element.pmViewDesc.posAtStart, element.pmViewDesc.posAtEnd], text: text, detail: extractor(element)};
            }
        } else {
            //This branch indicates that user has selected some text or even nodes, therefore the text should be taken from selection
            // and url from the first <a> tag in selection
            const text = this._editor.getSelectedText(selection[0], selection[1]);
            const nodes = [];
            for (let i = selection[0]; i <= selection[1]; i++) {
                const node = this._editor.wwEditor.view.domAtPos(i, selection[1] - i).node;
                if (node) {
                    nodes.push(node);
                }
            }
            const element = nodes.map(node => findParentBy.bind(this)(node, predicate)).find(a => a);
            return (element && {text: text, detail: extractor(element)}) || {text: text, detail: ''};
        }
    }
}

function handleTaskListItemStatusChange(e) {
    const pos = this._editor.wwEditor.view.posAtCoords({left:e.clientX, top:e.clientY});
    const node = pos && this._editor.wwEditor.view.domAtPos(pos.pos, pos.inside);
    if (node && node.node.hasAttribute && node.node.hasAttribute('data-task')) {
        const style = getComputedStyle(node.node, ':before');
        if (isPositionInBox(style, e.offsetX, e.offsetY)) {
            this.changeEventHandler();
            if (this.shadowRoot.activeElement && isMobileApp()) {
                this._editor.setSelection(node.node.pmViewDesc.posAtStart, node.node.pmViewDesc.posAtStart);
            }
        }
    }
}

function isPositionInBox(style, offsetX, offsetY) {
    const left = parseInt(style.left, 10);
    const top = parseInt(style.top, 10);
    const width = parseInt(style.width, 10) + parseInt(style.paddingLeft, 10) + parseInt(style.paddingRight, 10);
    const height = parseInt(style.height, 10) + parseInt(style.paddingTop, 10) + parseInt(style.paddingBottom, 10);
  
    return offsetX >= left && offsetX <= left + width && offsetY >= top && offsetY <= top + height;
}

function focusOnKeyDown(event) {
    if (event.keyCode === 13 && !this.shadowRoot.activeElement) {
        this._editor.moveCursorToStart(true);
    }
}

function focusEditor() {
    if (this._editor) {
        this._editor.focus();
    }
}

function focusView() {
    if (this._editor) {
        this._editor.wwEditor.view.focus();
    }
}

function editElement(predicate, extractor) {
    const element = getElementToEdit.bind(this)(predicate, extractor);
    if (element) {
        if (element.pos) {
            this._editor.setSelection(element.pos[0], element.pos[1]);
        }
        return element;
    }
}

function rgbToHex(rgbString) {
    return "#" + rgbString
        .split("(")[1]
        .split(")")[0]
        .split(",")
        .map(colorComponent => {            
            const parsedColor = parseInt(colorComponent).toString(16); //Convert to a base16 string
            return (parsedColor.length === 1) ? "0" + parsedColor : parsedColor;
        })
        .join("");
}

function preventListIdentation(event) {
    if (event.keyCode === 9 && getElementToEdit.bind(this)(el => el.tagName && el.tagName === 'LI', el => el)) {
        event.preventDefault();
    }
}

function getEditorHTMLText() {
    return this._editor.getHTML().replace(/<mark>(.*?)<\/mark>/g, '$1');
}

function applyFakeSelection(selection) {
    if (selection && selection[0] !== selection[1]) {
        this._editor.exec('fakeSelect', {from: selection[0], to: selection[1]});
    }
}

function applyFakeUnselection(selection) {
    this._editor.exec('fakeUnselect', {from: selection[0], to: selection[1]});
}

function initLinkEditing() {
    return editElement.bind(this)(isLink, el => el.getAttribute('href'));
}

function initColorEditing() {
    return editElement.bind(this)(isColoredSpan, el => rgbToHex(el.style.color));
}

function toggleLink(url, text) {
    const selection = this._editor.getSelection();
    if (selection && selection[0] !== selection[1] && !url) {
        this._editor.exec('toggleLink');
    } else {
        this._editor.exec('addLink', { linkUrl: url, linkText: text });
    }
    this.changeEventHandler();
}

 function applyColor(selectedColor) {
    focusView.bind(this)();
    if (selectedColor) {
        this._editor.exec("color", {selectedColor: selectedColor});
    } else {
        this._editor.exec('clearColor');
    }
    this.changeEventHandler();
}

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

function getSelectionCoordinates() {
    if (this._editor && this._editor.getSelection()) {
        const view = this._editor.wwEditor.view;
        const selection = this._editor.getSelection();
        return [view.coordsAtPos(selection[0]), view.coordsAtPos(selection[1])];
    }
}

function scrollIntoView() {
    this._editor.wwEditor.view.dispatch(this._editor.wwEditor.view.state.tr.scrollIntoView());
}

const template = html`
    <style include='rich-text-enhanced-styles'>
        :host {
            @apply --layout-vertical;
        }
        ::selection {
            color: currentcolor;
            background-color: rgba(31,  176, 255, 0.3);
        }
        mark {
            display: inline-block;
            background-color: rgba(31,  176, 255, 0.3);
            color: inherit;
        }
        del a mark {
            text-decoration: line-through underline !important;
        }
        del mark {
            text-decoration: line-through;  
        }
        a mark {
            text-decoration: underline;
        }
        .editor-toolbar {
            overflow: hidden;
            padding-top: 8px;
        }
        .custom-responsive-toolbar {
            --tg-responsove-toolbar-expand-button: {
                padding: 0;
                width: 18px;
                height: 18px;
                color:var(--paper-input-container-color, var(--secondary-text-color));
            };
            --tg-responsove-toolbar-dropdown-content: {
                padding: 8px 0 8px 8px;
            }
        }
        .toolbar-action {
            flex-shrink: 0;
            width: 18px;
            height: 18px;
            /*margin-right: 8px;*/
            cursor: pointer;
            color: var(--paper-input-container-color, var(--secondary-text-color));
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
    </iron-dropdown>
    <tg-responsive-toolbar class="custom-responsive-toolbar editor-toolbar">
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="tg-rich-text-editor:header-1" action-title="Heading 1" tooltip-text="Make your text header 1" on-down="_stopMouseEvent" on-tap="_applyHeader1"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="tg-rich-text-editor:header-2" action-title="Heading 2" tooltip-text="Make your text header 2" on-down="_stopMouseEvent" on-tap="_applyHeader2"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="tg-rich-text-editor:header-3" action-title="Heading 3" tooltip-text="Make your text header 3" on-down="_stopMouseEvent" on-tap="_applyHeader3"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="tg-rich-text-editor:format-paragraph" action-title="Paragraph" tooltip-text="Make your text paragraph" on-down="_stopMouseEvent" on-tap="_applyParagraph"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="editor:format-bold" action-title="Bold" tooltip-text="Make your text bold, Ctrl+B, &#x2318;+B" on-down="_stopMouseEvent" on-tap="_applyBold"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="editor:format-italic" action-title="Italic" tooltip-text="Italicize yor text, Ctrl+I, &#x2318;+I" on-down="_stopMouseEvent" on-tap="_applyItalic"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="editor:strikethrough-s" action-title="Strikethrough" tooltip-text="Cross text out by drawing a line through it, Ctrl+S, &#x2318;+S" on-down="_stopMouseEvent" on-tap="_applyStrikethough"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="editor:format-color-text" action-title="Font Color" tooltip-text="Change the color of your text" on-down="_applyFakeSelect" on-tap="_changeTextColor"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="editor:insert-link" action-title="Insert Link" tooltip-text="Insert link into your text" on-down="_applyFakeSelect" on-tap="_toggleLink"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="editor:format-list-bulleted" action-title="Bullets" tooltip-text="Create a bulleted list, Ctrl+U, &#x2318;+U" on-down="_stopMouseEvent" on-tap="_createBulletList"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="editor:format-list-numbered" action-title="Numbering" tooltip-text="Create a numbered list, Ctrl+O, &#x2318;+O" on-down="_stopMouseEvent" on-tap="_createOrderedList"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="tg-rich-text-editor:list-checkbox" action-title="Task List" tooltip-text="Create a task list" on-down="_stopMouseEvent" on-tap="_createTaskList"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="icons:undo" action-title="Undo" tooltip-text="Undo last action, Ctrl+Z, &#x2318;+Z" on-down="_stopMouseEvent" on-tap="_undo"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="icons:redo" action-title="Redo" tooltip-text="Redo last action, Ctrl+Y, &#x2318;+Y" on-down="_stopMouseEvent" on-tap="_redo"></iron-icon>
    </tg-responsive-toolbar>
    <div id="editor"></div>`; 

class TgRichTextInput extends mixinBehaviors([IronResizableBehavior, IronA11yKeysBehavior, TgTooltipBehavior], PolymerElement) {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            value: {
                type: String,
                observer: "_valueChanged",
                notify: true,
            },

            changeEventHandler: {
                type: Function,
                value: null
            },

            keyDownHandler: {
                type:Function,
                value: null
            },

            height: {
                type: String,
                observer: "_heightChanged"
            },

            minHeight: {
                type: String,
                observer: "_minHeightChanged"
            },

            disabled: {
                type: Boolean,
                value: false,
                reflectToAttribute: true
            },

            _editor: Object,
            _fakeSelection: {
                type: Array,
            },

            _cancelLinkInsertion: Function,
            _acceptLink: Function,

            _cancelColorAction: Function,
            _acceptColor: Function
        }
    }

    static get observers() {
        return ["_disabledChanged(disabled, _editor)"]
    }

    ready() {
        super.ready();
        //Initialise link and color dialogs
        this.$.linkDropdown.positionTarget = document.body;
        this._cancelLinkInsertion = function () {
            this.$.linkDropdown.cancel();
            focusEditor.bind(this)();
        }.bind(this);
        this._acceptLink = function () {
            const link = initLinkEditing.bind(this)();
            toggleLink.bind(this)(this.$.linkDialog.url, (link && link.text) || this.$.linkDialog.url);
            this.$.linkDropdown.close();
        }.bind(this);
        this.$.colorDropdown.positionTarget = document.body;
        this._cancelColorAction = function() {
            this.$.colorDropdown.cancel();
            focusEditor.bind(this)();
        }.bind(this);
        this._acceptColor = function() {
            initColorEditing.bind(this)();
            applyColor.bind(this)(this.$.colorDialog.color);
            this.$.colorDropdown.close();
        }.bind(this);
        // Create editor
        this._editor = new toastui.Editor({
            el: this.$.editor,
            height: this.height,
            minHeight: this.minHeight,
            initialEditType: 'wysiwyg',
            events: {
                change: this._htmlContentChanged.bind(this),
                blur: this._focusLost.bind(this),
                focus: this._focusGain.bind(this),
                keydown: (viewType, event) => this.keyDownHandler(event)
            },
            plugins: [colorTextPlugin, fakeSelection],
            linkAttributes: {target: "_blank"},
            useCommandShortcut: false,
            usageStatistics: false,
            toolbarItems: [],
            hideModeSwitch: true
        });
        //trigger tooltips manually
        this.triggerManual = true;
        //The following code is nedded to preserve whitespaces after loading html into editor.
        this._editor.wwEditor.schema.cached.domParser.rules.forEach(r => r.preserveWhitespace = "full");
        //Make editable container not tabbable. It will remain focusable with mouse pointer. 
        this._getEditableContent().setAttribute('tabindex', '-1');
        //Add event listeners for tooltips on editor
        this._getEditableContent().addEventListener("mouseover", mouseOverHandler.bind(this));
        //Add event listener to handle case when clicking on task list checkbox
        this._getEditableContent().addEventListener("mousedown", handleTaskListItemStatusChange.bind(this));
        //Add event listeners to make link clickable and with proper cursor
        this._getEditableContent().addEventListener("mousedown", mouseDownHandler.bind(this));
        this._getEditableContent().addEventListener("mouseup", mouseUpHandler.bind(this));
        this._getEditableContent().addEventListener("touchstart", mouseDownHandler.bind(this));
        this._getEditableContent().addEventListener("touchend", mouseUpHandler.bind(this));
        //Add key down to prevent tab key on list
        this._getEditableContent().addEventListener("keydown", preventListIdentation.bind(this), true);
        //Initiate key binding and key event target
        this.addOwnKeyBinding('ctrl+b meta+b', '_applyBold');
        this.addOwnKeyBinding('ctrl+i meta+i', '_applyItalic');
        this.addOwnKeyBinding('ctrl+s meta+s', '_applyStrikethough');
        this.addOwnKeyBinding('ctrl+z meta+z', '_undo');
        this.addOwnKeyBinding('ctrl+y meta+y', '_redo');
        this.addOwnKeyBinding('tab', '_stopKeyboradEvent');
        this.addOwnKeyBinding('shift+tab', '_stopKeyboradEvent');
        this.addOwnKeyBinding('ctrl+u meta+u', '_createBulletList');
        this.addOwnKeyBinding('ctrl+o meta+o', '_createOrderedList');
        this.addOwnKeyBinding('esc', '_stopEditing');
        this.keyEventTarget = this._getEditableContent();
        //Adjust key event handler to be able to process events from _editor when event was prevented
        const prevKeyBindingHandler = this._onKeyBindingEvent.bind(this);
        this._onKeyBindingEvent = function (keyBindings, event) {
            Object.defineProperty(event, 'defaultPrevented', {value: false})
            prevKeyBindingHandler(keyBindings, event);
        };
        this.addEventListener('keydown', focusOnKeyDown.bind(this));
    }

    makeEditable(editable) {
        this._getEditableContent().setAttribute("contenteditable", editable + "");
    }

    getHeight() {
        return this._editor && this._editor.getHeight();
    }

    _stopKeyboradEvent(event) {
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    _stopMouseEvent(e) {
        e.preventDefault();
    }

    _applyHeader1(event) {
        this._editor.exec('heading', { level: 1 });
    }

    _applyHeader2(event) {
        this._editor.exec('heading', { level: 2 });
    }

    _applyHeader3(event) {
        this._editor.exec('heading', { level: 3 });
    }

    _applyParagraph(event) {
        this._editor.exec('heading', { level: 0 });
    }

    _applyBold(event) {
        this._editor.exec('bold');
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    _applyItalic(event) {
        this._editor.exec('italic');
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    _applyStrikethough(event) {
        this._editor.exec('strike');
        const selection = this._editor.getSelection();
        if (selection && selection[0] !== selection[1]) {
            tearDownEvent(event.detail && event.detail.keyboardEvent);
        }
    }

    _changeTextColor(e) {
        scrollIntoView.bind(this)();
        const textColorObj = initColorEditing.bind(this)();
        this._applyFakeSelect();
        if (textColorObj) {
            this.$.colorDialog.color = textColorObj.detail;
        }
        setDialogPosition(this.$.colorDropdown, getSelectionCoordinates.bind(this)());
        this.$.colorDropdown.open();
    }

    _toggleLink(e) {
        scrollIntoView.bind(this)();
        const link = initLinkEditing.bind(this)();
        this._applyFakeSelect();
        if (link) {
            this.$.linkDialog.url = link.detail;
        }
        setDialogPosition(this.$.linkDropdown, getSelectionCoordinates.bind(this)());
        this.$.linkDropdown.open();
    }

    _undo(event) {
        this._editor.exec('undo');
        this.changeEventHandler();
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    _redo(event) {
        this._editor.exec('redo');
        this.changeEventHandler();
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    _createBulletList(e) {
        this._editor.exec('bulletList');
        tearDownEvent(e.detail && e.detail.keyboardEvent);
        scrollIntoView.bind(this)();
    }

    _createOrderedList(e) {
        this._editor.exec('orderedList');
        tearDownEvent(e.detail && e.detail.keyboardEvent);
        scrollIntoView.bind(this)();
    }

    _createTaskList(e) {
        this._editor.exec('taskList');
        scrollIntoView.bind(this)();
    }

    _applyFakeSelect() {
        this._fakeSelection = this._editor.getSelection();
        applyFakeSelection.bind(this)(this._fakeSelection);
    }

    _applyFakeUnselect() {
        applyFakeUnselection.bind(this)(this._fakeSelection);
        delete this._fakeSelection;
    }

    _stopEditing(event) {
        const selection = this._editor.getSelection();
        const cursorPosition = selection ? selection[1] : 0;
        this._editor.setSelection(cursorPosition, cursorPosition);
        this.focus();
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    _valueChanged(newValue) {
        if(this._editor && newValue !== getEditorHTMLText.bind(this)()) {
            this._editor.setHTML(newValue, false);
        }
    }

    _htmlContentChanged(e) {
        const htmlText = getEditorHTMLText.bind(this)();
        if (this.value !== htmlText) {
            this.value = htmlText;
        }
    }

    _focusLost(e) {
        this.changeEventHandler(e);
        if (this._fakeSelection) {
            this._fakeSelection = this._editor.getSelection();
            applyFakeSelection.bind(this)(this._fakeSelection);
        }
    }
    
    _focusGain(e) {
        if (this._fakeSelection) {
            applyFakeUnselection.bind(this)(this._fakeSelection);
        }
    }

    _heightChanged(newHeight) {
        if (this._editor) {
            this._editor.setHeight(newHeight);
            this.fire("iron-resize", {
                node: this,
                bubbles: true,
            });
        }
    }

    _minHeightChanged(newMinHeight) {
        if (this._editor) {
            this._editor.setMinHeight(newMinHeight);
        }
    }

    _disabledChanged(newDisabled, _editor) {
        if (_editor) {
            this.makeEditable(!newDisabled);
        }
    }

    _getEditableContent() {
        return this._editor.getEditorElements().wwEditor.children[0];
    }

    _dialogClosed(e) {
        if (e.composedPath()[0].tagName == "IRON-DROPDOWN") {
            const dropDownContent = e.composedPath()[0].$.content.assignedNodes()[0];
            if (dropDownContent && dropDownContent.resetState) {
                dropDownContent.resetState();
                this._applyFakeUnselect();
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

    _getActionStyle() {
        return `width: 18px;height:18px;cursor:pointer;margin-right:8px;color:var(--paper-input-container-color, var(--secondary-text-color));`;
    }
}

customElements.define('tg-rich-text-input', TgRichTextInput);
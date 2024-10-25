
import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import {mixinBehaviors} from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';
import '/resources/toastui-editor/toastui-editor-all.js';

import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';
import { IronA11yKeysBehavior } from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';

import '/resources/components/rich-text/tg-rich-text-input-enhanced-styles.js';

import { TgTooltipBehavior } from '/resources/components/tg-tooltip-behavior.js';
import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js';
import { excludeErrors } from '/resources/components/tg-global-error-handler.js';

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

function focusEditor(event) {
    if (event.keyCode === 13 && !this.shadowRoot.activeElement) {
        this._editor.moveCursorToStart(true);
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

const template = html`
    <style include='rich-text-enhanced-styles'>
        ::selection {
            color: currentcolor;
            background-color: rgba(31,  176, 255, 0.3);
        }
        mark {
            display: inline-block;
            background-color: rgba(31,  176, 255, 0.3);
            color: unset;
        }
    </style>
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
            _fakeSelection:{
                type: Array,
                observer: "_fakeSelectionChanged"
            }
        }
    }

    static get observers() {
        return ["_disabledChanged(disabled, _editor)"]
    }

    ready() {
        super.ready();
        this._editor = new toastui.Editor({
            el: this.$.editor,
            height: this.height,
            minHeight: this.minHeight,
            initialEditType: 'wysiwyg',
            events: {
                change: this._htmlContentChanged.bind(this),
                caretChange: this._updateFakeSelection.bind(this),
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
        this.addOwnKeyBinding('ctrl+b meta+b', 'applyBold');
        this.addOwnKeyBinding('ctrl+i meta+i', 'applyItalic');
        this.addOwnKeyBinding('ctrl+s meta+s', 'applyStrikethough');
        this.addOwnKeyBinding('ctrl+z meta+z', 'undo');
        this.addOwnKeyBinding('ctrl+y meta+y', 'redo');
        this.addOwnKeyBinding('tab', 'stopEvent');
        this.addOwnKeyBinding('shift+tab', 'stopEvent');
        this.addOwnKeyBinding('ctrl+u meta+u', 'createBulletList');
        this.addOwnKeyBinding('ctrl+o meta+o', 'createOrderedList');
        this.addOwnKeyBinding('esc', 'stopEditing');
        this.keyEventTarget = this._getEditableContent();
        //Adjust key event handler to be able to process events from _editor when event was prevented
        const prevKeyBindingHandler = this._onKeyBindingEvent.bind(this);
        this._onKeyBindingEvent = function (keyBindings, event) {
            Object.defineProperty(event, 'defaultPrevented', {value: false})
            prevKeyBindingHandler(keyBindings, event);
        };
        this.addEventListener('keydown', focusEditor.bind(this));
    }

    scrollIntoView() {
        this._editor.wwEditor.view.dispatch(this._editor.wwEditor.view.state.tr.scrollIntoView());
    }

    stopEvent(event) {
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    applyHeader1(event) {
        this._editor.exec('heading', { level: 1 });
    }

    applyHeader2(event) {
        this._editor.exec('heading', { level: 2 });
    }

    applyHeader3(event) {
        this._editor.exec('heading', { level: 3 });
    }

    applyParagraph(event) {
        this._editor.exec('heading', { level: 0 });
    }

    applyBold(event) {
        this._editor.exec('bold');
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    applyItalic(event) {
        this._editor.exec('italic');
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    applyStrikethough(event) {
        this._editor.exec('strike');
        const selection = this._editor.getSelection();
        if (selection && selection[0] !== selection[1]) {
            tearDownEvent(event.detail && event.detail.keyboardEvent);
        }
    }

    undo(event) {
        this._editor.exec('undo');
        this.changeEventHandler();
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    redo(event) {
        this._editor.exec('redo');
        this.changeEventHandler();
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    initLinkEditing() {
        return editElement.bind(this)(isLink, el => el.getAttribute('href'));
    }

    initColorEditing() {
        return editElement.bind(this)(isColoredSpan, el => rgbToHex(el.style.color));
    }

    toggleLink(url, text) {
        const selection = this._editor.getSelection();
        if (selection && selection[0] !== selection[1] && !url) {
            this._editor.exec('toggleLink');
        } else {
            this._editor.exec('addLink', { linkUrl: url, linkText: text });
        }
        this.changeEventHandler();
    }

    applyColor(selectedColor) {
        this.focusView();
        if (selectedColor) {
            this._editor.exec("color", {selectedColor: selectedColor});
        } else {
            this._editor.exec('clearColor');
        }
        this.changeEventHandler();
    }

    fakeSelect() {
        this._fakeSelection = this._editor.getSelection();
        applyFakeSelection.bind(this)(this._fakeSelection);
    }

    fakeUnselect() {
        applyFakeUnselection.bind(this)(this._fakeSelection);
        delete this._fakeSelection;
    }

    createBulletList(event) {
        this._editor.exec('bulletList');
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    createOrderedList(event) {
        this._editor.exec('orderedList');
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    createTaskList(event) {
        this._editor.exec('taskList');
    }

    stopEditing(event) {
        const selection = this._editor.getSelection();
        const cursorPosition = selection ? selection[1] : 0;
        this._editor.setSelection(cursorPosition, cursorPosition);
        this.focus();
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    makeEditable(editable) {
        this._getEditableContent().setAttribute("contenteditable", editable + "");
    }

    focusEditor() {
        if (this._editor) {
            this._editor.focus();
        }
    }

    focusView() {
        if (this._editor) {
            this._editor.wwEditor.view.focus();
        }
    }

    getHeight() {
        return this._editor && this._editor.getHeight();
    }

    getSelectionCoordinates() {
        if (this._editor && this._editor.getSelection()) {
            const view = this._editor.wwEditor.view;
            const selection = this._editor.getSelection();
            return [view.coordsAtPos(selection[0]), view.coordsAtPos(selection[1])];
        }
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

    _updateFakeSelection(e) {
        // if (this._fakeSelection) {
        //     this._fakeSelection = this._editor.getSelection();
        // }
    }

    _fakeSelectionChanged(newSelection, oldSelection) {
        // if (!newSelection && oldSelection) {
        //     applyFakeUnselection.bind(this)(oldSelection);
        // } else if (newSelection && !this.shadowRoot.activeElement) {
        //     applyFakeSelection.bind(this)(newSelection);
        // }
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
}

customElements.define('tg-rich-text-input', TgRichTextInput);

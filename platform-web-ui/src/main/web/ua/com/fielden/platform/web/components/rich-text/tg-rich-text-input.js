
import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import {mixinBehaviors} from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';
import '/resources/toastui-editor/toastui-editor-all.min.js';

import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';
import { IronA11yKeysBehavior } from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';

import '/resources/components/rich-text/tg-rich-text-input-enhanced-styles.js';

import { TgTooltipBehavior } from '/resources/components/tg-tooltip-behavior.js';
import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js';
import { excludeErrors } from '/resources/components/tg-global-error-handler.js';

excludeErrors( e => e.filename && e.filename.includes("toastui-editor-all") && e.error && e.error.name === 'TransformError');

function createSelection(tr, selection, SelectionClass, openTag, closeTag) {
    const { mapping, doc } = tr;
    const { from, to, empty } = selection;
    const mappedFrom = mapping.map(from) + openTag.length;
    const mappedTo = mapping.map(to) - closeTag.length;

    return empty
        ? SelectionClass.create(doc, mappedTo, mappedTo)
        : SelectionClass.create(doc, mappedFrom, mappedTo);
}

function colorTextPlugin(context, options) {
    const { pmState } = context;


    return {
        markdownCommands: {
            color: ({ selectedColor }, { tr, selection, schema }, dispatch) => {
                if (selectedColor) {
                    const slice = selection.content();
                    const textContent = slice.content.textBetween(0, slice.content.size, '\n');
                    const openTag = `<span style='color: ${selectedColor}'>`;
                    const closeTag = `</span>`;
                    const colored = `${openTag}${textContent}${closeTag}`;

                    tr.replaceSelectionWith(schema.text(colored)).setSelection(
                        createSelection(tr, selection, pmState.TextSelection, openTag, closeTag)
                    );

                    dispatch(tr);

                    return true;
                }
                return false;
            },
        },
        wysiwygCommands: {
            color: ({ selectedColor }, { tr, selection, schema }, dispatch) => {
                if (selectedColor) {
                    const { from, to } = selection;
                    const attrs = { htmlAttrs: { style: `color: ${selectedColor}` } };
                    const mark = schema.marks.span.create(attrs);

                    tr.addMark(from, to, mark);
                    dispatch(tr);

                    return true;
                }
                return false;
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
    const a = findLinkParent.bind(this)(e.target);
    if (a && a.hasAttribute('href')) {
        if (currentTooltipElement !== a) {
            this._hideTooltip();
        }
        this.showTooltip(a.getAttribute('href'));
    } else {
        this._hideTooltip();
    }
    currentTooltipElement = a;
};

function findLinkParent(element) {
    return findParentBy.bind(this)(element, parent => parent.hasAttribute && parent.hasAttribute('href'));
};

function findParentBy(element, predicate) {
    let parent = element;
    while (parent && parent !== this._getEditableContent() && !predicate(parent)) {
        parent = parent.parentElement;
    }
    return parent;
}

let mouseTimer = null;
let longPress = false;
let shortPress = false;

function runLinkIfPossible(el) {
    const a = findLinkParent.bind(this)(el);
    if (a && a.hasAttribute('href')) {
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

function getLink() {
    if (this._prevSelection) {
        if (this._prevSelection[0] === this._prevSelection[1]) {
            //It means that only caret postion was set (no selection). Then take text and url from dom at caret position if it exists
            const node = this._editor.wwEditor.view.domAtPos(this._prevSelection[0], 1).node;
            const link = findLinkParent.bind(this)(node);
            if (link && link.pmViewDesc && link.hasAttribute("href")) {
                const text = this._editor.getSelectedText(link.pmViewDesc.posAtStart, link.pmViewDesc.posAtEnd);
                return {pos: [link.pmViewDesc.posAtStart, link.pmViewDesc.posAtEnd], text: text, url: link.getAttribute("href")};
            }
        } else {
            //This branch indicates that user has selected some text or even nodes, therefore the text should be taken from selection
            // and url from the first <a> tag in selection
            const text = this._editor.getSelectedText(this._prevSelection[0], this._prevSelection[1]);
            const nodes = [];
            for (let i = this._prevSelection[0]; i <= this._prevSelection[1]; i++) {
                const node = this._editor.wwEditor.view.domAtPos(i, this._prevSelection[1] - i).node;
                if (node) {
                    nodes.push(node);
                }
            }
            const link = nodes.map(node => findLinkParent.bind(this)(node)).find(a => a && a.hasAttribute('href'));
            return (link && {text: text, url: link.getAttribute("href")}) || {text: text, url: ''};
        }
    }
}

function handleTaskListItemStatusChange(e) {
    const pos = this._editor.wwEditor.view.posAtCoords({left:e.clientX, top:e.clientY});
    const node = pos && this._editor.wwEditor.view.domAtPos(pos.pos, pos.inside);
    if (node && node.node.hasAttribute && node.node.hasAttribute('data-task')) {
        const style = getComputedStyle(node.node, ':before');
        if (isPositionInBox(style, e.offsetX, e.offsetY)) {
            this._editor.focus();
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

const template = html`
    <style include='rich-text-enhanced-styles'></style>
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
            _prevSelection: Array
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
                blur: this.changeEventHandler,
                caretChange: this._saveSelection.bind(this),
                keydown: (viewType, event) => this.keyDownHandler(event)
            },
            plugins: [colorTextPlugin],
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
        //Initiate key binding and key event target
        this.addOwnKeyBinding('ctrl+b meta+b', 'applyBold');
        this.addOwnKeyBinding('ctrl+i meta+i', 'applyItalic');
        this.addOwnKeyBinding('ctrl+s meta+s', 'applyStrikethough');
        this.addOwnKeyBinding('ctrl+z meta+z', 'undo');
        this.addOwnKeyBinding('ctrl+y meta+y', 'redo');
        this.addOwnKeyBinding('tab', 'applyIndent');
        this.addOwnKeyBinding('shift+tab', 'applyOutdent');
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
        if (this._prevSelection && this._prevSelection[0] !== this._prevSelection[1]) {
            tearDownEvent(event.detail && event.detail.keyboardEvent);
        }
    }

    undo(event) {
        this._editor.exec('undo');
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    redo(event) {
        this._editor.exec('redo');
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    initLinkEditing() {
        const link = getLink.bind(this)();
        if (link) {
            if (link.pos) {
                this._editor.setSelection(link.pos[0], link.pos[1]);
            }
            return link;
        }
    }

    toggleLink(url, text) {
        if (this._prevSelection && this._prevSelection[0] !== this._prevSelection[1] && !url) {
            this._editor.exec('toggleLink');
        } else {
            this._editor.exec('addLink', { linkUrl: url, linkText: text });
        }
    }

    applyColor(selectedColor) {
        this._editor.exec("color", {selectedColor: selectedColor});
        this._editor.focus();
        this._editor.setSelection(this._prevSelection[0], this._prevSelection[1]);
    }

    applyIndent(event) {
        this._editor.exec('indent');
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    applyOutdent(event) {
        this._editor.exec('outdent');
        tearDownEvent(event.detail && event.detail.keyboardEvent);
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
        const cursorPosition = this._prevSelection ? this._prevSelection[1] : 0;
        this._editor.setSelection(cursorPosition, cursorPosition);
        this.focus();
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    makeEditable(editable) {
        this._getEditableContent().setAttribute("contenteditable", editable + "");
    }

    getHeight() {
        return this._editor && this._editor.getHeight();
    }

    getDomAtCaretPosition() {
        if (this._editor && this._prevSelection) {
            return this._editor.wwEditor.view.domAtPos(this._prevSelection[1]).node.parentElement;
        }
    }

    getSelectedText() {
        if (this._editor && this._prevSelection) {
            return this._editor.getSelectedText(this._prevSelection[0], this._prevSelection[1]);
        }
    }

    _valueChanged(newValue) {
        if(this._editor && newValue !== this._editor.getHTML()) {
            this._editor.setHTML(newValue);
        }
    }

    _saveSelection(e) {
        if (this._editor) {
            this._prevSelection = this._editor.getSelection();
        }
    }

    _htmlContentChanged(e) {
        const htmlText = this._editor.getHTML();
        if (this.value !== htmlText) {
            this.value = htmlText;
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

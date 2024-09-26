
import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import {mixinBehaviors} from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';
import '/resources/toastui-editor/toastui-editor-all.js';

import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

import { TgTooltipBehavior } from '/resources/components/tg-tooltip-behavior.js';

function createSelection(tr, selection, SelectionClass, openTag, closeTag) {
    const { mapping, doc } = tr;
    const { from, to, empty } = selection;
    const mappedFrom = mapping.map(from) + openTag.length;
    const mappedTo = mapping.map(to) - closeTag.length;

    return empty
        ? SelectionClass.create(doc, mappedTo, mappedTo)
        : SelectionClass.create(doc, mappedFrom, mappedTo);
}

function colorTextPlugin (context, options) {
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
    let parent = element;
    while (parent && parent !== this._editor.getEditorElements().wwEditor.children[0] && (!parent.hasAttribute || !parent.hasAttribute('href'))) {
        parent = parent.parentElement;
    }
    return parent;
};

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

const template = html`
    <link rel="stylesheet" href="/resources/toastui-editor/toastui-editor.min.css" />
    <style>
        :host {
            position: relative
            @apply --layout-vertical;
        }
        .toastui-editor-defaultUI {
            border: none !important;
        }
        .toastui-editor-toolbar {
            display: none;
        }
        .toastui-editor-defaultUI .ProseMirror {
            padding: 0 !important;
        }
        .toastui-editor-contents {
            font-size: inherit !important;
        }
        .toastui-editor-contents h1, .toastui-editor-contents h2 {
            border-bottom: none !important;
        }
        .toastui-editor-contents a {
            cursor: pointer !important;
        }
    </style>
    <div id="editor"></div>`; 

class TgRichTextInput extends mixinBehaviors([IronResizableBehavior, TgTooltipBehavior], PolymerElement) {

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

            height: {
                type: String,
                observer: "_heightChanged"
            },

            minHeight: {
                type: String,
                observer: "_minHeightChanged"
            },

            _editor: Object,
            _prevSelection: Array
        }
    }

    ready () {
        super.ready();
        this._editor = new toastui.Editor({
            el: this.$.editor,
            height: this.height,
            minHeight: this.minHeight,
            initialEditType: 'wysiwyg',
            events: {
                change: this._htmlContetnChanged.bind(this),
                blur: this.changeEventHandler.bind(this),
                caretChange: this._saveSelection.bind(this)
            },
            plugins: [colorTextPlugin],
            useCommandShortcut: true,
            usageStatistics: false,
            toolbarItems: [],
            hideModeSwitch: true
        });
        //trigger tooltips manually
        this.triggerManual = true;
        //The following code is nedded to preserve whitespaces after loading html into editor.
        this._editor.wwEditor.schema.cached.domParser.rules.forEach(r => r.preserveWhitespace = "full");
        //Add event listeners for tooltips on editor
        this._editor.getEditorElements().wwEditor.children[0].addEventListener("mouseover", mouseOverHandler.bind(this));
        //Add event listeners to make link clickable and with proper cursor
        this._editor.getEditorElements().wwEditor.children[0].addEventListener("mousedown", mouseDownHandler.bind(this));
        this._editor.getEditorElements().wwEditor.children[0].addEventListener("mouseup", mouseUpHandler.bind(this));
        this._editor.getEditorElements().wwEditor.children[0].addEventListener("touchstart", mouseDownHandler.bind(this));
        this._editor.getEditorElements().wwEditor.children[0].addEventListener("touchend", mouseUpHandler.bind(this));
    }

    applyHeader1() {
        this._editor.exec('heading', { level: 1 });
    }

    applyHeader2() {
        this._editor.exec('heading', { level: 2 });
    }

    applyHeader3() {
        this._editor.exec('heading', { level: 3 });
    }

    applyParagraph() {
        this._editor.exec('heading', { level: 0 });
    }

    applyBold() {
        this._editor.exec('bold');
    }

    applyItalic() {
        this._editor.exec('italic');
    }

    applyStrikethough() {
        this._editor.exec('strike');
    }

    undo() {
        this._editor.exec('undo');
    }

    redo() {
        this._editor.exec('redo');
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

    applyIndent() {
        this._editor.exec('indent');
    }

    applyOutdent() {
        this._editor.exec('outdent');
    }

    createBulletList(e) {
        this._editor.exec('bulletList');
    }

    createOrderedList(e) {
        this._editor.exec('orderedList');
    }

    createTaskList(e) {
        this._editor.exec('taskList');
    }

    makeReadOnly() {
        this._editor.getEditorElements().wwEditor.children[0].setAttribute("contenteditable", "false");
    }
    
    makeEditable() {
        this._editor.getEditorElements().wwEditor.children[0].setAttribute("contenteditable", "true");
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

    _htmlContetnChanged (e) {
        const htmlText = this._editor.getHTML();
        if (this.value !== htmlText) {
            this.value = htmlText;
        }
    }

    _heightChanged (newHeight) {
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
}

customElements.define('tg-rich-text-input', TgRichTextInput);
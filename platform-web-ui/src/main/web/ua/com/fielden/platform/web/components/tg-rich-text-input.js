
import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import '/resources/toastui-editor/toastui-editor-all.min.js';

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
    </style>
    <div id="editor"></div>`; 

class TgRichTextInput extends PolymerElement {

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
                type: String
            },

            _editor: Object,
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
                blur: this.changeEventHandler.bind(this)
            },
            linkAttributes: {
                target: "_blank"
            },
            useCommandShortcut: true,
            usageStatistics: false,
            toolbarItems: [],
            hideModeSwitch: true
        });
        //The following code is nedded to preserve whitespaces after loading html into editor.
        this._editor.wwEditor.schema.cached.domParser.rules.forEach(r => r.preserveWhitespace = "full");
        this._editor.setHTML(this.value);
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

    insertLink(url, text) {
        this._editor.exec('addLink', { linkText: text, linkUrl: url })
    }

    //TODO other methods for link and color text should go here

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

    _valueChanged(newValue) {
        if(this._editor && newValue !== this._editor.getHTML()) {
            this._editor.setHTML(newValue);
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
        }
    }
}

customElements.define('tg-rich-text-input', TgRichTextInput);

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
                change: this._changeValue.bind(this),
                blur: this.changeEventHandler.bind(this)
            },
            useCommandShortcut: true,
            usageStatistics: false,
            toolbarItems: [],
            hideModeSwitch: true
        });
        this._editor.setMarkdown(this.value);
    }

    switchToMarkdownMode() {
        this._editor.changeMode("markdown", false);
    }

    switchToWysiwyg() {
        this._editor.changeMode("wysiwyg", false);
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
        if(this._editor && newValue !== this._editor.getMarkdown()) {
            this._editor.setMarkdown(newValue);
        }
    }

    _changeValue (e) {
        const markdownText = this._editor.getMarkdown();
        if (this.value !== markdownText) {
            this.value = markdownText;
        }
    }

    _heightChanged (newHeight) {
        if (this._editor) {
            this._editor.setHeight(newHeight);
        }
    }
}

customElements.define('tg-rich-text-input', TgRichTextInput);
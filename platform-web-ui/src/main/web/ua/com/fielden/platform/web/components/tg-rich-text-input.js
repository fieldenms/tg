
import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import '/resources/toastui-editor/toastui-editor-all.min.js';

const template = html`
    <link rel="stylesheet" href="/resources/toastui-editor/toastui-editor.min.css" />
    <style>
        :host {
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
                observer: "_valueChanged"
            },

            _editor: Object,
        }
    }

    ready () {
        super.ready();
        
        this._editor = new toastui.Editor({
            el: this.$.editor,
            height: '500px',
            initialEditType: 'wysiwyg',
            usageStatistics: false,
            toolbarItems: [],
            hideModeSwitch: true
        });
        if (this.value) {
            this._editor.setMarkdown(this.value);
        }
    }

    getMarkdownText () {
        if (this._editor) {
            return this._editor.getMarkdown();
        }
    }

    _valueChanged(newValue) {
        if(this._editor) {
            this._editor.setMarkdown(newValue);
        }
    }
}

customElements.define('tg-rich-text-input', TgRichTextInput);

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
                observer: "_valueChanged",
                notify: true,
            },

            changeEventHandler: {
                type: Function,
                value: null
            },

            _editor: Object,
        }
    }

    ready () {
        super.ready();
        
        this._editor = new toastui.Editor({
            el: this.$.editor,
            height: '500px',
            initialValue: this.value,
            initialEditType: 'wysiwyg',
            events: {
                change: this._changeValue.bind(this),
                blur: this.changeEventHandler.bind(this)
            },
            usageStatistics: false,
            toolbarItems: [],
            hideModeSwitch: true
        });
        this._editor.setMarkdown(this.value);
    }

    _valueChanged(newValue) {
        if(this._editor && newValue !== this._editor.getMarkdown()) {
            this._editor.setMarkdown(newValue);
        }
    }

    _changeValue (e) {
        if (e === "wysiwyg" && this.value !== this._editor.getMarkdown()) {
            this.value = this._editor.getMarkdown();
        }
    }
}

customElements.define('tg-rich-text-input', TgRichTextInput);
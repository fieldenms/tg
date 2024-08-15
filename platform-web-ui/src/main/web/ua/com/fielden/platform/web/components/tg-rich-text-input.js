
import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import '/resources/toastui-editor/toastui-editor-all.min.js';

const template = html`
    <link rel="stylesheet" href="/resources/toastui-editor/toastui-editor.min.css" />
    <style>
        :host {
            @apply --layout-vertical;
        }
        .toastui-editor-toolbar {
            display: none;
        }

    </style>
    <div id="editor"></div>`; 

class TgRichTextInput extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            editor: String,
        }
    }

    ready () {
        super.ready();
        
        this.editor = new toastui.Editor({
            el: this.$.editor,
            height: '500px',
            initialEditType: 'wysiwyg',
            usageStatistics: false,
            toolbarItems: [],
            hideModeSwitch: false
        });
    }
}

customElements.define('tg-rich-text-input', TgRichTextInput);

import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import Editor from '/resources/polymer/lib/toastui-editor-lib.js';

const template = html`
    <link rel="stylesheet" href="/resources/spikes/toastui/toastui-editor.min.css"/>
    <style>
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
        .toastui-editor-contents .task-list-item:before {
            top: calc(0.8em - 9px) !important;
        }
        .toastui-editor-contents ul>li:before {
            margin-top: 0 !important;
            top: calc(0.8em - 2.5px);
        }
        .toastui-editor-contents :not(span, mark) {
            box-sizing: content-box;
            line-height: 160%;
        }
        del a span, del a {
            text-decoration: line-through underline !important;
        }
        del span {
            text-decoration: line-through !important;  
        }
        a span {
            text-decoration: underline !important;
        }
    </style>
    <div id="editor"></div>`; 

class PlainToastuiEditorWrapper extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {    
            _editor: Object,
        }
    }

    ready() {
        super.ready();
        this._editor = new Editor({
            el: this.$.editor,
            height: "500px",
            initialValue: "<h1>Hello World</h1>",
            initialEditType: 'wysiwyg',
            hideModeSwitch: true,
            events: {
                caretChange: this._selectionChange.bind(this)
            },
        });
    }

    _selectionChange (e) {
        console.log("----plain toast ui editor wrapper----", e);
    }

}

customElements.define('plain-toastui-editor-wrapper', PlainToastuiEditorWrapper);
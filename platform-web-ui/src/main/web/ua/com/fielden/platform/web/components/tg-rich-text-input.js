
import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import {GestureEventListeners} from '/resources/polymer/@polymer/polymer/lib/mixins/gesture-event-listeners.js';
import '/resources/toastui-editor/toastui-editor-all.min.js';

import { tearDownEvent} from '/resources/reflection/tg-polymer-utils.js';

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
        .noselect {
            -webkit-touch-callout: none;
            /* iOS Safari */
            -webkit-user-select: none;
            /* Safari */
            -khtml-user-select: none;
            /* Konqueror HTML */
            -moz-user-select: none;
            /* Firefox */
            -ms-user-select: none;
            /* Internet Explorer/Edge */
            user-select: none;
            /* Non-prefixed version, currently
               supported by Chrome and Opera */
        }
        #resizer {
            position: absolute;
            bottom: 0;
            right: 0;
            z-index: 21;
            --iron-icon-fill-color: var(--paper-grey-600);
        }
        #resizer:hover {
            cursor: ns-resize;
        }

    </style>
    <div id="editor"></div>
    <iron-icon id="resizer" icon="tg-icons:resize-bottom-right" on-tap="_resetHeight" on-track="_resizeInput" tooltip-text="Drag to resize<br>Double tap to reset height"></iron-icon>`; 

class TgRichTextInput extends GestureEventListeners(PolymerElement) {

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
                type: String
            },

            minHeight: {
                type: String
            },

            withoutResizing: {
                type: Boolean,
                value: false
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
        const markdownText = this._editor.getMarkdown();
        if (this.value !== markdownText) {
            this.value = markdownText;
        }
    }

    _resizeInput (event) {
        const target = event.target || event.srcElement;
        if (target === this.$.resizer && !this.withoutResizing) {
            switch (event.detail.state) {
                case 'start':
                    this.$.editor.classList.toggle("noselect", true);
                    this._editor.getEditorElements().wwEditor.children[0].setAttribute("contenteditable", "false");
                    document.styleSheets[0].insertRule('* { cursor: ns-resize !important; }', 0); // override custom cursors in all application with resizing cursor
                    break;
                case 'track':
                    let newHeight = this.$.editor.offsetHeight + event.detail.ddy;
                    this._editor.setHeight(newHeight + "px");
                    break;
                case 'end':
                    this.$.editor.classList.toggle("noselect", false);
                    this._editor.getEditorElements().wwEditor.children[0].setAttribute("contenteditable", "true");
                    if (document.styleSheets.length > 0 && document.styleSheets[0].cssRules.length > 0) {
                        document.styleSheets[0].deleteRule(0);
                    }
                    break;
            }
        }
        tearDownEvent(event);  
    }

    _resetHeight(e) {
        console.log("Height was reset");
    }
}

customElements.define('tg-rich-text-input', TgRichTextInput);
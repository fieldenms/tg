
import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';

const template = html`
    <style>
        :host {
            @apply --layout-vertical;
        }
        .toolbar {
            @apply --layout-horizontal; 
        }
        .content {
            @apply --layout-flex;
        }
    </style>
    <div class="toolbar"></div>
    <div id="textContainer" class="content" contenteditable="true" inner-H-T-M-L="[[richTextContent]]"></div>`; 

class TgRichTextInput extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            richTextContent: String,
        }
    }

    ready () {
        super.ready();
        
        
    }
}

customElements.define('tg-rich-text-input', TgRichTextInput);
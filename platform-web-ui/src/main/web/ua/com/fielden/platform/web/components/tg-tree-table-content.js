import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';

const template = html`
    <slot></slot>`; 

class TgTreeTableContent extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            column: {
                type: Object,
                observer: "_columnChanged"
            },
            entity: {
                type: Object,
                observer: "_entityChanged"
            }
        };
    }

    ready () {
        super.ready();
    }

    _columnChanged () {
        this.dispatchEvent(new CustomEvent("tg-tree-table-column-entity-changed", {
            detail: this.column,
            bubbles: false, 
            composed: true
        }));
    }

    _entityChanged () {
        this.dispatchEvent(new CustomEvent("tg-tree-table-content-entity-changed", {
            detail: this.entity,
            bubbles: false, 
            composed: true
        }));
    }
}

customElements.define('tg-tree-table-content', TgTreeTableContent);
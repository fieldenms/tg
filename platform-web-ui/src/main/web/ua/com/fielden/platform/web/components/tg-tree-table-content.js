import { allDefined } from '/resources/reflection/tg-polymer-utils.js';
import { TgEgiDataRetrievalBehavior } from '/resources/egi/tg-egi-data-retrieval-behavior.js';
import { _removeAllLightDOMChildrenFrom } from '/resources/reflection/tg-polymer-utils.js';

import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { mixinBehaviors } from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';

const template = html`
    <slot></slot>`; 

class TgTreeTableContent extends mixinBehaviors([TgEgiDataRetrievalBehavior], PolymerElement) {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            column: {
                type: Object
            },
            entity: {
                type: Object
            }
        };
    }

    static get observers() {
        return [
          '_dataChanged(entity, column)'
        ];
    }

    _dataChanged (entity, column) {
        _removeAllLightDOMChildrenFrom(this);
        if (!allDefined(arguments)) {
            this.innerHTML = "";
        } else if (entity.loaderIndicator) {
            if (column.isHierarchyColumn) {
                this.innerHTML = entity.entity.key;
            } else {
                this.innerHTML = "";
            }
        } else if (column.elementProvider) {
            column.elementProvider(this, entity, column);
        } else if (column.contentBuilder) {
            this.innerHTML = column.contentBuilder(entity, column);
        } else {
            this.innerHTML = this.getBindedValue(entity.entity, column);
        }
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
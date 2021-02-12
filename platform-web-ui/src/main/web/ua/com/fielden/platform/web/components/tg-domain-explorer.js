import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-icons/communication-icons.js';

import '/resources/components/tg-tree-table.js';
import '/resources/egi/tg-property-column.js';
import '/resources/egi/tg-hierarchy-column.js';

import {html, PolymerElement} from '/resources/polymer/@polymer/polymer/polymer-element.js';

const template = html`
    <style>
        :host {
            position: relative;
            width: 100%;
            height: 100%;
            @apply --layout-horizontal;
        }

        .domain-explorer-container {
            padding: 0 18px;
            @apply --layout-vertical;
            @apply --layout-flex;
        }

        .editor-container {
            padding: 0 4px;
            @apply --layout-vertical;
        }

        .domain-explorer-tree {
            min-height: 0;
            @apply --layout-flex;
        }

        .lock-layer {
            opacity: 0.5;
            display: none;
            background-color: white;
            @apply --layout-fit;
        }
        .lock-layer[lock] {
            display: initial;
        }
    </style>
    <div class="domain-explorer-container">
        <div class="editor-container">
            <slot name="filter-element"></slot>
        </div>
        <tg-tree-table id="domainExplorerTree" class="domain-explorer-tree" model="[[treeModel]]" on-tg-load-subtree="_loadSubtree">
            <tg-hierarchy-column slot='hierarchy-column' property="name" type="String" width="200" min-width="80" grow-factor="1" column-title="Title" column-desc="Title description" content-builder="[[_buildContent]]"></tg-hierarchy-column>
            <tg-property-column slot='regular-column' property="propertyType" type="String" width="100" min-width="80" grow-factor="1" column-title="Property Type" column-desc="Property type"></tg-property-column>
            <tg-property-column slot='regular-column' property="desc" type="String" width="100" min-width="80" grow-factor="1" column-title="Description" column-desc="Description"></tg-property-column>
            <tg-property-column slot='regular-column' property="internalName" type="String" width="160" min-width="80" grow-factor="1" column-title="Internal Name" column-desc="Internal type name"></tg-property-column>
            <tg-property-column slot='regular-column' property="dbSchema" type="String" width="200" min-width="80" grow-factor="1" column-title="DB Schema" column-desc="Table Name"></tg-property-column>
            <tg-property-column slot='regular-column' property="refTable" type="String" width="160" min-width="80" grow-factor="1" column-title="Ref Table" column-desc="References Table Name"></tg-property-column>
        </tg-tree-table>
    </div>
    <div class="lock-layer" lock$="[[lock]]"></div>`;
    
const keyIcon = function (entity) {
    return "<iron-icon icon='communication:vpn-key' style='height:18px; transform: scale(-1, 1) rotate(90deg); color:" + (entity.entity.isRequired ? "#03A9F4" : "black") + "'></iron-icon>";
}
const keyNumber = function (entity, numOfKeys) {
    return numOfKeys > 1 ? ("<sup style='position:absolute; left:18px;'><b>" + entity.entity.order + "</b></sup>") : ""; 
}
const getKeyIcon = function (entity, numOfKeys) {
    return entity.entity.isKey 
                ? "<span style='position:relative;'>" + keyIcon(entity) + keyNumber(entity, numOfKeys) + "</span>" 
                : (numOfKeys > 0 ? "<span style='min-width:24px;'></span>" : "");
};

class TgDomainExplorer extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            treeModel: {
                type: Object
            },
    
            entity: {
                type: Object,
                observer: "_entityChanged"
            },
    
            /**
             * Need for locking domain explorer component during data loading.
             */
            lock: {
                type: Boolean,
                value: false
            },
            
            _saveQueue: Array,
            _saveInProgress: Boolean
        };
    }

    ready () {
        super.ready();
    }

    _buildContent (entity) {
        const parentEntity = entity.parent;
        const numOfKeys = parentEntity ? parentEntity.entity.children.filter(ent => ent.isKey).length : 0;
        return getKeyIcon(entity, numOfKeys) + "<span class='truncate'" + (numOfKeys !== 1 ? "style='margin-left:8px;'" : "") + ">" + entity.entity.name + "</span>";
    }

    _entityChanged (newBindingEntity) {
        const newEntity = newBindingEntity ? newBindingEntity['@@origin'] : null;
        if (newEntity) {
            //TODO Implement logic that updates the treeModel according to new data that arrived from server.
        }
        if (this._saveInProgress) {
            this._saveInProgress = false;
        }
        if (this._saveQueue.length > 0) {
            this._processEvent(this._saveQueue.shift());
        }
    }

    _loadSubtree (e) {
        if (this._saveQueue.length !== 0 || this._saveInProgress) {
            this._saveQueue.push(e);
        } else {
            this._processEvent(e);
        }
    }

    _processEvent (e) {
        //TODO nned to initialise entity for next save action
        this._saveInProgress = true;
        this.fire("tg-load-sub-domain", this.entity);
    }

    /**
     * Filters the hierarchy tree.
     */
    filterDomain (text) {
        this.$.domainExplorerTree.filter(text);
    }
}

customElements.define('tg-domain-explorer', TgDomainExplorer);
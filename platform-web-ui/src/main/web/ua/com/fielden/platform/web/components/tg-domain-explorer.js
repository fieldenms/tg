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
            width: 100%;
            height: 100%;
            @apply --layout-horizontal;
            @apply --layout-justified;
        }

        .domain-explorer-container {
            padding: 0 18px;
            @apply --layout-fit;
            @apply --layout-vertical;
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
        <tg-tree-table id="domainExplorerTree" class="domain-explorer-tree" model="[[treeModel]]" last-search-text="{{lastSearchText}}" on-current-matched-item-changed="_updateCurrentMatchedItemRelatedData" on-tg-load-subtree="_loadSubtree" on>
            <tg-hierarchy-column slot='hierarchy-column' property="key" type="String" width="200" min-width="80" grow-factor="1" column-title="Title" column-desc="Domain tpye or property title" content-builder="[[_buildContent]]"></tg-hierarchy-column>
            <tg-property-column slot='regular-column' property="propertyType.desc" type="String" width="100" min-width="80" grow-factor="1" column-title="Property Type" column-desc="Property type"></tg-property-column>
            <tg-property-column slot='regular-column' property="desc" type="String" width="100" min-width="80" grow-factor="1" column-title="Description" column-desc="Domain type or property description"></tg-property-column>
            <tg-property-column slot='regular-column' property="internalName" type="String" width="160" min-width="80" grow-factor="1" column-title="Internal Name" column-desc="Internal domain type or property name" content-builder="[[_buildInternalNameContent]]"></tg-property-column>
            <tg-property-column slot='regular-column' property="dbSchema" type="String" width="200" min-width="80" grow-factor="1" column-title="DB Schema" column-desc="Table Name" content-builder="[[_buildDbSchemaContent]]"></tg-property-column>
            <tg-property-column slot='regular-column' property="refTable" type="String" width="160" min-width="80" grow-factor="1" column-title="Ref Table" column-desc="References Table Name"></tg-property-column>
        </tg-tree-table>
    </div>
    <div class="lock-layer" lock$="[[lock]]"></div>`;
    
const keyIcon = function (entity) {
    return "<iron-icon icon='communication:vpn-key' style='height:18px; transform: scale(-1, 1) rotate(90deg); color:" + (entity.entity.isRequired ? "#03A9F4" : "black") + "'></iron-icon>";
}
const keyNumber = function (entity, numOfKeys) {
    return numOfKeys > 1 ? ("<sup style='position:absolute; left:18px;'><b>" + entity.entity.keyOrder + "</b></sup>") : ""; 
}
const getKeyIcon = function (entity, numOfKeys) {
    return entity.entity.isKey 
                ? "<span style='position:relative;'>" + keyIcon(entity) + keyNumber(entity, numOfKeys) + "</span>" 
                : (numOfKeys > 0 ? "<span style='min-width:24px;'></span>" : "");
};
const generatePath = function(treeModel, loadedHierarchy) {
    let path = "treeModel";
    let model = treeModel;
    loadedHierarchy.forEach((entityIndex, index) => {
        const treeEntry = model[entityIndex];
        if (treeEntry) {
            path += "." + entityIndex + ".children";
        } else {
            throw {
                msg: "The domain wasn't detecteted for entity index: " + entityIndex + " at level: " + (index + 1)
            };
        }
        model = model[entityIndex].children;
    });
    return path;
};
const getPathItem = function(treeModel, loadedHierarchy) {
    let parent = null;
    loadedHierarchy.forEach(entityIndex => {
        parent = ((parent && parent.children) || treeModel)[entityIndex];
    });
    return parent;
};
const calculateNumberOfLevels = function (entity) {
    let parent = entity;
    let numberOfLevels = 0;
    while (parent) {
        numberOfLevels += 1;
        parent = parent.parent;
    }
    return numberOfLevels;
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

            lastSearchText: {
                type: String,
                value: "",
                notify: true
            },
            
            matchedItemOrder: {
                type: Number,
                value: 0,
                notify: true
            },
                
            numberOfMatchedItems: {
                type: Number,
                value: 0,
                notify: true
            },
            
            _saveQueue: Array,
            _saveInProgress: Boolean
        };
    }

    ready () {
        super.ready();
        //Configure the component's properties.
        this._saveQueue = [];
        this._saveInProgress = false;

        this._buildContent = this._buildContent.bind(this);
        this._buildInternalNameContent = this._buildInternalNameContent.bind(this);
        this._buildDbSchemaContent = this._buildDbSchemaContent.bind(this);
    }

    goToNextMatchedItem () {
        this.$.domainExplorerTree.goToNextMatchedItem();
    }

    goToPreviousMatchedItem () {
        this.$.domainExplorerTree.goToPreviousMatchedItem();
    }

    _updateCurrentMatchedItemRelatedData(e) {
        const oldItemIdx = this.matchedItemOrder - 1;
        if (this.$.domainExplorerTree._matchedTreeItems) {
            this.matchedItemOrder = this.$.domainExplorerTree._matchedTreeItems.indexOf(this.$.domainExplorerTree.currentMatchedItem) + 1;
            this.numberOfMatchedItems = this.$.domainExplorerTree._matchedTreeItems.length;
        }
    }

    _buildContent (entity) {
        const parentEntity = entity.parent;
        const numOfKeys = parentEntity ? parentEntity.entity.children.filter(ent => ent.isKey).length : 0;
        return getKeyIcon(entity, numOfKeys) + "<span class='truncate'" + (numOfKeys !== 1 ? "style='margin-left:8px;'" : "") + ">" + entity.entity.key + "</span>";
    }

    _buildInternalNameContent (entity) {
        let internalName = entity.entity.internalName;
        let parent = entity.parent;
        while(parent !== null && parent.parent !== null) {
            internalName = parent.entity.internalName + "." + internalName;
            parent = parent.parent;
        }
        return internalName
    }

    _buildDbSchemaContent (entity, column) {
        const numOfLevels = calculateNumberOfLevels(entity);
        if (entity.entity.union) {
            return "[REFER SUBPROPERTIES]"   
        }
        if (numOfLevels > 3 || (numOfLevels === 3 && !entity.parent.entity.union)) {
            return "[JOIN]";
        }
        return this.$.domainExplorerTree.getBindedValue(entity.entity, column);
    }

    _entityChanged (newBindingEntity) {
        const newEntity = newBindingEntity ? newBindingEntity['@@origin'] : null;
        if (newEntity) {
            const path = generatePath(this.treeModel, newEntity.loadedHierarchy);
            const parent = getPathItem(this.treeModel, newEntity.loadedHierarchy);
            newEntity.generatedHierarchy.forEach(entity => {
                entity.parent = parent;
            });
            this.set(path, newEntity.generatedHierarchy);
            newEntity.set("generatedHierarchy", []);
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
        const parentsPath = e.detail.parentPath;
        const indexes = parentsPath.map(entity =>  {
            const parentList = entity.parent ? entity.parent.children: this.treeModel;
            return parentList.indexOf(entity);
        });
        const lastEntity = parentsPath[parentsPath.length - 1];
        this.entity.setAndRegisterPropertyTouch("loadedHierarchy", indexes);
        this.entity.setAndRegisterPropertyTouch("domainTypeName", (lastEntity.propertyType && lastEntity.propertyType.key) || lastEntity.internalName);
        this.entity.setAndRegisterPropertyTouch("domainTypeHolderId", (lastEntity.propertyType && lastEntity.propertyType.id) || lastEntity.entityId);
        this.entity.setAndRegisterPropertyTouch("domainPropertyHolderId", (lastEntity.propertyType && lastEntity.entityId) || null);
        this._saveInProgress = true;
        this.dispatchEvent(new CustomEvent('tg-load-sub-domain',  { bubbles: true, composed: true, detail: this.entity }));
    }

    /**
     * Performs search of tree items in tree table.
     */
    find (text) {
        this.$.domainExplorerTree.find(text);
    }
}

customElements.define('tg-domain-explorer', TgDomainExplorer);
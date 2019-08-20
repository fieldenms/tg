import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-icons/av-icons.js';
import '/resources/polymer/@polymer/iron-list/iron-list.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { isMobileApp } from '/resources/reflection/tg-polymer-utils.js';

const template = html`
    <style>
        /* Container styles*/
        :host {
            @apply --layout-vertical;

        }
        iron-list {
            @apply --layout-flex;
        }
        .expand-button {
            padding: 4px 0;
            width: 16px;
            height: 16px;
        }
        .expand-button:not([collapsed]) {
            transform: rotate(90deg);
        }
        iron-icon[invisible] {
            visibility: hidden;
        }
        [highlighted] .part-to-highlight {
            font-weight: bold;
        }
        .tree-item {
            padding: 1px 4px;
        }
        .tree-node[selected],
        .tree-node[selected] > .tree-item-actions {
            background-color: #F5F5F5;
        }
        .tree-node:hover,
        .tree-node:hover > .tree-item-actions {
            background-color: #EEEEEE
        }
        .tree-item-actions {
            position: sticky;
            position: -webkit-sticky;
            right: 0;
            opacity: 0.7;
        }
        .tree-item-actions > iron-icon {
            padding: 0 8px;
            cursor: pointer;
        }
        .tree-item-actions {
            display: none;
            flex-direction: row;
        }
        .tree-node[selected] > .tree-item-actions, 
        .tree-node:hover > .tree-item-actions {
            display: flex;
        }
        .no-wrap {
            min-width: min-content;
            white-space: nowrap;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <iron-list id="treeList" items="[[_entities]]" as="entity" default-physical-count="500" selection-enabled>
        <template>
            <div class="layout horizontal tree-node no-wrap" selected$="[[selected]]" style$="[[_calcItemStyle(entity)]]">
                <iron-icon class="expand-button" icon="av:play-arrow" style="flex-grow:0;flex-shrink:0;" invisible$="[[!entity.entity.hasChildren]]" collapsed$="[[!entity.opened]]" on-tap="_toggle"></iron-icon>
                <span class="tree-item"  highlighted$="[[entity.highlight]]" inner-h-t-m-l="[[contentBuilder(entity, entity.opened)]]"></span>
                <span class="tree-item-actions" on-tap="actionRunner" mobile$="[[mobile]]" inner-h-t-m-l="[[actionBuilder(entity)]]"></span>
            </div>
        </template>
    </iron-list>`;

const calculateNumberOfOpenedItems = function (entity) {
    let length = 0;
    if (entity.entity.hasChildren && entity.opened) {
        entity.children.forEach(child => {
            if (child.visible) {
                length += calculateNumberOfOpenedItems(child) + 1;
            }
        });
    }
    return length;
};

const getParentsPath = function (entity) {
    const path = [];
    let parent = entity;
    while (parent) {
        path.push(parent.entity);
        parent = parent.parent;
    }
    return path.reverse();
};

const getChildrenToAdd = function (entity, shouldFireLoad) {
    const childrenToAdd = [];
    if (entity.opened && entity.entity.hasChildren) {
        if (entity.children.length === 1 && entity.children[0].loaderIndicator && shouldFireLoad) {
            this.fire("tg-load-subtree", {parentPath: getParentsPath(entity), loadAll: false});
        }
        childrenToAdd.push(...composeChildren.bind(this)(entity.children, shouldFireLoad));
    }
    return childrenToAdd;
};

const composeChildren = function (entities, shouldFireLoad) {
    const list = [];
    entities.filter(entity => entity.visible).forEach(entity => {
        list.push(entity);
        list.push(...getChildrenToAdd.bind(this)(entity, shouldFireLoad));
    });
    return list;
};

const generateChildrenModel = function (children, parentEntity) {
    return children.map(child => {
        const parent = {
            entity: child,
            parent: parentEntity,
            opened: false,
            visible: true,
            highlight: false
        };
        if (child.hasChildren) {
            if (child.children && child.children.length > 0) {
                parent.children = generateChildrenModel(child.children, parent);
            } else {
                parent.children = [generateLoadingIndicator(parent)];
            }
        }
        return parent;
    });
};


const makeParentVisible = function (entity) {
    let parent = entity.parent;
    while (parent) {
        parent.visible = true;
        parent = parent.parent;
    }
};

const hasMatchedAncestor = function (entity) {
    let parent = entity.parent;
    while (parent) {
        if (parent.visible && parent.highlight) {
            return true;
        }
        parent = parent.parent;
    }
    return false;
}

const wasLoaded = function (entity) {
    return entity.entity.hasChildren && entity.children && entity.children.length > 0 && !(entity.children.length === 1 && entity.children[0].loaderIndicator);
};

const generateLoadingIndicator = function (parent) {
    return {
        entity: {
            key: "Loading data...",
            hasChildren: false,
        },
        opened: false,
        visible: true,
        highlight: false,
        parent: parent,
        loaderIndicator: true
    };
};

const refreshTree = function () {
    const props = ["opened", "highlight"];
    this._entities.forEach((entity, idx) => {
        if (this.$.treeList._isIndexRendered(idx)) {
            props.forEach(prop => this.notifyPath("_entities." + idx + "." + prop)); 
        }
    });
};

Polymer({
    _template: template,

    is: 'tg-tree',

    properties: {

        /**
         * Represents tree model
         */
        model: {
            type: Array,
            observer: "_modelChanged"
        },
        
        /**
         * Indicates whether component is opened in mobile mode.
         */
        mobile: {
            type: Boolean,
            value: isMobileApp()
        },
        
        contentBuilder: Function,
        actionBuilder: Function,
        actionRunner: Function,
        
        /**
         * The tree model that holds some visual specific properties and is created from model.
         */
        _treeModel: {
            type: Array
        },

        /**
         * Represents the linear tree model.
         */
        _entities: {
            type: Array
        },
        
        _lastFilterText: {
            type: String,
            value: ""
        }
    },
    
    observers: ["_modelChanged(model.*)"],

    ready: function () {
        this.scopeSubtree(this.$.treeList, true);
    },

    attached: function () {},
    
    filter: function (text) {
        this._lastFilterText = text;
        this._filterSubTree(text, this._treeModel, true);
        this.splice("_entities", 0, this._entities.length, ...composeChildren.bind(this)(this._treeModel, true));
        this.debounce("refreshTree", refreshTree.bind(this));
    },
    
    reloadSubtreeFull: function (idx, entity) {
        if (entity.entity.hasChildren) {
            const numOfItemsToDelete = calculateNumberOfOpenedItems(entity);
            entity.children = [generateLoadingIndicator(entity)];
            if (!entity.opened) {
                this.set("_entities." + idx + ".opened", true);
            }
            this.splice("_entities", idx + 1, numOfItemsToDelete, ...getChildrenToAdd.bind(this)(entity, false));
            this.fire("tg-load-subtree", {parentPath: getParentsPath(entity), loadAll: true});
        }
    },
    
    removeSubtreeFull: function (idx, entity) {
        if (entity.entity.hasChildren) {
            if (entity.opened) {
                const numOfItemsToDelete = calculateNumberOfOpenedItems(entity);
                entity.children = [generateLoadingIndicator(entity)];
                this.splice("_entities", idx + 1, numOfItemsToDelete, ...getChildrenToAdd.bind(this)(entity, true));
            }
        }
    },
    
    _filterSubTree: function (text, subtree, expand) {
        subtree.forEach(treeEntity => {
            let visible = undefined;
            let highlight = undefined;
            let opened = undefined;
            if (treeEntity.entity.key.toLowerCase().search(text.toLowerCase()) >= 0) {
                treeEntity.visible = true;
                treeEntity.highlight = text ? true : false;
                makeParentVisible(treeEntity);
            } else if (hasMatchedAncestor(treeEntity)) {
                treeEntity.visible = true;
                treeEntity.highlight = false;
            } else {
                treeEntity.visible = false;
                treeEntity.highlight = false;
            }
            if (treeEntity.entity.hasChildren && wasLoaded(treeEntity)) {
                treeEntity.opened = true;
                this._filterSubTree(text, treeEntity.children, expand);
            }
        });
    },

    _regenerateModel: function () {
        this._treeModel = generateChildrenModel(this.model); 
        this._entities = this._treeModel.slice();
    },

    /**
     * Reacts on changes of tree model and updates list model.
     */
    _modelChanged: function (change) {
        if (change.path === "model") {
            this._regenerateModel();
            if (this._lastFilterText) {
                this.filter(this._lastFilterText);
            }
        } else if (change.path && change.path.endsWith("children")) {
            const path = change.path.substring(0, change.path.lastIndexOf(".")).replace("model", "_treeModel").replace(/#/g, "");
            const parentItem = this.get(path);
            const modelIdx = this._entities.indexOf(parentItem);
            if (parentItem) {
                const numOfItemsToDelete = calculateNumberOfOpenedItems(parentItem);
                parentItem.children = generateChildrenModel(change.value, parentItem);
                this._filterSubTree(this._lastFilterText, parentItem.children, false);
                if (typeof modelIdx !== 'undefined') {
                    this.splice("_entities", modelIdx + 1, numOfItemsToDelete, ...getChildrenToAdd.bind(this)(parentItem, true));
                }
            }
        }
    },

    _toggle: function (e) {
        e.stopPropagation();
        const entity = e.model.entity;
        const idx = e.model.index;
        if (entity.opened) {
            this.splice("_entities", idx + 1, calculateNumberOfOpenedItems(entity));
            this.set("_entities." + idx + ".opened", false);
        } else {
            this.set("_entities." + idx + ".opened", true);
            this.splice("_entities", idx + 1, 0, ...getChildrenToAdd.bind(this)(entity, true));
        }
    },

    _calcItemStyle: function (entity) {
        let paddingLeft = 0;
        let parent = entity.parent;
        while (parent) {
            paddingLeft += 16;
            parent = parent.parent;
        }
        return "padding-left: " + paddingLeft + "px";
    }

});
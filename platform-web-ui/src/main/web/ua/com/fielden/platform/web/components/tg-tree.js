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
        .tree-node[over],
        .tree-node[over] > .tree-item-actions {
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
        .tree-node[over] > .tree-item-actions {
            display: flex;
        }
        .no-wrap {
            min-width: min-content;
            white-space: nowrap;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <iron-list id="treeList" items="[[_entities]]" as="entity" selected-item="{{selectedEntity}}" default-physical-count="500" selection-enabled>
        <template>
            <div class="layout horizontal center tree-node no-wrap" over$="[[entity.over]]" selected$="[[_isSelected(selectedEntity, entity)]]" on-mouseenter="_mouseItemEnter" on-mouseleave="_mouseItemLeave" style$="[[_calcItemStyle(entity)]]">
                    <iron-icon class="expand-button" icon="av:play-arrow" style="flex-grow:0;flex-shrink:0;" invisible$="[[!entity.entity.hasChildren]]" collapsed$="[[!entity.opened]]" on-tap="_toggle"></iron-icon>
                    <span class="tree-item" highlighted$="[[entity.highlight]]" inner-h-t-m-l="[[contentBuilder(entity, entity.opened)]]" on-tap="treeItemAction"></span>
                    <span class="tree-item-actions" on-tap="actionRunner" mobile$="[[mobile]]" inner-h-t-m-l="[[actionBuilder(entity)]]"></span>
                </div>
        </template>
    </iron-list>`;

const calculateNumberOfOpenedItems = function (entity, from, count) {
    let length = 0;
    if (entity.entity.hasChildren && entity.opened) {
        const children = (typeof from !== 'undefined' && typeof count !== 'undefined') ? 
            entity.children.slice(from, from + count) : entity.children
        children.forEach(child => {
            if (child.visible) {
                length += calculateNumberOfOpenedItems(child) + 1;
                length += child.additionalInfoNodes.length;
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

const getChildrenToAdd = function (entity, shouldFireLoad, expandAll, from, count) {
    const childrenToAdd = [];
    if (entity.opened && entity.entity.hasChildren) {
        let subChildrenToAdd;
        if (entity.children.length === 1 && entity.children[0].loaderIndicator && shouldFireLoad) {
            this.fire("tg-load-subtree", {parentPath: getParentsPath(entity), loadAll: expandAll});
            subChildrenToAdd = entity.children;
        } else if (typeof from !== 'undefined' && typeof count !== 'undefined') {
            subChildrenToAdd = entity.children.slice(from , from + count);
        } else {
            subChildrenToAdd = entity.children
        }
        childrenToAdd.push(...composeChildren.bind(this)(subChildrenToAdd, shouldFireLoad, expandAll));
    }
    return childrenToAdd;
};

const composeChildren = function (entities, shouldFireLoad, expandAll) {
    const list = [];
    entities.filter(entity => entity.visible).forEach(entity => {
        list.push(entity);
        list.push(...entity.additionalInfoNodes);
        list.push(...getChildrenToAdd.bind(this)(entity, shouldFireLoad, expandAll));
    });
    return list;
};

const generateChildrenModel = function (children, parentEntity, additionalInfoCb) {
    return children.map( child => {
        const parent = {
            entity: child,
            parent: parentEntity,
            opened: false,
            visible: true,
            highlight: false,
            selected: false,
            over: false
        };
        if (child.hasChildren) {
            if (child.children && child.children.length > 0) {
                parent.children = generateChildrenModel(child.children, parent, additionalInfoCb);
            } else {
                parent.children = [generateLoadingIndicator(parent)];
            }
        }
        parent.additionalInfoNodes = additionalInfoCb(parent).map(entity => {
            entity.relatedTo = parent;
            entity.over = false;
            return entity;
        });
        return parent;
    })
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
        additionalInfoNodes: [],
        loaderIndicator: true,
        selected: false,
        over: false
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
        treeItemAction: Function,
        actionRunner: Function,
        additionalInfoCb: Function,
        
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
        this.treeItemAction = function(e){};
        //this.scopeSubtree(this.$.treeList, true);
    },

    attached: function () {},
    
    filter: function (text) {
        this._lastFilterText = text;
        this._filterSubTree(text, this._treeModel, true);
        this.splice("_entities", 0, this._entities.length, ...composeChildren.bind(this)(this._treeModel, true));
        this.debounce("refreshTree", refreshTree.bind(this));
    },

    expandSubTree: function(parentItem, refreshLoaded) {
        parentItem.opened = true;
        if (parentItem.entity.hasChildren && refreshLoaded && parentItem.entity.subtreeRefreshable) {
            parentItem.children = [generateLoadingIndicator(parentItem)];
        }
        parentItem.children.forEach(treeEntity => {
            if (treeEntity.entity.hasChildren && (refreshLoaded || wasLoaded(treeEntity))) {
                this.expandSubTree(treeEntity, refreshLoaded);
            }
        });
    },

    expandSubTreeView: function (idx, parentItem) {
        if (parentItem.entity.hasChildren) {
            const numOfItemsToDelete = calculateNumberOfOpenedItems(parentItem);
            this.expandSubTree(parentItem, true);
            this.notifyPath("_entities." + idx + ".opened", true);
            this.splice("_entities", idx + 1 + parentItem.additionalInfoNodes.length, numOfItemsToDelete, ...getChildrenToAdd.bind(this)(parentItem, true, true));
            this.debounce("refreshTree", refreshTree.bind(this));
        }
    },

    collapseSubTree: function (parentItem) {
        if (parentItem.entity.hasChildren) {
            if (parentItem.entity.subtreeRefreshable) {
                parentItem.children = [generateLoadingIndicator(parentItem)];
            } else {
                parentItem.children.forEach(child => this.collapseSubTree(child));
            }
            parentItem.opened = false;
        }
    },

    collapseSubTreeView: function(idx, parentItem) {
        if (parentItem.entity.hasChildren && parentItem.opened) {
            const numOfItemsToDelete = calculateNumberOfOpenedItems(parentItem);
            parentItem.children.forEach(child => this.collapseSubTree(child));
            this.splice("_entities", idx + 1 + parentItem.additionalInfoNodes.length, numOfItemsToDelete, ...getChildrenToAdd.bind(this)(parentItem, true, false));
            this.debounce("refreshTree", refreshTree.bind(this));
        }
    },
    
    reloadSubtreeFull: function (idx, entity) {
        if (entity.entity.hasChildren) {
            const numOfItemsToDelete = calculateNumberOfOpenedItems(entity);
            entity.children = [generateLoadingIndicator(entity)];
            if (!entity.opened) {
                this.set("_entities." + idx + ".opened", true);
            }
            this.splice("_entities", idx + 1 + entity.additionalInfoNodes.length, numOfItemsToDelete, ...getChildrenToAdd.bind(this)(entity, false, false));
            this.fire("tg-load-subtree", {parentPath: getParentsPath(entity), loadAll: true});
        }
    },
    
    removeSubtreeFull: function (idx, entity) {
        if (entity.entity.hasChildren) {
            if (entity.opened) {
                const numOfItemsToDelete = calculateNumberOfOpenedItems(entity);
                entity.children = [generateLoadingIndicator(entity)];
                this.splice("_entities", idx + 1 + entity.additionalInfoNodes.length, numOfItemsToDelete, ...getChildrenToAdd.bind(this)(entity, true, false));
            }
        }
    },

    _isSelected: function (selectedEntity, entity) {
        if (entity !== selectedEntity) {
            if (entity.additionalInfoNodes) {
                return entity.additionalInfoNodes.indexOf(selectedEntity) >= 0;
            } else if (entity.isAdditionalInfo) {
                return entity.relatedTo === selectedEntity || entity.relatedTo.additionalInfoNodes.indexOf(selectedEntity) >= 0
            }
        }
        return entity === selectedEntity && !entity.loaderIndicator;
    },

    _mouseItemEnter: function (e) {
        const entity = e.model.entity;
        if (entity.additionalInfoNodes && !entity.loaderIndicator) {
            this._setOver(e.model.index, true);
        } else if (entity.isAdditionalInfo) {
            this._setOver(this._getBaseEntityIdx(e.model.index), true);
        }
    },

    _mouseItemLeave: function (e) {
        const fromEntity = e.model.entity.isAdditionalInfo ? e.model.entity.relatedTo : e.model.entity;
        let toElement = e.toElement;
        while (toElement && !toElement.classList.contains("tree-node")) {
            toElement = toElement.parentElement;
        }
        const entityModel = this.$.treeList.modelForElement(toElement);
        const entity = entityModel && entityModel.entity;
        const toEntity = entity && entity.isAdditionalInfo ? entity.relatedTo : entity;
        if (fromEntity !== toEntity) {
            this._setOver(this._getBaseEntityIdx(e.model.index), false);
        }
    },

    _getBaseEntityIdx: function (idx) {
        const entity = this._entities[idx];
        if (entity) {
            if (entity.isAdditionalInfo) {
                const additionalInfoIdx = entity.relatedTo.additionalInfoNodes.indexOf(entity);
                return idx - additionalInfoIdx - 1;
            }
            return idx;
        }
        return -1;
    },

    _setOver: function (idx, over) {
        const entity = this._entities[idx];
        if (entity) {
            this.set("_entities." + idx + ".over", over);
            entity.additionalInfoNodes && entity.additionalInfoNodes.forEach((item, additionalInfoIdx) => {
                this.set("_entities." + (additionalInfoIdx + idx + 1) + ".over", over);
            });
        }
    },
    
    _filterSubTree: function (text, subtree, expand) {
        subtree.forEach(treeEntity => {
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
                treeEntity.opened = expand;
                this._filterSubTree(text, treeEntity.children, expand);
            }
        });
    },

    _regenerateModel: function () {
        this._treeModel = generateChildrenModel(this.model, null, this.additionalInfoCb); 
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
            this._childrenModelChanged(change);
        } else if (change.path && change.path.endsWith("splices")) {
            this._childrenSplices(change);
        }
    },

    _childrenModelChanged: function(change) {
        const path = change.path.substring(0, change.path.lastIndexOf(".")).replace("model", "_treeModel").replace(/#/g, "");
        const parentItem = this.get(path);
        const modelIdx = this._entities.indexOf(parentItem);
        if (parentItem) {
            const numOfItemsToDelete = calculateNumberOfOpenedItems(parentItem);
            parentItem.children = generateChildrenModel(change.value, parentItem, this.additionalInfoCb);
            this._lastFilterText && this._filterSubTree(this._lastFilterText, parentItem.children, false);
            this.fire("tg-tree-model-changed", parentItem);
            this.splice("_entities", modelIdx + 1 + parentItem.additionalInfoNodes.length, numOfItemsToDelete, ...getChildrenToAdd.bind(this)(parentItem, true, false));
            this.$.treeList.notifyResize();
        }
    },

    _childrenSplices: function (change) {
        const path = change.path.substring(0, change.path.lastIndexOf(".children.splices")).replace("model", "_treeModel").replace(/#/g, "");
        const parentItem = this.get(path);
        if (parentItem) {
            change.value.indexSplices.forEach(splice => {
                const indexForSplice = this._entities.indexOf(parentItem.children[splice.index]);
                const numOfItemsToDelete = calculateNumberOfOpenedItems(parentItem, splice.index, splice.removed.length);
                parentItem.children.splice(splice.index, splice.removed.length, ...generateChildrenModel(splice.object.slice(splice.index, splice.index + splice.addedCount), parentItem, this.additionalInfoCb));
                this._lastFilterText && this._filterSubTree(this._lastFilterText, parentItem.children.slice(splice.index, splice.index + splice.addedCount), false);
                this.fire("tg-tree-model-changed", parentItem);
                this.splice("_entities", indexForSplice, numOfItemsToDelete, ...getChildrenToAdd.bind(this)(parentItem, true, false, splice.index, splice.addedCount));
                this.$.treeList.notifyResize();
            });
        }
    },

    _toggle: function (e) {
        e.stopPropagation();
        const entity = e.model.entity;
        const idx = e.model.index;
        if (entity.opened) {
            this.splice("_entities", idx + 1 + entity.additionalInfoNodes.length, calculateNumberOfOpenedItems(entity));
            this.set("_entities." + idx + ".opened", false);
        } else {
            this.set("_entities." + idx + ".opened", true);
            this.splice("_entities", idx + 1 + entity.additionalInfoNodes.length, 0, ...getChildrenToAdd.bind(this)(entity, true, false));
        }
    },

    _calcItemStyle: function (entity) {
        let paddingLeft = 0;
        let parent = entity.entity ? entity.parent : entity.relatedTo.parent;
        while (parent) {
            paddingLeft += 32;
            parent = parent.parent;
        }
        return "padding-left: " + paddingLeft + "px";
    }

});
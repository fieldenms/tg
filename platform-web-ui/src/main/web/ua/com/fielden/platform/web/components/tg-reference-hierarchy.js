import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';

import '/resources/components/tg-tree.js';

import '/app/tg-reflector.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { composeDefaultUnconvertedEntityValue } from '/resources/editors/tg-entity-formatter.js';

const template = html`
    <style>
        :host {
            position: relative;
            @apply --layout-horizontal;
            width: 100%;
            height: 100%;
        }

        .hierarchy-container {
            @apply --layout-vertical;
            @apply --layout-flex;
            padding: 0 18px;
        }

        .editor-container {
            @apply --layout-vertical;
            padding: 0 4px;
        }

        .reference-hierarchy-tree {
            @apply --layout-flex;
            min-height: 0;
        }

        .lock-layer {
            @apply --layout-fit;
            opacity: 0.5;
            display: none;
            background-color: white;
        }
        .lock-layer[lock] {
            display: initial;
        }
    </style>
    <tg-reflector id="reflector"></tg-reflector>
    <div class="hierarchy-container">
        <div class="editor-container">
            <slot name="filter-element"></slot>
        </div>
        <tg-tree id="referenceHierarchyTree" class="reference-hierarchy-tree" model="[[treeModel]]" content-builder="[[_buildContent]]" tree-item-action="[[_loadMoreAction]]" additional-info-cb="[[_buildAdditionalInfo]]" action-builder="[[_buildActions]]" action-runner="[[_runAction]]" on-tg-load-subtree="_loadSubtree"></tg-tree>
    </div>
    <div class="lock-layer" lock$="[[lock]]"></div>`;

template.setAttribute('strip-whitespace', '');

const referenceHierarchyLevel = {
    TYPE: "TYPE",
    INSTANCE: "INSTANCE",
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
                msg: "The hierarchy wasn't detecteted for entity index: " + entityIndex + " at level: " + (index + 1)
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

const getParentsPath = function (entity) {
    const path = [];
    let parent = entity;
    while (parent) {
        path.push(parent);
        parent = parent.parent;
    }
    return path.reverse();
};

const getKeys = function (entity, entityWithKey) {
    let titleObject = filterKeys(entity, composeDefaultUnconvertedEntityValue(entityWithKey)).map(keyValue => {
        return {
            title: keyValue.title,
            value: typeof keyValue.value.type === 'function' ? getKeys(entity, keyValue.value) : keyValue.value
        };
    });
    if (titleObject.length === 1) {
        delete titleObject[0].title;
    }
    return titleObject;
}

const filterKeys = function (entity, keyValues) {
    return keyValues.filter(key => 
        typeof key.value.type === 'undefined' ||
        (typeof key.value.type === 'function' && 
        (key.value.type().fullClassName() !== entity.entity.parent.refEntityType || key.value.get("id") !== entity.entity.parent.refEntityId)));
};

const buildTitles = function (titleObject, reflector) {
    return titleObject.reduce((accum, curr, idx) => {
        const valueStyle = "font-size:16px;display:flex;flex-direction:row;align-items:center;" + (idx < titleObject.length - 1 ? "padding-right: 5px;" : "");
        accum += curr.title ? "<span style='font-size:0.8em;color:#737373;font-weight:bold;padding-right:2px;'>" + curr.title + ":&nbsp;</span>": "";
        accum += "<span class='part-to-highlight' style='" + valueStyle + "'>" + (curr.title && Array.isArray(curr.value) && curr.value.length > 1 ? "<span style='padding-right:2px;color:#737373;'>{</span>" : "")
            + (Array.isArray(curr.value) ? buildTitles(curr.value, reflector) : reflector.convert(curr.value)) + (curr.title && Array.isArray(curr.value) && curr.value.length > 1 ? "<span style='padding-left:2px;color:#737373;'>}</span>" : "") + "</span>";
        return accum;
    }, "");
};

const buildFilteringKey = function(titleObject) {
    return titleObject.map(curr => (Array.isArray(curr.value) ? buildFilteringKey(curr.value) : curr.value)).join(" ");
};

Polymer({
    _template: template,

    is: 'tg-reference-hierarchy',

    properties: {
        treeModel: {
            type: Object
        },

        entity: {
            type: Object,
            observer: "_entityChanged"
        },

        /**
         * Need for locking reference hierarchy component during data loading.
         */
        lock: {
            type: Boolean,
            value: false
        },
        
        _customActions: {
            type: Array
        },
        
        _saveQueue: Array,
        _saveInProgress: Boolean
    },

    /**
     * Initialisation block. It has all children web components already initialised.
     */
    ready: function() {
        //Configure the component's properties.
        this._saveQueue = [];
        this._saveInProgress = false;

        this._buildContent = function(entity, opened) {
            return "<div style='height:28px;font-size:16px;display:flex;flex-direction:row;align-items:center;'>" + 
                        this._getTitle(entity) + this._getAdditionalInfo(entity) +
                    "</div>";
        }.bind(this);
        this._loadMoreAction = function (e) {
            const entity = e.model.entity.entity;
            if (entity.isLoadMore) {
                entity.parent.pageNumber += 1;
                this.fire("tg-load-subtree", {parentPath: getParentsPath(entity.parent), loadAll: false});
            }
        }
        this._buildAdditionalInfo = function(entity) {
            return [];
        }.bind(this);
        this._buildActions = function (entity) {
           return "";
        }.bind(this);
        this._runAction = function (e) {
            e.stopPropagation();
            //TODO invoke action for tree item
        }.bind(this);
    },

    _getTitle: function (entity) {
        if (entity.entity.level === referenceHierarchyLevel.INSTANCE && !entity.entity.isLoadMore) {
            const titleObject = getKeys(entity, entity.entity.entity);
            entity.entity.key = buildFilteringKey(titleObject);
            return buildTitles(titleObject, this.$.reflector);
        } 
        return "<span class='part-to-highlight'>" + entity.entity.key + "</span>";
    },

    _getAdditionalInfo: function (entity) {
        let additionalInfo = "<span style='color:#737373'>";
        if (entity.entity.level === referenceHierarchyLevel.TYPE) {
            additionalInfo += "&nbsp;(" + entity.entity.numberOfEntities + ")";
        }
        return  additionalInfo + (entity.entity.desc ? "&nbsp;&ndash;&nbsp;<i>" + entity.entity.desc + "</i>" : "") + "</span>";       
    },

    _entityChanged: function(newBindingEntity) {
        const newEntity = newBindingEntity ? newBindingEntity['@@origin'] : null;
        if (newEntity) {
            this.fire('tg-dynamic-title-changed', newEntity.title);
            if (newEntity.resetFilter) {
                this.$.referenceHierarchyTree._lastFilterText = "";
            }
            const path = generatePath(this.treeModel, newEntity.loadedHierarchy);
            const parent = getPathItem(this.treeModel, newEntity.loadedHierarchy);
            newEntity.generatedHierarchy.forEach(entity => {
                entity.parent = parent;
            });
            if (parent && parent.level === referenceHierarchyLevel.TYPE) {
                parent.pageSize = newEntity.pageSize;
                parent.pageNumber = newEntity.pageNumber;
                parent.pageCount = newEntity.pageCount;
                if (parent.pageNumber === 0) {// Loading first page of instances
                    if (parent.pageCount > 1) {//Add load more if there are more pages
                        newEntity.generatedHierarchy.push({key: "Load more", desc: "", parent: parent, entity: null, isLoadMore: true, level: referenceHierarchyLevel.INSTANCE, hasChildren: false, children: []});
                    }
                    this.set(path, newEntity.generatedHierarchy);
                } else if (parent.pageNumber + 1 < parent.pageCount) { // Loading page that and there are more pages (children already have load more action)
                    this.splice(path, parent.children.length - 1, 0, ...newEntity.generatedHierarchy);
                } else if (parent.pageNumber + 1 >= parent.pageCount) {//Loading last page (remove load more action)
                    this.splice(path, parent.children.length - 1, 1, ...newEntity.generatedHierarchy);
                }
            } else {
                this.set(path, newEntity.generatedHierarchy);
            }
            newEntity.set("generatedHierarchy", []);
        }
        if (this._saveInProgress) {
            this._saveInProgress = false;
        }
        if (this._saveQueue.length > 0) {
            this._processEvent(this._saveQueue.shift());
        }
    },

    _loadSubtree: function(e) {
        if (this._saveQueue.length !== 0 || this._saveInProgress) {
            this._saveQueue.push(e);
        } else {
            this._processEvent(e);
        }
    },

    _processEvent: function (e) {
        const parentsPath = e.detail.parentPath;
        const indexes = parentsPath.map(entity =>  {
            const parentList = entity.parent ? entity.parent.children: this.treeModel;
            return parentList.indexOf(entity);
        });
        this.entity.setAndRegisterPropertyTouch("loadedHierarchy", indexes);
        const lastEntity = parentsPath[parentsPath.length - 1];
        if (lastEntity.level === referenceHierarchyLevel.TYPE) {
            this.entity.setAndRegisterPropertyTouch("pageSize", lastEntity.pageSize);
            this.entity.setAndRegisterPropertyTouch("pageNumber", lastEntity.pageNumber);
            this.entity.setAndRegisterPropertyTouch("entityType", lastEntity.entityType);
            this.entity.setAndRegisterPropertyTouch("refEntityType", lastEntity.refEntityType);
            this.entity.setAndRegisterPropertyTouch("refEntityId", lastEntity.refEntityId);
        } else {
            this.entity.setAndRegisterPropertyTouch("pageSize", 0);
            this.entity.setAndRegisterPropertyTouch("pageNumber", 0);
            this.entity.setAndRegisterPropertyTouch("entityType", null);
            this.entity.setAndRegisterPropertyTouch("refEntityType", lastEntity.entity.type().fullClassName());
            this.entity.setAndRegisterPropertyTouch("refEntityId", lastEntity.entity.get("id"));
        }
        this._saveInProgress = true;
        this.fire("tg-load-refrence-hierarchy", this.entity);
    },

    /**
     * Filters the hierarchy tree.
     */
    filterHierarchy: function(text) {
        this.$.referenceHierarchyTree.filter(text);
    },
});
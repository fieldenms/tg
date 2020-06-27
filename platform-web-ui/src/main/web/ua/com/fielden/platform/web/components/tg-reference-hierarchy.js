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
    <slot id="actions" name="reference-hierarchy-action"></slot>
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
    REFERENCE_INSTANCE: "REFERENCE_INSTANCE",
    REFERENCE_BY_INSTANCE: "REFERENCE_BY_INSTANCE",
    REFERENCES: "REFERENCES",
    REFERENCED_BY: "REFERENCED_BY",
    LOAD_MORE: "LOAD_MORE"
};

const referenceHierarchyActions = {
    EDIT: "EDIT",
    REFERENCE_HIERARCHY: "REFERENCE_HIERARCHY"
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
            value: typeof keyValue.value.type === 'function' ? getKeys(entity, keyValue.value) : keyValue.value,
            propertyName: keyValue.propertyName,
            type: keyValue.type
        };
    });
    if (titleObject.length === 1) {
        delete titleObject[0].title;
    }
    return titleObject;
}

const filterKeys = function (entity, keyValues) {
    return keyValues.length <= 1 ? keyValues : keyValues.filter(key => 
        typeof key.value.type === 'undefined' ||
        (typeof key.value.type === 'function' && 
        (key.value.type().fullClassName() !== entity.entity.parent.refEntityType || key.value.get("id") !== entity.entity.parent.refEntityId)));
};

const buildTitles = function (titleObject, reflector) {
    return titleObject.reduce((accum, curr, idx) => {
        const valueStyle = "font-size:16px;display:flex;flex-direction:row;align-items:center;" + (idx < titleObject.length - 1 ? "padding-right: 5px;" : "");
        accum += curr.title ? "<span style='font-size:0.8em;color:#737373;font-weight:bold;padding-right:2px;'>" + curr.title + ":&nbsp;</span>": "";
        accum += "<span class='part-to-highlight' style='" + valueStyle + "'>" + (curr.title && Array.isArray(curr.value) && curr.value.length > 1 ? "<span style='padding-right:2px;color:#737373;'>{</span>" : "")
            + (Array.isArray(curr.value) ? buildTitles(curr.value, reflector) : reflector.tg_toString(curr.value, curr.type, curr.propertyName)) + (curr.title && Array.isArray(curr.value) && curr.value.length > 1 ? "<span style='padding-left:2px;color:#737373;'>}</span>" : "") + "</span>";
        return accum;
    }, "");
};

const buildFilteringKey = function(titleObject) {
    return titleObject.map(curr => (Array.isArray(curr.value) ? buildFilteringKey(curr.value) : curr.value)).join(" ");
};

const escapeHtmlText = function(text) {
    const searchFor = [/</g, />/g];
    const replaceWith = ['&lt;', '&gt;'];
    let escapedStr = text;
    searchFor.forEach((search, i) => {
        escapedStr = escapedStr.replace(search, replaceWith[i]);
    });
    return escapedStr;
}

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

        centreUuid: {
            type: String
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

        //Configure action functions
        this._actions = {};
        this._actionBuilder = {};
        this._actionBuilder[referenceHierarchyActions.EDIT] = this._buildEditAction.bind(this)(this.$.actions.assignedNodes()[0]);
        this._actionBuilder[referenceHierarchyActions.REFERENCE_HIERARCHY] = this._buildReferenceHierarchyAction.bind(this)(this.$.actions.assignedNodes()[1]);

        this._buildContent = function(entity, opened) {
            return "<div style='height:28px;font-size:16px;display:flex;flex-direction:row;align-items:center;'>" + 
                        this._getTitle(entity) + this._getAdditionalInfo(entity) +
                    "</div>";
        }.bind(this);
        this._loadMoreAction = function (e) {
            const entity = e.model.entity.entity;
            if (entity.level === referenceHierarchyLevel.LOAD_MORE) {
                entity.parent.pageNumber += 1;
                this.fire("tg-load-subtree", {parentPath: getParentsPath(entity.parent), loadAll: false});
            }
        }
        this._buildAdditionalInfo = function(entity) {
            return [];
        }.bind(this);
        this._buildActions = function (entity) {
            let actionsStr = "";
            entity.entity && entity.entity.actions && entity.entity.actions.forEach(action => {
                actionsStr += this._actionBuilder[action](entity);
            });
            return actionsStr;
        }.bind(this);
        this._runAction = function (e) {
            e.stopPropagation();
            const target = e.target || e.srcElement;
            this._actions[target.getAttribute("action-attr")](e);
        }.bind(this);
    },

    _buildEditAction: function(action) {
        action.shortDesc = "";
        this._actions[referenceHierarchyActions.EDIT] = (e) => {
            action.currentEntity = e.model.entity.entity.entity;
            action._run();
        };
        return (entity) => {
            const typeTitle = this.$.reflector.getType(entity.entity.entity.type().notEnhancedFullClassName()).entityTitle();
            action.longDesc = "Edit " + typeTitle;  
            return this._generateIconForAction(action, referenceHierarchyActions.EDIT);
        }
    },

    _buildReferenceHierarchyAction: function(action) {
        this._actions[referenceHierarchyActions.REFERENCE_HIERARCHY] = (e) => {
            action.currentEntity = e.model.entity.entity.entity;
            action._run();
        }
        return (entity) => {    
            return this._generateIconForAction(action, referenceHierarchyActions.REFERENCE_HIERARCHY);
        }
    },

    _generateIconForAction: function (action, attrValue) {
        return "<iron-icon style='" + action.iconStyle + "' icon='" + action.icon + "' action-attr='" + attrValue + "' tooltip-text='" + action.longDesc + "'></iron-icon>";
    },

    _getTitle: function (entity) {
        if (entity.entity.level === referenceHierarchyLevel.REFERENCE_BY_INSTANCE || entity.entity.level === referenceHierarchyLevel.REFERENCE_INSTANCE) {
            const titleObject = getKeys(entity, entity.entity.entity);
            entity.entity.key = (entity.entity.level === referenceHierarchyLevel.REFERENCE_INSTANCE ? entity.entity.propertyTitle + ":" : "") 
                                + buildFilteringKey(titleObject);
            return (entity.entity.level === referenceHierarchyLevel.REFERENCE_INSTANCE ? "<span class='part-to-highlight'>" + entity.entity.propertyTitle + ":&nbsp;</span>" : "") 
                    + buildTitles(titleObject, this.$.reflector);
        } else if (entity.entity.level === referenceHierarchyLevel.LOAD_MORE) {
            return "<div style='padding:3px;color:#03a9f4;-moz-user-select: none;-ms-user-select: none;-webkit-user-select: none;"+
                    "user-select: none;cursor: pointer;text-transform: uppercase;' tooltip-text='Load more data'>More</div";
        }
        return "<span class='part-to-highlight'>" + entity.entity.key + "</span>";
    },

    _getAdditionalInfo: function (entity) {
        if (entity.entity.level === referenceHierarchyLevel.TYPE) {
            return "<span style='color:#737373'>&nbsp;(" + entity.entity.numberOfEntities + ")"
                    + (entity.entity.desc ? "&nbsp;&ndash;&nbsp;<i>" + escapeHtmlText(entity.entity.desc) + "</i>" : "") + "</span>";
        } else if (entity.entity.level === referenceHierarchyLevel.REFERENCED_BY ||
            entity.entity.level === referenceHierarchyLevel.REFERENCES) {
            return "<span style='color:#737373'>&nbsp;(" + entity.entity.desc + ")</span>";
        }else {
            return "<span style='color:#737373'>" + (entity.entity.desc ? "&nbsp;&ndash;&nbsp;<i>" + escapeHtmlText(entity.entity.desc) + "</i>" : "") + "</span>"; 
        }
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
                        newEntity.generatedHierarchy.push({key: "Load more", desc: "", parent: parent, entity: null, level: referenceHierarchyLevel.LOAD_MORE, hasChildren: false, children: []});
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
        this.entity.setAndRegisterPropertyTouch("loadedLevel", lastEntity.level);
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
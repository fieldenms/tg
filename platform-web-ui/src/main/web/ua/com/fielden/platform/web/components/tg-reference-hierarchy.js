import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';

import '/resources/components/tg-tree.js';

import '/app/tg-reflector.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const template = html`
    <style>
        :host {
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
        }
    </style>
    <tg-reflector id="reflector"></tg-reflector>
    <div class="hierarchy-container">
        <div class="editor-container">
            <slot name="filter-element"></slot>
        </div>
        <tg-tree id="referenceHierarchyTree" class="reference-hierarchy-tree" model="[[treeModel]]" content-builder="[[_buildContent]]" additional-info-cb="[[_buildAdditionalInfo]]" action-builder="[[_buildActions]]" action-runner="[[_runAction]]" on-tg-load-subtree="_loadSubtree"></tg-tree>
    </div>`;

template.setAttribute('strip-whitespace', '');

const generatePath = function(treeModel, loadedHierarchy) {
    let path = "treeModel";
    let model = treeModel;
    loadedHierarchy.forEach((entityIndex, index) => {
        const treeEntry = model[entityIndex];
        if (treeEntry) {
            path += "." + entityIndex + ".children";
        } else {
            throw {
                msg: "The hierarchy wasn't detecteted for id: " + id + " at level: " + (index + 1)
            };
        }
        model = model[treeEntryIndex].children;
    });
    return path;
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
            return "<div>" + this.$.reflector.convert(entity.entity) + "</div>";
        }.bind(this);
        this._buildAdditionalInfo = function(entity) {
            return [];
        }.bind(this);
        this._buildActions = function (entity) {
            //TODO build actions as icons return html string of those actions 
            return "";
        }.bind(this);
        this._runAction = function (e) {
            e.stopPropagation();
            //TODO invoke action for tree item
        }.bind(this);
    },
    
    _entityChanged: function(newBindingEntity) {
        const newEntity = newBindingEntity ? newBindingEntity['@@origin'] : null;
        if (newEntity) {
            const path = generatePath(this.treeModel, newEntity.loadedHierarchy);
            const childrenIndex = path.lastIndexOf(".children");
            if (childrenIndex >= 0) {
                const pathWithoutChildren = path.substring(0, childrenIndex);
                parent = this.get(pathWithoutChildren);
                newEntity.generatedHierarchy.forEach(entity => {
                    entity.parent = parent;
                })
            }
            this.set(path, newEntity.generatedHierarchy);
            newEntity.set("generatedHierarchy", []);
        }
        if (this._saveInProgress) {
            this._saveInProgress = false;
        }
        if (this._saveQueue.length > 0) {
            this._processEvent(this._saveQueue.shift());
        }
    },

    _processEvent: function (e) {
        //TODO Process another action in queue.
        this.fire("tg-load-refrence-hierarchy", this.entity);
    },

    /**
     * Filters the hierarchy tree.
     */
    filterHierarchy: function(text) {
        this.$.referenceHierarchyTree.filter(text);
    },
});
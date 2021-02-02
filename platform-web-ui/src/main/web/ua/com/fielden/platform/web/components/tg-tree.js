import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-icons/av-icons.js';
import '/resources/polymer/@polymer/iron-list/iron-list.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { TgTreeListBehavior } from '/resources/components/tg-tree-list-behavior.js';

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
    <iron-list id="treeList" items="[[_entities]]" as="entity" selected-item="{{selectedEntity}}" selection-enabled>
        <template>
            <div class="layout horizontal center tree-node no-wrap" over$="[[entity.over]]" selected$="[[_isSelected(selectedEntity, entity)]]" on-mouseenter="_mouseItemEnter" on-mouseleave="_mouseItemLeave" style$="[[itemStyle(entity)]]">
                <iron-icon class="expand-button" icon="av:play-arrow" style="flex-grow:0;flex-shrink:0;" invisible$="[[!entity.entity.hasChildren]]" collapsed$="[[!entity.opened]]" on-tap="_toggle"></iron-icon>
                <span class="tree-item" highlighted$="[[entity.highlight]]" inner-h-t-m-l="[[contentBuilder(entity, entity.opened)]]" on-tap="treeItemAction"></span>
                <span class="tree-item-actions" on-tap="actionRunner" inner-h-t-m-l="[[actionBuilder(entity)]]"></span>
            </div>
        </template>
    </iron-list>`;

Polymer({
    _template: template,

    is: 'tg-tree',

    properties: {

        contentBuilder: Function,
        actionBuilder: Function,
        treeItemAction: Function,
        actionRunner: Function,
    },

    behaviors: [TgTreeListBehavior],

    ready: function () {
        this.treeItemAction = function(e){};
    },

    resizeTree: function () {
        this.$.treeList.notifyResize();
    },

    isEntityRendered: function (index) {
        this.$.treeList._isIndexRendered(index)
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
            this.setOver(e.model.index, true);
        } else if (entity.isAdditionalInfo) {
            this.setOver(this._getBaseEntityIdx(e.model.index), true);
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
            this.setOver(this._getBaseEntityIdx(e.model.index), false);
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

    _toggle: function (e) {
        e.stopPropagation();
        this.toggle(e.model.index);
    },
});
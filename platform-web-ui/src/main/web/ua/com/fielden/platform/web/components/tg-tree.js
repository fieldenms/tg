import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-icons/av-icons.js';
import '/resources/polymer/@polymer/iron-list/iron-list.js';

import '/resources/polymer/@polymer/paper-checkbox/paper-checkbox.js';


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
        .tree-item:not([with-checkbox]) {
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
        paper-checkbox {
            padding-left: 4px;
            --paper-checkbox-animation-duration: 0;
            --paper-checkbox-checked-color: var(--paper-light-blue-700);
            --paper-checkbox-checked-ink-color: var(--paper-light-blue-700);
            --paper-checkbox-unchecked-color: var(--paper-grey-900);
            --paper-checkbox-unchecked-ink-color: var(--paper-grey-900); 
            --paper-checkbox-ink-size: 34px;
            --paper-checkbox-label_-_display: none;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <iron-list id="treeList" items="[[_entities]]" as="entity" selected-item="{{selectedEntity}}" selection-enabled="[[!selectWithCheckbox]]">
        <template>
            <div class="layout horizontal center tree-node no-wrap" over$="[[entity.over]]" selected$="[[_isSelected(selectedEntity, entity, selectWithCheckbox, entity.selected)]]" on-mouseenter="_mouseItemEnter" on-mouseleave="_mouseItemLeave" style$="[[itemStyle(entity)]]">
                <iron-icon class="expand-button" icon="av:play-arrow" style="flex-grow:0;flex-shrink:0;" invisible$="[[!entity.entity.hasChildren]]" collapsed$="[[!entity.opened]]" on-tap="_toggle"></iron-icon>
                <paper-checkbox noink checked="[[entity.selected]]" hidden$="[[_shouldHideCheckbox(selectWithCheckbox, entity.isAdditionalInfo)]]" on-tap="_changeSelection"></paper-checkbox>
                <span class="tree-item" with-checkbox$="[[selectWithCheckbox]]" highlighted$="[[entity.highlight]]" inner-h-t-m-l="[[contentBuilder(entity, entity.opened)]]" on-tap="treeItemAction"></span>
                <span class="tree-item-actions" on-tap="actionRunner" inner-h-t-m-l="[[actionBuilder(entity)]]"></span>
            </div>
        </template>
    </iron-list>`;

Polymer({
    _template: template,

    is: 'tg-tree',

    properties: {
        selectWithCheckbox: {
            type: Boolean,
            value: false
        },
        contentBuilder: Function,
        actionBuilder: Function,
        treeItemAction: Function,
        actionRunner: Function,
    },

    behaviors: [TgTreeListBehavior],

    ready: function () {
        this.treeItemAction = function(e){};
        this.$.treeList._isClientFull = this._isTreeClientFull.bind(this.$.treeList);
    },

    _isTreeClientFull: function () {
        return this._physicalSize >= this._viewportHeight * 2;
    },

    resizeTree: function () {
        this.$.treeList.notifyResize();
    },

    listEntityIndex: function (entity) {
        return this._entities.indexOf(entity);
    },

    isEntityRendered: function (index) {
        return this.$.treeList._isIndexRendered(index);
    },

    scrollToItem: function (treeItem, force) {
        const itemIndex = this._entities.indexOf(treeItem);
        if (itemIndex >= 0 && (force || (this.$.treeList.firstVisibleIndex >= itemIndex || this.$.treeList.lastVisibleIndex <= itemIndex))) {
            this.$.treeList.scrollToItem(treeItem);
        }
    },

    _isSelected: function (selectedEntity, entity, selectWithCheckbox, entitySelected) {
        if (selectWithCheckbox) {
            return entitySelected;
        } else if (entity !== selectedEntity) {
            if (entity.additionalInfoNodes) {
                return entity.additionalInfoNodes.indexOf(selectedEntity) >= 0;
            } else if (entity.isAdditionalInfo) {
                return entity.relatedTo === selectedEntity || entity.relatedTo.additionalInfoNodes.indexOf(selectedEntity) >= 0
            }
        }
        return entity === selectedEntity && !entity.loaderIndicator;
    },

    _shouldHideCheckbox: function (selectWithCheckbox, isAdditionalInfo) {
        return isAdditionalInfo || !selectWithCheckbox;
    },

    _changeSelection: function (e) {
        const entity = e.model.entity;
        if (entity.additionalInfoNodes && !entity.loaderIndicator) {
            this.setSelected(e.model.index, !entity.selected);
        } else if (entity.isAdditionalInfo) {
            this.setSelected(this._getBaseEntityIdx(e.model.index), !entity.selected);
        }
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
        if (fromEntity !== toEntity && this.currentMatchedItem !== this._entities[this._getBaseEntityIdx(e.model.index)]) {
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
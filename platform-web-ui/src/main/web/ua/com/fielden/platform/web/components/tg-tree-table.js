import {mixinBehaviors} from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';
import {html, PolymerElement} from '/resources/polymer/@polymer/polymer/polymer-element.js';

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
    <iron-list id="treeList" items="[[_entities]]" as="entity" selected-item="{{selectedEntity}}" default-physical-count="500" selection-enabled>
    <template>
        <div class="layout horizontal center tree-node no-wrap" over$="[[entity.over]]" selected$="[[_isSelected(selectedEntity, entity)]]" on-mouseenter="_mouseItemEnter" on-mouseleave="_mouseItemLeave" style$="[[itemStyle(entity)]]">
            <iron-icon class="expand-button" icon="av:play-arrow" style="flex-grow:0;flex-shrink:0;" invisible$="[[!entity.entity.hasChildren]]" collapsed$="[[!entity.opened]]" on-tap="_toggle"></iron-icon>
            <span class="tree-item" highlighted$="[[entity.highlight]]" inner-h-t-m-l="[[contentBuilder(entity, entity.opened)]]" on-tap="treeItemAction"></span>
            <span class="tree-item-actions" on-tap="actionRunner" inner-h-t-m-l="[[actionBuilder(entity)]]"></span>
        </div>
    </template>
    </iron-list>`;

export class TgEntityEditorResult extends mixinBehaviors([TgTreeListBehavior], PolymerElement) {

    static get properties() {
        return {
            
        };
    }
}
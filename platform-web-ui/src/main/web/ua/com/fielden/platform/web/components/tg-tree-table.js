import {mixinBehaviors} from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';
import {html, PolymerElement} from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { FlattenedNodesObserver } from '/resources/polymer/@polymer/polymer/lib/utils/flattened-nodes-observer.js';

import { TgTreeListBehavior } from '/resources/components/tg-tree-list-behavior.js';
import { TgEgiDataRetrievalBehavior } from '/resources/egi/tg-egi-data-retrieval-behavior.js';


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
        .no-wrap {
            min-width: min-content;
            white-space: nowrap;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <slot id="hierarchy_column_slot" name="hierarchy-column"></slot>
    <slot id="regular_column_slot" name="regular-column"></slot>
    <div id="scrollableContainer">
        <div id="baseContainer">
            <div id="top_left" class="layout horizontal" hidden$="[[!hierarchyColumn]]">
                <div class="no-wrap">[[hierarchyColumn.columnTitle]]</div>
            </div>
            <div id="top" class="layout horizontal">
                <template is="dom-repeat" items="[[regularColumns]]">
                    <div class="no-wrap">[[item.columnTitle]]</div>
                </template>
            </div>
            <div id="left">
                <iron-list id="mainTreeList" items="[[_entities]]" as="entity">
                    <div style$="[[itemStyle(entity)]]">
                        <iron-icon class="expand-button" icon="av:play-arrow" style="flex-grow:0;flex-shrink:0;" invisible$="[[!entity.entity.hasChildren]]" collapsed$="[[!entity.opened]]" on-tap="_toggle"></iron-icon>
                        <span>[[getBindedValue(entity, hierarchyColumn)]]</span>
                    </div>
                </iron-list>
            </div>
            <div id="centre">
                <iron-list id="regularTreeList" items="[[_entities]]" as="entity">
                    <template is="dom-repeat" items="[[regularColumns]]" as="column">
                        <div class="no-wrap" highlighted$="[[entity.highlight]]">[[getBindedValue(entity, column)]]</div>
                    </template>
                </iron-list>
            </div>
        </div>
    </div>`;

export class TgTreeTable extends mixinBehaviors([TgTreeListBehavior, TgEgiDataRetrievalBehavior], PolymerElement) {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            /**
             * Column descriptor that contains information about property that should be displayed in hierarchy of entities. Also it has
             * an information about property that creates relationship between entotoes in hierarchy.
             */    
            hierarchyColumn: Object,
            /**
             * Array of columns with information about additional properties that should be displayed along with hierarchy property.
             */
            regularColumns: Array
        };
    }

    ready () {
        super.ready();
        
        this.hierarchyColumn = this.$.hierarchy_column_slot.assignedNodes({flatten: true})[0];
        this.regularColumns = this.$.regular_column_slot.assignedNodes({flatten:  true});
    }

    resizeTree () {
        this.$.mainTreeList.notifyResize();
        this.$.regularTreeList.notifyResize();
    }

    isEntityRendered (index) {
        this.$.mainTreeList._isIndexRendered(idx)
    }

    _toggle (e) {
        e.stopPropagation();
        this.toggle(e.model.index);
    }

}

customElements.define('tg-tree-table', TgTreeTable);
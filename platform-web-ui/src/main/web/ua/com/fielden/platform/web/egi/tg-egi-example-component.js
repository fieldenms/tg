import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/egi/tg-entity-grid-inspector.js';
import '/resources/egi/tg-property-column.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { EntityStub } from '/resources/reflection/tg-polymer-utils.js';

const template = html`
    <tg-entity-grid-inspector id="egi" entities="[[entities]]" checkbox-visible>
        <template is="dom-repeat" items="[[columns]]">
            <tg-property-column slot='property-column' property="[[item.property]]" type="[[item.type]]" width="[[item.width]]" min-width="[[item.minWidth]]" grow-factor="[[item.growFactor]]" column-title="[[item.columnTitle]]" column-desc="[[item.columnDesc]]"></tg-property-column>
        </template>
    </tg-entity-grid-inspector>`;

function createEntity(id, params) {
    const entity = new EntityStub(id);
    Object.keys(params).forEach(key => {
        entity.set(key, params[key]);
    });
    return entity;
}

Polymer({

    _template: template,

    is: 'tg-egi-example-component',

    properties: {
        entities: Array
    },

    ready: function () {
        const columns = [];
        columns.push({
            property: "key",
            type: "String",
            width: 60,
            minWidth: 40,
            growFactor: 1,
            columnTitle: "Key",
            columnDesc: "Entity Key"
        });
        columns.push({
            property: "desc",
            type: "String",
            width: 80,
            minWidth: 60,
            growFactor: 1,
            columnTitle: "Description",
            columnDesc: "Entity Description"
        });
        columns.push({
            property: "intProp",
            type: "Integer",
            width: 60,
            minWidth: 40,
            growFactor: 1,
            columnTitle: "Integer Property",
            columnDesc: "Some Integer Property"
        });
        for (let i = 0; i <= 10; i++) {
            columns.push({
                property: "decProp" + i,
                type: "BigDecimal",
                width: 60,
                minWidth: 40,
                growFactor: 1,
                columnTitle: "Decimal Property " + i,
                columnDesc: "Some Decimal Property " + i
            });
        }
        this.columns = columns;
        const customEntitites = [];
        for (let i = 0; i < 20; i++) {
            const entity = {
                key: "KEY_" + (i + 1), 
                desc: (i + 1) + " Entity", 
                intProp: i + 34
            };
            for (let j = 0; j <= 10; j++) {
                entity["decProp" + j] = 12.23 * i + j;
            }
            customEntitites.push(createEntity(i + 1, entity));
        }
        this.entities = customEntitites;

        this.$.egi.hasAction = () => false;
        this.$.egi.tap = () => {};
    }
});
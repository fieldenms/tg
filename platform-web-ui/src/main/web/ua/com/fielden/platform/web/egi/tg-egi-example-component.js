import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/egi/tg-entity-grid-inspector.js';
import '/resources/egi/tg-property-column.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { EntityStub } from '/resources/reflection/tg-polymer-utils.js';

const template = html`
    <tg-entity-grid-inspector id="egi" entities="[[entities]]" checkbox-visible>
        <tg-property-column slot='property-column' property="key" type="String" width="60" min-width="40" grow-factor="1" column-title="Key" column-desc="Entity Key"></tg-property-column>
        <tg-property-column slot='property-column' property="desc" type="String" width="80" min-width="60" grow-factor="1" column-title="Description" column-desc="Entity Description"></tg-property-column>
        <tg-property-column slot='property-column' property="intProp" type="Integer" width="60" min-width="40" grow-factor="1" column-title="Integer Property" column-desc="Some Integer Property"></tg-property-column>
        <tg-property-column slot='property-column' property="decProp" type="BigDecimal" width="60" min-width="40" grow-factor="1" column-title="Decimal Property" column-desc="Some Decimal Property"></tg-property-column>
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
        const customEntitites = [];
        customEntitites.push(createEntity(1, {key: "KEY1", desc: "1 Entity", intProp: 34, decProp: 12.23}));
        customEntitites.push(createEntity(2, {key: "KEY2", desc: "2 Entity", intProp: 35, decProp: 13.23}));
        customEntitites.push(createEntity(3, {key: "KEY3", desc: "3 Entity", intProp: 36, decProp: 14.23}));
        customEntitites.push(createEntity(3, {key: "KEY4", desc: "4 Entity", intProp: 37, decProp: 15.23}));
        customEntitites.push(createEntity(3, {key: "KEY5", desc: "5 Entity", intProp: 38, decProp: 16.23}));
        customEntitites.push(createEntity(3, {key: "KEY6", desc: "6 Entity", intProp: 39, decProp: 17.23}));
        customEntitites.push(createEntity(3, {key: "KEY7", desc: "7 Entity", intProp: 40, decProp: 18.23}));
        customEntitites.push(createEntity(3, {key: "KEY8", desc: "8 Entity", intProp: 41, decProp: 19.23}));
        customEntitites.push(createEntity(3, {key: "KEY9", desc: "9 Entity", intProp: 42, decProp: 20.23}));
        customEntitites.push(createEntity(3, {key: "KEY10", desc: "10 Entity", intProp: 43, decProp: 21.23}));
        this.entities = customEntitites;

        this.$.egi.hasAction = () => false;
        this.$.egi.tap = () => {};
    }
});
import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const template = html`
    <style>
        :host {
            @apply --layout-horizontal;
            @apply --layout-center;
        }
        .truncate {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
    </style>
    <input id="checkbox" type="checkbox" on-change="_stateChanged">`;

Polymer({
    _template: template,

    is: 'tg-tree-table-regular-cell',

    properties: {
        column: Object,
        entity: {
            type: Object,
            observer: "_entityChanged"
        }
    },
    
    _entityChanged: function (newEntity, oldEntity) {
        const state = this.entity.getState(this.column.property);
        this._setState(state === "CHECKED", state);
        this.entity._tokenRoleAssociationHandler[this.column.property] = (value, state) => {
            this._setState(value, state);
        };
    },
    
    _setState(value, state) {
        this.$.checkbox.checked = value;
        this.$.checkbox.indeterminate = state === "SEMICHECKED";
    },
    
    _stateChanged: function (e) {
        const target = e.target || e.srcElement;
        this.column.check(this.entity, this.column.property, target.checked);
    },
    
    _isGroupCheckbox: function(children) {
        return !!(children && children.length > 0);
    },
});
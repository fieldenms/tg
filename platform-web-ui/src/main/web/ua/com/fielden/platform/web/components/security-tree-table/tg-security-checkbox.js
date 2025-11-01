import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';

import { allDefined } from '/resources/reflection/tg-polymer-utils.js';

const template = html`
    <style>
        :host {
            @apply --layout-horizontal;
            @apply --layout-center;
        }
    </style>
    <input id="checkbox" type="checkbox" on-change="_stateChanged">`; 

class TgSecurityCheckbox extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            column: Object,
            entity: Object
        };
    }

    static get observers() {
        return [
          '_dataChanged(entity, column)'
        ];
    }

    _dataChanged (entity, column) {
        if (!allDefined(arguments)) {
            return;
        }
        const state = this.entity.entity.getState(this.propertyName);
        this._setState(state === "CHECKED", state);
    }

    get propertyName () {
        if (this.column.isHierarchyColumn) {
            return "_token"
        }
        return this.column.property;
    }
    
    _setState(value, state) {
        this.$.checkbox.checked = value;
        this.$.checkbox.indeterminate = state === "SEMICHECKED";
    }
    
    _stateChanged (e) {
        if (this.entity && this.column) {
            this.column.check(this.entity.entity, this.propertyName, e.target.checked);
        }
    }
}

customElements.define('tg-security-checkbox', TgSecurityCheckbox);
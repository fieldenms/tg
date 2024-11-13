import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import '/resources/polymer/@polymer/paper-button/paper-button.js';

import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';

import '/resources/editors/tg-colour-picker.js';

import {TgReflector} from '/app/tg-reflector.js';
import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js';


const template = html`
    <style>
        :host {
            @apply --layout-vertical;
        }
        tg-colour-picker {
            padding: 20px 20px 0 20px;
        }
        .actions {
            padding: 10px;
            @apply --layout-horizontal;
            @apply --layout-wrap;
            @apply --layout-center-justified;
        }
        paper-button {
            width: 80px;
            font-size: 10pt;
            line-height: normal;
            margin: 10px;
        }
    </style>
    <tg-colour-picker id="colorEditor" entity="[[_entity]]" property-name="colorProp"
        current-state="EDIT" prop-title="Text Color" prop-desc="The color of the selected text"
        action="null" validation-callback="[[_validationCallback]]">
    </tg-colour-picker>
    <div class="actions">
        <paper-button raised roll="button" on-tap="cancelCallback" tooltip-text="Cancel text color">
            <span>Cancel</span>
        </paper-button>
        <paper-button raised roll="button" on-tap="okCallback" tooltip-text="Color the selected text">
            <span>OK</span>
        </paper-button>
    </div>`;

const createEntity = function(reflector) {
    const fullEntityType = reflector.getEntityPrototype();
    fullEntityType.compoundOpenerType = () => null;

    const fullEntity = reflector.newEntityEmpty();

    fullEntity.get = prop => {
        if (prop === '') { // empty property name means 'entity itself'
            return fullEntity;
        }
        return fullEntity[prop];
    };
    fullEntity._type = fullEntityType;
    fullEntity.id = -1;
    fullEntity.version = 0;
    fullEntity.colorProp = {hashlessUppercasedColourValue: ''};
    
    const bindingView = reflector.newEntityEmpty();
    bindingView['id'] = -1;
    bindingView['version'] = 0;
    bindingView['@@touchedProps'] = {
        names: [],
        values: [],
        counts: []
    };
    bindingView['@@origin'] = fullEntity;
    bindingView['colorProp'] = {hashlessUppercasedColourValue: ''};
    bindingView['@colorProp_editable'] = true;
    bindingView.get = prop => {
        if (prop === '') { // empty property name means 'entity itself'
            return bindingView;
        }
        return bindingView[prop];
    };
    const bindingViewType = reflector.getEntityPrototype();
    bindingViewType.prop = (name) => {
        return {
            type: () => name === 'colorProp' ? 'object' : null
        }
    };
    bindingView._type = bindingViewType;
    return bindingView;
}

export class TgColorPickerDialog extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            
            cancelCallback: Function,

            okCallback: Function,

            //private properties
            _validationCallback: Function,

            _entity: Object,
            
            _reflector: Object
        }
    }

    constructor() {
        super();
        this._reflector = new TgReflector();
        this._entity = createEntity(this._reflector);
        this._validationCallback = function () {
            console.log("validation of color entity");
        };
        this.addEventListener("addon-attached", this._onAddonAttached.bind(this));
    }

    ready () {
        super.ready();
    }

    set color(newColor) {
        const hashlessColor = newColor ? (newColor.startsWith('#') ? newColor.substring(1) : newColor).toUpperCase() : '';
        this.$.colorEditor.assignConcreteValue({hashlessUppercasedColourValue: hashlessColor}, this._reflector.tg_convert.bind(this._reflector));
        this.$.colorEditor.commitIfChanged();
    }

    get color() {
        return this._entity['colorProp'] ? `#${this._entity['colorProp'].hashlessUppercasedColourValue}` : '';
    }

    focusDefaultEditor() {
        this.$.colorEditor.$.input.focus();
    }

    resetState() {
        this.color = '';
    }

    cancel(e) {}

    accept(e) {
        this.$.colorEditor.commitIfChanged();
        return !this.$.colorEditor._error;
    }

    _onAddonAttached(e) {
        tearDownEvent(e);
    }
}

customElements.define('tg-color-picker-dialog', TgColorPickerDialog);
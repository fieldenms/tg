import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import '/resources/polymer/@polymer/paper-button/paper-button.js';

import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';

import '/resources/editors/tg-colour-picker.js';

import {TgReflector} from '/app/tg-reflector.js';

const template = html`
    <style>
        :host {
            padding: 20px;
            @apply --layout-vertical;
        }
        .actions {
            padding: 10px;
            @apply --layout-horizontal;
            @apply --layout-wrap;
            @apply --layout-center-justified;
        }
    </style>
    <tg-colour-picker id="colorEditor" entity="[[_entity]]" property-name="colorProp"
        current-state="EDIT" prop-title="Text Color" prop-desc="The color of the selected text"
        action="null" validation-callback="[[_validationCallback]]">
    </tg-colour-picker>
    <div class="actions">
        <paper-button raised roll="button" on-tap="_cancleLink" style="width:80px;" tooltip-text="Cancel text color">
            <span>Cancel</span>
        </paper-button>
        <paper-button raised roll="button" on-tap="_okLink" style="width:80px;" tooltip-text="Color the selected text">
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
    bindingView['@colorProp_required'] = true;
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
            //public properties
            color: {
                type: String,
                value: '',
                observer: "_colorChanged"
            },

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
    }

    _cancleLink() {
        this._resetEntity();
        this.cancelCallback && this.cancelCallback();
    }

    _okLink() {
        let hasError = false;
        this.$.colorEditor.commitIfChanged();
        if (!this._entity['colorProp'] || !this._entity['colorProp'].hashlessUppercasedColourValue) {
            this._entity['@colorProp_error'] = {
                '@resultType': 'ua.com.fielden.platform.error.Result',
                //'ex': {message: 'The URL should not be empty'},
                'message' : 'The color should not be empty'
            }
            this.$.colorEditor._updateMessagesForEntity(this._entity);
            this.$.colorEditor.$.input.focus();
            hasError = true;
        } else {
            delete this._entity['@colorProp_error'];
            this.$.colorEditor._updateMessagesForEntity(this._entity);
            hasError = false;
        }
        if (!hasError) {
            this.color = `#${this._entity['colorProp'].hashlessUppercasedColourValue}`;
            this._resetEntity();
            this.okCallback && this.okCallback();
        }
    }

    _colorChanged(newColor) {
        const hashlessColor = (newColor.startsWith('#') ? newColor.substring(1) : newColor).toUpperCase();
        if (this._entity['colorProp'] && this._entity['colorProp'].hashlessUppercasedColourValue !== hashlessColor) {
            this.$.colorEditor.assignConcreteValue({hashlessUppercasedColourValue: hashlessColor}, this._reflector.tg_convert.bind(this._reflector));
            this.$.colorEditor.commit();
        }
    }

    _resetEntity() {
        this.$.colorEditor.assignConcreteValue({hashlessUppercasedColourValue: ''}, this._reflector.tg_convert.bind(this._reflector));
        this.$.colorEditor.commit();
    }

}

customElements.define('tg-color-picker-dialog', TgColorPickerDialog);
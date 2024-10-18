import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import '/resources/polymer/@polymer/paper-button/paper-button.js';

import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';

import '/resources/editors/tg-singleline-text-editor.js';
import '/resources/editors/tg-hyperlink-editor.js';

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
    <tg-hyperlink-editor id="urlEditor" entity="[[_entity]]" property-name="urlProp"
        current-state="EDIT" prop-title="URL" prop-desc="Page URL to insert"
        action="null" validation-callback="[[validationCallback]]">
    </tg-hyperlink-editor>
    <div class="actions">
        <paper-button raised roll="button" on-tap="_cancelLink" style="width:80px;" tooltip-text="Do not insert a link">
            <span>Cancel</span>
        </paper-button>
        <paper-button raised roll="button" on-tap="_okLink" style="width:80px;" tooltip-text="Insert a link">
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
    fullEntity.urlProp = {value: ''};
    
    const bindingView = reflector.newEntityEmpty();
    bindingView['id'] = -1;
    bindingView['version'] = 0;
    bindingView['@@touchedProps'] = {
        names: [],
        values: [],
        counts: []
    };
    bindingView['@@origin'] = fullEntity;
    bindingView['urlProp'] = {value: ''};
    bindingView['@urlProp_editable'] = true;
    bindingView.get = prop => {
        if (prop === '') { // empty property name means 'entity itself'
            return bindingView;
        }
        return bindingView[prop];
    };
    const bindingViewType = reflector.getEntityPrototype();
    bindingViewType.prop = (name) => {
        return {
            type: () => 'object'
        }
    };
    bindingView._type = bindingViewType;
    return bindingView;
}

export class TgLinkDialog extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            //public properties
            linkText: {
                type: String,
                value: '',
            },

            validationCallback: Function,

            cancelCallback: Function,

            okCallback: Function,

            //private properties

            _entity: Object,
            
            _reflector: Object
        }
    }

    constructor() {
        super();
        this._reflector = new TgReflector();
        this._entity = createEntity(this._reflector);
        this.validationCallback = function () {
            console.log("validation of link entity");
        };
    }

    set url(newUrl) {
        this.$.urlEditor.assignConcreteValue({value: newUrl}, this._reflector.tg_convert.bind(this._reflector));
        this.$.urlEditor.commitIfChanged();
    }

    get url() {
        return this._entity['urlProp'] ? this._entity['urlProp'].value : '';
    }

    _cancelLink() {
        this.cancelCallback && this.cancelCallback();
    }

    _okLink() {
        this.$.urlEditor.commitIfChanged();
        if (!this.$.urlEditor._error) {
            this.okCallback && this.okCallback();
        }
    }

    resetState() {
        this.url = '';
        this.linkText = '';
    }
}

customElements.define('tg-link-dialog', TgLinkDialog);
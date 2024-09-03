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
    <tg-singleline-text-editor id="linkTextEditor" entity="[[_entity]]" property-name="linkTextProp"
        current-state="EDIT" prop-title="URL Text" prop-desc="The description of the URL to insert"
        action="null" validation-callback="[[validationCallback]]">
    </tg-singleline-text-editor>
    <div class="actions">
        <paper-button raised roll="button" on-tap="_cancleLink" style="width:80px;" tooltip-text="Do not insert a link">
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
    fullEntity.linkTextProp = '';
    
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
    bindingView['@urlProp_required'] = true;
    bindingView['linkTextProp'] = '';
    bindingView['@linkTextProp_editable'] = true;
    bindingView['@linkTextProp_required'] = true;
    bindingView.get = prop => {
        if (prop === '') { // empty property name means 'entity itself'
            return bindingView;
        }
        return bindingView[prop];
    };
    const bindingViewType = reflector.getEntityPrototype();
    bindingViewType.prop = (name) => {
        return {
            type: () => name === 'linkTextProp' ? 'string' : 'object'
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
            url: {
                type: String,
                value: ''
            },

            linkText: {
                type: String,
                value: ''
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

    _cancleLink() {
        this._resetEntity();
        this.cancelCallback && this.cancelCallback();
    }

    _okLink() {
        let hasError = false;
        this.$.urlEditor.commitIfChanged();
        this.$.linkTextEditor.commitIfChanged();
        if (!this._entity['urlProp'] || !this._entity['urlProp'].value) {
            this._entity['@urlProp_error'] = {
                '@resultType': 'ua.com.fielden.platform.error.Result',
                //'ex': {message: 'The URL should not be empty'},
                'message' : 'The URL should not be empty'
            }
            this.$.urlEditor._updateMessagesForEntity(this._entity);
            this.$.urlEditor.$.input.focus();
            hasError = true;
        } else {
            delete this._entity['@urlProp_error'];
            this.$.urlEditor._updateMessagesForEntity(this._entity);
        }
        if (!this._entity['linkTextProp']) {
            this._entity['@linkTextProp_error'] = {
                '@resultType': 'ua.com.fielden.platform.error.Result',
                //'ex': {message: 'The link description should not be empty'},
                'message' : 'The link description should not be empty'
            }
            this.$.linkTextEditor._updateMessagesForEntity(this._entity);
            if (!hasError) {
                this.$.linkTextEditor.$.input.focus();
            }
            hasError = true;
        } else {
            delete this._entity['@linkTextProp_error'];
            this.$.linkTextEditor._updateMessagesForEntity(this._entity);
        }
        if (!hasError) {
            this.url = this._entity['urlProp'].value;
            this.linkText = this._entity['linkTextProp'];
            this._resetEntity();
            this.okCallback && this.okCallback();
        }
    }

    _resetEntity() {
        this.$.urlEditor.assignConcreteValue({value: ''}, this._reflector.tg_convert.bind(this._reflector));
        this.$.urlEditor.commit();
        this.$.linkTextEditor.assignConcreteValue('', this._reflector.tg_convert.bind(this._reflector));
        this.$.linkTextEditor.commit();
    }

}

customElements.define('tg-link-dialog', TgLinkDialog);
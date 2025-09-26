import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import '/resources/polymer/@polymer/paper-button/paper-button.js';

import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import {mixinBehaviors} from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';

import '/resources/editors/tg-singleline-text-editor.js';
import '/resources/editors/tg-hyperlink-editor.js';
import { tearDownEvent, createStubBindingEntity} from '/resources/reflection/tg-polymer-utils.js';
import {TgTooltipBehavior} from '/resources/components/tg-tooltip-behavior.js';

import {TgReflector} from '/app/tg-reflector.js';

const template = html`
    <style>
        :host {
            background-color: white;
            box-shadow: 0px 2px 6px #ccc;
            @apply --layout-vertical;
        }
        tg-hyperlink-editor {
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
    <tg-hyperlink-editor id="urlEditor" entity="[[_entity]]" property-name="urlProp"
        current-state="EDIT" prop-title="URL" prop-desc="Page URL to insert"
        action="null" validation-callback="[[validationCallback]]" toaster="[[toaster]]">
    </tg-hyperlink-editor>
    <div class="actions">
        <paper-button raised roll="button" on-tap="cancelCallback" tooltip-text="Do not insert a link">
            <span>Cancel</span>
        </paper-button>
        <paper-button raised roll="button" on-tap="okCallback" tooltip-text="Insert a link">
            <span>OK</span>
        </paper-button>
    </div>`;
    
export class TgLinkDialog extends mixinBehaviors([TgTooltipBehavior], PolymerElement) {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            //public properties
            validationCallback: Function,

            cancelCallback: Function,

            okCallback: Function,

            toaster: Object,

            //private properties

            _entity: Object,
            
            _reflector: Object
        }
    }

    constructor() {
        super();
        this._reflector = new TgReflector();
        this._entity = createStubBindingEntity('LinkDialogEntity',
            {'urlProp': {value: {value: ''}, editable: true}},
            (name) => {
                return {
                    type: () => 'object'
                }
            }
        );
        this.validationCallback = function () {};
        this.addEventListener("addon-attached", this._onAddonAttached.bind(this));
    }

    ready () {
        super.ready();
    }

    set url(newUrl) {
        this.$.urlEditor.assignConcreteValue({value: newUrl}, this._reflector.tg_convert.bind(this._reflector));
        this.$.urlEditor.commitIfChanged();
    }

    get url() {
        return this._entity['urlProp'] ? this._entity['urlProp'].value : '';
    }

    focusDefaultEditor() {
        this.$.urlEditor.$.input.focus();
    }

    resetState() {
        this.url = '';
    }

    cancel(e) {}

    accept(e) {
        this.$.urlEditor.commitIfChanged();
        return !this.$.urlEditor._error;
    }

    _onAddonAttached(e) {
        tearDownEvent(e);
    }
}

customElements.define('tg-link-dialog', TgLinkDialog);
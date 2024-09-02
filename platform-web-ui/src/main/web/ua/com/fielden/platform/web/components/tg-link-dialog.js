import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import '/resources/polymer/@polymer/paper-button/paper-button.js';

import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';

import '/resources/editors/tg-singleline-text-editor.js';
import '/resources/editors/tg-hyperlink-editor.js';

import { EntityStub } from '/resources/reflection/tg-polymer-utils.js';

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
    <tg-hyperlink-editor entity="[[entity]]" property-name="urlProp"
        current-state="EDIT" prop-title="URL" prop-desc="Page URL to insert"
        action="null" validation-callback="[[validationCallback]]">
    </tg-hyperlink-editor>
    <tg-singleline-text-editor entity="[[entity]]" property-name="linkTextProp"
        current-state="EDIT" prop-title="URL Text" prop-desc="The description of the URL to insert"
        action="null">
    </tg-singleline-text-editor>
    <div class="actions">
        <paper-button raised roll="button" on-tap="_cancleLink" style="width:80px;" tooltip-text="Do not insert a link">
            <span>Cancel</span>
        </paper-button>
        <paper-button raised roll="button" on-tap="_okLink" style="width:80px;" tooltip-text="Insert a link">
            <span>OK</span>
        </paper-button>
    </div>`;

class LinkEntity extends EntityStub {

    constructor(id) {
        super(id);
        this.urlProp = null;
        this.linkTextProp = null;
    }
} 

export class TgLinkDialog extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            entity: Object,

            validaionCallback: {
                type: Function,
            }
        }
    }

    constructor() {
        super();
        this.entity = new LinkEntity(-1);
        this.validaionCallback = function () {
            console.log("validation of link entity");
        };
    }

}

customElements.define('tg-link-dialog', TgLinkDialog);
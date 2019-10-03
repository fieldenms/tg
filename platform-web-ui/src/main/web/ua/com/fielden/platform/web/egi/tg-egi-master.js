import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import { TgEntityMasterBehavior } from '/resources/master/tg-entity-master-behavior.js';

import {PolymerElement, html} from '/resources/polymer/@polymer/polymer/polymer-element.js';
import {mixinBehaviors} from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';

const template = html`
    <tg-entity-master
        id="masterDom"
        entity-type="[[entityType]]"
        entity-id="[[entityId]]"
        _post-validated-default="[[_postValidatedDefault]]"
        _post-validated-default-error="[[_postValidatedDefaultError]]"
        _process-response="[[_processResponse]]"
        _process-error="[[_processError]]"
        _process-retriever-response="[[_processRetrieverResponse]]"
        _process-retriever-error="[[_processRetrieverError]]"
        _process-saver-response="[[_processSaverResponse]]"
        _process-saver-error="[[_processSaverError]]"
        _saver-loading="{{_saverLoading}}">
            <slot id="egiEditors" name="egi-editor"></slot>
    </tg-entity-master>`;

export class TgEgiMaster extends mixinBehaviors([TgEntityMasterBehavior], PolymerElement) {

    static get template() { 
        return template;
    }

    static get properties () {
        return {
            editors: Array
        };
    }

    constructor () {
        this.noUI = false;
        this.saveOnActivation = false;
    }

    ready () {
        super.ready();
        this.editors = this.$.egiEditors.assignedNodes();
    }

    _masterDom () {
        return this.$.masterDom;
    }

    /**
     * The core-ajax component for entity retrieval.
     */
    _ajaxRetriever () {
        return this._masterDom()._ajaxRetriever();
    }

    /**
     * The core-ajax component for entity saving.
     */
    _ajaxSaver () {
        return this._masterDom()._ajaxSaver();
    }

    /**
     * The validator component.
     */
    _validator () {
        return this._masterDom()._validator();
    }

    /**
     * The component for entity serialisation.
     */
    _serialiser () {
        return this._masterDom()._serialiser();
    }

    /**
     * The reflector component.
     */
    _reflector () {
        return this._masterDom()._reflector();
    }

    /**
     * The toast component.
     */
    _toastGreeting () {
        return this._masterDom()._toastGreeting();
    }
}

customElements.define('tg-egi-master', TgEgiMaster);
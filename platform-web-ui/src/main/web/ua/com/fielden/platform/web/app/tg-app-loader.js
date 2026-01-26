import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import {mixinBehaviors} from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import moment from '/resources/polymer/lib/moment-lib.js';

import { TgEntityMasterBehavior } from '/resources/master/tg-entity-master-behavior.js';
import { generateUUID } from '/resources/reflection/tg-polymer-utils.js';

import '/resources/master/tg-entity-master.js';

import '/app/tg-app-config.js';
import '/app/tg-app.js';

const template = html`
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <style>
        tg-app-template {
            height: 100vh;
        }
    </style>
    <tg-app-config id="appConfig"></tg-app-config>
    <template is="dom-if" if="[[_appConfigLoaded]]">
        <tg-app-template class="layout vertical" app-title="[[appTitle]]" idea-uri="[[ideaUri]]"></tg-app-template>
    </template>
    <tg-entity-master
        id="masterDom"
        entity-type="[[entityType]]"
        entity-id="[[entityId]]"
        hidden
        _post-validated-default="[[_postValidatedDefault]]"
        _post-validated-default-error="[[_postValidatedDefaultError]]"
        _process-response="[[_processResponse]]"
        _process-error="[[_processError]]"
        _process-retriever-response="[[_processRetrieverResponse]]"
        _process-retriever-error="[[_processRetrieverError]]"
        _process-saver-response="[[_processSaverResponse]]"
        _process-saver-error="[[_processSaverError]]"
        _saver-loading="{{_saverLoading}}">
    </tg-entity-master>`;
    
export class TgAppLoader extends mixinBehaviors([TgEntityMasterBehavior], PolymerElement) {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            /**
             * The property which indicates whether entity master is 'UI-less'.
             */
            noUI: {
                type: Boolean,
                value: true
            },
            
            appTitle: String,
            
            ideaUri: String,
            
            _appConfigLoaded: {
                type: Boolean,
                readOnly: true,
                value: false

            }
        }
    }

    ready () {
        super.ready();
        //setting the uuid for this master.
        this.uuid = this.is + '/' + generateUUID();
        //Init master related functions.
        this.entityType = "ua.com.fielden.platform.entity.ApplicationConfigEntity";
        this.entityId = "new";
        this.postRetrieved = (entity, bindingEntity, customObject) => {
            this.$.appConfig.setMinDesktopWidth(entity.minDesktopWidth);
            this.$.appConfig.setMinTabletWidth(entity.minTabletWidth);
            this.$.appConfig.setLocale(entity.locale);
            this.$.appConfig.setMasterActionOptions(entity.masterActionOptions);
            this.$.appConfig.setFirstDayOfWeek(entity.firstDayOfWeek);
            this.$.appConfig.setSiteAllowlist(entity.siteAllowlist.map(site => new RegExp(site)));
            this.$.appConfig.setDaysUntilSitePermissionExpires(entity.daysUntilSitePermissionExpires);
            this.$.appConfig.setCurrencySymbol(entity.currencySymbol);
            if (entity.timeZone) {
                moment.tz.setDefault(entity.timeZone);
            }
            moment.locale('custom-locale', {
                longDateFormat: {
                    LTS: entity.timeWithMillisFormat,
                    LT: entity.timeFormat,
                    L: entity.dateFormat
                }
            });
            this._set_appConfigLoaded(true);
        };
        setTimeout(() => {
            const context = this._reflector().createContextHolder(null, null, null, null, null, null, null);
            
            this.retrieve(context);
            this._toastGreeting().text = "Loading configurations...";
            this._toastGreeting().hasMore = false;
            this._toastGreeting().showProgress = true;
            this._toastGreeting().msgHeading = "Info";
            this._toastGreeting().isCritical = false;
            this._toastGreeting().show();
        }, 0);
    }


   //Entity master related functions

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

customElements.define('tg-app-loader', TgAppLoader);
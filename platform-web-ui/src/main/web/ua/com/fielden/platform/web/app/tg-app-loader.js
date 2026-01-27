import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import moment from '/resources/polymer/lib/moment-lib.js';
import '/resources/polymer/@polymer/iron-ajax/iron-ajax.js';
import '/resources/components/tg-toast.js';
import '/app/tg-app-config.js';

const template = html`
    <tg-app-config id="appConfig"></tg-app-config>
    <iron-ajax id="configLoader"
        headers="[[_headers]]"
        url="/app/configuration"
        method="GET"
        handle-as="json"
        on-response="_processAppConfigResponse"
        reject-with-request
        on-error="_processAppConfigError">
    </iron-ajax>
    <!--template is="dom-if" if="[[_appConfigLoaded]]">
        <tg-app-template class="layout vertical" app-title="[[appTitle]]" idea-uri="[[ideaUri]]"></tg-app-template>
    </template-->
    <tg-toast id="messageToast"></tg-toast>`;
    
export class TgAppLoader extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            appTitle: String,
            
            ideaUri: String,
            
            resourcesToImport: String
        }
    }

    ready () {
        super.ready();
        
        this._processAppConfigResponse = (e) => {
            if (e.detail.xhr.status === 200 && e.detail.response) {
                const entity = e.detail.response;
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
                this._importAppAndLoadIntoBody();
            }
            
        };
        this._processAppConfigError = (e) => {
            console.log('PROCESS ERROR', error);
            const xhr = e.xhr;
            if (xhr.status === 500) { // internal server error, which could either be due to business rules or have some other cause due to a bug or db connectivity issue
                this._openErrorToast('Server responded with error.', xhr.errorMsg, true);
            } else if (xhr.status === 403) { // forbidden!
                this._openErrorToast('Access denied.', 'The current session has expired. Please login and try again.', true);
            } else if (xhr.status === 503) { // service unavailable
                this._openErrorToast('Service Unavailable.', 'Server responded with error 503 (Service Unavailable).', true);
            } else if (xhr.status >= 400) { // other client or server error codes
                this._openErrorToast('Service Error (' + xhr.status + ').', 'Server responded with error code ' + xhr.status, true);
            } else {
                // this situation may occur if the server was accessible, but the return status code does not match any of the expected ones, or
                // the server should not be reached, for example, due to a network failure, or
                // the request was aborted -- aborted requests should not report any errors to users
                console.warn('Server responded with error code ', xhr.status);
                if (!request.aborted) {
                    const [msgHeader, msgBody] = xhr.status === 0 // if status is 0 then it is most likely a network failure
                                                ? ['Could not process the request.', 'Please make sure your device is connected to the network.']
                                                : ['Unexpected error occurred.', `Error code [${xhr.status}]. Please contact support.`];
                    this._openErrorToast(`${msgHeader}`, error.message, true);
                }
            }
        };
        setTimeout(() => {
            this.$.configLoader.generateRequest();
            this._openLoadToast();
        }, 0);
    }

    _importAppAndLoadIntoBody() {
        import(this.resourcesToImport).then((module) => {
            const appElement = document.createElement("tg-app-template");
            appElement.classList.add("layout", "vertical");
            appElement.appTitle = this.appTitle;
            appElement.ideaUri = this.ideaUri;
            document.body.appendChild(appElement);
        });
    }

    _openLoadToast() {
        this.$.messageToast.text = "Loading configurations...";
        this.$.messageToast.hasMore = false;
        this.$.messageToast.showProgress = true;
        this.$.messageToast.msgHeading = "Info";
        this.$.messageToast.isCritical = false;
        this.$.messageToast.show();
    }

    _openErrorToast (toastMsg, moreInfo, isCritical) {
        this.$.messageToast.isCritical = isCritical;
        this.$.messageToast.text = toastMsg;
        if (moreInfo) {
            this.$.messageToast.hasMore = true;
            this.$.messageToast.msgText = moreInfo;
        }
        this.$.messageToast.showProgress = false;
        this.$.messageToast.msgHeading = "Error";
        this.$.messageToast.show();
    }
}

customElements.define('tg-app-loader', TgAppLoader);
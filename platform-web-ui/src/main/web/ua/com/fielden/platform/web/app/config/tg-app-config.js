import { PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';

import '/resources/components/postal-lib.js';
import moment from '/resources/polymer/lib/moment-lib.js';

if (window.TG_APP && window.TG_APP.timeZone) {
    moment.tz.setDefault(window.TG_APP.timeZone);
}
moment.locale('custom-locale', {
    longDateFormat: {
        LTS: (window.TG_APP && window.TG_APP.timeWithMillisFormat) || "HH:mm:ss.SSS",
        LT: (window.TG_APP && window.TG_APP.timeFormat) || "HH:mm",
        L: (window.TG_APP && window.TG_APP.dateFormat) || "DD/MM/YYYY"
    }
});

export const MasterActionOptions = {
    ALL_ON: "ALL_ON",
    ALL_OFF: "ALL_OFF"
};

export class TgAppConfig extends PolymerElement {

    static get properties() {
        return {
            // Determines the minimum screen width at which the desktop layout is applied.
            // This variable is assigned only once.
            //
            minDesktopWidth: {
                type: Number,
                readOnly: true,
                notify: true,
                value: (window.TG_APP && window.TG_APP.minDesktopWidth) || 980
            },
            // Determines the minimum screen width at which the tablet layout is applied.
            // This variable is assigned only once.
            //
            minTabletWidth: {
                type: Number,
                readOnly: true,
                notify: true,
                value: (window.TG_APP && window.TG_APP.minTabletWidth) || 768
            },
            // Determines the locale for this application.
            // This variable is assigned only once.
            //
            locale: {
                type: String,
                readOnly: true,
                notify: true,
                value: (window.TG_APP && window.TG_APP.locale) || "en-AU"
            },
            // A variable that defines a currency symbol, used to represent monetary values as strings.
            // This variable is assigned only once.
            //
            currencySymbol: {
                type: String,
                readOnly: true,
                notify: true,
                value: (window.TG_APP && window.TG_APP.currencySymbol) || "$"
            },
            // Determines the options for master actions.
            // This variable is assigned only once.
            //
            masterActionOptions: {
                type: String,
                readOnly: true,
                notify: true,
                value: (window.TG_APP && window.TG_APP.masterActionOptions) || MasterActionOptions.ALL_OFF
            },
            // Determines the first day of the week (Sunday, Monday,...).
            // This variable is assigned only once.
            //
            firstDayOfWeek: {
                type: Number,
                notify: true,
                readOnly: true,
                value: window.TG_APP && typeof window.TG_APP.firstDayOfWeek !== 'undefined' ? window.TG_APP.firstDayOfWeek : 0
            },
            // External site allowlist for hyperlinks that can be opened without a confirmation prompt.
            // This variable is assigned only once.
            //
            siteAllowlist: {
                type: Array,
                notify: true,
                readOnly: true,
                value: (window.TG_APP && window.TG_APP.siteAllowlist && window.TG_APP.siteAllowlist.map(site => new RegExp(site))) || []
            },
            // A number of days for caching user-allowed sites/links that can be opened without a confirmation prompt.
            // This variable is assigned only once.
            //
            daysUntilSitePermissionExpires: {
                type: Number,
                notify: true,
                readOnly: true,
                value: window.TG_APP && typeof window.TG_APP.daysUntilSitePermissionExpires !== 'undefined' ? window.TG_APP.daysUntilSitePermissionExpires : 0
            }

        }
    }

    ready () {
        super.ready();
        this.style.display = 'none';
    }
}

customElements.define('tg-app-config', TgAppConfig);
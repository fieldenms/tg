import { PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';

import '/resources/components/postal-lib.js';
import moment from '/resources/polymer/lib/moment-lib.js';

/**
 * Timer Id to reconnect via sse after an error.
 */
let timerIdForReconnection = null;

/**
 *  Registers new single event source for this client.
 */
const registerEventSource = function () {
    const uid = crypto.randomUUID();
    const source = {initialised: false, sseUid: uid, uri: `/sse/${uid}`, shouldReconnectWhenError: true, errorReconnectionDelay: 15000};
    console.log('Determine if EventSrouce needs polyfilling: ', window.EventSource)
    if (window.EventSource == undefined) {
        console.warn('EventSource polyfilling is in progress.');
        const esPoly = document.createElement('script');
        esPoly.src = '/resources/polyfills/eventsource.min.js';
        esPoly.onload = () => registerEventSourceHandlers(source);
        document.head.appendChild(esPoly);
    } else {
        registerEventSourceHandlers(source);
    }
    return source;
};

const registerEventSourceHandlers = function (sourceObj) {
    console.log("Registering EventSource handlers for URI ", sourceObj.uri);

    const source = new EventSource(sourceObj.uri);
    sourceObj.source = source;
    sourceObj.initialised = true;

    source.addEventListener('message', function (e) {

        const data = JSON.parse(e.data);

        postal.publish({
            channel: "sse-event",
            topic: `${data.eventSourceClass ? "/" + data.eventSourceClass : ""}${data.jobUid ? "/" + data.jobUid : ""}/message`,
            data: {
                msg: data
            }
        });

    }, false);

    source.addEventListener('completed', function (e) {
        console.log('the observable at the server-side completed');
    }, false);

    source.addEventListener('connection', function (e) {
        console.log('connection message received from server');
    }, false);

    source.addEventListener('open', function (e) {
        console.log('opened connection');
    }, false);

    source.addEventListener('error', function (e) {
        console.log('an error occurred: ', e);

        // publish event for error handling
        postal.publish({
            channel: "sse-event",
            topic: "error",
            data: {
                event: e,
                source: sourceObj
            }
        });

        // only after custom error handling should we attempt to reconnect
        // this is because the decision to reconnect can be made in the custom error handler
        if (sourceObj.shouldReconnectWhenError === true && e.eventPhase === EventSource.CLOSED) {
            // connection was closed by the server;
            // ensure client-side EventSource to be closed and initialised flag to be false;
            // ensure also that previous reconnection timeout is cancelled and made null;
            // previous reconnection timeout should not be possible here because SSE EventSource (and server side resource) will be closed prior to this;
            // however it is not harmful to check -- see closeEventSource() method
            closeEventSource(sourceObj);
            // Let's kick a timer for reconnection...
            timerIdForReconnection = setTimeout(() => {
                try {
                    registerEventSourceHandlers(sourceObj);
                } finally {
                    timerIdForReconnection = null;
                }
            }, sourceObj.errorReconnectionDelay);
        }

    }, false);
};

/* Closes an existing event source connection. This operation also ensures that any scheduled reconnections are cancelled and cleared after EventSource closing. */
const closeEventSource = function (sourceObj) {
    if (sourceObj.source) {
        sourceObj.initialised = false;
        const src = sourceObj.source;
        sourceObj.source = null;
        src.close();
    }
    if (typeof timerIdForReconnection === 'number') { // if there is in-progress timeout handling reconnection
        clearTimeout(timerIdForReconnection); // then cancel this timeout immediately
        timerIdForReconnection = null;
    }
};

//Holds all registered event sources in application.
export const eventSource = registerEventSource();

/**
 * Determines the minimum screen width at which the desktop layout is applied.
 */
let minDesktopWidth;

/**
 * Determines the minimum screen width at which the tablet layout is applied.
 */
let minTabletWidth;

/**
 * Determines the locale for this application.
 */
let locale;

/**
 * Determines the options for master actions.
 */
let masterActionOptions;

/**
 * Determines the first day of the week (Sunday, Monday,...).
 */
let firstDayOfWeek;

/**
 * External site allowlist for hyperlinks that can be opened without a confirmation prompt.
 */
let siteAllowlist;

/**
 * A number of days for caching user-allowed sites/links that can be opened without a confirmation prompt.
 */
let daysUntilSitePermissionExpires;

export const MasterActionOptions = {
    ALL_ON: "ALL_ON",
    ALL_OFF: "ALL_OFF"
};

export class TgAppConfig extends PolymerElement {

    static get properties() {
        return {
            minDesktopWidth: {
                type: Number,
                readOnly: true,
                notify: true,
                value: minDesktopWidth
            },
            minTabletWidth: {
                type: Number,
                readOnly: true,
                notify: true,
                value: minTabletWidth
            },
            locale: {
                type: String,
                readOnly: true,
                notify: true,
                value: locale
            },
            masterActionOptions: {
                type: String,
                readOnly: true,
                notify: true,
                value: masterActionOptions
            },
            firstDayOfWeek: {
                type: Number,
                notify: true,
                readOnly: true,
                value: firstDayOfWeek
            },
            siteAllowlist: {
                type: Array,
                notify: true,
                readOnly: true,
                value: siteAllowlist
            },
            daysUntilSitePermissionExpires: {
                type: Number,
                notify: true,
                readOnly: true,
                value: daysUntilSitePermissionExpires
            }

        }
    }

    set minDesktopWidth (value) {
        if (typeof minDesktopWidth === 'undefined') {
            minDesktopWidth = value;
        }
    }

    set minTabletWidth (value) {
        if (typeof minTabletWidth === 'undefined') {
            minTabletWidth = value;
        }
    }

    set locale (value) {
        if (typeof locale === 'undefined') {
            locale = value;
        }
    }

    set masterActionOptions (value) {
        if (typeof masterActionOptions === 'undefined') {
            masterActionOptions = value;
        }
    }

    set firstDayOfWeek (value) {
        if (typeof firstDayOfWeek === 'undefined') {
            firstDayOfWeek = value;
        }
    }

    set siteAllowlist (value) {
        if (typeof siteAllowlist === 'undefined') {
            siteAllowlist = value;
        }
    }

    set daysUntilSitePermissionExpires (value) {
        if (typeof daysUntilSitePermissionExpires === 'undefined') {
            daysUntilSitePermissionExpires = value;
        }
    }
}

customElements.define('tg-app-config', TgAppConfig);
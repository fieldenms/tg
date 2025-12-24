import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';

import '/resources/components/postal-lib.js';
import moment from '/resources/polymer/lib/moment-lib.js';

moment.locale('custom-locale', {
    longDateFormat: {
        LTS: @timeWithMillisFormat,
        LT: @timeFormat,
        L: @dateFormat
    }
});

/**
 * External site allowlist for hyperlinks that can be opened without a confirmation prompt.
 */
let siteAllowlist;

/**
 * A number of days for caching user-allowed sites/links that can be opened without a confirmation prompt.
 */
let daysUntilSitePermissionExpires;

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

export const MasterActionOptions = {
    ALL_ON: "ALL_ON",
    ALL_OFF: "ALL_OFF"
};

export const TgAppConfig = Polymer({
    
    is: "tg-app-config",
    
    properties: {
        minDesktopWidth: {
            type: Number,
            readOnly: true,
            value: @minDesktopWidth
        },
        minTabletWidth: {
            type: Number,
            readOnly: true,
            value: @minTabletWidth
        },
        locale: {
            type: String,
            readOnly: true,
            value: @locale
        },
        dateFormat: {
            type: String,
            readOnly: true,
            value: @dateFormat
        },
        timeFormat: {
            type: String,
            readOnly: true,
            value: @timeFormat
        },
        masterActionOptions: {
            type: String,
            readOnly: true,
            notify: true,
            value: @masterActionOptions
        },
        firstDayOfWeek: {
            type: Number,
            notify: true,
            readOnly: true,
            value: window.firstDayOfWeek
        },
    },
    
    attached: function() {
        this.style.display = "none";
    },

    getSiteAllowlist: function () {
        return siteAllowlist;
    },

    setSiteAllowlist: function (allowlist) {
        if (!siteAllowlist) {
            siteAllowlist = allowlist;
        }
    },

    getDaysUntilSitePermissionExpires: function () {
        return daysUntilSitePermissionExpires;
    },

    setDaysUntilSitePermissionExpires: function (expiryDays) {
        if (!daysUntilSitePermissionExpires) {
            daysUntilSitePermissionExpires = expiryDays;
        }
    }
    
});
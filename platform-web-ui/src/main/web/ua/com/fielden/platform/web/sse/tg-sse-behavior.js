import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/components/postal-lib.js';

import { random } from '/resources/reflection/tg-numeric-utils.js';

//Holds all registered event sources in application.
const eventSources = {};

/**
 *  Registers new event source or returns the existing one
 */
const registerEventSource = function (uri) {
    let source = eventSources[uri];
    if (!source) {
        source = {initialised: false, uri: uri, shouldReconnectWhenError: true, errorReconnectionDelay: 15000};
        eventSources[uri] = source;
        console.log('Determine if EventSrouce needs polyfilling: ', window.EventSource)
        if (window.EventSource == undefined) {
            console.warn('EventSrouce polyfilling is in progress.');
            const esPoly = document.createElement('script');
            esPoly.src = '/resources/polyfills/eventsource.min.js';
            esPoly.onload = () => registerEventSourceHandlers(source);
            document.head.appendChild(esPoly);
        } else {
            registerEventSourceHandlers(source);
        }
    }
    return source;
};

const registerEventSourceHandlers = function (sourceObj) {
    console.log("Registering EventSrouce handlers for URI ", sourceObj.uri);

    const source = new EventSource(sourceObj.uri);
    sourceObj.source = source;
    sourceObj.initialised = true;

    source.addEventListener('message', function (e) {

        const data = JSON.parse(e.data);

        postal.publish({
            channel: "sse-event",
            topic: `${sourceObj.uri}${data.eventSourceClass ? "/" + data.eventSourceClass : ""}/message`,
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
            topic: sourceObj.uri + "/error",
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
            sourceObj._timerIdForReconnection = setTimeout(() => {
                try {
                    registerEventSourceHandlers(sourceObj);
                } finally {
                    sourceObj._timerIdForReconnection = null;
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
    if (typeof sourceObj._timerIdForReconnection === 'number') { // if there is in-progress timeout handling reconnection
        clearTimeout(sourceObj._timerIdForReconnection); // then cancel this timeout immediately
        sourceObj._timerIdForReconnection = null;
    }
};

export const TgSseBehavior = {

    properties: {
        /* A URI that should be connected to for listening to push events. */
        uri: {
            type: String,
            observer: '_uriChanged'
        },

        eventSourceClass: {
            type: String
        },

        /* A function that takes a single String arugment representing the push event data. */
        dataHandler: {
            type: Function
        },

        /* An optional function to react to SSE errors. Accepts error event as an argument. */
        errorHandler: {
            type: Function
        },

        /**postal subcription on sse message event */
        _messageSubscription: Object,

        /**postal subscription on sse error event */
        _errorSubscription: Object,

        /* A timer ID to delay reactions to SSE events in case a delay was requested. */
        _timerIdForDataHandling: {
            type: Number,
            value: null
        },

        /* Indicates the need to schedule dataHandler execution based on 'minDelay' and 'delay' properties of event data. */
        useTimerBasedScheduling: {
            type: Boolean,
            value: true
        }

    },

    attached: function () {
        if (this.uri && this.source && !this._messageSubscription && !this._errorSubscription) {
            this._subscribeToSseEvent();
        }
    },

    detached: function () {
        this._unsubscribeFromSseEvents();
    },

    /** EventSource registration happens as the result of assiging the value to property uri. */
    _uriChanged: function (newUri, oldUri) {
        //Unsubscribe from old sse evnts.
        if (oldUri) {
            this._unsubscribeFromSseEvents();
            this._source = null;
        }

        // if uri value is missing then skip EvenSource registration
        if (newUri) {
            this._source = registerEventSource(newUri);
            this._subscribeToSseEvent();
        }

    },

    _subscribeToSseEvent: function () {
        if (this._messageSubscription) {
            throw new Error("Unsubscribe from sse message event first in order to subscribe to the new one");
        }

        if (this._errorSubscription) {
            throw new Error("Unsubscribe from sse error event first in order to subscribe to the new one");
        }

        this._messageSubscription = postal.subscribe({
            channel: "sse-event",
            topic: `${this.uri}${this.eventSourceClass ? "/" + this.eventSourceClass : ""}/message`,
            callback: (data, envelope) => {
                const msg = data.msg;
                if (this.useTimerBasedScheduling) {
                    // Ensures that if there is a pending data handling request then new requests are ignored
                    if (this._timerIdForDataHandling) {
                        return;
                    }
                    const minDelay = msg.minDelay ? msg.minDelay : 0;
                    const rndDelay = msg.delay ? random(msg.delay) + 1 : 1;
                    console.log("min delay: ", minDelay, " rnd delay: ", rndDelay);
                    this._execDataHandlerWithDelay(msg, minDelay + rndDelay);
                } else {
                    this.dataHandler(msg);
                }
            }
        });

        this._errorSubscription = postal.subscribe({
            channel: "sse-event",
            topic: this.uri + "/error",
            callback: (data, envelope) => {
                // invoke error handler if provided
                if (this.errorHandler) {
                    this.errorHandler(data.event, data.source);
                }
            }
        });
    },

    _unsubscribeFromSseEvents: function () {
        this._messageSubscription && this._messageSubscription.unsubscribe();
        this._errorSubscription && this._errorSubscription.unsubscribe();
        delete this._messageSubscription;
        delete this._errorSubscription;
    },

    /** Executes dataHandler with delay. */
    _execDataHandlerWithDelay: function (msg, delay) {
        const self = this;
        self._timerIdForDataHandling = setTimeout(() => {
            try {
                self.dataHandler(msg);
            } finally {
                self._timerIdForDataHandling = null;
            }
        }, delay);
    }

}; // end of behaviour declaration
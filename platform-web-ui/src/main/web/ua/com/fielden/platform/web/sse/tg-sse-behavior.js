import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { random } from '/resources/reflection/tg-numeric-utils.js';

export const TgSseBehavior = {

    properties: {
        /* A URI that should be connected to for listening to push events. */
        uri: {
            type: String,
            observer: '_registerEventSource'
        },

        /** Let's be defensive and try to prevent repeated initialisations of EventSource by
         * assigning this property value true upon the very first initialisation and then validating against it. */
        _initialised: {
            type: Boolean,
            value: false
        },

        /* A function that takes a single String arugment representing the push event data. */
        dataHandler: {
            type: Function
        },

        /* An optional function to react to SSE errors. Accepts error event as an argument. */
        errorHandler: {
            type: Function
        },

        /* Can be used to influence the default behaviour to reconnect when an error occurs. */
        shouldReconnectWhenError: {
            type: Boolean,
            value: true
        },

        /* Time in milliseconds when the next try for SSE reconnection will be performed after SSE error. Works only if shouldReconnectWhenError is true. */
        errorReconnectionDelay: {
            type: Number,
            value: 15000
        },

        /* A reference to an established EventSource object. */
        _source: {
            type: Object,
            value: null
        },

        /* A timer ID to delay reactions to SSE events in case a delay was requested. */
        _timerIdForDataHandling: {
            type: Number,
            value: null
        },

        /* A timer ID for attempts to reestablish the connection with the server. */
        _timerIdForReconnection: {
            type: Number,
            value: null
        },

        /* Indicates the need to schedule dataHandler execution based on 'minDelay' and 'delay' properties of event data. */
        useTimerBasedScheduling: {
            type: Boolean,
            value: true
        }

    },

    /* Closes an existing event source connection. This operation also ensures that any scheduled reconnections are cancelled and cleared after EventSource closing. */
    closeEventSource: function () {
        if (this._source) {
            this._initialised = false;
            const src = this._source;
            this._source = null;
            src.close();
        }
        if (typeof this._timerIdForReconnection === 'number') { // if there is in-progress timeout handling reconnection
            clearTimeout(this._timerIdForReconnection); // then cancel this timeout immediately
            this._timerIdForReconnection = null;
        }
    },

    /** EventSource registration happens as the result of assiging the value to property uri. */
    _registerEventSource: function (newUri, oldUri) {
        const self = this;
        if (self._initialised === true) {
            throw new Error('EventSource for URI [' + oldUri + '] should not be initialised to [' + newUri + '] without closing an existing event source connection.');
        }
        // if uri value is missing then skip EvenSource registration
        if (self.uri) {
            self._initialised = true;
            console.log('Determine if EventSrouce needs polyfilling: ', window.EventSource)
            if (window.EventSource == undefined) {
                console.warn('EventSrouce polyfilling is in progress.');
                const esPoly = document.createElement('script');
                esPoly.src = '/resources/polyfills/eventsource.min.js';
                esPoly.onload = self._registerEventSourceHandlers.bind(self);
                document.head.appendChild(esPoly);
            } else {
                self._registerEventSourceHandlers();
            }
        }
    },

    _registerEventSourceHandlers: function () {
        const self = this;
        console.log("Registering EventSrouce handlers for URI ", self.uri);

        self._source = new EventSource(self.uri);
        const source = self._source;

        source.addEventListener('message', function (e) {
            if (self.useTimerBasedScheduling) {
                // Ensures that if there is a pending data handling request then new requests are ignored
                if (self._timerIdForDataHandling) {
                    return;
                }
                const msg = JSON.parse(e.data);
                const minDelay = msg.minDelay ? msg.minDelay : 0;
                const rndDelay = msg.delay ? random(msg.delay) + 1 : 1;
                console.log("min delay: ", minDelay, " rnd delay: ", rndDelay);
                self._execDataHandlerWithDelay(msg, minDelay + rndDelay);
            } else {
                self.dataHandler(JSON.parse(e.data));
            }
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

            // invoke error handler if provided
            if (self.errorHandler) {
                self.errorHandler(e);
            }

            // only after custom error handling should we attempt to reconnect
            // this is because the decision to reconnect can be made in the custom error handler
            if (self.shouldReconnectWhenError === true && e.eventPhase === EventSource.CLOSED) {
                // connection was closed by the server;
                // ensure client-side EventSource to be closed and _initialised flag to be false;
                // ensure also that previous reconnection timeout is cancelled and made null;
                // previous reconnection timeout should not be possible here because SSE EventSource (and server side resource) will be closed prior to this;
                // however it is not harmful to check -- see closeEventSource() method
                self.closeEventSource();
                // Let's kick a timer for reconnection...
                self._timerIdForReconnection = setTimeout(() => {
                    try {
                        self._registerEventSourceHandlers();
                    } finally {
                        self._timerIdForReconnection = null;
                    }
                }, self.errorReconnectionDelay);
            }

        }, false);
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
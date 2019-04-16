import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

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
        }
    },

    /* Closes an existing event source connection. */
    closeEventSource: function () {
        if (this._source) {
            this._initialised = false;
            const src = this._source;
            this._source = null;
            src.close();
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
            // Ensures that if there is a pending data handling request then new requests are ignored
            if (self._timerIdForDataHandling) {
                return;
            }
            const msg = JSON.parse(e.data);
            const minDelay = msg.minDelay ? msg.minDelay : 0;
            const rndDelay = msg.delay ? random(msg.delay) + 1 : 1;
            console.log("min delay: ", minDelay, " rnd delay: ", rndDelay);
            self._execDataHandlerWithDelay(msg, minDelay + rndDelay);
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
            // TODO Might need to implement some error recovery strategy to reconnect to the server-side observable
            console.log('an error occurred: ', e);

            if (e.eventPhase === EventSource.CLOSED) {
                // Connection was closed by the server
                // Let's kick a timer for reconnection.
                self.closeEventSource();
                _timerIdForReconnection = setTimeout(() => {
                    try {
                        self._registerEventSourceHandlers();
                    } finally {
                        self._timerIdForReconnection = null;
                    }
                }, 15000);
            }

            // invoke error handler if provided
            if (self.errorHandler) {
                self.errorHandler(e);
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
    },

}; // end of behaviour declaration
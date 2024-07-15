import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/components/postal-lib.js';

import { random } from '/resources/reflection/tg-numeric-utils.js';

export const TgSseBehavior = {

    properties: {
        
        /**
         * Event source class that is used to identify what process is sending the server event.
         */
        eventSourceClass: {
            type: String
        },

        /**
         * The identifier of sse job that uses the same eventSourceClass (for example file upload process).
         */
        jobUid: {
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

    observers: ['_sseAttributesChanged(eventSourceClass, jobUid)'],

    _sseAttributesChanged: function (eventSourceClass, jobUid) {
        this.unsubscribeFromSseEvents();
        if (eventSourceClass) {
            this.subscribeToSseEvent();
        }
    },

    attached: function () {
        if (this.eventSourceClass && !this._messageSubscription && !this._errorSubscription) {
            this.subscribeToSseEvent();
        }
    },

    detached: function () {
        this.unsubscribeFromSseEvents();
    },

    subscribeToSseEvent: function () {
        if (this._messageSubscription) {
            throw new Error("Unsubscribe from SSE message event first in order to subscribe to the new one.");
        }

        if (this._errorSubscription) {
            throw new Error("Unsubscribe from SSE error event first in order to subscribe to the new one.");
        }

        this._messageSubscription = postal.subscribe({
            channel: "sse-event",
            topic: `${this.eventSourceClass ? "/" + this.eventSourceClass : ""}${this.jobUid ? "/" + this.jobUid : ""}/message`,
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
            topic: "error",
            callback: (data, envelope) => {
                // invoke error handler if provided
                if (this.errorHandler) {
                    this.errorHandler(data.event, data.source);
                }
            }
        });
    },

    unsubscribeFromSseEvents: function () {
        if (this._messageSubscription) {
            this._messageSubscription.unsubscribe();
            delete this._messageSubscription;
        }
        if (this._errorSubscription) {
            this._errorSubscription.unsubscribe();
            delete this._errorSubscription;
        }
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
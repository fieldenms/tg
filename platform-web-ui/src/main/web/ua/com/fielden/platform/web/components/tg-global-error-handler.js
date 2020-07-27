import {PolymerElement, html} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import { _timeZoneHeader } from '/resources/reflection/tg-date-utils.js';
import { containsRestictedTags } from '/resources/reflection/tg-polymer-utils.js';

import '/resources/polymer/@polymer/iron-ajax/iron-ajax.js';

export class UnreportableError extends Error {
    constructor(...params) {
        super(params);

        if (Error.captureStackTrace) {
            Error.captureStackTrace(this, UnreportableError);
        }

        this.name = "UnreportableError";
    }
}

/**
 * Augments non-empty catched 'error' state restoration logic with 'restoreStateFunction', that is applicable in current context.
 */
export function enhanceStateRestoration (error, restoreStateFunction) {
    const preRestoreState = error.restoreState;
    error.restoreState = function () {
        preRestoreState && preRestoreState();
        restoreStateFunction();
    };
    return error;
};

/**
 * Replaces newline symbol with \r\n sequence or <br> tag based on whether specified input contains restricted tags or not.
 * This is done in order to format input for tg-toast (see tg-toast.js: _showMessageDlg method). 
 */
function replaceNewline (input) {
    const newline = "\r\n";
    return input.replace(/\n/gi, containsRestictedTags(input) ? newline : '<br>');
}

const template = html`
    <iron-ajax id="errorSender" headers="[[_headers]]" url="/error" method="PUT" content-type="text/plain" handle-as="text" on-response="_processResponse"></iron-ajax>`;

class TgGlobalErrorHandler extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties () {
        return {
            

            //The maximal length of error queue. If more errors happens then they will be handled with alternative handler
            maxErrorQueueLength: {
                type: Number,
                value: 10
            },

            toaster: Object,

            //Queue of errors to log on server.
            _errorQueue: Array,

            /**
             * Additional headers for 'iron-ajax' client-side requests. These only contain 
             * our custom 'Time-Zone' header that indicates real time-zone for the client application.
             * The time-zone then is to be assigned to threadlocal 'IDates.timeZone' to be able
             * to compute 'Now' moment properly.
             */
            _headers: {
                type: String,
                value: _timeZoneHeader
            },
        };
    }

    ready () {
        super.ready();

        //Configuring error handler related properties.
        this._errorQueue = [];
        this._handleError = this._handleError.bind(this);
        this._handleUnhandledPromiseError = this._handleUnhandledPromiseError.bind(this);

        //Add error handling errors
        window.addEventListener('error', this._handleError);
        window.addEventListener('rejectionhandled', this._handleUnhandledPromiseError);
        window.addEventListener('unhandledrejection', this._handleUnhandledPromiseError);
    }

    errorHandler (errorMsg) {
        this.$.errorSender.body = errorMsg;
        this.$.errorSender.generateRequest();
    }
    alternativeErrorHandler (errorMsg) {
        console.error(errorMsg);
    }

    _handleUnhandledPromiseError (e) {
        const errorMsg = e.reason.message + "\n" + e.reason.stack;
        this._acceptError(e.composedPath()[0], e.reason, errorMsg);
    }

    _handleError (e) {
        const errorDetail = e.detail ? e.detail : e;
        const errorMsg = errorDetail.message + " Error happened in: " + errorDetail.filename + " at Ln: " + errorDetail.lineno + ", Co: " + errorDetail.colno
                        + "\n" + ((errorDetail.error && errorDetail.error.stack) ?  errorDetail.error.stack : JSON.stringify(errorDetail.error));
        this._acceptError(e.composedPath()[0], errorDetail, errorMsg);
    }

    /**
     * Receives error or error events and then analyses whether they are reportable or have restoreState
     * to perform appropriate tasks with them. Also sends error to the server if possible otherwise adds error to
     * the queue for further processing. Shows toast to the user for errors of type other than UnreportableError.
     * 
     * @param {*} from 
     * @param {*} e -- event of type ErrorEvent (syntax error, throwing of Error etc.) or promise rejection error
     * @param {*} errorMsg 
     */
    _acceptError (from, e, errorMsg) {
        if (from !== this.$.errorSender) {
            const error = e.error || e;
            if (error.restoreState) {
                error.restoreState();
            }
            if ( !(error instanceof UnreportableError)) {
                this.toaster.openToastForError("Unexpected error occurred.", replaceNewline(errorMsg), true);
            }
            if (this._errorQueue.length >= this.maxErrorQueueLength) {
                this.alternativeErrorHandler(errorMsg);
            } else {
                this._errorQueue.push(errorMsg);
            }
            if (!this.$.errorSender.loading) {
                this.errorHandler(this._errorQueue[0]);
            }
        }
    }
    
    _processResponse (e) {
        this._errorQueue.shift();
        if (this._errorQueue.length > 0) {
            this.errorHandler(this._errorQueue[0]);
        }
    }
}

customElements.define('tg-global-error-handler', TgGlobalErrorHandler);
import {PolymerElement, html} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import { _timeZoneHeader } from '/resources/reflection/tg-date-utils.js';
import { containsRestrictedTags } from '/resources/reflection/tg-polymer-utils.js';

import '/resources/polymer/@polymer/iron-ajax/iron-ajax.js';

/**
 * Throw or reject with this type of error if you don't want to report this error to the user but want to report it to the server.
 */
export class UnreportableError extends Error {
    constructor(...params) {
        super(params);

        if (Error.captureStackTrace) {
            Error.captureStackTrace(this, UnreportableError);
        }
        this.name = this.constructor.name;
    }
}

/**
 * Throw or reject with this type of error if you don't want to report this error neither to the user nor to the server.
 */
export class ExpectedError extends Error {
    constructor(...params) {
        super(params);

        if (Error.captureStackTrace) {
            Error.captureStackTrace(this, ExpectedError);
        }
        this.name = this.constructor.name;
    }
}

/**
 * Augments a non-empty state restoration logic for caught errors with 'restoreStateFunction', that is applicable for the current context.
 * @param {Object} error -- an error to be processed
 * @param {Function} restoreStateFunction -- context-specific restoration function
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
    return input.replace(/\n/gi, containsRestrictedTags(input) ? newline : '<br>');
}

const template = html`
    <iron-ajax id="errorSender" headers="[[_headers]]" url="/error" method="PUT" content-type="text/plain" handle-as="text" reject-with-request on-response="_processResponse"></iron-ajax>`;

/**
 * A web component used as a global error handler for unhandled exceptions.
 * Should be used only once in the application, preferably in the main scaffolding file.
 */
class TgGlobalErrorHandler extends PolymerElement {

    static get template() { 
        return template;
    }

    static get properties () {
        return {
            /**
             * @property {Number}
             * The maximum size of the error queue. All errors that do not fit into the queue are processed by the alternative handler.
             */
            maxErrorQueueLength: {
                type: Number,
                value: 10
            },

            /**
             * @property {Object}
             * An instance of the toaster component, used for reporting errors to users.
             */
            toaster: Object,

            /**
             * @property {Array}
             * Queue of errors to be sent to the server for processing.
             * /
            _errorQueue: Array,

            /**
             * @property {String}
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
        window.addEventListener('unhandledrejection', this._handleUnhandledPromiseError);
    }

    /**
     * Sends the error message to the server. 
     * @param {String} errorMsg -- the value of an error message.
     */
    errorHandler (errorMsg) {
        this.$.errorSender.body = errorMsg;
        this.$.errorSender.generateRequest();
    }

    /**
     * Errors that do not fit into the error queue are processed by this method.
     * @param {String} errorMsg -- the value of an error message.
     */
    alternativeErrorHandler (errorMsg) {
        console.error(errorMsg);
    }

    _handleUnhandledPromiseError (e) {
        const error = e.reason.error || e.reason;
        const errorMsg = error.message + "\n" + error.stack;
        if (!e.reason.request || "IRON-REQUEST" !== e.reason.request.tagName) {
            this._acceptError(e.composedPath()[0], error, errorMsg);
        }
    }

    _handleError (e) {
        const errorDetail = e.detail || e;
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
            if (!(error instanceof ExpectedError)) {
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
    }
    
    _processResponse (e) {
        this._errorQueue.shift();
        if (this._errorQueue.length > 0) {
            this.errorHandler(this._errorQueue[0]);
        }
    }
}

customElements.define('tg-global-error-handler', TgGlobalErrorHandler);
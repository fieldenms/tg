import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js';
import { UnreportableError } from '/resources/components/tg-global-error-handler.js';

/**
 * Processes an error event fired by an iron request.
 *
 * @param {Event} e - the error event.
 * @param {TgReflector} reflector
 * @param {TgSerialiser} serialiser
 * @param {Function} [customHandler] - a function that will be called with the processed error (a {Result} object or a {String} message).
 * @param {Object} [toaster] - an object with functions to display a toast.
 */
export function processResponseErrorEvent (e, reflector, serialiser, customHandler, toaster) {
    tearDownEvent(e);
    processResponseError(e.detail.request, e.error, reflector, serialiser, customHandler, toaster);
}

/**
 * Processes an error that resulted from an iron request.
 *
 * @param {IronRequestElement} request
 * @param {Error} error
 * @param {TgReflector} reflector
 * @param {TgSerialiser} serialiser
 * @param {Function} [customHandler] - a function that will be called with the processed error (a {Result} object or a {String} message).
 * @param {Object} [toaster] - an object with functions to display a toast.
 */
export function processResponseError (request, error, reflector, serialiser, customHandler, toaster) {
    console.log('PROCESS ERROR', error);
    const xhr = request.xhr;
    if (xhr.status === 500) { // internal server error, which could either be due to business rules or have some other cause due to a bug or db connectivity issue
        const deserialisedResult = serialiser.deserialise(xhr.response);

        if (reflector.isError(deserialisedResult)) {
            // throw the toast message about the server-side error
            toaster && toaster.openToastForErrorResult(deserialisedResult, true);
            // continue with custom error handling of the error result
            customHandler && customHandler(deserialisedResult);
        } else {
            const error = new UnreportableError('A response with error code 500 was received, but no error cause was provided.');
            customHandler && customHandler(error.message);
            toaster && toaster.openToastForError('Server responded with error.', error.message, true);
            throw error;
        }
    } else if (xhr.status === 403) { // forbidden!
        // TODO should prompt for login in place...
        toaster && toaster.openToastForError('Access denied.', 'The current session has expired. Please login and try again.', true);
        customHandler && customHandler('Access denied');
    } else if (xhr.status === 503) { // service unavailable
        toaster && toaster.openToastForError('Service Unavailable.', 'Server responded with error 503 (Service Unavailable).', true);
        customHandler && customHandler('Service Unavailable');
    } else if (xhr.status >= 400) { // other client or server error codes
        toaster && toaster.openToastForError('Service Error (' + xhr.status + ').', 'Server responded with error code ' + xhr.status, true);
        customHandler && customHandler('Service Error (' + xhr.status + ').');
    } else {
        // this situation may occur if the server was accessible, but the return status code does not match any of the expected ones, or
        // the server should not be reached, for example, due to a network failure, or
        // the request was aborted -- aborted requests should not report any errors to users
        console.warn('Server responded with error code ', xhr.status);
        if (!request.aborted) {
            const [msgHeader, msgBody] = xhr.status === 0 // if status is 0 then it is most likely a network failure
                                         ? ['Could not process the request.', 'Please make sure your device is connected to the network.']
                                         : ['Unexpected error occurred.', `Error code [${xhr.status}]. Please contact support.`];
            const error = new UnreportableError(`${msgHeader} ${msgBody}`);
            customHandler && customHandler(error.message);
            toaster && toaster.openToastForError(`${msgHeader}`, error.message, true);
            throw error;
        }
    }
}

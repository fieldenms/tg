export function processResponseError (e, reflector, serialiser, customHandler, toaster) {
    console.log('PROCESS ERROR', e.error);
        const xhr = e.detail.request.xhr;
        if (xhr.status === 500) { // internal server error, which could either be due to business rules or have some other cause due to a bug or db connectivity issue
            const deserialisedResult = serialiser.deserialise(xhr.response);

            if (reflector.isError(deserialisedResult)) {
                // throw the toast message about the server-side error
                toaster && toaster.openToastForError(reflector.exceptionMessage(deserialisedResult.ex), toastMsgForError(reflector, deserialisedResult), true);
                // continue with custom error handling of the error result
                customHandler && customHandler(deserialisedResult);
            } else {
                //throw new Error('Responses with status code 500 suppose to carry an error cause!');
                customHandler && customHandler('Responses with status code 500 suppose to carry an error cause!');
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
        } else { // for other codes just log the code
            console.warn('Server responded with error code ', xhr.status);
        }
}

export function toastMsgForError (reflector, errorResult) {
    return reflector.stackTrace(errorResult.ex);
}
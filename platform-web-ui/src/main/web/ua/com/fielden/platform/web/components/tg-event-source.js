/**
 * Timer identifier used to schedule SSE reconnection after an error.
 */
let timerIdForReconnection = null;

/**
 *  Registers a new single event source for this client.
 */
const registerEventSource = function () {
    const uid = crypto.randomUUID();
    const source = {initialised: false, sseUid: uid, uri: `/sse/${uid}`, shouldReconnectWhenError: true, errorReconnectionDelay: 15000};
    console.log('Determine if EventSource needs polyfilling: ', window.EventSource)
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

        // Publish event for error handling.
        postal.publish({
            channel: "sse-event",
            topic: "error",
            data: {
                event: e,
                source: sourceObj
            }
        });

        // Attempt reconnection only after custom error handling has completed,
        // so that the handler can decide whether reconnection should occur.
        if (sourceObj.shouldReconnectWhenError === true && e.eventPhase === EventSource.CLOSED) {
            // Connection was closed by the server.
            // Ensure the client-side EventSource is closed and its "initialised" flag is reset.
            // Also cancel any pending reconnection timeout, if present.
            // In practice a previous reconnection timeout should not exist here because the
            // EventSource (and its server-side resource) is closed before this point, but the extra check is prudent and harmless (see closeEventSource()).
            closeEventSource(sourceObj);

            // Schedule a reconnection attempt after the configured delay.
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

/*
 * Closes an existing EventSource connection.
 * Also cancels any scheduled reconnection attempts and clears their timer once the EventSource is closed.
 */
const closeEventSource = function (sourceObj) {
    if (sourceObj.source) {
        sourceObj.initialised = false;
        const src = sourceObj.source;
        sourceObj.source = null;
        src.close();
    }
    if (typeof timerIdForReconnection === 'number') { // if there is an in-progress timeout handling reconnection
        clearTimeout(timerIdForReconnection); // then cancel this timeout immediately
        timerIdForReconnection = null;
    }
};

// Holds all registered event sources in the application.
export const eventSource = registerEventSource();
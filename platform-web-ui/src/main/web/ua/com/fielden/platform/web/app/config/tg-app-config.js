import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';

import {generateUUID} from '/resources/reflection/tg-polymer-utils.js';

import '/resources/components/postal-lib.js';
import '/resources/components/moment-lib.js';

@independentTimeZoneSetting

moment.locale('custom-locale', {
    longDateFormat: {
        LTS: @timeWithMillisFormat,
        LT: @timeFormat,
        L: @dateFormat
    }
});

/**
 * Timer Id to reconnect via sse after an error.
 */
let timerIdForReconnection = null;

/**
 * Holds registered event sources in application.
 */
export let eventSource = {initialised: false, shouldReconnectWhenError: true, errorReconnectionDelay: 15000};

/**
 * Sends postal sse event for listeners.
 * 
 * @param {String} data - The JSON String that represents the sse event message data.  
 */
const publishData = function (data) {

    const parsedData = JSON.parse(data);

        postal.publish({
            channel: "sse-event",
            topic: `${parsedData.eventSourceClass ? "/" + parsedData.eventSourceClass : ""}${parsedData.jobUid ? "/" + parsedData.jobUid : ""}/message`,
            data: {
                msg: parsedData
            }
        });
}

/**
 *  Registers new single event source for this client.
 */
const registerRemoteEventSource = function (userName, uid) {
    console.log('Determine if EventSrouce needs polyfilling: ', window.EventSource)
    if (window.EventSource == undefined) {
        console.warn('EventSource polyfilling is in progress.');
        const esPoly = document.createElement('script');
        esPoly.src = '/resources/polyfills/eventsource.min.js';
        esPoly.onload = () => registerEventSourceHandlers(userName, uid);
        document.head.appendChild(esPoly);
    } else {
        registerEventSourceHandlers(userName, uid);
    }
};

const registerEventSourceHandlers = function (userName, uid) {
    console.log("Registering EventSource handlers for URI ", eventSource.uri);

    const source = new EventSource(eventSource.uri);
    eventSource.source = source;
    eventSource.initialised = true;

    //Local storage event listener for isAlive event from locally registered sse. That is needed to make sure tthat there is at least on tab or window with the remotly registered sse
    const aliveListener = function (event) {
        if (event.storageArea != localStorage) {
            return;
        }

        if (event.key === isAliveKey(userName, uid)) {
            const newVal = localStorage.getItem(aliveKey(userName, uid)) == 'true' ? 'false' :  'true';
            localStorage.setItem(aliveKey(userName, uid), newVal);
        }
    };

    window.addEventListener('storage', aliveListener);

    source.addEventListener('message', function (e) {
        publishData(e.data);
        localStorage.removeItem(messageKey(userName, uid));
        localStorage.setItem(messageKey(userName, uid) , e.data);
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
                source: eventSource
            }
        });

        // only after custom error handling should we attempt to reconnect
        // this is because the decision to reconnect can be made in the custom error handler
        if (eventSource.shouldReconnectWhenError === true && e.eventPhase === EventSource.CLOSED) {

            // connection was closed by the server;
            // ensure client-side EventSource to be closed and initialised flag to be false;
            // ensure also that previous reconnection timeout is cancelled and made null;
            // previous reconnection timeout should not be possible here because SSE EventSource (and server side resource) will be closed prior to this;
            // however it is not harmful to check -- see closeEventSource() method
            closeEventSource();
            window.removeEventListener("storage", aliveListener);
            
            // Let's kick a timer for reconnection...
            timerIdForReconnection = setTimeout(() => {
                try {
                    registerLocalEventSource(userName, uid);
                } finally {
                    timerIdForReconnection = null;
                }
            }, eventSource.errorReconnectionDelay);

        }

    }, false);
};

/* Closes an existing event source connection. This operation also ensures that any scheduled reconnections are cancelled and cleared after EventSource closing. */
const closeEventSource = function () {
    if (eventSource.source) {
        eventSource.initialised = false;
        const src = eventSource.source;
        eventSource.source = null;
        src.close();
    }
    if (typeof timerIdForReconnection === 'number') { // if there is in-progress timeout handling reconnection
        clearTimeout(timerIdForReconnection); // then cancel this timeout immediately
        timerIdForReconnection = null;
    }
};

/**
 * Registers a sort of SSE listener but for local storage.
 */
const registerLocalEventSource = function (userName, uid) {

    let heartBeatTimer = null;
    
    //Will be invoked when it turns out that theres is no alive server source event.
    //In that case it will unregister local storage events and will try to register server source event (remote SSE).
    const heartBeatFailed = function () {
        window.removeEventListener("storage", heartBeatListener);
        window.removeEventListener("storage", dataListener);
        //Oh no! locally registered SSE failed to hear a hart beat from remote sse. Then deinitialise this sse and register remote one. 
        eventSource.initialised = false;
        registerRemoteEventSource(userName, uid);
    };

    //Will be invoked to check whether server source event (remote sse) is alive or not.
    const isAlive = function() {
        //First create a timer that waits for 2 seconds for response from remote sse (it might be less time but 10 seconds is more then enough)
        heartBeatTimer = setTimeout(heartBeatFailed, 2000);
        //Set the locals storage value to fire storage event that is listened by remote sse. If remote sse is alive it will respond immediately (that's why timer can be less than 10s) 
        const newVal = localStorage.getItem(isAliveKey(userName, uid)) == 'true' ? 'false' :  'true';
        localStorage.setItem(isAliveKey(userName, uid), newVal);
        
    };
    
    //Local storage event listner, that will be invoked when remote sse responds which means that it is still alive.
    //After this listner get invoked new timeout to check remote sse heart beat will be created with 10s (it can be less. It is option to play with)
    const heartBeatListener = function (event) {
        if (event.storageArea != localStorage) {
            return;
        }
        if (event.key === aliveKey(userName, uid)) {
            //If remote SSE is alive then this locally registered sse should be initialised
            eventSource.initialised = true;
            clearTimeout(heartBeatTimer);
            heartBeatTimer = setTimeout(isAlive, 2000);// Time before the next heart beat
        }
    }

    //Local storage event listener for remote sse message events.
    const dataListener = function (event) {
        if (event.storageArea != localStorage) {
            return;
        }

        if (event.key === messageKey(userName, uid) && event.newValue) {
            publishData(event.newValue);
        }
    };

    //Register needed local storage event listeners.
    window.addEventListener('storage', heartBeatListener);
    window.addEventListener('storage', dataListener);

    //Run hart beat
    isAlive();
};

const uir = function (uid) {
    return `/sse/${uid}`;
}

const isAliveKey = function (userName, uid) {
    return `${userName}_${uid}_isAlive`;
}

const aliveKey = function (userName, uid) {
    return `${userName}_${uid}_alive`;
}

const messageKey = function (userName, uid) {
    return `${userName}_${uid}_sseMessage`;
}

const connectToDb = function () {
    return new Promise(function(resolve, reject) {
        const openRequest = indexedDB.open("tg", 1);

        openRequest.onupgradeneeded = function(event) {
            const db = event.target.result;

            if (!db.objectStoreNames.contains('uids')) {
                db.createObjectStore("uids", { keyPath: "userName" });
            }

        };
          
        openRequest.onerror = function(evnet) {
            console.error("Error", openRequest.error);
            reject("Could not load db");
        };
          
        openRequest.onsuccess = function(event) {
            resolve(event.target.result);
        };
    });
}

const saveUuid = function (userName, uid, db) {
    return new Promise (function (resolve, reject) {
        const saveRequest = db.transaction("uids", "readwrite")
                              .objectStore("uids")
                              .add({userName: userName, uid: uid});
        saveRequest.onsuccess = (event) => {
            resolve(uid);
        };
        saveRequest.onerror = (event) => {
            reject(`Failed to save uuid: ${uid} for user: ${userName}`);
        }
    });
}

const getUid = function (userName, db) {
    return new Promise (function (resolve, reject) {
        const getRequest = db.transaction("uids", "readonly")
                              .objectStore("uids")
                              .get(userName);
        getRequest.onsuccess = (event) => {
            resolve(event.target.result.uid);
        };
        getRequest.onerror = (event) => {
            reject(`Failed to get request for username: ${userName}`);
        }
    });
}

const initEventSource = function (uid) {
    eventSource.sseUid = uid;
    eventSource.uri = uir(uid);
}

/**
 * Registers sse or listens to the local storage event for sse events from other tabs or windows opened for the same user
 * 
 * @param {String} userName - the user name that loads this application. 
 */
export async function establishSSE (userName) {
    //0. If event source was already initialised then return it otherwise initialise it localy
    if(eventSource.initialised) {
        return eventSource;
    }
    //1. Connect to tg indexDb.
    const db = await connectToDb();
    //2. Try to save generated id into tg index db if it is successful then register remote sse otherwise create local one.
    await saveUuid(userName, generateUUID(), db).then(uid => {
        initEventSource(uid);
        registerRemoteEventSource(userName, uid);
    }).catch (async (e) => {
        await getUid(userName, db).then(uid => {
            initEventSource(uid);
            registerLocalEventSource(userName, uid);
        }).catch (e => {
            throw new Error(`Failed to get uid for user: ${userName}`);
        });
    });

    return eventSource;
}

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
        firstDayOfWeek: {
            type: Number,
            notify: true,
            readOnly: true,
            value: window.firstDayOfWeek
        }
    },
    
    attached: function() {
        this.style.display = "none";
    }
    
});
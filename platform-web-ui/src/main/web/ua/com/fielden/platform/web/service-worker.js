console.debug('Service worker script...started');

const cacheName = 'tg-air-dev-cache';

const isStatic = function (url) {
    const pathname = new URL(url).pathname;
    // console.debug(`isStatic [${url}] pathname [${pathname}]`);
    return pathname.startsWith('/resources/')
        || pathname.startsWith('/app/')
        /*pathname === '/resources/startup-resources-vulcanized.js'
        || pathname === '/resources/polymer/@webcomponents/webcomponentsjs/webcomponents-bundle.js'
        || pathname === '/resources/polymer/web-animations-js/web-animations-next-lite.min.js'
        || pathname === '/resources/lodash/4.17.11/lodash.min.js'
        || pathname === '/resources/postal/2.0.5/postal.min.js'
        || pathname === '/resources/filesaver/FileSaver.min.js'
        || pathname === '/resources/manifest.webmanifest'
        || pathname === '/resources/icons/tg-icon192x192.png'
        || pathname === '/resources/icons/tg-icon144x144.png'*/
        // TODO caching here does not work, strangely; this will be example to make loading successful in complete offline mode; || pathname === '/entity/ua.com.fielden.platform.menu.Menu/new'
        || pathname === '/';
};

const staleResponse = function () {
    return new Response('STALE', {status: 412, statusText: 'BAD', headers: {'Content-Type': 'text/plain'}});
};

const cacheIfSuccessful = async function (fetchedResponse, url) {
    if (fetchedResponse && fetchedResponse.status === 200 && fetchedResponse.type === 'basic') { // cache response if it is successful
        // IMPORTANT: Clone the response. A response is a stream
        // and because we want the browser to consume the response
        // as well as the cache consuming the response, we need
        // to clone it so we have two streams.
        const fetchedResponseToCache = fetchedResponse.clone();
        const cache = await caches.open(cacheName);
        cache.put(url, fetchedResponseToCache);
    }
};

self.addEventListener('fetch', function (event) {
    const request = event.request;
    const url = request.url;
    if (isStatic(url)) {
        event.respondWith(async function() {
            const cachedResponse = await caches.match(url);
            if (cachedResponse) { // cached entry exists
                const cachedChecksum = '456def'; // TODO fetch it from cache
                const serverChecksum = '456def'; // TODO fetch it from server
                if (!serverChecksum) { // resource has been deleted on server
                    const cache = await caches.open(cacheName);
                    const deleted = await cache.delete(url);
                    if (!deleted) {
                        console.warn(`Cached resource [${url}] was not deleted.`);
                    }
                    return staleResponse();
                } else if (serverChecksum !== cachedChecksum) { // resource has been modified on server
                    const fetchedResponse = await fetch(url);
                    cacheIfSuccessful(fetchedResponse, url);
                    return fetchedResponse;
                } else { // serverChecksum === cachedChecksum; resource is the same on server and in client cache
                    return cachedResponse;
                }
            } else { // there is no cached entry
                const serverChecksum = '456def'; // TODO fetch it from server
                if (!serverChecksum) { // resource has been deleted on server
                    return staleResponse();
                } else { // resource exists on server
                    const fetchedResponse = await fetch(url);
                    cacheIfSuccessful(fetchedResponse, url);
                    return fetchedResponse;
                }
            }
        }());
    } // all non-static resources should be bypassed by service worker, just ignoring them in 'fetch' event; this will trigger default logic
});

console.debug('Service worker script...completed');
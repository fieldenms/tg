console.debug('Service worker script...started');

const cacheName = 'tg-air-dev-cache';

const isStatic = function (url) {
    const pathname = new URL(url).pathname;
    // console.debug(`isStatic [${url}] pathname [${pathname}]`);
    return pathname === '/' ||
        pathname.startsWith('/resources/') ||
        pathname.startsWith('/app/'); // 
        // pathname === '/resources/startup-resources-vulcanized.js' ||
        // pathname === '/resources/polymer/@webcomponents/webcomponentsjs/webcomponents-bundle.js' ||
        // pathname === '/resources/polymer/web-animations-js/web-animations-next-lite.min.js' ||
        // pathname === '/resources/lodash/4.17.11/lodash.min.js' ||
        // pathname === '/resources/postal/2.0.5/postal.min.js' ||
        // pathname === '/resources/filesaver/FileSaver.min.js' ||
        // pathname === '/resources/manifest.webmanifest' ||
        // pathname === '/resources/icons/tg-icon192x192.png' ||
        // pathname === '/resources/icons/tg-icon144x144.png';
        // TODO caching here does not work, strangely; this will be example to make loading successful in complete offline mode; || pathname === '/entity/ua.com.fielden.platform.menu.Menu/new'
};

const staleResponse = function () {
    return new Response('STALE', {status: 412, statusText: 'BAD', headers: {'Content-Type': 'text/plain'}});
};

const isResponseSuccessful = function (response) {
    return response && response.status === 200 && response.type === 'basic';
};

const cacheIfSuccessful = async function (response, checksumRequest, checksumResponse, url, cache) {
    if (isResponseSuccessful(response)) { // cache response if it is successful
        // IMPORTANT: Clone the response. A response is a stream
        // and because we want the browser to consume the response
        // as well as the cache consuming the response, we need
        // to clone it so we have two streams.
        const responseToCache = response.clone();
        await cache.put(url, responseToCache);
        await cache.put(checksumRequest, checksumResponse);
    }
};

self.addEventListener('fetch', function (event) {
    const request = event.request;
    const url = request.url;
    if (isStatic(url)) {
        event.respondWith(async function() {
            const cache = await caches.open(cacheName);
            const cachedResponseAndChecksum = await cache.matchAll(url, { ignoreMethod: true });
            const serverChecksumRequest = new Request(url, { method: 'HEAD' });
            const serverChecksumResponse = await fetch(serverChecksumRequest);
            const serverChecksumResponseToCache = serverChecksumResponse.clone();
            const serverChecksum = isResponseSuccessful(serverChecksumResponse) && serverChecksumResponse.text;
            if (cachedResponseAndChecksum && cachedResponseAndChecksum.length === 2) { // cached entry exists and it has proper checksum too
                const cachedChecksum = cachedResponseAndChecksum[0].text;
                if (!serverChecksum) { // resource has been deleted on server
                    const deleted = await cache.delete(url);
                    if (!deleted) {
                        console.warn(`Cached resource [${url}] was not deleted.`);
                    }
                    return staleResponse();
                } else if (serverChecksum !== cachedChecksum) { // resource has been modified on server
                    const fetchedResponse = await fetch(url);
                    cacheIfSuccessful(fetchedResponse, serverChecksumRequest, serverChecksumResponseToCache, url, cache);
                    return fetchedResponse;
                } else { // serverChecksum === cachedChecksum; resource is the same on server and in client cache
                    return cachedResponseAndChecksum[0];
                }
            } else { // there is no cached entry
                if (!serverChecksum) { // resource has been deleted on server
                    return staleResponse();
                } else { // resource exists on server
                    const fetchedResponse = await fetch(url);
                    cacheIfSuccessful(fetchedResponse, serverChecksumRequest, serverChecksumResponseToCache, url, cache);
                    return fetchedResponse;
                }
            }
        }());
    } // all non-static resources should be bypassed by service worker, just ignoring them in 'fetch' event; this will trigger default logic
});

console.debug('Service worker script...completed');
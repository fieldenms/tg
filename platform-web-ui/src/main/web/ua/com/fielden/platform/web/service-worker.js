console.debug('Service worker script...started');

const cacheName = 'tg-air-dev-cache';
const checksumCacheName = 'tg-air-dev-cache-checksums';

const isStatic = function (url) {
    const pathname = new URL(url).pathname;
    // console.debug(`isStatic [${url}] pathname [${pathname}]`);
    return pathname === '/' ||
        pathname.startsWith('/resources/') ||
        pathname.startsWith('/app/') ||
        pathname.startsWith('/centre_ui/') ||
        pathname.startsWith('/master_ui/') ||
        pathname.startsWith('/custom_view/');
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

const cacheIfSuccessful = function (response, checksumRequest, checksumResponseToCache, url, cache, checksumCache) {
    if (isResponseSuccessful(response)) { // cache response if it is successful
        // IMPORTANT: Clone the response. A response is a stream
        // and because we want the browser to consume the response
        // as well as the cache consuming the response, we need
        // to clone it so we have two streams.
        const responseToCache = response.clone();
        return cache.put(url, responseToCache).then(function() {
            return checksumCache.put(checksumRequest, checksumResponseToCache).then(function () {
                return response;
            });
        });
    }
    return Promise.resolve(response);
};

self.addEventListener('fetch', function (event) {
    const request = event.request;
    const url = request.url;
    if (isStatic(url)) {
        event.respondWith(function() {
            console.time('open and fetch checksum ' + url);
            return caches.open(cacheName).then(function (cache) {
                const serverChecksumRequest = new Request(url + '?checksum=true', { method: 'GET' });
                return fetch(serverChecksumRequest).then(function(serverChecksumResponse) {
                    console.timeEnd('open and fetch checksum ' + url);
                    console.time('matchAll ' + url);
                    return cache.match(url).then(function (cachedResponse) {
                        return caches.open(checksumCacheName).then(function (checksumCache) {
                            return checksumCache.match(url + '?checksum=true').then(function (cachedChecksum1) {
                                console.timeEnd('matchAll ' + url);
                                const serverChecksumResponseToCache = isResponseSuccessful(serverChecksumResponse) && serverChecksumResponse.clone();
                                // const serverChecksum = /* isResponseSuccessful(serverChecksumResponse) && */ await serverChecksumResponse.text();
                                return serverChecksumResponse.text().then(function (serverChecksum) {
                                    if (cachedResponse && cachedChecksum1) { // cached entry exists and it has proper checksum too
                                        return cachedChecksum1.text().then(function (cachedChecksum) {
                                            console.debug(`${url} CACHED: cachedChecksum = ${cachedChecksum} serverChecksum = ${serverChecksum}`);
                                            if (!serverChecksum) { // resource has been deleted on server
                                                console.warn(`Resource ${url} has been deleted on server.`);
                                                return cache.delete(url).then(function (deleted) {
                                                    if (!deleted) {
                                                        console.error(`Cached resource [${url}] was not deleted.`);
                                                    }
                                                    return staleResponse();
                                                });
                                            } else if (serverChecksum !== cachedChecksum) { // resource has been modified on server
                                                console.warn(`Resource ${url} has been modified on server. CachedChecksum ${cachedChecksum} vs serverChecksum ${serverChecksum}. MODIFIED RESOURCE WILL BE RE-CACHED.`);
                                                return fetch(url).then(function (fetchedResponse) {
                                                    return cacheIfSuccessful(fetchedResponse, serverChecksumRequest, serverChecksumResponseToCache, url, cache, checksumCache);
                                                });
                                            } else { // serverChecksum === cachedChecksum; resource is the same on server and in client cache
                                                return cachedResponse;
                                            }
                                        });
                                    } else { // there is no cached entry
                                        console.debug(`${url} NEW: serverChecksum = ${serverChecksum}`);
                                        if (!serverChecksum) { // resource has been deleted on server
                                            console.warn(`Resource ${url} has been deleted on server.`);
                                            return staleResponse();
                                        } else { // resource exists on server
                                            console.warn(`Resource ${url} exists on server. ServerChecksum ${serverChecksum}. NEW RESOURCE WILL BE CACHED.`);
                                            return fetch(url).then(function (fetchedResponse) {
                                                return cacheIfSuccessful(fetchedResponse, serverChecksumRequest, serverChecksumResponseToCache, url, cache, checksumCache);
                                            });
                                        }
                                    }
                                });
                            });
                        });
                    });
                });
            });
        }());
    } // all non-static resources should be bypassed by service worker, just ignoring them in 'fetch' event; this will trigger default logic
});

console.debug('Service worker script...completed');
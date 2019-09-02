/**
 * The name for resources cache.
 */
const cacheName = 'tg-deployment-cache';
/**
 * The name for separate cache of resource checksums.
 */
const checksumCacheName = 'tg-deployment-cache-checksums';

/**
 * Determines whether request 'url' represents static resource, i.e. such resource that does not change between releases.
 * 
 * Please note that for deployment mode only '/', '/logout' and '/resources/...' are needed.
 * However, we have listed all possible resources here to avoid the change to service worker later.
 * 
 * @param url
 * @param method
 */
const isStatic = function (url, method) {
    const pathname = new URL(url).pathname;
    return 'GET' === method && (pathname === '/' ||
        pathname === '/logout' ||
        pathname === '/forgotten' ||
        pathname.startsWith('/resources/') ||
        pathname.startsWith('/app/') ||
        pathname.startsWith('/centre_ui/') ||
        pathname.startsWith('/master_ui/') ||
        pathname.startsWith('/custom_view/'));
};

/**
 * Creates response indicating that client application is stale and is needed to be refreshed fully.
 */
const staleResponse = function () {
    return new Response('STALE', {status: 412, statusText: 'BAD', headers: {'Content-Type': 'text/plain'}});
};

/**
 * Indicates whether response is successful.
 */
const isResponseSuccessful = function (response) {
    return response && response.status === 200 && response.type === 'basic';
};

/**
 * Caches the specified 'response' and its checksum ('checksumResponseToCache') in case where they are both successful.
 * Returns promise resolving to 'response'.
 */
const cacheIfSuccessful = function (response, checksumRequest, checksumResponseToCache, url, cache, checksumCache) {
    if (isResponseSuccessful(response) && isResponseSuccessful(checksumResponseToCache)) { // cache response if it is successful
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

/**
 * Returns promise resolving to reponse text if successful, otherwise to empty string.
 * 
 * @param {*} serverChecksumResponse 
 */
const serverChecksumPromise = function (serverChecksumResponse) {
    if (isResponseSuccessful(serverChecksumResponse)) {
        return serverChecksumResponse.text();
    } else {
        return Promise.resolve(''); // empty serverChecksum
    }
};

self.addEventListener('activate', event => {
    clients.claim();
});

self.addEventListener('fetch', function (event) {
    const request = event.request;
    const urlObj = new URL(request.url);
    const url = urlObj.origin + urlObj.pathname;
    if (isStatic(url, request.method)) {
        event.respondWith(function() {
            return caches.open(cacheName).then(function (cache) {
                const serverChecksumRequest = new Request(url + '?checksum=true', { method: 'GET' });
                return fetch(serverChecksumRequest).then(function(serverChecksumResponse) {
                    return cache.match(url).then(function (cachedResponse) {
                        return caches.open(checksumCacheName).then(function (checksumCache) {
                            return checksumCache.match(url + '?checksum=true').then(function (cachedChecksumResponse) {
                                const serverChecksumResponseToCache = isResponseSuccessful(serverChecksumResponse) && serverChecksumResponse.clone();
                                return serverChecksumPromise(serverChecksumResponse).then(function (serverChecksum) {
                                    if (cachedResponse && cachedChecksumResponse) { // cached entry exists and it has proper checksum too
                                        return cachedChecksumResponse.text().then(function (cachedChecksum) {
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
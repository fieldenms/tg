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
 * Please note that for deployment mode only '/', '/logout', '/forgotten' and '/resources/...' are needed.
 * However, we have listed all possible resources here to avoid the change to service worker later.
 * 
 * @param url
 * @param method
 */
const isStatic = function (url, method) {
    const pathname = new URL(url).pathname;
    return 'GET' === method && (pathname === '/' ||
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
    return response && response.ok;
};

/**
 * Caches the specified 'response' and its checksum ('checksumResponse') in case where they are both successful.
 * Returns promise resolving to 'response'.
 */
const cacheIfSuccessful = function (response, checksumRequest, checksumResponse, url, cache, checksumCache) {
    if (isResponseSuccessful(response)) { // cache response if it is successful; 'checksumResponse' is successful at this stage
        // IMPORTANT: Clone the response. A response is a stream and because we want the browser to consume the response
        // as well as the cache consuming the response, we need to clone it so we have two streams.
        return cache.put(url, response.clone()).then(function() { // cache response; it should not fail (otherwise bad response will be returned)
            return checksumCache.put(checksumRequest, checksumResponse).then(function () { // cache checksum; it should not fail (otherwise bad response will be returned)
                return response;
            });
        });
    }
    return Promise.resolve(response); // do not blow up response if for some reason response was not successful; just return it as if the request was not intercepted by service worker
};

/**
 * Returns promise resolving to response text if successful, otherwise returns rejection promise containing unsuccessful response.
 * 
 * @param response 
 */
const getTextFrom = function (response) {
    if (isResponseSuccessful(response)) {
        return response.clone().text(); // perform cloning here to leave original 'response' stream unaffected
    } else {
        return Promise.reject(response);
    }
};

self.addEventListener('activate', event => {
    // By default the page's fetches will not go through service worker if it was not fetched through service worker.
    // This is the case for the very first time index.html loading.
    // However we can enforce service worker to take full control as soon as first activation performs.
    // This makes [immediate caching of index.html dependencies] possible.
    clients.claim();
});

self.addEventListener('fetch', function (event) {
    const request = event.request;
    const urlObj = new URL(request.url);
    const url = urlObj.origin + urlObj.pathname;
    if (isStatic(url, request.method)) { // only consider intercepting for static resources
        event.respondWith(function() {
            return caches.open(cacheName).then(function (cache) { // open main cache; it should not fail (otherwise bad response will be returned)
                const serverChecksumRequest = new Request(url + '?checksum=true', { method: 'GET' });
                return fetch(serverChecksumRequest).then(function(serverChecksumResponse) { // fetch checksum for the intercepted resource; it should not fail (otherwise bad response will be returned)
                    return cache.match(url).then(function (cachedResponse) { // match resource in main cache; it should not fail (otherwise bad response will be returned)
                        return caches.open(checksumCacheName).then(function (checksumCache) { // open checksum cache; it should not fail (otherwise bad response will be returned)
                            return checksumCache.match(url + '?checksum=true').then(function (cachedChecksumResponse) { // match resource's checksum in checksum cache; it should not fail (otherwise bad response will be returned)
                                return getTextFrom(serverChecksumResponse).then(function (serverChecksum) { // get checksum text; checksum response should be successful (otherwise bad response will be returned)
                                    if (cachedResponse && cachedChecksumResponse) { // cached entry exists and it has proper checksum too
                                        return cachedChecksumResponse.text().then(function (cachedChecksum) { // cachedChecksumResponse always is successful, because only successful checksumResponse can be cached
                                            if (!serverChecksum) { // resource has been deleted on server
                                                console.warn(`Resource ${url} has been deleted on server.`);
                                                return cache.delete(url).then(function (deleted) {
                                                    if (!deleted) {
                                                        console.warn(`Cached resource [${url}] was not deleted.`); // do not blow up response if for some reason deletion was not successful; but it should be successful
                                                    }
                                                    return staleResponse();
                                                });
                                            } else if (serverChecksum !== cachedChecksum) { // resource has been modified on server
                                                console.warn(`Resource ${url} has been modified on server. CachedChecksum ${cachedChecksum} vs serverChecksum ${serverChecksum}. MODIFIED RESOURCE WILL BE RE-CACHED.`);
                                                return fetch(url).then(function (fetchedResponse) {
                                                    return cacheIfSuccessful(fetchedResponse, serverChecksumRequest, serverChecksumResponse, url, cache, checksumCache);
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
                                                return cacheIfSuccessful(fetchedResponse, serverChecksumRequest, serverChecksumResponse, url, cache, checksumCache);
                                            });
                                        }
                                    }
                                }, function (serverChecksumResponseError) { // it is very important not to chain catch clause but to use onRejected callback; this is because we need to process errors only from getTextFrom(...) promise and not from getTextFrom(...).then(...) promise.
                                    if (serverChecksumResponseError instanceof Response && !isResponseSuccessful(serverChecksumResponseError) &&
                                        (serverChecksumResponseError.status === 403 || serverChecksumResponseError.status === 503)) {
                                        // If server checksum response is Forbidden (403) or Service Unavailable (503) then we need to respond with redirection response to a login resource.
                                        return Response.redirect(url + 'login/');
                                    } else {
                                        throw serverChecksumResponseError; // rethrow the error in other cases as if there was no onRejected clause here; this would lead to promise rejection
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
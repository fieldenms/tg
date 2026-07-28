/**
 * The name for resources cache.
 */
const CACHE_NAME = 'tg-deployment-cache';
/**
 * The name for separate cache of resource checksums.
 */
const CHECKSUM_CACHE_NAME = 'tg-deployment-cache-checksums';

/**
 * Determines whether request 'pathName' represents static resource, i.e. such resource that does not change between releases.
 * 
 * Please note that for deployment mode only '/', '/forgotten' and '/resources/...' are needed.
 * However, we have listed all possible resources here to avoid the change to service worker later.
 * 
 * @param pathName
 * @param method
 */
function isStatic (pathName, method) {
    return 'GET' === method && (pathName === '/' ||
        pathName === '/forgotten' ||
        pathName.startsWith('/resources/') ||
        pathName.startsWith('/app/') ||
        pathName.startsWith('/centre_ui/') ||
        pathName.startsWith('/master_ui/') ||
        pathName.startsWith('/custom_view/'));
}

/**
 * Creates response indicating that client application is stale and is needed to be refreshed fully.
 */
function staleResponse () {
    return new Response('STALE', {status: 412, statusText: 'BAD', headers: {'Content-Type': 'text/plain'}});
}

/**
 * Indicates whether response is successful.
 */
function isResponseSuccessful (response) {
    return response && response.ok;
}

/**
 * Creates URL object from 'requestUrl' string.
 */
function createURL (requestUrl) {
    return new URL(requestUrl);
}

/**
 * Creates GET Request object from 'url'.
 */
function createGETRequest (url) {
    return new Request(url, { method: 'GET' });
}

/**
 * Creates Promise for 'cache' entry by it's 'url'.
 */
function deleteCacheEntry (url, cache) {
    return cache.delete(url).then(
        deleted => {
            if (!deleted) {
                console.warn(`Cached resource [${url}] was not deleted.`); // do not blow up response if for some reason deletion was not successful; but it should be successful
            }
            return deleted;
        },
        error => {
            console.warn(`Cached resource [${url}] was not deleted. Error: [${error}].`);
            return Promise.reject(error); // preserve rejection
        }
    );
}

function cleanUp (url, cache) {
    const serverResourcesRequest = createGETRequest(url + '?resources=true');
    return fetch(serverResourcesRequest).then(serverResourcesResponse => { // fetch resources; it should not fail (otherwise bad response will be returned)
        return getTextFrom(serverResourcesResponse).then(serverResourcesStr => {
            const serverResources = new Set(serverResourcesStr.split('|'));
            return cache.keys().then(requests => {
                return Promise.all(
                    requests.filter(request => {
                        const pathName = createURL(request.url).pathname;
                        return pathName.startsWith('/resources/') && !serverResources.has(pathName);
                    })
                    .map(request => request.url)
                    .map(url => deleteCacheEntry(url, cache)
                        .then(deleted => deleteCacheEntry(url + '?checksum=true', checksumCache))
                    )
                );
            });
        });
    });
}

/**
 * Caches the specified 'response' and its checksum ('checksumResponse') in case where they are both successful.
 * Returns promise resolving to 'response'.
 */
function cacheIfSuccessful (response, checksumRequest, checksumResponse, url, cache, checksumCache, urlObj, event) {
    if (isResponseSuccessful(response)) { // cache response if it is successful; 'checksumResponse' is successful at this stage
        // IMPORTANT: Clone the response. A response is a stream and because we want the browser to consume the response
        // as well as the cache consuming the response, we need to clone it so we have two streams.
        return cache.put(url, response.clone()).then(() => { // cache response; it should not fail (otherwise bad response will be returned)
            return checksumCache.put(checksumRequest, checksumResponse).then(() => { // cache checksum; it should not fail (otherwise bad response will be returned)
                if (urlObj.pathname === '/') { // main 'index.html' file has been re-cached after a change (or cached for the first time)
                    event.waitUntil( // insist to keep service worker alive until the following promise completes
                        cleanUp(url, cache).catch(error => { // start cleaning up redundant resources
                            console.warn(`Cleaning up failed with error [${error}].`, error);
                        })
                    );
                }
                return response;
            });
        });
    }
    return Promise.resolve(response); // do not blow up response if for some reason response was not successful; just return it as if the request was not intercepted by service worker
}

/**
 * Returns promise resolving to response text if successful, otherwise returns rejection promise containing unsuccessful response.
 * 
 * @param response 
 */
function getTextFrom (response) {
    if (isResponseSuccessful(response)) {
        return response.clone().text(); // perform cloning here to leave original 'response' stream unaffected
    } else {
        return Promise.reject(response);
    }
}

addEventListener('install', event => {
    // New updated service worker can be installed, but not yet activated until the page will be closed / opened again.
    // Currently, even 'Hard reload' or 'Empty cache and hard reload' in Chrome does not insist on service worker update.
    // Actually, these actions do nothing - not even installing an updated service worker (unlike Normal Reload, Ctrl+R).
    // So, new updated service worker gets installed and keeps being in 'waiting to activate' state.
    // This is because the previous service worker already controls 'index.html' and by default new service worker is not activated.
    // We want to take control immediately for all pages, because every change to service worker are backward compatible.
    // Practically skipWaiting() enforces control on every tab / window already opened.
    //   (See https://w3c.github.io/ServiceWorker/#activate 8.1 and 8.2).
    // In case of some browser implementation deficiencies, clients.claim() should also additionally enforce that.
    // But clients.claim() is not strictly required (see it's usage below for more details on the reason why it is needed).
    skipWaiting(); // progressing service worker to 'activating' state and further - no need to 'waitUntil' here
});

addEventListener('activate', event => {
    // By default the page's fetches will not go through service worker if it was not fetched through service worker.
    // This is the case for the very first time 'index.html' loading.
    // However we can enforce service worker to take full control as soon as first activation performs.
    // This makes immediate caching of 'index.html' dependencies possible.
    event.waitUntil(clients.claim()); // wait for the promise to settle and only then allow service worker to dispose
});

addEventListener('fetch', event => {
    const request = event.request;
    const urlObj = createURL(request.url);
    if (isStatic(urlObj.pathname, request.method)) { // only consider intercepting for static resources
        event.respondWith(
            caches.open(CACHE_NAME).then(cache => { // open main cache; it should not fail (otherwise bad response will be returned)
                // 'request.url' may contain '#' / '?' parts -- use only 'origin' and 'pathname'.
                const url = urlObj.origin + urlObj.pathname;
                const serverChecksumRequest = createGETRequest(url + '?checksum=true');
                return fetch(serverChecksumRequest).then(serverChecksumResponse => { // fetch checksum for the intercepted resource; it should not fail (otherwise bad response will be returned)
                    return cache.match(url).then(cachedResponse => { // match resource in main cache; it should not fail (otherwise bad response will be returned)
                        return caches.open(CHECKSUM_CACHE_NAME).then(checksumCache => { // open checksum cache; it should not fail (otherwise bad response will be returned)
                            return checksumCache.match(url + '?checksum=true').then(cachedChecksumResponse => { // match resource's checksum in checksum cache; it should not fail (otherwise bad response will be returned)
                                return getTextFrom(serverChecksumResponse).then(serverChecksum => { // get checksum text; checksum response should be successful (otherwise bad response will be returned)
                                    if (cachedResponse && cachedChecksumResponse) { // cached entry exists and it has proper checksum too
                                        return cachedChecksumResponse.text().then(cachedChecksum => { // cachedChecksumResponse always is successful, because only successful checksumResponse can be cached
                                            if (!serverChecksum) { // resource has been deleted on server
                                                console.warn(`Resource ${url} has been deleted on server.`);
                                                return deleteCacheEntry(url, cache)
                                                    .then(deleted => deleteCacheEntry(url + '?checksum=true', checksumCache))
                                                    .then(deleted => staleResponse());
                                            } else if (serverChecksum !== cachedChecksum) { // resource has been modified on server
                                                console.warn(`Resource ${url} has been modified on server. CachedChecksum ${cachedChecksum} vs serverChecksum ${serverChecksum}. MODIFIED RESOURCE WILL BE RE-CACHED.`);
                                                return fetch(url).then(fetchedResponse => {
                                                    return cacheIfSuccessful(fetchedResponse, serverChecksumRequest, serverChecksumResponse, url, cache, checksumCache, urlObj, event);
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
                                            return fetch(url).then(fetchedResponse => {
                                                return cacheIfSuccessful(fetchedResponse, serverChecksumRequest, serverChecksumResponse, url, cache, checksumCache, urlObj, event);
                                            });
                                        }
                                    }
                                }, serverChecksumResponseError => { // it is very important not to chain catch clause but to use onRejected callback; this is because we need to process errors only from getTextFrom(...) promise and not from getTextFrom(...).then(...) promise.
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
            })
        )
    } // all non-static resources should be bypassed by service worker, just ignoring them in 'fetch' event; this will trigger default logic
});
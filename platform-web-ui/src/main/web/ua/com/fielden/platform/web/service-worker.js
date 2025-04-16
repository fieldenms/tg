/**
 * The name for resources cache.
 */
const CACHE_NAME = 'tg-deployment-cache';
/**
 * The name for separate cache of resource checksums.
 */
const CHECKSUM_CACHE_NAME = 'tg-deployment-cache-checksums';

/**
 * Suffix for checksum request URL.
 */
const CHECKSUM_URL_SUFFIX = '?checksum=true';
/**
 * Suffix for resource paths request URL.
 */
const RESOURCES_URL_SUFFIX = '?resources=true';
/**
 * Delimiter for resource paths.
 */
const RESOURCES_DELIMITER = '|';

/**
 * Determines whether request 'pathName' represents static resource, i.e. such resource that does not change between releases.
 * 
 * Please note that for deployment mode only '/', '/forgotten' and '/resources/...' are needed.
 * However, we have listed all possible resources here to avoid the change to service worker later.
 * 
 * @param pathName
 * @param method
 */
function isStatic(pathName, method) {
    return 'GET' === method && (pathName === '/' ||
        pathName === '/forgotten' ||
        pathName.startsWith('/resources/') ||
        pathName.startsWith('/app/') ||
        pathName.startsWith('/centre_ui/') ||
        pathName.startsWith('/master_ui/') ||
        pathName.startsWith('/custom_view/'));
}

/**
 * Creates a response indicating that client application is stale and is needed to be refreshed fully.
 */
function staleResponse() {
    console.info(`The client app is stale now.`);
    return new Response('STALE', {status: 412, statusText: 'BAD', headers: {'Content-Type': 'text/plain'}});
}

/**
 * Indicates whether the 'response' is successful.
 */
function isResponseSuccessful(response) {
    return response && response.ok;
}

/**
 * Creates an URL object from 'requestUrl' string.
 */
function createURL(requestUrl) {
    return new URL(requestUrl);
}

/**
 * Creates GET Request object from 'url'.
 */
function createGETRequest(url) {
    // GET is the default 'method', but make it a little bit more explicit.
    return new Request(url, { method: 'GET' });
}

/**
 * Creates a 'Promise' for 'cache' entry deletion by it's 'url'.
 * Warns about unsuccessful deletion or if the resource was not found ('deleted' === false).
 */
function deleteCacheEntry(url, cache) {
    return cache.delete(url).then(
        deleted => {
            if (!deleted) {
                console.warn(`The cached resource at [${url}] was not deleted. It was likely deleted manually earlier.`);
            }
            return deleted;
        },
        error => {
            console.warn(`The cached resource at [${url}] was not deleted. Error:`, error);
            // Preserve rejection as in original 'cache.delete' promise.
            return Promise.reject(error);
        }
    );
}

/**
 * Creates a 'Promise' for redundant 'url' resource deletion, assuming its presence in both 'cache' and 'checksumCache'.
 * Warns about some unusual deletion problems and shows informational message for easier inspection.
 * Use Chrome 'Default levels' (Info, Warnings, Errors) and unchecked 'Selected context only' and checked 'Preserve log'.
 */
function deleteRedundantResource(url, cache, checksumCache) {
    // Shows informational message on 'url' resource deletion from a server and, consequently, from a Cache Storage.
    console.info(`The resource at [${url}] has been deleted on the server. It will be removed from the cache.`);
    return deleteCacheEntry(url, cache)
        .then(_ => deleteCacheEntry(url + CHECKSUM_URL_SUFFIX, checksumCache));
}

/**
 * Asynchronously cleans up Cache Storage by removing redundant entries, not present on a server.
 * It does so by loading a set of present server resources and comparing it with Cache Storage entries.
 * Missing server resources will be deleted from both 'cache' and 'checksumCache'.
 */
function cleanUp(url, cache, checksumCache) {
    console.info(`Starting cleanup of redundant resources...`);
    // Create special request against root '/' (aka 'index.html') to load paths of current resources.
    const serverResourcesRequest = createGETRequest(url + RESOURCES_URL_SUFFIX);
    // Fetch the request and get text from a response.
    return fetch(serverResourcesRequest).then(serverResourcesResponse => {
        return getTextFrom(serverResourcesResponse).then(serverResourcesStr => {
            // Create a set of resource paths from a string, returned by a server.
            const serverResources = new Set(serverResourcesStr.split(RESOURCES_DELIMITER));
            // Find all 'cache' entries...
            return cache.keys().then(requests => {
                return Promise.all(
                    // ... and filter out those not present on a server;
                    requests.filter(request => !serverResources.has(createURL(request.url).pathname))
                    // Remove found entries from both caches.
                    .map(request => deleteRedundantResource(request.url, cache, checksumCache))
                );
            });
        });
    });
}

/**
 * Caches the specified 'response' and its checksum ('checksumResponse') in case where they are both successful.
 * Returns promise resolving to 'response'.
 *
 * Also initiates 'cleanUp' for changed '/' resource.
 */
function cacheIfSuccessful(response, checksumRequest, checksumResponse, url, cache, checksumCache, urlObj, event) {
    // Cache response if it is successful; 'checksumResponse' is successful at this stage.
    if (isResponseSuccessful(response)) {
        // IMPORTANT: Clone the response. We need to clone it so we have two streams.
        // First stream is for the browser to consume the response.
        // Second is for a cache consuming the response.
        // Cache response; it should not fail (otherwise net::ERR_CACHE_* will be returned; see chrome://network-errors/):
        return cache.put(url, response.clone()).then(() => {
            // Cache checksum; it should not fail (otherwise - net::ERR_CACHE_*):
            return checksumCache.put(checksumRequest, checksumResponse).then(() => {
                if (urlObj.pathname === '/') {
                    // Main 'index.html' file has been re-cached after a change (or cached for the first time).
                    // Start cleaning up of Cache Storage asynchronously.
                    // Insist to keep service worker alive until 'cleanUp' promise completes:
                    event.waitUntil(
                        // Actual clean up of redundant resources:
                        cleanUp(url, cache, checksumCache).catch(error => {
                            console.warn(`Cleanup failed with error:`, error);
                        })
                    );
                }
                // Return response quite soon after 'checksumResponse' is inside the Cache Storage (no clean up blocking).
                return response;
            });
        });
    }
    // Do not blow up response if for some reason it was not successful.
    // Just return it as if the request was not intercepted by service worker.
    return Promise.resolve(response);
}

/**
 * Returns promise resolving to response text if successful, otherwise returns rejection promise containing unsuccessful response.
 * 
 * @param response 
 */
function getTextFrom(response) {
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
    // Only consider intercepting of static resources.
    if (isStatic(urlObj.pathname, request.method)) {
        // 'respondWith' will insist on service worker to live until the promise will be resolved.
        event.respondWith(
            // Open the main cache; it should not fail (otherwise net::ERR_CACHE_* will be returned; see chrome://network-errors/).
            caches.open(CACHE_NAME).then(cache => {
                // 'request.url' may contain '#' / '?' parts -- use only 'origin' and 'pathname'.
                const url = urlObj.origin + urlObj.pathname;
                const serverChecksumRequest = createGETRequest(url + CHECKSUM_URL_SUFFIX);
                // Fetch checksum for the intercepted resource; it should not fail (otherwise - net::ERR_*).
                return fetch(serverChecksumRequest).then(serverChecksumResponse => {
                    // Match resource in the main cache; it should not fail (otherwise - net::ERR_*).
                    return cache.match(url).then(cachedResponse => {
                        // Open the checksum cache; it should not fail (otherwise - net::ERR_*).
                        return caches.open(CHECKSUM_CACHE_NAME).then(checksumCache => {
                            // Match resource's checksum in the checksum cache; it should not fail (otherwise - net::ERR_*).
                            return checksumCache.match(url + CHECKSUM_URL_SUFFIX).then(cachedChecksumResponse => {
                                // Get checksum text; checksum response should be successful (otherwise - net::ERR_*).
                                return getTextFrom(serverChecksumResponse).then(serverChecksum => {
                                    if (cachedResponse && cachedChecksumResponse) {
                                        // Cached entry exists and it has a proper checksum too.
                                        // 'cachedChecksumResponse' is always successful, because only successful 'checksumResponse' can be cached.
                                        return cachedChecksumResponse.text().then(cachedChecksum => {
                                            if (!serverChecksum) {
                                                return deleteRedundantResource(url, cache, checksumCache).then(_ => staleResponse());
                                            } else if (serverChecksum !== cachedChecksum) {
                                                console.info(`The resource at [${url}] has been modified on the server. CachedChecksum ${cachedChecksum} vs serverChecksum ${serverChecksum}. The modified resource will be re-cached.`);
                                                return fetch(url).then(fetchedResponse => {
                                                    return cacheIfSuccessful(fetchedResponse, serverChecksumRequest, serverChecksumResponse, url, cache, checksumCache, urlObj, event);
                                                });
                                            } else {
                                                // 'serverChecksum' === 'cachedChecksum'.
                                                // Resource is the same on the server and in the client cache. Just return it.
                                                return cachedResponse;
                                            }
                                        });
                                    } else {
                                        // There is no cached entry (or for some reason it is incomplete, e.g. without a checksum).
                                        if (!serverChecksum) {
                                            return staleResponse();
                                        } else {
                                            console.info(`The resource at [${url}] exists on the server. ServerChecksum ${serverChecksum}. The new resource will be cached.`);
                                            return fetch(url).then(fetchedResponse => {
                                                return cacheIfSuccessful(fetchedResponse, serverChecksumRequest, serverChecksumResponse, url, cache, checksumCache, urlObj, event);
                                            });
                                        }
                                    }
                                // It is very important not to chain catch clause but to use 'onRejected' callback.
                                // This is because we need to process errors only from getTextFrom(...) promise;
                                //   (i.e. not from getTextFrom(...).then(...) promise).
                                }, serverChecksumResponseError => {
                                    if (serverChecksumResponseError instanceof Response && !isResponseSuccessful(serverChecksumResponseError) &&
                                        (serverChecksumResponseError.status === 403 || serverChecksumResponseError.status === 503)) {
                                        // Server checksum response is Forbidden (403) or Service Unavailable (503).
                                        // In this case we need to respond with redirection response to a login resource.
                                        return Response.redirect(url + 'login/');
                                    } else {
                                        // Re-throw the error in other cases as if there was no 'onRejected' clause here.
                                        // This would lead to promise rejection.
                                        throw serverChecksumResponseError;
                                    }
                                });
                            });
                        });
                    });
                });
            })
        )
    }
    // Else: all non-static resources should be bypassed by service worker - just ignoring them in 'fetch' event.
    // This will trigger the default logic.
});
/// The name for resources cache.
///
const CACHE_NAME = 'tg-deployment-cache';
/// The name for separate cache of resource checksums.
///
const CHECKSUM_CACHE_NAME = 'tg-deployment-cache-checksums';

/// Suffix for checksum request URL.
///
const CHECKSUM_URL_SUFFIX = '?checksum=true';
/// Suffix for resource paths request URL.
///
const RESOURCES_URL_SUFFIX = '?resources=true';
/// Delimiter for resource paths.
///
const RESOURCES_DELIMITER = '\n';

/// Path of the root resource (aka 'index.html').
/// It also serves the paths of all deployment resources (see 'RESOURCES_URL_SUFFIX').
/// Server-side counterpart is 'AppIndexResource.BINDING_PATH'.
///
const ROOT_PATH = '/';
/// Path of the vulcanised file with all client-side application resources.
/// This is the only deployment resource that reliably changes on every release.
/// That is why its re-caching induces clearing of redundant resources.
/// Server-side counterpart is 'VulcanizingUtility.FILE_STARTUP_RESOURCES_VULCANIZED_JS'.
///
const STARTUP_RESOURCES_PATH = '/resources/startup-resources-vulcanized.js';

/// Determines whether request 'pathName' represents static resource.
/// That is, a deployment resource with a checksum, that can only change on releases.
/// These are exactly the paths of 'checksums.json', as served by 'RESOURCES_URL_SUFFIX' request.
///
/// Everything else bypasses service worker and is always served as fresh as a server makes it.
/// This is essential for dynamic '/app/configuration' with its up-to-date application parameters (see #2567).
/// Generated '/app/...', '/master_ui/...' and '/centre_ui/...' resources are vulcanised into 'STARTUP_RESOURCES_PATH'.
/// So, in deployment mode, they are never requested separately.
/// '/custom_view/...' resources are not vulcanised, however they have no checksums and were never cacheable.
///
function isStatic(pathName, method) {
    return 'GET' === method && (
        pathName === ROOT_PATH ||
        pathName === '/forgotten' ||
        pathName.startsWith('/resources/')
    );
}

/// Creates a response indicating that client application is stale and is needed to be refreshed fully.
///
function staleResponse() {
    console.info(`The client app is stale now.`);
    return new Response('STALE', {status: 412, statusText: 'BAD', headers: {'Content-Type': 'text/plain'}});
}

/// Indicates whether the 'response' is successful.
///
function isResponseSuccessful(response) {
    return response && response.ok;
}

/// Creates an URL object from 'requestUrl' string.
///
function createURL(requestUrl) {
    return new URL(requestUrl);
}

/// Creates GET Request object from 'url'.
///
function createGETRequest(url) {
    // GET is the default 'method', but make it a little bit more explicit.
    return new Request(url, { method: 'GET' });
}

/// Creates a 'Promise' for 'cache' entry deletion by it's 'url'.
/// Warns about unsuccessful deletion or if the resource was not found ('deleted' === false).
///
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

/// Creates a 'Promise' for redundant 'url' resource deletion.
/// Its presence in both 'cache' and 'checksumCache' is assumed.
/// Warns about some unusual deletion problems and shows informational message for easier inspection.
/// Use Chrome 'Default levels' (Info, Warnings, Errors).
/// Also, uncheck 'Selected context only' and check 'Preserve log'.
///
function deleteRedundantResource(url, cache, checksumCache) {
    // Shows informational message on 'url' resource deletion from a server and, consequently, from a Cache Storage.
    console.info(`The resource at [${url}] has been deleted on the server. It will be removed from the cache.`);
    return deleteCacheEntry(url, cache)
        .then(_ => deleteCacheEntry(url + CHECKSUM_URL_SUFFIX, checksumCache));
}

/// Indicates whether 'serverResources' is a plausible list of deployment resources.
///
/// Cleaning up is induced exclusively by re-caching of 'STARTUP_RESOURCES_PATH', which a server has just served together with its checksum.
/// So that path must be among the resources that the very same server reports as deployed.
/// If it is not, then the response did not come from 'AppIndexResource'.
/// For example, it may be a login or an error page, returned with a 200 status by something sitting in front of a server.
/// Deleting on such a response would clear the whole Cache Storage, because no cached path would match.
///
function isResourceListPlausible(serverResources) {
    return serverResources.has(STARTUP_RESOURCES_PATH);
}

/// Asynchronously cleans up Cache Storage by removing redundant entries, not present on a server.
/// It does so by loading a set of present server resources and comparing it with Cache Storage entries.
/// Missing server resources will be deleted from both 'cache' and 'checksumCache'.
///
/// Resource paths are always requested against 'ROOT_PATH' of 'origin'.
/// It does not matter which resource has induced the cleaning up.
///
function cleanUp(origin, cache, checksumCache) {
    console.info(`Starting cleanup of redundant resources...`);
    // Create special request against root '/' (aka 'index.html') to load paths of current resources.
    const serverResourcesUrl = origin + ROOT_PATH + RESOURCES_URL_SUFFIX;
    const serverResourcesRequest = createGETRequest(serverResourcesUrl);
    // Fetch the request and get text from a response.
    return fetch(serverResourcesRequest).then(serverResourcesResponse => {
        return getTextFrom(serverResourcesResponse).then(serverResourcesStr => {
            // Create a set of resource paths from a string, returned by a server.
            const serverResources = new Set(serverResourcesStr.split(RESOURCES_DELIMITER));
            // Never delete anything unless the response is an actual list of deployment resources.
            if (!isResourceListPlausible(serverResources)) {
                console.warn(`Skipping cleanup: [${serverResourcesUrl}] did not return a list of deployment resources.`);
                return;
            }
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

/// Caches the specified 'response' and its checksum ('checksumResponse') in case where they are both successful.
/// Returns promise resolving to 'response'.
///
/// Also initiates 'cleanUp' for changed 'STARTUP_RESOURCES_PATH' resource.
///
function cacheIfSuccessful(response, checksumRequest, checksumResponse, url, cache, checksumCache, urlObj, event) {
    // Cache response if it is successful; 'checksumResponse' is successful at this stage.
    if (isResponseSuccessful(response)) {
        // IMPORTANT: Clone the response. We need to clone it so we have two streams.
        // First stream is for the browser to consume the response.
        // Second is for a cache consuming the response.
        // Cache response; it should not fail.
        // Otherwise net::ERR_CACHE_* will be returned (see chrome://network-errors/):
        return cache.put(url, response.clone()).then(() => {
            // Cache checksum; it should not fail (otherwise - net::ERR_CACHE_*):
            return checksumCache.put(checksumRequest, checksumResponse).then(() => {
                if (urlObj.pathname === STARTUP_RESOURCES_PATH) {
                    // The vulcanised file with all client-side application resources has been re-cached after a change.
                    // It is requested exactly once per client app load.
                    // Also, it is the only resource that changes often (mostly on every release).
                    // Start cleaning up of Cache Storage asynchronously.
                    // Insist to keep service worker alive until 'cleanUp' promise completes:
                    event.waitUntil(
                        // Actual clean up of redundant resources:
                        cleanUp(urlObj.origin, cache, checksumCache).catch(error => {
                            console.warn(`Cleanup failed with error:`, error);
                        })
                    );
                }
                // Return response quite soon after 'checksumResponse' is inside the Cache Storage.
                // Clean up does not block it.
                return response;
            });
        });
    }
    // Do not blow up response if for some reason it was not successful.
    // Just return it as if the request was not intercepted by service worker.
    return Promise.resolve(response);
}

/// Returns promise resolving to response text if successful.
/// Otherwise, returns rejection promise containing unsuccessful response.
///
function getTextFrom(response) {
    if (isResponseSuccessful(response)) {
        // Perform cloning here to leave original 'response' stream unaffected.
        return response.clone().text();
    } else {
        return Promise.reject(response);
    }
}

addEventListener('install', event => {
    // New updated service worker can be installed, but not yet activated until the page will be closed / opened again.
    // Currently, even 'Hard reload' or 'Empty cache and hard reload' in Chrome does not insist on that update.
    // Actually, these actions do nothing.
    // They do not even install an updated service worker (unlike Normal Reload, Ctrl+R).
    // So, new updated service worker gets installed and keeps being in 'waiting to activate' state.
    // This is because the previous service worker already controls 'index.html'.
    // And by default new service worker is not activated.
    // We want to take control immediately for all pages.
    // This is because every change to service worker is backward compatible.
    // Practically skipWaiting() enforces control on every tab / window already opened.
    //   (See https://w3c.github.io/ServiceWorker/#activate 8.1 and 8.2).
    // In case of some browser implementation deficiencies, clients.claim() should also additionally enforce that.
    // But clients.claim() is not strictly required.
    // See its usage below for more details on the reason why it is needed.
    // Progressing service worker to 'activating' state and further.
    // There is no need to 'waitUntil' here.
    skipWaiting();
});

addEventListener('activate', event => {
    // By default the page's fetches will not go through service worker if it was not fetched through service worker.
    // This is the case for the very first time 'index.html' loading.
    // However we can enforce service worker to take full control as soon as first activation performs.
    // This makes immediate caching of 'index.html' dependencies possible.
    // Wait for the promise to settle and only then allow service worker to dispose.
    event.waitUntil(clients.claim());
});

addEventListener('fetch', event => {
    const request = event.request;
    const urlObj = createURL(request.url);
    // Only consider intercepting of static resources.
    if (isStatic(urlObj.pathname, request.method)) {
        // 'respondWith' will insist on service worker to live until the promise will be resolved.
        event.respondWith(
            // Open the main cache; it should not fail.
            // Otherwise net::ERR_CACHE_* will be returned (see chrome://network-errors/).
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
                            // Match resource's checksum in the checksum cache.
                            // It should not fail (otherwise - net::ERR_*).
                            return checksumCache.match(url + CHECKSUM_URL_SUFFIX).then(cachedChecksumResponse => {
                                // Get checksum text; checksum response should be successful (otherwise - net::ERR_*).
                                return getTextFrom(serverChecksumResponse).then(serverChecksum => {
                                    if (cachedResponse && cachedChecksumResponse) {
                                        // Cached entry exists and it has a proper checksum too.
                                        // 'cachedChecksumResponse' is always successful.
                                        // This is because only successful 'checksumResponse' can be cached.
                                        return cachedChecksumResponse.text().then(cachedChecksum => {
                                            if (!serverChecksum) {
                                                // Respond as stale regardless of whether the deletion has succeeded.
                                                // 'deleteCacheEntry' preserves the rejection of 'cache.delete', having warned about it already.
                                                // Letting that rejection through would fail the whole 'respondWith' promise with net::ERR_FAILED,
                                                // instead of informing a client that it is stale and is needed to be refreshed fully.
                                                return deleteRedundantResource(url, cache, checksumCache).then(_ => staleResponse(), _ => staleResponse());
                                            } else if (serverChecksum !== cachedChecksum) {
                                                console.info(`The resource at [${url}] has been modified on the server. CachedChecksum ${cachedChecksum} vs serverChecksum ${serverChecksum}. The modified resource will be re-cached.`);
                                                return fetch(url).then(fetchedResponse => {
                                                    return cacheIfSuccessful(fetchedResponse, serverChecksumRequest, serverChecksumResponse, url, cache, checksumCache, urlObj, event);
                                                });
                                            } else {
                                                // 'serverChecksum' === 'cachedChecksum'.
                                                // Resource is the same on the server and in the client cache.
                                                // Just return it.
                                                return cachedResponse;
                                            }
                                        });
                                    } else {
                                        // There is no cached entry.
                                        // Or, for some reason, it is incomplete (e.g. without a checksum).
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
                                        // In this case we need to respond with a redirection to a login resource.
                                        return Response.redirect(url + 'login/');
                                    } else {
                                        // Re-throw the error in other cases.
                                        // Behave as if there was no 'onRejected' clause here.
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
console.debug('service worker script');
const CACHE_NAME = 'tgdevcache';

self.addEventListener('install', function(event) {
    console.debug('service worker install');
    event.waitUntil(
        caches.open(CACHE_NAME).then(function(cache) {
            return cache.addAll([
                '/resources/startup-resources-vulcanized.js'
            ]).then(function () {
                cache.keys().then(function(keys) {
                    //console.error('keys = ', keys);
                    //do something with your array of requests
                });
            }).catch(function (err) {
                //console.error('err = ', err);
            });
        })
    );
});

self.addEventListener('fetch', function(event) {
    if (event.request.url.endsWith('/resources/startup-resources-vulcanized.js')) {
      //console.debug('event.request.url =', event.request.url);
      console.debug('event =', event);
      console.debug('event.request =', event.request);
      event.respondWith(
          caches.match(event.request.url).then(function(response) {
              return response;
          })
      );
    }
    // event.respondWith(
    //     caches.match(event.request).then(function(response) {
    //       return /*response ||*/ fetch(event.request);
    //     })
    // );
});

//self.addEventListener('fetch', function(event) {
//	  event.respondWith(
//	    caches.match(event.request)
//	      .then(function(response) {
//	        // Cache hit - return response
//	        if (response) {
//	          return response;
//	        }
//
//	        return fetch(event.request).then(
//	          function(response) {
//	            // Check if we received a valid response
//	            if(!response || response.status !== 200 || response.type !== 'basic') {
//	              return response;
//	            }
//
//	            // IMPORTANT: Clone the response. A response is a stream
//	            // and because we want the browser to consume the response
//	            // as well as the cache consuming the response, we need
//	            // to clone it so we have two streams.
//	            var responseToCache = response.clone();
//
//	            caches.open(CACHE_NAME)
//	              .then(function(cache) {
//	                cache.put(event.request, responseToCache);
//	              });
//
//	            return response;
//	          }
//	        );
//	      })
//	    );
//	});
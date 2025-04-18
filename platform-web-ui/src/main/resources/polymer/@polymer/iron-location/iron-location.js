import '../polymer/polymer-legacy.js';
import { Polymer } from '../polymer/lib/legacy/polymer-fn.js';
import { dom } from '../polymer/lib/legacy/polymer.dom.js';

/**
@license
Copyright (c) 2015 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at
http://polymer.github.io/LICENSE.txt The complete set of authors may be found at
http://polymer.github.io/AUTHORS.txt The complete set of contributors may be
found at http://polymer.github.io/CONTRIBUTORS.txt Code distributed by Google as
part of the polymer project is also subject to an additional IP rights grant
found at http://polymer.github.io/PATENTS.txt
*/
/**

The `iron-location` element manages binding to and from the current URL.

iron-location is the first, and lowest level element in the Polymer team's
routing system. This is a beta release of iron-location as we continue work
on higher level elements, and as such iron-location may undergo breaking
changes.

#### Properties

When the URL is: `/search?query=583#details` iron-location's properties will be:

  - path: `'/search'`
  - query: `'query=583'`
  - hash: `'details'`

These bindings are bidirectional. Modifying them will in turn modify the URL.

iron-location is only active while it is attached to the document.

#### Links

While iron-location is active in the document it will intercept clicks on links
within your site, updating the URL pushing the updated URL out through the
databinding system. iron-location only intercepts clicks with the intent to
open in the same window, so middle mouse clicks and ctrl/cmd clicks work fine.

You can customize this behavior with the `urlSpaceRegex`.

#### Dwell Time

iron-location protects against accidental history spamming by only adding
entries to the user's history if the URL stays unchanged for `dwellTime`
milliseconds.

@demo demo/index.html

 */
Polymer({
  is: 'iron-location',
  _template: null,

  properties: {
    /*TG #2329*/
    //Indicates whether uri should be docoded on window.location changes or not.
    noDecode: {
      type: Boolean,
      value: false 
    },
    /**
     * The pathname component of the URL.
     */
    path: {
      type: String,
      notify: true,
      value: function() {
        /*TG #2329*/
        return this.noDecode ? window.location.pathname : window.decodeURIComponent(window.location.pathname);
      }
    },

    /**
     * The query string portion of the URL.
     */
    query: {
      type: String,
      notify: true,
      value: function() {
        return window.location.search.slice(1);
      }
    },

    /**
     * The hash component of the URL.
     */
    hash: {
      type: String,
      notify: true,
      value: function() {
        /*TG #2329*/
        return this.noDecode ? window.location.hash.slice(1) : window.decodeURIComponent(window.location.hash.slice(1));
      }
    },

    /**
     * If the user was on a URL for less than `dwellTime` milliseconds, it
     * won't be added to the browser's history, but instead will be replaced
     * by the next entry.
     *
     * This is to prevent large numbers of entries from clogging up the user's
     * browser history. Disable by setting to a negative number.
     */
    dwellTime: {type: Number, value: 2000},

    /**
     * A regexp that defines the set of URLs that should be considered part
     * of this web app.
     *
     * Clicking on a link that matches this regex won't result in a full page
     * navigation, but will instead just update the URL state in place.
     *
     * This regexp is given everything after the origin in an absolute
     * URL. So to match just URLs that start with /search/ do:
     *     url-space-regex="^/search/"
     *
     * @type {string|RegExp}
     */
    urlSpaceRegex: {type: String, value: ''},

    /**
     * A flag that specifies whether the spaces in query that would normally be
     * encoded as %20 should be encoded as +.
     *
     * Given an example text "hello world", it is encoded in query as
     * - "hello%20world" without the parameter
     * - "hello+world" with the parameter
     */
    encodeSpaceAsPlusInQuery: {type: Boolean, value: false},

    /**
     * urlSpaceRegex, but coerced into a regexp.
     *
     * @type {RegExp}
     */
    _urlSpaceRegExp: {computed: '_makeRegExp(urlSpaceRegex)'},

    _lastChangedAt: {type: Number},

    _initialized: {type: Boolean, value: false}
  },

  hostAttributes: {hidden: true},

  observers: ['_updateUrl(path, query, hash)'],

  created: function() {
    this.__location = window.location;
  },

  attached: function() {
    this.listen(window, 'hashchange', '_hashChanged');
    this.listen(window, 'location-changed', '_urlChanged');
    this.listen(window, 'popstate', '_urlChanged');
    this.listen(
        /** @type {!HTMLBodyElement} */ (document.body),
        'click',
        '_globalOnClick');
    // Give a 200ms grace period to make initial redirects without any
    // additions to the user's history.
    this._lastChangedAt = window.performance.now() - (this.dwellTime - 200);
    this._initialized = true;

    this._urlChanged();
  },

  detached: function() {
    this.unlisten(window, 'hashchange', '_hashChanged');
    this.unlisten(window, 'location-changed', '_urlChanged');
    this.unlisten(window, 'popstate', '_urlChanged');
    this.unlisten(
        /** @type {!HTMLBodyElement} */ (document.body),
        'click',
        '_globalOnClick');
    this._initialized = false;
  },

  _hashChanged: function() {
    /*TG #2329*/
    if (this.noDecode) {
      this._hashChangedOld();
    } else {
      this._hashChangedNew();
    }
  },  
  _hashChangedOld: function () {
    this.hash = window.location.hash.substring(1);
  },
  _hashChangedNew: function () {
    /*TG #2329*/ /*end*/
    this.hash = window.decodeURIComponent(this.__location.hash.substring(1));
  },

  _urlChanged: function() {
    /*TG #2329*/
    if (this.noDecode) {
      this._urlChangedOld();
    } else {
      this._urlChangedNew();
    }
  },
  _urlChangedOld: function () {
    // We want to extract all info out of the updated URL before we
    // try to write anything back into it.
    //
    // i.e. without _dontUpdateUrl we'd overwrite the new path with the old
    // one when we set this.hash. Likewise for query.
    this._dontUpdateUrl = true;
    this._hashChanged();
    this.path = window.location.pathname;
    this.query = window.location.search.substring(1);
    this._dontUpdateUrl = false;
    this._updateUrl();
  },
  _urlChangedNew: function () {
    /*TG #2329*/ /*end*/
    // We want to extract all info out of the updated URL before we
    // try to write anything back into it.
    //
    // i.e. without _dontUpdateUrl we'd overwrite the new path with the old
    // one when we set this.hash. Likewise for query.
    this._dontUpdateUrl = true;
    this._hashChanged();
    this.path = window.decodeURIComponent(this.__location.pathname);
    this.query = this.__location.search.substring(1);
    this._dontUpdateUrl = false;
    this._updateUrl();
  },

  _getUrl: function() {
    /*TG #2329*/
    if (this.noDecode) {
      return this._getUrlOld();
    } else {
      return this._getUrlNew();
    }
  },
  _getUrlOld: function() {
    var url = this.path;
    var query = this.query;
    if (query) {
      url += '?' + query;
    }
    if (this.hash) {
      url += '#' + this.hash;
    }
    return url;
  },
  _getUrlNew: function () {
    /*TG #2329*/ /*end*/
    var partiallyEncodedPath = window.encodeURI(this.path).replace(/\#/g, '%23').replace(/\?/g, '%3F');
    var partiallyEncodedQuery = '';
    if (this.query) {
      partiallyEncodedQuery = '?' + this.query.replace(/\#/g, '%23');
      if (this.encodeSpaceAsPlusInQuery) {
        partiallyEncodedQuery = partiallyEncodedQuery.replace(/\+/g, '%2B')
                                    .replace(/ /g, '+')
                                    .replace(/%20/g, '+');
      } else {
        // required for edge
        partiallyEncodedQuery =
            partiallyEncodedQuery.replace(/\+/g, '%2B').replace(/ /g, '%20');
      }
    }
    var partiallyEncodedHash = '';
    if (this.hash) {
      partiallyEncodedHash = '#' + window.encodeURI(this.hash);
    }
    return (
        partiallyEncodedPath + partiallyEncodedQuery + partiallyEncodedHash);
  },

  _updateUrl: function() {
    /*TG #2329*/
    if (this.noDecode) {
      this._updateUrlOld();
    } else {
      this._updateUrlNew();
    }
  },
  _updateUrlOld: function() {
    if (this._dontUpdateUrl || !this._initialized) {
      return;
    }
    var newUrl = this._getUrl();
    var currentUrl = (
        window.location.pathname +
        window.location.search +
        window.location.hash
    );
    if (newUrl == currentUrl) {
      // nothing to do, the URL didn't change
      return;
    }
    // Need to use a full URL in case the containing page has a base URI.
    var fullNewUrl = new URL(
        newUrl, window.location.protocol + '//' + window.location.host).href;
    var now = window.performance.now();
    var shouldReplace =
        this._lastChangedAt + this.dwellTime > now;
    this._lastChangedAt = now;
    if (shouldReplace) {
      window.history.replaceState({}, '', fullNewUrl);
    } else {
      window.history.pushState({}, '', fullNewUrl);
    }
    this.fire('location-changed', {}, {node: window});
  },
  _updateUrlNew: function () {
    /*TG #2329*/ /*end*/
    if (this._dontUpdateUrl || !this._initialized) {
      return;
    }

    if (this.path === window.decodeURIComponent(this.__location.pathname) &&
        this.query === this.__location.search.substring(1) &&
        this.hash ===
            window.decodeURIComponent(this.__location.hash.substring(1))) {
      // Nothing to do, the current URL is a representation of our properties.
      return;
    }

    var newUrl = this._getUrl();
    // Need to use a full URL in case the containing page has a base URI.
    var fullNewUrl =
        new URL(newUrl, this.__location.protocol + '//' + this.__location.host)
            .href;
    var now = window.performance.now();
    var shouldReplace = this._lastChangedAt + this.dwellTime > now;
    this._lastChangedAt = now;

    if (shouldReplace) {
      window.history.replaceState({}, '', fullNewUrl);
    } else {
      window.history.pushState({}, '', fullNewUrl);
    }

    this.fire('location-changed', {}, {node: window});
  },

  /**
   * A necessary evil so that links work as expected. Does its best to
   * bail out early if possible.
   *
   * @param {MouseEvent} event .
   */
  _globalOnClick: function(event) {
    // If another event handler has stopped this event then there's nothing
    // for us to do. This can happen e.g. when there are multiple
    // iron-location elements in a page.
    if (event.defaultPrevented) {
      return;
    }

    var href = this._getSameOriginLinkHref(event);

    if (!href) {
      return;
    }

    event.preventDefault();

    // If the navigation is to the current page we shouldn't add a history
    // entry or fire a change event.
    if (href === this.__location.href) {
      return;
    }

    window.history.pushState({}, '', href);
    this.fire('location-changed', {}, {node: window});
  },

  /**
   * Returns the absolute URL of the link (if any) that this click event
   * is clicking on, if we can and should override the resulting full
   * page navigation. Returns null otherwise.
   *
   * @param {MouseEvent} event .
   * @return {string?} .
   */
  _getSameOriginLinkHref: function(event) {
    // We only care about left-clicks.
    if (event.button !== 0) {
      return null;
    }

    // We don't want modified clicks, where the intent is to open the page
    // in a new tab.
    if (event.metaKey || event.ctrlKey || event.shiftKey) {
      return null;
    }

    var eventPath = dom(event).path;
    var anchor = null;

    for (var i = 0; i < eventPath.length; i++) {
      var element = eventPath[i];

      if (element.tagName === 'A' && element.href) {
        anchor = element;
        break;
      }
    }

    // If there's no link there's nothing to do.
    if (!anchor) {
      return null;
    }

    // Target blank is a new tab, don't intercept.
    if (anchor.target === '_blank') {
      return null;
    }

    // If the link is for an existing parent frame, don't intercept.
    if ((anchor.target === '_top' || anchor.target === '_parent') &&
        window.top !== window) {
      return null;
    }

    // If the link is a download, don't intercept.
    if (anchor.download) {
      return null;
    }

    var href = anchor.href;

    // It only makes sense for us to intercept same-origin navigations.
    // pushState/replaceState don't work with cross-origin links.
    var url;

    if (document.baseURI != null) {
      url = new URL(href, /** @type {string} */ (document.baseURI));
    } else {
      url = new URL(href);
    }

    var origin;

    // IE Polyfill
    if (this.__location.origin) {
      origin = this.__location.origin;
    } else {
      origin = this.__location.protocol + '//' + this.__location.host;
    }

    var urlOrigin;

    if (url.origin) {
      urlOrigin = url.origin;
    } else {
      // IE always adds port number on HTTP and HTTPS on <a>.host but not on
      // window.location.host
      var urlHost = url.host;
      var urlPort = url.port;
      var urlProtocol = url.protocol;
      var isExtraneousHTTPS = urlProtocol === 'https:' && urlPort === '443';
      var isExtraneousHTTP = urlProtocol === 'http:' && urlPort === '80';

      if (isExtraneousHTTPS || isExtraneousHTTP) {
        urlHost = url.hostname;
      }
      urlOrigin = urlProtocol + '//' + urlHost;
    }

    if (urlOrigin !== origin) {
      return null;
    }

    var normalizedHref = url.pathname + url.search + url.hash;

    // pathname should start with '/', but may not if `new URL` is not supported
    if (normalizedHref[0] !== '/') {
      normalizedHref = '/' + normalizedHref;
    }

    // If we've been configured not to handle this url... don't handle it!
    if (this._urlSpaceRegExp && !this._urlSpaceRegExp.test(normalizedHref)) {
      return null;
    }

    // Need to use a full URL in case the containing page has a base URI.
    var fullNormalizedHref = new URL(normalizedHref, this.__location.href).href;
    return fullNormalizedHref;
  },

  _makeRegExp: function(urlSpaceRegex) {
    return RegExp(urlSpaceRegex);
  }
});

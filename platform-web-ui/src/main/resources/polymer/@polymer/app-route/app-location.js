import '../polymer/polymer-legacy.js';
import '../iron-location/iron-location.js';
import '../iron-location/iron-query-params.js';
import { Polymer } from '../polymer/lib/legacy/polymer-fn.js';
import { html } from '../polymer/lib/utils/html-tag.js';
import { AppRouteConverterBehavior } from './app-route-converter-behavior.js';

/**
@license
Copyright (c) 2016 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at
http://polymer.github.io/LICENSE.txt The complete set of authors may be found at
http://polymer.github.io/AUTHORS.txt The complete set of contributors may be
found at http://polymer.github.io/CONTRIBUTORS.txt Code distributed by Google as
part of the polymer project is also subject to an additional IP rights grant
found at http://polymer.github.io/PATENTS.txt
*/

/**
`app-location` is an element that provides synchronization between the
browser location bar and the state of an app. When created, `app-location`
elements will automatically watch the global location for changes. As changes
occur, `app-location` produces and updates an object called `route`. This
`route` object is suitable for passing into a `app-route`, and other similar
elements.

An example of the public API of a route object that describes the URL
`https://elements.polymer-project.org/elements/app-location`:

    {
      prefix: '',
      path: '/elements/app-location'
    }

Example Usage:

    <app-location route="{{route}}"></app-location>
    <app-route route="{{route}}" pattern="/:page" data="{{data}}"></app-route>

As you can see above, the `app-location` element produces a `route` and that
property is then bound into the `app-route` element. The bindings are two-
directional, so when changes to the `route` object occur within `app-route`,
they automatically reflect back to the global location.

### Hashes vs Paths

By default `app-location` routes using the pathname portion of the URL. This has
broad browser support but it does require cooperation of the backend server. An
`app-location` can be configured to use the hash part of a URL instead using
the `use-hash-as-path` attribute, like so:

    <app-location route="{{route}}" use-hash-as-path></app-location>

### Integrating with other routing code

There is no standard event that is fired when window.location is modified.
`app-location` fires a `location-changed` event on `window` when it updates the
location. It also listens for that same event, and re-reads the URL when it's
fired. This makes it very easy to interop with other routing code.

So for example if you want to navigate to `/new_path` imperatively you could
call `window.location.pushState` or `window.location.replaceState` followed by
firing a `location-changed` event on `window`. i.e.

    window.history.pushState({}, null, '/new_path');
    window.dispatchEvent(new CustomEvent('location-changed'));

@element app-location
@demo demo/index.html
*/
Polymer({
  /*TG #2329*/ /* no-decode="[[noDecode]]" */
  _template: html`
    <iron-query-params params-string="{{__query}}" params-object="{{queryParams}}">
    </iron-query-params>
    <iron-location no-decode="[[noDecode]]" path="{{__path}}" query="{{__query}}" hash="{{__hash}}" url-space-regex="[[urlSpaceRegex]]" dwell-time="[[dwellTime]]">
    </iron-location>
  `,

  is: 'app-location',

  properties: {
    /*TG #2329*/
    noDecode: {
      type: Boolean,
      value: false 
    },
    /**
     * A model representing the deserialized path through the route tree, as
     * well as the current queryParams.
     */
    route: {
      type: Object,
      notify: true,
    },

    /**
     * In many scenarios, it is convenient to treat the `hash` as a stand-in
     * alternative to the `path`. For example, if deploying an app to a static
     * web server (e.g., Github Pages) - where one does not have control over
     * server-side routing - it is usually a better experience to use the hash
     * to represent paths through one's app.
     *
     * When this property is set to true, the `hash` will be used in place of

     * the `path` for generating a `route`.
     */
    useHashAsPath: {
      type: Boolean,
      value: false,
    },

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
    urlSpaceRegex: {
      type: String,
      notify: true,
    },

    /**
     * A set of key/value pairs that are universally accessible to branches
     * of the route tree.
     */
    __queryParams: {
      type: Object,
    },

    /**
     * The pathname component of the current URL.
     */
    __path: {
      type: String,
    },

    /**
     * The query string portion of the current URL.
     */
    __query: {
      type: String,
    },

    /**
     * The hash portion of the current URL.
     */
    __hash: {
      type: String,
    },

    /**
     * The route path, which will be either the hash or the path, depending
     * on useHashAsPath.
     */
    path: {
      type: String,
      observer: '__onPathChanged',
    },

    /**
     * Whether or not the ready function has been called.
     */
    _isReady: {
      type: Boolean,
    },

    /**
     * If the user was on a URL for less than `dwellTime` milliseconds, it
     * won't be added to the browser's history, but instead will be
     * replaced by the next entry.
     *
     * This is to prevent large numbers of entries from clogging up the
     * user's browser history. Disable by setting to a negative number.
     *
     * See `iron-location` for more information.
     */
    dwellTime: {
      type: Number,
    }
  },

  behaviors: [AppRouteConverterBehavior],
  observers: ['__computeRoutePath(useHashAsPath, __hash, __path)'],

  ready: function() {
    this._isReady = true;
  },

  __computeRoutePath: function() {
    this.path = this.useHashAsPath ? this.__hash : this.__path;
  },

  __onPathChanged: function() {
    if (!this._isReady) {
      return;
    }

    if (this.useHashAsPath) {
      this.__hash = this.path;
    } else {
      this.__path = this.path;
    }
  }
});

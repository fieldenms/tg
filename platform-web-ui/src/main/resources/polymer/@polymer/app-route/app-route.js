import '../polymer/polymer-legacy.js';
import { Polymer } from '../polymer/lib/legacy/polymer-fn.js';

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
`app-route` is an element that enables declarative, self-describing routing
for a web app.

In its typical usage, a `app-route` element consumes an object that describes
some state about the current route, via the `route` property. It then parses
that state using the `pattern` property, and produces two artifacts: some `data`
related to the `route`, and a `tail` that contains the rest of the `route` that
did not match.

Here is a basic example, when used with `app-location`:

    <app-location route="{{route}}"></app-location>
    <app-route
        route="{{route}}"
        pattern="/:page"
        data="{{data}}"
        tail="{{tail}}">
    </app-route>

In the above example, the `app-location` produces a `route` value. Then, the
`route.path` property is matched by comparing it to the `pattern` property. If
the `pattern` property matches `route.path`, the `app-route` will set or update
its `data` property with an object whose properties correspond to the parameters
in `pattern`. So, in the above example, if `route.path` was `'/about'`, the
value of `data` would be `{"page": "about"}`.

The `tail` property represents the remaining part of the route state after the
`pattern` has been applied to a matching `route`.

Here is another example, where `tail` is used:

    <app-location route="{{route}}"></app-location>
    <app-route
        route="{{route}}"
        pattern="/:page"
        data="{{routeData}}"
        tail="{{subroute}}">
    </app-route>
    <app-route
        route="{{subroute}}"
        pattern="/:id"
        data="{{subrouteData}}">
    </app-route>

In the above example, there are two `app-route` elements. The first
`app-route` consumes a `route`. When the `route` is matched, the first
`app-route` also produces `routeData` from its `data`, and `subroute` from
its `tail`. The second `app-route` consumes the `subroute`, and when it
matches, it produces an object called `subrouteData` from its `data`.

So, when `route.path` is `'/about'`, the `routeData` object will look like
this: `{ page: 'about' }`

And `subrouteData` will be null. However, if `route.path` changes to
`'/article/123'`, the `routeData` object will look like this:
`{ page: 'article' }`

And the `subrouteData` will look like this: `{ id: '123' }`

`app-route` is responsive to bi-directional changes to the `data` objects
they produce. So, if `routeData.page` changed from `'article'` to `'about'`,
the `app-route` will update `route.path`. This in-turn will update the
`app-location`, and cause the global location bar to change its value.

@element app-route
@demo demo/index.html
@demo demo/data-loading-demo.html
@demo demo/simple-demo.html
*/
Polymer({
  is: 'app-route',

  properties: {
    /**
     * The URL component managed by this element.
     */
    route: {
      type: Object,
      notify: true,
    },

    /**
     * The pattern of slash-separated segments to match `route.path` against.
     *
     * For example the pattern "/foo" will match "/foo" or "/foo/bar"
     * but not "/foobar".
     *
     * Path segments like `/:named` are mapped to properties on the `data`
     * object.
     */
    pattern: {
      type: String,
    },

    /**
     * The parameterized values that are extracted from the route as
     * described by `pattern`.
     */
    data: {
      type: Object,
      value: function() {
        return {};
      },
      notify: true,
    },

    /**
     * Auto activate route if path empty
     */
    autoActivate: {
      type: Boolean,
      value: false,
    },

    _queryParamsUpdating: {
      type: Boolean,
      value: false,
    },

    /**
     * @type {?Object}
     */
    queryParams: {
      type: Object,
      value: function() {
        return {};
      },
      notify: true,
    },

    /**
     * The part of `route.path` NOT consumed by `pattern`.
     */
    tail: {
      type: Object,
      value: function() {
        return {
          path: null,
          prefix: null,
          __queryParams: null,
        };
      },
      notify: true,
    },

    /**
     * Whether the current route is active. True if `route.path` matches the
     * `pattern`, false otherwise.
     */
    active: {
      type: Boolean,
      notify: true,
      readOnly: true,
    },

    /**
     * @type {?string}
     */
    _matched: {
      type: String,
      value: '',
    }
  },

  observers: [
    '__tryToMatch(route.path, pattern)',
    '__updatePathOnDataChange(data.*)',
    '__tailPathChanged(tail.path)',
    '__routeQueryParamsChanged(route.__queryParams)',
    '__tailQueryParamsChanged(tail.__queryParams)',
    '__queryParamsChanged(queryParams.*)'
  ],

  created: function() {
    this.linkPaths('route.__queryParams', 'tail.__queryParams');
    this.linkPaths('tail.__queryParams', 'route.__queryParams');
  },

  /**
   * Deal with the query params object being assigned to wholesale.
   */
  __routeQueryParamsChanged: function(queryParams) {
    if (queryParams && this.tail) {
      if (this.tail.__queryParams !== queryParams) {
        this.set('tail.__queryParams', queryParams);
      }

      if (!this.active || this._queryParamsUpdating) {
        return;
      }

      // Copy queryParams and track whether there are any differences compared
      // to the existing query params.
      var copyOfQueryParams = {};
      var anythingChanged = false;
      for (var key in queryParams) {
        copyOfQueryParams[key] = queryParams[key];
        if (anythingChanged || !this.queryParams ||
            queryParams[key] !== this.queryParams[key]) {
          anythingChanged = true;
        }
      }
      // Need to check whether any keys were deleted
      for (var key in this.queryParams) {
        if (anythingChanged || !(key in queryParams)) {
          anythingChanged = true;
          break;
        }
      }

      if (!anythingChanged) {
        return;
      }
      this._queryParamsUpdating = true;
      this.set('queryParams', copyOfQueryParams);
      this._queryParamsUpdating = false;
    }
  },

  __tailQueryParamsChanged: function(queryParams) {
    if (queryParams && this.route && this.route.__queryParams != queryParams) {
      this.set('route.__queryParams', queryParams);
    }
  },

  __queryParamsChanged: function(changes) {
    if (!this.active || this._queryParamsUpdating) {
      return;
    }

    this.set('route.__' + changes.path, changes.value);
  },

  __resetProperties: function() {
    this._setActive(false);
    this._matched = null;
  },

  __tryToMatch: function() {
    if (!this.route) {
      return;
    }

    var path = this.route.path;
    var pattern = this.pattern;

    if (this.autoActivate && path === '') {
      path = '/';
    }

    if (!pattern) {
      return;
    }

    if (!path) {
      this.__resetProperties();
      return;
    }

    var remainingPieces = path.split('/');
    var patternPieces = pattern.split('/');

    var matched = [];
    var namedMatches = {};

    for (var i = 0; i < patternPieces.length; i++) {
      var patternPiece = patternPieces[i];
      if (!patternPiece && patternPiece !== '') {
        break;
      }
      var pathPiece = remainingPieces.shift();

      // We don't match this path.
      if (!pathPiece && pathPiece !== '') {
        this.__resetProperties();
        return;
      }
      matched.push(pathPiece);

      if (patternPiece.charAt(0) == ':') {
        namedMatches[patternPiece.slice(1)] = pathPiece;
      } else if (patternPiece !== pathPiece) {
        this.__resetProperties();
        return;
      }
    }

    this._matched = matched.join('/');

    // Properties that must be updated atomically.
    var propertyUpdates = {};

    // this.active
    if (!this.active) {
      propertyUpdates.active = true;
    }

    // this.tail
    var tailPrefix = this.route.prefix + this._matched;
    var tailPath = remainingPieces.join('/');
    if (remainingPieces.length > 0) {
      tailPath = '/' + tailPath;
    }
    if (!this.tail || this.tail.prefix !== tailPrefix ||
        this.tail.path !== tailPath) {
      propertyUpdates.tail = {
        prefix: tailPrefix,
        path: tailPath,
        __queryParams: this.route.__queryParams
      };
    }

    // this.data
    propertyUpdates.data = namedMatches;
    this._dataInUrl = {};
    for (var key in namedMatches) {
      this._dataInUrl[key] = namedMatches[key];
    }

    if (this.setProperties) {
      // atomic update
      this.setProperties(propertyUpdates, true);
    } else {
      this.__setMulti(propertyUpdates);
    }
  },

  __tailPathChanged: function(path) {
    if (!this.active) {
      return;
    }
    var tailPath = path;
    var newPath = this._matched;
    if (tailPath) {
      if (tailPath.charAt(0) !== '/') {
        tailPath = '/' + tailPath;
      }
      newPath += tailPath;
    }
    this.set('route.path', newPath);
  },

  __updatePathOnDataChange: function() {
    if (!this.route || !this.active) {
      return;
    }
    var newPath = this.__getLink({});
    var oldPath = this.__getLink(this._dataInUrl);
    if (newPath === oldPath) {
      return;
    }
    this.set('route.path', newPath);
  },

  __getLink: function(overrideValues) {
    var values = {tail: null};
    for (var key in this.data) {
      values[key] = this.data[key];
    }
    for (var key in overrideValues) {
      values[key] = overrideValues[key];
    }
    var patternPieces = this.pattern.split('/');
    var interp = patternPieces.map(function(value) {
      if (value[0] == ':') {
        value = values[value.slice(1)];
      }
      return value;
    }, this);
    if (values.tail && values.tail.path) {
      if (interp.length > 0 && values.tail.path.charAt(0) === '/') {
        interp.push(values.tail.path.slice(1));
      } else {
        interp.push(values.tail.path);
      }
    }
    return interp.join('/');
  },

  __setMulti: function(setObj) {
    // HACK(rictic): skirting around 1.0's lack of a setMulti by poking at
    //     internal data structures. I would not advise that you copy this
    //     example.
    //
    //     In the future this will be a feature of Polymer itself.
    //     See: https://github.com/Polymer/polymer/issues/3640
    //
    //     Hacking around with private methods like this is juggling footguns,
    //     and is likely to have unexpected and unsupported rough edges.
    //
    //     Be ye so warned.
    for (var property in setObj) {
      this._propertySetter(property, setObj[property]);
    }
    // notify in a specific order
    if (setObj.data !== undefined) {
      this._pathEffector('data', this.data);
      this._notifyChange('data');
    }
    if (setObj.active !== undefined) {
      this._pathEffector('active', this.active);
      this._notifyChange('active');
    }
    if (setObj.tail !== undefined) {
      this._pathEffector('tail', this.tail);
      this._notifyChange('tail');
    }
  }
});

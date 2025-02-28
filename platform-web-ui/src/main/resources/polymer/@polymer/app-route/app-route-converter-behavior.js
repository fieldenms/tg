import '../polymer/polymer-legacy.js';

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
 * Provides bidirectional mapping between `path` and `queryParams` and a
 * app-route compatible `route` object.
 *
 * For more information, see the docs for `app-route-converter`.
 *
 * @polymerBehavior
 */
const AppRouteConverterBehavior = {
  properties: {
    /**
     * A model representing the deserialized path through the route tree, as
     * well as the current queryParams.
     *
     * A route object is the kernel of the routing system. It is intended to
     * be fed into consuming elements such as `app-route`.
     *
     * @type {?Object|undefined}
     */
    route: {
      type: Object,
      notify: true,
    },

    /**
     * A set of key/value pairs that are universally accessible to branches of
     * the route tree.
     *
     * @type {?Object}
     */
    queryParams: {
      type: Object,
      notify: true,
    },

    /**
     * The serialized path through the route tree. This corresponds to the
     * `window.location.pathname` value, and will update to reflect changes
     * to that value.
     */
    path: {
      type: String,
      notify: true,
    }
  },

  observers: [
    '_locationChanged(path, queryParams)',
    '_routeChanged(route.prefix, route.path)',
    '_routeQueryParamsChanged(route.__queryParams)'
  ],

  created: function() {
    this.linkPaths('route.__queryParams', 'queryParams');
    this.linkPaths('queryParams', 'route.__queryParams');
  },

  /**
   * Handler called when the path or queryParams change.
   */
  _locationChanged: function() {
    if (this.route && this.route.path === this.path &&
        this.queryParams === this.route.__queryParams) {
      return;
    }
    this.route = {prefix: '', path: this.path, __queryParams: this.queryParams};
  },

  /**
   * Handler called when the route prefix and route path change.
   */
  _routeChanged: function() {
    if (!this.route) {
      return;
    }

    this.path = this.route.prefix + this.route.path;
  },

  /**
   * Handler called when the route queryParams change.
   *
   * @param  {Object} queryParams A set of key/value pairs that are
   * universally accessible to branches of the route tree.
   */
  _routeQueryParamsChanged: function(queryParams) {
    if (!this.route) {
      return;
    }
    this.queryParams = queryParams;
  }
};

export { AppRouteConverterBehavior };

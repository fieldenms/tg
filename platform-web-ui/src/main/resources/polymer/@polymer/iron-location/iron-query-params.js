import '../polymer/polymer-legacy.js';
import { Polymer } from '../polymer/lib/legacy/polymer-fn.js';

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
 * @demo demo/iron-query-params.html
 */
Polymer({
  is: 'iron-query-params',
  _template: null,

  properties: {
    /**
     * @type{string|undefined}
     */
    paramsString: {
      type: String,
      notify: true,
      observer: 'paramsStringChanged',
    },

    /**
     * @type{Object|undefined}
     */
    paramsObject: {
      type: Object,
      notify: true,
    },

    _dontReact: {type: Boolean, value: false}
  },

  hostAttributes: {hidden: true},

  observers: ['paramsObjectChanged(paramsObject.*)'],

  paramsStringChanged: function() {
    this._dontReact = true;
    this.paramsObject = this._decodeParams(this.paramsString);
    this._dontReact = false;
  },

  paramsObjectChanged: function() {
    if (this._dontReact) {
      return;
    }
    this.paramsString = this._encodeParams(this.paramsObject)
                            .replace(/%3F/g, '?')
                            .replace(/%2F/g, '/')
                            .replace(/'/g, '%27');
  },

  _encodeParams: function(params) {
    var encodedParams = [];

    for (var key in params) {
      var value = params[key];

      if (value === '') {
        encodedParams.push(encodeURIComponent(key));

      } else if (value) {
        encodedParams.push(
            encodeURIComponent(key) + '=' +
            encodeURIComponent(value.toString()));
      }
    }
    return encodedParams.join('&');
  },

  _decodeParams: function(paramString) {
    var params = {};
    // Work around a bug in decodeURIComponent where + is not
    // converted to spaces:
    paramString = (paramString || '').replace(/\+/g, '%20');
    var paramList = paramString.split('&');
    for (var i = 0; i < paramList.length; i++) {
      var param = paramList[i].split('=');
      if (param[0]) {
        params[decodeURIComponent(param[0])] =
            decodeURIComponent(param[1] || '');
      }
    }
    return params;
  }
});

"use strict";
/**
 * @license
 * Copyright (c) 2018 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt The complete set of authors may be found
 * at http://polymer.github.io/AUTHORS.txt The complete set of contributors may
 * be found at http://polymer.github.io/CONTRIBUTORS.txt Code distributed by
 * Google as part of the polymer project is also subject to an additional IP
 * rights grant found at http://polymer.github.io/PATENTS.txt
 */
Object.defineProperty(exports, "__esModule", { value: true });
var config = require("./config.js");
/**
 * @param {function()} callback A function to call when the active web component
 *     frameworks have loaded.
 */
function whenFrameworksReady(callback) {
    debug('whenFrameworksReady');
    function done() {
        debug('whenFrameworksReady done');
        callback();
    }
    // If webcomponents script is in the document, wait for WebComponentsReady.
    if (window.WebComponents && !window.WebComponents.ready) {
        debug('WebComponentsReady?');
        window.addEventListener('WebComponentsReady', function wcReady() {
            window.removeEventListener('WebComponentsReady', wcReady);
            debug('WebComponentsReady');
            done();
        });
    }
    else {
        done();
    }
}
exports.whenFrameworksReady = whenFrameworksReady;
/**
 * @return {string} '<count> <kind> tests' or '<count> <kind> test'.
 */
function pluralizedStat(count, kind) {
    if (count === 1) {
        return count + ' ' + kind + ' test';
    }
    else {
        return count + ' ' + kind + ' tests';
    }
}
exports.pluralizedStat = pluralizedStat;
/**
 * @param {string} path The URI of the script to load.
 * @param {function} done
 */
function loadScript(path, done) {
    var script = document.createElement('script');
    script.src = path;
    if (done) {
        script.onload = done.bind(null, null);
        script.onerror = done.bind(null, 'Failed to load script ' + script.src);
    }
    document.head.appendChild(script);
}
exports.loadScript = loadScript;
/**
 * @param {string} path The URI of the stylesheet to load.
 * @param {function} done
 */
function loadStyle(path, done) {
    var link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = path;
    if (done) {
        link.onload = done.bind(null, null);
        link.onerror = done.bind(null, 'Failed to load stylesheet ' + link.href);
    }
    document.head.appendChild(link);
}
exports.loadStyle = loadStyle;
/**
 * @param {...*} var_args Logs values to the console when the `debug`
 *     configuration option is true.
 */
function debug() {
    var var_args = [];
    for (var _i = 0; _i < arguments.length; _i++) {
        var_args[_i] = arguments[_i];
    }
    if (!config.get('verbose')) {
        return;
    }
    var args = [window.location.pathname].concat(var_args);
    (console.debug || console.log).apply(console, args);
}
exports.debug = debug;
// URL Processing
/**
 * @param {string} url
 * @return {{base: string, params: string}}
 */
function parseUrl(url) {
    var parts = url.match(/^(.*?)(?:\?(.*))?$/);
    return {
        base: parts[1],
        params: getParams(parts[2] || ''),
    };
}
exports.parseUrl = parseUrl;
/**
 * Expands a URL that may or may not be relative to `base`.
 *
 * @param {string} url
 * @param {string} base
 * @return {string}
 */
function expandUrl(url, base) {
    if (!base) {
        return url;
    }
    if (url.match(/^(\/|https?:\/\/)/)) {
        return url;
    }
    if (base.substr(base.length - 1) !== '/') {
        base = base + '/';
    }
    return base + url;
}
exports.expandUrl = expandUrl;
/**
 * @param {string=} opt_query A query string to parse.
 * @return {!Object<string, !Array<string>>} All params on the URL's query.
 */
function getParams(query) {
    query = typeof query === 'string' ? query : window.location.search;
    if (query.substring(0, 1) === '?') {
        query = query.substring(1);
    }
    // python's SimpleHTTPServer tacks a `/` on the end of query strings :(
    if (query.slice(-1) === '/') {
        query = query.substring(0, query.length - 1);
    }
    if (query === '') {
        return {};
    }
    var result = {};
    query.split('&').forEach(function (part) {
        var pair = part.split('=');
        if (pair.length !== 2) {
            console.warn('Invalid URL query part:', part);
            return;
        }
        var key = decodeURIComponent(pair[0]);
        var value = decodeURIComponent(pair[1]);
        if (!result[key]) {
            result[key] = [];
        }
        result[key].push(value);
    });
    return result;
}
exports.getParams = getParams;
/**
 * Merges params from `source` into `target` (mutating `target`).
 *
 * @param {!Object<string, !Array<string>>} target
 * @param {!Object<string, !Array<string>>} source
 */
function mergeParams(target, source) {
    Object.keys(source).forEach(function (key) {
        if (!(key in target)) {
            target[key] = [];
        }
        target[key] = target[key].concat(source[key]);
    });
}
exports.mergeParams = mergeParams;
/**
 * @param {string} param The param to return a value for.
 * @return {?string} The first value for `param`, if found.
 */
function getParam(param) {
    var params = getParams();
    return params[param] ? params[param][0] : null;
}
exports.getParam = getParam;
/**
 * @param {!Object<string, !Array<string>>} params
 * @return {string} `params` encoded as a URI query.
 */
function paramsToQuery(params) {
    var pairs = [];
    Object.keys(params).forEach(function (key) {
        params[key].forEach(function (value) {
            pairs.push(encodeURIComponent(key) + '=' + encodeURIComponent(value));
        });
    });
    return (pairs.length > 0) ? ('?' + pairs.join('&')) : '';
}
exports.paramsToQuery = paramsToQuery;
function getPathName(location) {
    return typeof location === 'string' ? location : location.pathname;
}
function basePath(location) {
    return getPathName(location).match(/^.*\//)[0];
}
exports.basePath = basePath;
function relativeLocation(location, basePath) {
    var path = getPathName(location);
    if (path.indexOf(basePath) === 0) {
        path = path.substring(basePath.length);
    }
    return path;
}
exports.relativeLocation = relativeLocation;
function cleanLocation(location) {
    var path = getPathName(location);
    if (path.slice(-11) === '/index.html') {
        path = path.slice(0, path.length - 10);
    }
    return path;
}
exports.cleanLocation = cleanLocation;
function parallel(runners, maybeLimit, done) {
    var limit;
    if (typeof maybeLimit !== 'number') {
        done = maybeLimit;
        limit = 0;
    }
    else {
        limit = maybeLimit;
    }
    if (!runners.length) {
        return done && done();
    }
    var called = false;
    var total = runners.length;
    var numActive = 0;
    var numDone = 0;
    function runnerDone(error) {
        if (called) {
            return;
        }
        numDone = numDone + 1;
        numActive = numActive - 1;
        if (error || numDone >= total) {
            called = true;
            done && done(error);
        }
        else {
            runOne();
        }
    }
    function runOne() {
        if (limit && numActive >= limit) {
            return;
        }
        if (!runners.length) {
            return;
        }
        numActive = numActive + 1;
        runners.shift()(runnerDone);
    }
    runners.forEach(runOne);
}
exports.parallel = parallel;
/**
 * Finds the directory that a loaded script is hosted on.
 *
 * @param {string} filename
 * @return {string?}
 */
function scriptPrefix(filename) {
    var scripts = document.querySelectorAll('script[src*="/' + filename + '"]');
    if (scripts.length !== 1) {
        return null;
    }
    var script = scripts[0].src;
    return script.substring(0, script.indexOf(filename));
}
exports.scriptPrefix = scriptPrefix;
//# sourceMappingURL=util.js.map
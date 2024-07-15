"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
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
var util = require("./util.js");
/**
 * A Mocha suite (or suites) run within a child iframe, but reported as if they
 * are part of the current context.
 */
var ChildRunner = /** @class */ (function () {
    function ChildRunner(url, parentScope) {
        this.eventListenersToRemoveOnClean = [];
        this.onRunComplete = null;
        this.share = null;
        this.parentScope = parentScope;
        var urlBits = util.parseUrl(url);
        util.mergeParams(urlBits.params, util.getParams(parentScope.location.search));
        delete urlBits.params.cli_browser_id;
        this.url = "" + urlBits.base + util.paramsToQuery(urlBits.params);
        this.state = 'initializing';
    }
    /**
     * Listeners added using this method will be removed on done()
     *
     * @param type event type
     * @param listener object which receives a notification
     * @param target event target
     */
    ChildRunner.prototype.addEventListener = function (type, listener, target) {
        target.addEventListener(type, listener);
        var descriptor = { target: target, type: type, listener: listener };
        this.eventListenersToRemoveOnClean.push(descriptor);
    };
    /**
     * Removes all event listeners added by a method addEventListener defined
     * on an instance of ChildRunner.
     */
    ChildRunner.prototype.removeAllEventListeners = function () {
        this.eventListenersToRemoveOnClean.forEach(function (_a) {
            var target = _a.target, type = _a.type, listener = _a.listener;
            return target.removeEventListener(type, listener);
        });
    };
    /**
     * @return {ChildRunner} The `ChildRunner` that was registered for this
     * window.
     */
    ChildRunner.current = function () {
        return ChildRunner.get(window);
    };
    /**
     * @param {!Window} target A window to find the ChildRunner of.
     * @param {boolean} traversal Whether this is a traversal from a child window.
     * @return {ChildRunner} The `ChildRunner` that was registered for `target`.
     */
    ChildRunner.get = function (target, traversal) {
        var childRunner = ChildRunner.byUrl[target.location.href];
        if (childRunner) {
            return childRunner;
        }
        if (window.parent === window) {
            // Top window.
            if (traversal) {
                console.warn('Subsuite loaded but was never registered. This most likely is due to wonky history behavior. Reloading...');
                window.location.reload();
            }
            return null;
        }
        // Otherwise, traverse.
        return window.parent.WCT._ChildRunner.get(target, true);
    };
    /**
     * Loads and runs the subsuite.
     *
     * @param {function} done Node-style callback.
     */
    ChildRunner.prototype.run = function (done) {
        var _this = this;
        util.debug('ChildRunner#run', this.url);
        this.state = 'loading';
        this.onRunComplete = done;
        this.container = document.getElementById('subsuites');
        if (!this.container) {
            var container_1 = (this.container = document.createElement('div'));
            container_1.id = 'subsuites';
            document.body.appendChild(container_1);
        }
        var container = this.container;
        var iframe = (this.iframe = document.createElement('iframe'));
        iframe.classList.add('subsuite');
        iframe.src = this.url;
        // Let the iframe expand the URL for us.
        var url = (this.url = iframe.src);
        container.appendChild(iframe);
        ChildRunner.byUrl[url] = this;
        this.timeoutId = window.setTimeout(function () { return _this.loaded(new Error('Timed out loading ' + url)); }, ChildRunner.loadTimeout);
        this.addEventListener('error', function () { return _this.loaded(new Error('Failed to load document ' + _this.url)); }, iframe);
        this.addEventListener('DOMContentLoaded', function () { return _this.loaded(); }, iframe.contentWindow);
    };
    /**
     * Called when the sub suite's iframe has loaded (or errored during load).
     *
     * @param {*} error The error that occured, if any.
     */
    ChildRunner.prototype.loaded = function (error) {
        util.debug('ChildRunner#loaded', this.url, error);
        if (!this.iframe || this.iframe.contentWindow == null && error) {
            this.signalRunComplete(error);
            this.done();
            return;
        }
        // Not all targets have WCT loaded (compatiblity mode)
        if (this.iframe.contentWindow.WCT) {
            this.share = this.iframe.contentWindow.WCT.share;
        }
        if (error) {
            this.signalRunComplete(error);
            this.done();
        }
    };
    /**
     * Called in mocha/run.js when all dependencies have loaded, and the child is
     * ready to start running tests
     *
     * @param {*} error The error that occured, if any.
     */
    ChildRunner.prototype.ready = function (error) {
        util.debug('ChildRunner#ready', this.url, error);
        if (this.timeoutId) {
            clearTimeout(this.timeoutId);
        }
        if (error) {
            this.signalRunComplete(error);
            this.done();
        }
    };
    /**
     * Called when the sub suite's tests are complete, so that it can clean up.
     */
    ChildRunner.prototype.done = function () {
        var _this = this;
        util.debug('ChildRunner#done', this.url, arguments);
        // Make sure to clear that timeout.
        this.ready();
        this.signalRunComplete();
        if (this.iframe) {
            // Be safe and avoid potential browser crashes when logic attempts to
            // interact with the removed iframe.
            setTimeout(function () {
                _this.removeAllEventListeners();
                _this.container.removeChild(_this.iframe);
                _this.iframe = undefined;
                _this.share = null;
            }, 0);
        }
    };
    ChildRunner.prototype.signalRunComplete = function (error) {
        if (this.onRunComplete) {
            this.state = 'complete';
            this.onRunComplete(error);
            this.onRunComplete = null;
        }
    };
    // ChildRunners get a pretty generous load timeout by default.
    ChildRunner.loadTimeout = 60000;
    // We can't maintain properties on iframe elements in Firefox/Safari/???, so
    // we track childRunners by URL.
    ChildRunner.byUrl = {};
    return ChildRunner;
}());
exports.default = ChildRunner;
//# sourceMappingURL=childrunner.js.map
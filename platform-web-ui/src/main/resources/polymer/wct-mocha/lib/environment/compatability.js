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
var childrunner_js_1 = require("../childrunner.js");
// polymer-test-tools (and Polymer/tools) support HTML tests where each file is
// expected to call `done()`, which posts a message to the parent window.
window.addEventListener('message', function (event) {
    if (!event.data || (event.data !== 'ok' && !event.data.error)) {
        return;
    }
    var eventSourceWindow = event.source;
    var childRunner = childrunner_js_1.default.get(eventSourceWindow);
    if (!childRunner) {
        return;
    }
    childRunner.ready();
    // The name of the suite as exposed to the user.
    var reporter = childRunner.parentScope.WCT._reporter;
    var title = reporter.suiteTitle(eventSourceWindow.location);
    reporter.emitOutOfBandTest('page-wide tests via global done()', event.data.error, title, true);
    childRunner.done();
});
// Attempt to ensure that we complete a test suite if it is interrupted by a
// document unload.
window.addEventListener('unload', function (_event) {
    // Mocha's hook queue is asynchronous; but we want synchronous behavior if
    // we've gotten to the point of unloading the document.
    Mocha.Runner['immediately'] = function (callback) {
        callback();
    };
});
//# sourceMappingURL=compatability.js.map
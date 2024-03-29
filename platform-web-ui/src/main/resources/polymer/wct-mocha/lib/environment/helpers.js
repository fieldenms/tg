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
// Make sure that we use native timers, in case they're being stubbed out.
var nativeSetInterval = window.setInterval;
var nativeSetTimeout = window.setTimeout;
var nativeRequestAnimationFrame = window.requestAnimationFrame;
/**
 * Runs `stepFn`, catching any error and passing it to `callback` (Node-style).
 * Otherwise, calls `callback` with no arguments on success.
 *
 * @param {function()} callback
 * @param {function()} stepFn
 */
function safeStep(callback, stepFn) {
    var err;
    try {
        stepFn();
    }
    catch (error) {
        err = error;
    }
    callback(err);
}
exports.safeStep = safeStep;
/**
 * Runs your test at declaration time (before Mocha has begun tests). Handy for
 * when you need to test document initialization.
 *
 * Be aware that any errors thrown asynchronously cannot be tied to your test.
 * You may want to catch them and pass them to the done event, instead. See
 * `safeStep`.
 *
 * @param {string} name The name of the test.
 * @param {function(?function())} testFn The test function. If an argument is
 *     accepted, the test will be treated as async, just like Mocha tests.
 */
function testImmediate(name, testFn) {
    if (testFn.length > 0) {
        return testImmediateAsync(name, testFn);
    }
    var err;
    try {
        testFn();
    }
    catch (error) {
        err = error;
    }
    test(name, function (done) {
        done(err);
    });
}
exports.testImmediate = testImmediate;
/**
 * An async-only variant of `testImmediate`.
 *
 * @param {string} name
 * @param {function(?function())} testFn
 */
function testImmediateAsync(name, testFn) {
    var testComplete = false;
    var err;
    test(name, function (done) {
        var intervalId = nativeSetInterval(function () {
            if (!testComplete) {
                return;
            }
            clearInterval(intervalId);
            done(err);
        }, 10);
    });
    try {
        testFn(function (error) {
            if (error) {
                err = error;
                testComplete = true;
            }
        });
    }
    catch (error) {
        err = error;
        testComplete = true;
    }
}
exports.testImmediateAsync = testImmediateAsync;
/**
 * Triggers a flush of any pending events, observations, etc and calls you
 * back after they have been processed.
 *
 * @param {function()} callback
 */
function flush(callback) {
    // Ideally, this function would be a call to Polymer.dom.flush, but that
    // doesn't support a callback yet
    // (https://github.com/Polymer/polymer-dev/issues/851),
    // ...and there's cross-browser flakiness to deal with.
    // Make sure that we're invoking the callback with no arguments so that the
    // caller can pass Mocha callbacks, etc.
    var done = function done() {
        callback();
    };
    // Because endOfMicrotask is flaky for IE, we perform microtask checkpoints
    // ourselves (https://github.com/Polymer/polymer-dev/issues/114):
    var isIE = navigator.appName === 'Microsoft Internet Explorer';
    if (isIE && window.Platform && window.Platform.performMicrotaskCheckpoint) {
        var reallyDone_1 = done;
        done = function doneIE() {
            Platform.performMicrotaskCheckpoint();
            nativeSetTimeout(reallyDone_1, 0);
        };
    }
    // Everyone else gets a regular flush.
    var scope;
    if (window.Polymer && window.Polymer.dom && window.Polymer.dom.flush) {
        scope = window.Polymer.dom;
    }
    else if (window.Polymer && window.Polymer.flush) {
        scope = window.Polymer;
    }
    else if (window.WebComponents && window.WebComponents.flush) {
        scope = window.WebComponents;
    }
    if (scope && scope.flush) {
        scope.flush();
    }
    // Ensure that we are creating a new _task_ to allow all active microtasks
    // to finish (the code you're testing may be using endOfMicrotask, too).
    nativeSetTimeout(done, 0);
}
exports.flush = flush;
/**
 * Advances a single animation frame.
 *
 * Calls `flush`, `requestAnimationFrame`, `flush`, and `callback`
 * sequentially
 * @param {function()} callback
 */
function animationFrameFlush(callback) {
    flush(function () {
        nativeRequestAnimationFrame(function () {
            flush(callback);
        });
    });
}
exports.animationFrameFlush = animationFrameFlush;
/**
 * DEPRECATED: Use `flush`.
 * @param {function} callback
 */
function asyncPlatformFlush(callback) {
    console.warn('asyncPlatformFlush is deprecated in favor of the more terse flush()');
    return window.flush(callback);
}
exports.asyncPlatformFlush = asyncPlatformFlush;
/**
 *
 */
function waitFor(fn, next, intervalOrMutationEl, timeout, timeoutTime) {
    timeoutTime = timeoutTime || Date.now() + (timeout || 1000);
    intervalOrMutationEl = intervalOrMutationEl || 32;
    try {
        fn();
    }
    catch (e) {
        if (Date.now() > timeoutTime) {
            throw e;
        }
        else {
            if (typeof intervalOrMutationEl !== 'number') {
                intervalOrMutationEl.onMutation(intervalOrMutationEl, function () {
                    waitFor(fn, next, intervalOrMutationEl, timeout, timeoutTime);
                });
            }
            else {
                nativeSetTimeout(function () {
                    waitFor(fn, next, intervalOrMutationEl, timeout, timeoutTime);
                }, intervalOrMutationEl);
            }
            return;
        }
    }
    next();
}
exports.waitFor = waitFor;
window.safeStep = safeStep;
window.testImmediate = testImmediate;
window.testImmediateAsync = testImmediateAsync;
window.flush = flush;
window.animationFrameFlush = animationFrameFlush;
window.asyncPlatformFlush = asyncPlatformFlush;
window.waitFor = waitFor;
//# sourceMappingURL=helpers.js.map
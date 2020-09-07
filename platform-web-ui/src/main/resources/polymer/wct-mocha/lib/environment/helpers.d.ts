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
/**
 * Runs `stepFn`, catching any error and passing it to `callback` (Node-style).
 * Otherwise, calls `callback` with no arguments on success.
 *
 * @param {function()} callback
 * @param {function()} stepFn
 */
export declare function safeStep(callback: (error?: {}) => void, stepFn: () => void): void;
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
export declare function testImmediate(name: string, testFn: Function): void;
/**
 * An async-only variant of `testImmediate`.
 *
 * @param {string} name
 * @param {function(?function())} testFn
 */
export declare function testImmediateAsync(name: string, testFn: Function): void;
/**
 * Triggers a flush of any pending events, observations, etc and calls you
 * back after they have been processed.
 *
 * @param {function()} callback
 */
export declare function flush(callback: () => void): void;
/**
 * Advances a single animation frame.
 *
 * Calls `flush`, `requestAnimationFrame`, `flush`, and `callback`
 * sequentially
 * @param {function()} callback
 */
export declare function animationFrameFlush(callback: () => void): void;
/**
 * DEPRECATED: Use `flush`.
 * @param {function} callback
 */
export declare function asyncPlatformFlush(callback: () => void): void;
export interface MutationEl {
    onMutation(mutationEl: this, cb: () => void): void;
}
/**
 *
 */
export declare function waitFor(fn: () => void, next: () => void, intervalOrMutationEl: number | MutationEl, timeout: number, timeoutTime: number): void;
declare global {
    interface Window {
        safeStep: typeof safeStep;
        testImmediate: typeof testImmediate;
        testImmediateAsync: typeof testImmediateAsync;
        flush: typeof flush;
        animationFrameFlush: typeof animationFrameFlush;
        asyncPlatformFlush: typeof asyncPlatformFlush;
        waitFor: typeof waitFor;
    }
}

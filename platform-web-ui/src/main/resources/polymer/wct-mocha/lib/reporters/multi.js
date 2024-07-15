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
var normalization_js_1 = require("../stacktrace/normalization.js");
var util = require("../util.js");
var STACKY_CONFIG = {
    indent: '  ',
    locationStrip: [
        /^https?:\/\/[^\/]+/,
        /\?.*$/,
    ],
    filter: function (line) {
        return !!line.location.match(/\/web-component-tester\/[^\/]+(\?.*)?$/);
    },
};
// https://github.com/visionmedia/mocha/blob/master/lib/runner.js#L36-46
var MOCHA_EVENTS = [
    'start',
    'end',
    'suite',
    'suite end',
    'test',
    'test end',
    'hook',
    'hook end',
    'pass',
    'fail',
    'pending',
    'childRunner end'
];
// Until a suite has loaded, we assume this many tests in it.
var ESTIMATED_TESTS_PER_SUITE = 3;
/**
 * A Mocha-like reporter that combines the output of multiple Mocha suites.
 */
var MultiReporter = /** @class */ (function () {
    /**
     * @param numSuites The number of suites that will be run, in order to
     *     estimate the total number of tests that will be performed.
     * @param reporters The set of reporters that
     *     should receive the unified event stream.
     * @param parent The parent reporter, if present.
     */
    function MultiReporter(numSuites, reporters, parent) {
        var _this = this;
        this.reporters = reporters.map(function (reporter) {
            return new reporter(_this);
        });
        this.parent = parent;
        this.basePath = parent && parent.basePath || util.basePath(window.location);
        this.total = numSuites * ESTIMATED_TESTS_PER_SUITE;
        // Mocha reporters assume a stream of events, so we have to be careful to
        // only report on one runner at a time...
        this.currentRunner = null;
        // ...while we buffer events for any other active runners.
        this.pendingEvents = [];
        this.emit('start');
    }
    /**
     * @return A reporter-like "class" for each child suite
     *     that should be passed to `mocha.run`.
     */
    MultiReporter.prototype.childReporter = function () {
        var _a;
        // The reporter is used as a constructor, so we can't depend on `this` being
        // properly bound.
        var self = this;
        return _a = /** @class */ (function () {
                function ChildReporter(runner) {
                    runner.name = window.name;
                    self.bindChildRunner(runner);
                }
                return ChildReporter;
            }()),
            _a.title = window.name,
            _a;
    };
    /** Must be called once all runners have finished. */
    MultiReporter.prototype.done = function () {
        this.complete = true;
        this.flushPendingEvents();
        this.emit('end');
    };
    MultiReporter.prototype.epilogue = function () {
    };
    /**
     * Emit a top level test that is not part of any suite managed by this
     * reporter.
     *
     * Helpful for reporting on global errors, loading issues, etc.
     *
     * @param title The title of the test.
     * @param error An error associated with this test. If falsy, test is
     *     considered to be passing.
     * @param suiteTitle Title for the suite that's wrapping the test.
     * @param estimated If this test was included in the original
     *     estimate of `numSuites`.
     */
    MultiReporter.prototype.emitOutOfBandTest = function (title, error, suiteTitle, estimated) {
        util.debug('MultiReporter#emitOutOfBandTest(', arguments, ')');
        var root = new Mocha.Suite(suiteTitle || '');
        var test = new Mocha.Test(title, function () { return undefined; });
        test.parent = root;
        test.state = error ? 'failed' : 'passed';
        test.err = error;
        if (!estimated) {
            this.total = this.total + ESTIMATED_TESTS_PER_SUITE;
        }
        var runner = { total: 1 };
        this.proxyEvent('start', runner);
        this.proxyEvent('suite', runner, root);
        this.proxyEvent('test', runner, test);
        if (error) {
            this.proxyEvent('fail', runner, test, error);
        }
        else {
            this.proxyEvent('pass', runner, test);
        }
        this.proxyEvent('test end', runner, test);
        this.proxyEvent('suite end', runner, root);
        this.proxyEvent('end', runner);
    };
    /**
     * @param {!Location|string} location
     * @return {string}
     */
    MultiReporter.prototype.suiteTitle = function (location) {
        var path = util.relativeLocation(location, this.basePath);
        path = util.cleanLocation(path);
        return path;
    };
    // Internal Interface
    /** @param {!Mocha.runners.Base} runner The runner to listen to events for. */
    MultiReporter.prototype.bindChildRunner = function (runner) {
        var _this = this;
        MOCHA_EVENTS.forEach(function (eventName) {
            runner.on(eventName, _this.proxyEvent.bind(_this, eventName, runner));
        });
    };
    /**
     * Evaluates an event fired by `runner`, proxying it forward or buffering it.
     *
     * @param {string} eventName
     * @param {!Mocha.runners.Base} runner The runner that emitted this event.
     * @param {...*} var_args Any additional data passed as part of the event.
     */
    MultiReporter.prototype.proxyEvent = function (eventName, runner) {
        var _extra = [];
        for (var _i = 2; _i < arguments.length; _i++) {
            _extra[_i - 2] = arguments[_i];
        }
        var extraArgs = Array.prototype.slice.call(arguments, 2);
        if (this.complete) {
            console.warn('out of order Mocha event for ' + runner.name + ':', eventName, extraArgs);
            return;
        }
        if (this.currentRunner && runner !== this.currentRunner) {
            this.pendingEvents.push(Array.prototype.slice.call(arguments));
            return;
        }
        util.debug('MultiReporter#proxyEvent(', arguments, ')');
        // This appears to be a Mocha bug: Tests failed by passing an error to their
        // done function don't set `err` properly.
        //
        // TODO(nevir): Track down.
        if (eventName === 'fail' && !extraArgs[0].err) {
            extraArgs[0].err = extraArgs[1];
        }
        if (eventName === 'start') {
            this.onRunnerStart(runner);
        }
        else if (eventName === 'end') {
            this.onRunnerEnd(runner);
        }
        else {
            this.cleanEvent(eventName, runner, extraArgs);
            this.emit.apply(this, [eventName].concat(extraArgs));
        }
    };
    /**
     * Cleans or modifies an event if needed.
     *
     * @param eventName
     * @param runner The runner that emitted this event.
     * @param extraArgs
     */
    MultiReporter.prototype.cleanEvent = function (eventName, _runner, extraArgs) {
        // Suite hierarchy
        if (extraArgs[0]) {
            extraArgs[0] = this.showRootSuite(extraArgs[0]);
        }
        // Normalize errors
        if (eventName === 'fail') {
            extraArgs[1] = normalization_js_1.normalize(extraArgs[1], STACKY_CONFIG);
        }
        var extra0 = extraArgs[0];
        if (extra0 && extra0.err) {
            extra0.err = normalization_js_1.normalize(extra0.err, STACKY_CONFIG);
        }
    };
    /**
     * We like to show the root suite's title, which requires a little bit of
     * trickery in the suite hierarchy.
     *
     * @param {!Mocha.Runnable} node
     */
    MultiReporter.prototype.showRootSuite = function (node) {
        var leaf = node = Object.create(node);
        while (node && node.parent) {
            var wrappedParent = Object.create(node.parent);
            node.parent = wrappedParent;
            node = wrappedParent;
        }
        node.root = false;
        return leaf;
    };
    /** @param {!Mocha.runners.Base} runner */
    MultiReporter.prototype.onRunnerStart = function (runner) {
        util.debug('MultiReporter#onRunnerStart:', runner.name);
        this.total = this.total - ESTIMATED_TESTS_PER_SUITE + runner.total;
        this.currentRunner = runner;
    };
    /** @param {!Mocha.runners.Base} runner */
    MultiReporter.prototype.onRunnerEnd = function (runner) {
        util.debug('MultiReporter#onRunnerEnd:', runner.name);
        this.currentRunner = null;
        this.flushPendingEvents();
    };
    /**
     * Flushes any buffered events and runs them through `proxyEvent`. This will
     * loop until all buffered runners are complete, or we have run out of
     * buffered events.
     */
    MultiReporter.prototype.flushPendingEvents = function () {
        var _this = this;
        var events = this.pendingEvents;
        this.pendingEvents = [];
        events.forEach(function (eventArgs) {
            _this.proxyEvent.apply(_this, eventArgs);
        });
    };
    return MultiReporter;
}());
exports.default = MultiReporter;
//# sourceMappingURL=multi.js.map
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
var util = require("../util.js");
var stats_collector_js_1 = require("./stats-collector.js");
// We capture console events when running tests; so make sure we have a
// reference to the original one.
var console = window.console;
var FONT = ';font: normal 13px "Roboto", "Helvetica Neue", "Helvetica", sans-serif;';
var STYLES = {
    plain: FONT,
    suite: 'color: #5c6bc0' + FONT,
    test: FONT,
    passing: 'color: #259b24' + FONT,
    pending: 'color: #e65100' + FONT,
    failing: 'color: #c41411' + FONT,
    stack: 'color: #c41411',
    results: FONT + 'font-size: 16px',
};
// I don't think we can feature detect this one...
var userAgent = navigator.userAgent.toLowerCase();
var CAN_STYLE_LOG = userAgent.match('firefox') || userAgent.match('webkit');
var CAN_STYLE_GROUP = userAgent.match('webkit');
// Track the indent for faked `console.group`
var logIndent = '';
function getStyle(style) {
    if (style === undefined) {
        return STYLES.plain;
    }
    return STYLES[style] || STYLES.plain;
}
function log(text, style) {
    text = text.split('\n').map(function (l) { return logIndent + l; }).join('\n');
    if (CAN_STYLE_LOG) {
        console.log('%c' + text, getStyle(style));
    }
    else {
        console.log(text);
    }
}
function logGroup(text, style) {
    if (CAN_STYLE_GROUP) {
        console.group('%c' + text, getStyle(style));
    }
    else if (console.group) {
        console.group(text);
    }
    else {
        logIndent = logIndent + '  ';
        log(text, style);
    }
}
function logGroupEnd() {
    if (console.groupEnd) {
        console.groupEnd();
    }
    else {
        logIndent = logIndent.substr(0, logIndent.length - 2);
    }
}
function logException(error) {
    log(error.stack || error.message || (error + ''), 'stack');
}
/**
 * A Mocha reporter that logs results out to the web `console`.
 */
var Console = /** @class */ (function () {
    /**
     * @param runner The runner that is being reported on.
     */
    function Console(runner) {
        // Mocha 6 runner doesn't have stats at this point so we need to use
        // the stats-collector from Mocha to add them before calling the base
        // reporter.
        if (!runner.stats) {
            stats_collector_js_1.createStatsCollector(runner);
        }
        Mocha.reporters.Base.call(this, runner);
        runner.on('suite', function (suite) { return suite.root && logGroup(suite.title, 'suite'); });
        runner.on('suite end', function (suite) { return suite.root && logGroupEnd(); });
        runner.on('test', function (test) { return logGroup(test.title, 'test'); });
        runner.on('pending', function (test) { return logGroup(test.title, 'pending'); });
        runner.on('fail', function (_test, error) { return logException(error); });
        runner.on('test end', function (_test) { return logGroupEnd(); });
        runner.on('end', this.logSummary.bind(this));
    }
    /** Prints out a final summary of test results. */
    Console.prototype.logSummary = function () {
        logGroup('Test Results', 'results');
        if (this.stats.failures > 0) {
            log(util.pluralizedStat(this.stats.failures, 'failing'), 'failing');
        }
        if (this.stats.pending > 0) {
            log(util.pluralizedStat(this.stats.pending, 'pending'), 'pending');
        }
        log(util.pluralizedStat(this.stats.passes, 'passing'));
        if (!this.stats.failures) {
            log('test suite passed', 'passing');
        }
        log('Evaluated ' + this.stats.tests + ' tests in ' + this.stats.duration +
            'ms.');
        logGroupEnd();
    };
    return Console;
}());
exports.default = Console;
//# sourceMappingURL=console.js.map
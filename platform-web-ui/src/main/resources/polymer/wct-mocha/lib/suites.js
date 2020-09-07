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
var childrunner_js_1 = require("./childrunner.js");
var config = require("./config.js");
var util = require("./util.js");
exports.htmlSuites = [];
exports.jsSuites = [];
// We process grep ourselves to avoid loading suites that will be filtered.
var GREP = util.getParam('grep');
// work around mocha bug (https://github.com/mochajs/mocha/issues/2070)
if (GREP) {
    GREP = GREP.replace(/\\\./g, '.');
}
/**
 * Loads suites of tests, supporting both `.js` and `.html` files.
 *
 * @param files The files to load.
 */
function loadSuites(files) {
    files.forEach(function (file) {
        if (/\.js(\?.*)?$/.test(file)) {
            exports.jsSuites.push(file);
        }
        else if (/\.html(\?.*)?$/.test(file)) {
            exports.htmlSuites.push(file);
        }
        else {
            throw new Error('Unknown resource type: ' + file);
        }
    });
}
exports.loadSuites = loadSuites;
/**
 * @return The child suites that should be loaded, ignoring
 *     those that would not match `GREP`.
 */
function activeChildSuites() {
    var subsuites = exports.htmlSuites;
    if (GREP) {
        var cleanSubsuites = [];
        for (var i = 0, subsuite = void 0; subsuite = subsuites[i]; i++) {
            if (GREP.indexOf(util.cleanLocation(subsuite)) !== -1) {
                cleanSubsuites.push(subsuite);
            }
        }
        subsuites = cleanSubsuites;
    }
    return subsuites;
}
exports.activeChildSuites = activeChildSuites;
/**
 * Loads all `.js` sources requested by the current suite.
 */
function loadJsSuites(_reporter, done) {
    util.debug('loadJsSuites', exports.jsSuites);
    // We only support `.js` dependencies for now.
    var loaders = exports.jsSuites.map(function (file) { return util.loadScript.bind(util, file); });
    util.parallel(loaders, done);
}
exports.loadJsSuites = loadJsSuites;
function runSuites(reporter, childSuites, done) {
    util.debug('runSuites');
    var suiteRunners = [
        // Run the local tests (if any) first, not stopping on error;
        _runMocha.bind(null, reporter),
    ];
    // As well as any sub suites. Again, don't stop on error.
    childSuites.forEach(function (file) {
        suiteRunners.push(function (next) {
            var childRunner = new childrunner_js_1.default(file, window);
            reporter.emit('childRunner start', childRunner);
            childRunner.run(function (error) {
                reporter.emit('childRunner end', childRunner);
                if (error) {
                    reporter.emitOutOfBandTest(file, error);
                }
                next();
            });
        });
    });
    util.parallel(suiteRunners, config.get('numConcurrentSuites'), function (error) {
        reporter.done();
        done(error);
    });
}
exports.runSuites = runSuites;
/**
 * Kicks off a mocha run, waiting for frameworks to load if necessary.
 *
 * @param {!MultiReporter} reporter Where to send Mocha's events.
 * @param {function} done A callback fired, _no error is passed_.
 */
function _runMocha(reporter, done, waited) {
    if (config.get('waitForFrameworks') && !waited) {
        var waitFor = (config.get('waitFor') || util.whenFrameworksReady).bind(window);
        waitFor(_runMocha.bind(null, reporter, done, true));
        return;
    }
    util.debug('_runMocha');
    var mocha = window.mocha;
    var Mocha = window.Mocha;
    mocha.reporter(reporter.childReporter());
    mocha.suite.title = reporter.suiteTitle(window.location);
    mocha.grep(GREP);
    // We can't use `mocha.run` because it bashes over grep, invert, and friends.
    // See https://github.com/visionmedia/mocha/blob/master/support/tail.js#L137
    var runner = Mocha.prototype.run.call(mocha, function (_error) {
        if (document.getElementById('mocha')) {
            Mocha.utils.highlightTags('code');
        }
        done(); // We ignore the Mocha failure count.
    });
    // Mocha's default `onerror` handling strips the stack (to support really old
    // browsers). We upgrade this to get better stacks for async errors.
    //
    // TODO(nevir): Can we expand support to other browsers?
    if (navigator.userAgent.match(/chrome/i)) {
        window.onerror = null;
        window.addEventListener('error', function (event) {
            if (!event.error) {
                return;
            }
            if (event.error.ignore) {
                return;
            }
            if (window.uncaughtErrorFilter && window.uncaughtErrorFilter(event)) {
                event.preventDefault();
                return;
            }
            runner.uncaught(event.error);
        });
    }
    else {
        window.onerror = null;
        window.addEventListener('error', function (event) {
            if (window.uncaughtErrorFilter && window.uncaughtErrorFilter(event)) {
                event.preventDefault();
                return;
            }
            runner.uncaught(event.error);
        });
    }
}
//# sourceMappingURL=suites.js.map
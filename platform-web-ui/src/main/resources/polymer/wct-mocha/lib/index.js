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
// Registers a bunch of globals:
require("./environment/helpers.js");
var childrunner_js_1 = require("./childrunner.js");
exports.ChildRunner = childrunner_js_1.default;
var clisocket_js_1 = require("./clisocket.js");
var config = require("./config.js");
var environment = require("./environment.js");
var errors = require("./environment/errors.js");
var mocha = require("./mocha.js");
var reporters = require("./reporters.js");
var multi_js_1 = require("./reporters/multi.js");
var suites = require("./suites.js");
var util = require("./util.js");
var extend_js_1 = require("./mocha/extend.js");
exports.extendInterfaces = extend_js_1.extendInterfaces;
function initialize(initConfig) {
    var _config = {};
    if (initConfig) {
        config.deepMerge(_config, initConfig);
    }
    // You can configure WCT before it has loaded by assigning your custom
    // configuration to the global `WCT`.
    if (window.WCT) {
        config.deepMerge(_config, window.WCT);
    }
    config.setup(_config);
    // Maybe some day we'll expose WCT as a module to whatever module registry you
    // are using (aka the UMD approach), or as an es6 module.
    var WCT = window.WCT = {
        // A generic place to hang data about the current suite. This object is
        // reported
        // back via the `sub-suite-start` and `sub-suite-end` events.
        share: {},
        // Until then, we get to rely on it to expose parent runners to their
        // children.
        _ChildRunner: childrunner_js_1.default,
        _reporter: undefined,
        _config: config._config,
        // Public API
        /**
         * Loads suites of tests, supporting both `.js` and `.html` files.
         *
         * @param {!Array.<string>} files The files to load.
         */
        loadSuites: suites.loadSuites,
    };
    // Load Process
    errors.listenForErrors();
    mocha.stubInterfaces();
    environment.loadSync();
    // Give any scripts on the page a chance to declare tests and muck with
    // things.
    document.addEventListener('DOMContentLoaded', function () {
        util.debug('DOMContentLoaded');
        environment.ensureDependenciesPresent();
        // We need the socket built prior to building its reporter.
        clisocket_js_1.default.init(function (error, socket) {
            if (error) {
                throw error;
            }
            // Are we a child of another run?
            var current = childrunner_js_1.default.current();
            var parent = current && current.parentScope.WCT._reporter;
            util.debug('parentReporter:', parent);
            var childSuites = suites.activeChildSuites();
            var reportersToUse = reporters.determineReporters(socket, parent);
            // +1 for any local tests.
            var reporter = new multi_js_1.default(childSuites.length + 1, reportersToUse, parent);
            WCT._reporter = reporter; // For environment/compatibility.js
            // We need the reporter so that we can report errors during load.
            suites.loadJsSuites(reporter, function (error) {
                // Let our parent know that we're about to start the tests.
                if (current) {
                    current.ready(error);
                }
                if (error) {
                    throw error;
                }
                // Emit any errors we've encountered up til now
                errors.globalErrors.forEach(function (error) {
                    reporter.emitOutOfBandTest('Test Suite Initialization', error);
                });
                suites.runSuites(reporter, childSuites, function (error) {
                    // Make sure to let our parent know that we're done.
                    if (current) {
                        current.done();
                    }
                    if (error) {
                        throw error;
                    }
                });
            });
        });
    });
}
exports.initialize = initialize;
//# sourceMappingURL=index.js.map
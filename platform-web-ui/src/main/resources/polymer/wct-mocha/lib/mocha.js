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
require("./mocha/fixture.js");
var config = require("./config.js");
var extend_js_1 = require("./mocha/extend.js");
// Mocha global helpers, broken out by testing method.
//
// Keys are the method for a particular interface; values are their analog in
// the opposite interface.
var MOCHA_EXPORTS = {
    // https://github.com/visionmedia/mocha/blob/master/lib/interfaces/tdd.js
    tdd: {
        'setup': '"before"',
        'teardown': '"after"',
        'suiteSetup': '"beforeEach"',
        'suiteTeardown': '"afterEach"',
        'suite': '"describe" or "context"',
        'test': '"it" or "specify"',
    },
    // https://github.com/visionmedia/mocha/blob/master/lib/interfaces/bdd.js
    bdd: {
        'before': '"setup"',
        'after': '"teardown"',
        'beforeEach': '"suiteSetup"',
        'afterEach': '"suiteTeardown"',
        'describe': '"suite"',
        'context': '"suite"',
        'xdescribe': '"suite.skip"',
        'xcontext': '"suite.skip"',
        'it': '"test"',
        'xit': '"test.skip"',
        'specify': '"test"',
        'xspecify': '"test.skip"',
    },
};
/**
 * Exposes all Mocha methods up front, configuring and running mocha
 * automatically when you call them.
 *
 * The assumption is that it is a one-off (sub-)suite of tests being run.
 */
function stubInterfaces() {
    var keys = Object.keys(MOCHA_EXPORTS);
    keys.forEach(function (ui) {
        Object.keys(MOCHA_EXPORTS[ui]).forEach(function (key) {
            window[key] = function wrappedMochaFunction() {
                _setupMocha(ui, key, MOCHA_EXPORTS[ui][key]);
                if (!window[key] || window[key] === wrappedMochaFunction) {
                    throw new Error('Expected mocha.setup to define ' + key);
                }
                window[key].apply(window, arguments);
            };
        });
    });
}
exports.stubInterfaces = stubInterfaces;
// Whether we've called `mocha.setup`
var _mochaIsSetup = false;
/**
 * @param {string} ui Sets up mocha to run `ui`-style tests.
 * @param {string} key The method called that triggered this.
 * @param {string} alternate The matching method in the opposite interface.
 */
function _setupMocha(ui, key, alternate) {
    var mochaOptions = config.get('mochaOptions');
    if (mochaOptions.ui && mochaOptions.ui !== ui) {
        var message = 'Mixing ' + mochaOptions.ui + ' and ' + ui +
            ' Mocha styles is not supported. ' +
            'You called "' + key + '". Did you mean ' + alternate + '?';
        throw new Error(message);
    }
    if (_mochaIsSetup) {
        return;
    }
    extend_js_1.applyExtensions();
    mochaOptions.ui = ui;
    mocha.setup(mochaOptions); // Note that the reporter is configured in run.js.
}
//# sourceMappingURL=mocha.js.map
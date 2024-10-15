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
var util = require("./util.js");
/**
 * The global configuration state for WCT's browser client.
 */
exports._config = {
    environmentScripts: [],
    environmentImports: [],
    root: null,
    waitForFrameworks: true,
    waitFor: null,
    numConcurrentSuites: 1,
    trackConsoleError: true,
    mochaOptions: { timeout: 10 * 1000 },
    verbose: false,
};
/**
 * Merges initial `options` into WCT's global configuration.
 *
 * @param {Object} options The options to merge. See `browser/config.ts` for a
 *     reference.
 */
function setup(options) {
    var childRunner = childrunner_js_1.default.current();
    if (childRunner) {
        deepMerge(exports._config, childRunner.parentScope.WCT._config);
        // But do not force the mocha UI
        delete exports._config.mochaOptions.ui;
    }
    if (options && typeof options === 'object') {
        deepMerge(exports._config, options);
    }
    if (!exports._config.root) {
        // Sibling dependencies.
        var wctMochaJsRoot = util.scriptPrefix('wct-mocha.js');
        var browserJsRoot = util.scriptPrefix('browser.js');
        var scriptName = wctMochaJsRoot ? 'wct-mocha.js' : 'browser.js';
        var root = (wctMochaJsRoot || browserJsRoot);
        exports._config.root = util.basePath(root.substr(0, root.length - 1));
        if (!exports._config.root) {
            throw new Error("Unable to detect root URL for WCT sources. " +
                ("Please set WCT.root before loading " + scriptName + " first."));
        }
    }
}
exports.setup = setup;
/**
 * Retrieves a configuration value.
 */
function get(key) {
    return exports._config[key];
}
exports.get = get;
function deepMerge(target, source) {
    Object.keys(source).forEach(function (untypedKey) {
        var key = untypedKey;
        var targetValue = target[key];
        if (targetValue != null && typeof targetValue === 'object' &&
            !Array.isArray(targetValue)) {
            deepMerge(targetValue, source[key]);
        }
        else {
            target[key] = source[key];
        }
    });
}
exports.deepMerge = deepMerge;
//# sourceMappingURL=config.js.map
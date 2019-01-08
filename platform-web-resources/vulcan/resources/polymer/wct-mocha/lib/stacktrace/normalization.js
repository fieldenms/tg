"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
/**
 * @license
 * Copyright (c) 2018 The Polymer Project Authors. All rights reserved.
 *
 * This code may only be used under the BSD style license found at
 * polymer.github.io/LICENSE.txt The complete set of authors may be found at
 * polymer.github.io/AUTHORS.txt The complete set of contributors may be found
 * at polymer.github.io/CONTRIBUTORS.txt Code distributed by Google as part of
 * the polymer project is also subject to an additional IP rights grant found at
 * polymer.github.io/PATENTS.txt
 */
var formatting_js_1 = require("./formatting.js");
var parsing_js_1 = require("./parsing.js");
function normalize(error, prettyOptions) {
    if (error.parsedStack) {
        return error;
    }
    var message = error.message || error.description || error || '<unknown error>';
    var parsedStack = [];
    try {
        parsedStack = parsing_js_1.parse(error.stack || error.toString());
    }
    catch (error) {
        // Ah well.
    }
    if (parsedStack.length === 0 && error.fileName) {
        parsedStack.push({
            method: '',
            location: error.fileName,
            line: error.lineNumber || -1,
            column: error.columnNumber || -1,
        });
    }
    if (!prettyOptions || !prettyOptions.showColumns) {
        for (var i = 0, line = void 0; line = parsedStack[i]; i++) {
            delete line.column;
        }
    }
    var prettyStack = message;
    if (parsedStack.length > 0) {
        prettyStack = prettyStack + '\n' + formatting_js_1.pretty(parsedStack, prettyOptions);
    }
    var cleanErr = Object.create(Error.prototype);
    cleanErr.message = message;
    cleanErr.stack = prettyStack;
    cleanErr.parsedStack = parsedStack;
    return cleanErr;
}
exports.normalize = normalize;
//# sourceMappingURL=normalization.js.map
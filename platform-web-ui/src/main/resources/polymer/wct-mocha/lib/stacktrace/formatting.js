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
var parsing_js_1 = require("./parsing.js");
exports.defaults = {
    maxMethodPadding: 40,
    indent: '',
    methodPlaceholder: '<unknown>',
    locationStrip: [],
    unimportantLocation: [],
    filter: function () { return false; },
    styles: {
        method: passthrough,
        location: passthrough,
        line: passthrough,
        column: passthrough,
        unimportant: passthrough,
    },
};
function pretty(stackOrParsed, maybeOptions) {
    var options = mergeDefaults(maybeOptions || {}, exports.defaults);
    var linesAndNulls = Array.isArray(stackOrParsed) ? stackOrParsed : parsing_js_1.parse(stackOrParsed);
    var lines = clean(linesAndNulls, options);
    var padSize = methodPadding(lines, options);
    var parts = lines.map(function (line) {
        var method = line.method || options.methodPlaceholder;
        var pad = options.indent + padding(padSize - method.length);
        var locationBits = [
            options.styles.location(line.location),
            options.styles.line(line.line.toString()),
        ];
        if ('column' in line) {
            locationBits.push(options.styles.column(line.column.toString()));
        }
        var location = locationBits.join(':');
        var text = pad + options.styles.method(method) + ' at ' + location;
        if (!line.important) {
            text = options.styles.unimportant(text);
        }
        return text;
    });
    return parts.join('\n');
}
exports.pretty = pretty;
function clean(lines, options) {
    var result = [];
    for (var i = 0, line = void 0; line = lines[i]; i++) {
        if (options.filter && options.filter(line)) {
            continue;
        }
        line.location = cleanLocation(line.location, options);
        line.important = isImportant(line, options);
        result.push(line);
    }
    return result;
}
// Utility
function passthrough(text) {
    return text;
}
function mergeDefaults(options, defaults) {
    var result = Object.create(defaults);
    Object.keys(options).forEach(function (untypedKey) {
        var key = untypedKey;
        var value = options[key];
        if (typeof value === 'object' && !Array.isArray(value)) {
            value =
                mergeDefaults(value, defaults[key]);
        }
        result[key] = value;
    });
    return result;
}
function methodPadding(lines, options) {
    var size = (options.methodPlaceholder || '').length;
    for (var i = 0, line = void 0; line = lines[i]; i++) {
        size = Math.min(options.maxMethodPadding || Infinity, Math.max(size, line.method.length));
    }
    return size;
}
function padding(length) {
    var result = '';
    for (var i = 0; i < length; i++) {
        result = result + ' ';
    }
    return result;
}
function cleanLocation(location, options) {
    if (options.locationStrip) {
        for (var i = 0, matcher = void 0; matcher = options.locationStrip[i]; i++) {
            location = location.replace(matcher, '');
        }
    }
    return location;
}
function isImportant(line, options) {
    if (options.unimportantLocation) {
        for (var i = 0, matcher = void 0; matcher = options.unimportantLocation[i]; i++) {
            if (line.location.match(matcher)) {
                return false;
            }
        }
    }
    return true;
}
//# sourceMappingURL=formatting.js.map
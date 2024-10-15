"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var console_js_1 = require("./reporters/console.js");
var html_js_1 = require("./reporters/html.js");
var multi_js_1 = require("./reporters/multi.js");
var title_js_1 = require("./reporters/title.js");
var suites = require("./suites.js");
exports.htmlSuites = [];
exports.jsSuites = [];
/**
 * @param {CLISocket} socket The CLI socket, if present.
 * @param {MultiReporter} parent The parent reporter, if present.
 * @return {!Array.<!Mocha.reporters.Base} The reporters that should be used.
 */
function determineReporters(socket, parent) {
    // Parents are greedy.
    if (parent) {
        return [parent.childReporter()];
    }
    // Otherwise, we get to run wild without any parental supervision!
    var reporters = [title_js_1.default, console_js_1.default];
    if (socket) {
        reporters.push((function (runner) {
            socket.observe(runner);
        }));
    }
    if (suites.htmlSuites.length > 0 || suites.jsSuites.length > 0) {
        reporters.push(html_js_1.default);
    }
    return reporters;
}
exports.determineReporters = determineReporters;
/**
 * Yeah, hideous, but this allows us to be loaded before Mocha, which is handy.
 */
function injectMocha(Mocha) {
    _injectPrototype(console_js_1.default, Mocha.reporters.Base.prototype);
    _injectPrototype(html_js_1.default, Mocha.reporters.HTML.prototype);
    // Mocha doesn't expose its `EventEmitter` shim directly, so:
    _injectPrototype(multi_js_1.default, Object.getPrototypeOf(Mocha.Runner.prototype));
}
exports.injectMocha = injectMocha;
function _injectPrototype(klass, prototype) {
    var newPrototype = Object.create(prototype);
    // Only support
    Object.keys(klass.prototype).forEach(function (key) {
        newPrototype[key] = klass.prototype[key];
    });
    klass.prototype = newPrototype;
}
//# sourceMappingURL=reporters.js.map
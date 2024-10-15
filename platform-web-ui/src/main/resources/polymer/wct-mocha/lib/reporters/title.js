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
var ARC_OFFSET = 0; // start at the right.
var ARC_WIDTH = 6;
/**
 * A Mocha reporter that updates the document's title and favicon with
 * at-a-glance stats.
 *
 * @param {!Mocha.Runner} runner The runner that is being reported on.
 */
var Title = /** @class */ (function () {
    function Title(runner) {
        Mocha.reporters.Base.call(this, runner);
        runner.on('test end', this.report.bind(this));
    }
    /** Reports current stats via the page title and favicon. */
    Title.prototype.report = function () {
        this.updateTitle();
        this.updateFavicon();
    };
    /** Updates the document title with a summary of current stats. */
    Title.prototype.updateTitle = function () {
        if (this.stats.failures > 0) {
            document.title = util.pluralizedStat(this.stats.failures, 'failing');
        }
        else {
            document.title = util.pluralizedStat(this.stats.passes, 'passing');
        }
    };
    /** Updates the document's favicon w/ a summary of current stats. */
    Title.prototype.updateFavicon = function () {
        var canvas = document.createElement('canvas');
        canvas.height = canvas.width = 32;
        var context = canvas.getContext('2d');
        var passing = this.stats.passes;
        var pending = this.stats.pending;
        var failing = this.stats.failures;
        var total = Math.max(this.runner.total, passing + pending + failing);
        drawFaviconArc(context, total, 0, passing, '#0e9c57');
        drawFaviconArc(context, total, passing, pending, '#f3b300');
        drawFaviconArc(context, total, pending + passing, failing, '#ff5621');
        this.setFavicon(canvas.toDataURL());
    };
    /** Sets the current favicon by URL. */
    Title.prototype.setFavicon = function (url) {
        var current = document.head.querySelector('link[rel="icon"]');
        if (current) {
            document.head.removeChild(current);
        }
        var link = document.createElement('link');
        link.rel = 'icon';
        link.type = 'image/x-icon';
        link.href = url;
        link.setAttribute('sizes', '32x32');
        document.head.appendChild(link);
    };
    return Title;
}());
exports.default = Title;
/**
 * Draws an arc for the favicon status, relative to the total number of tests.
 */
function drawFaviconArc(context, total, start, length, color) {
    var arcStart = ARC_OFFSET + Math.PI * 2 * (start / total);
    var arcEnd = ARC_OFFSET + Math.PI * 2 * ((start + length) / total);
    context.beginPath();
    context.strokeStyle = color;
    context.lineWidth = ARC_WIDTH;
    context.arc(16, 16, 16 - ARC_WIDTH / 2, arcStart, arcEnd);
    context.stroke();
}
//# sourceMappingURL=title.js.map
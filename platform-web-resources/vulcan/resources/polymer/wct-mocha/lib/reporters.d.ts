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
import CLISocket from './clisocket.js';
import MultiReporter, { ReporterFactory } from './reporters/multi.js';
export declare let htmlSuites: Array<undefined>;
export declare let jsSuites: Array<undefined>;
/**
 * @param {CLISocket} socket The CLI socket, if present.
 * @param {MultiReporter} parent The parent reporter, if present.
 * @return {!Array.<!Mocha.reporters.Base} The reporters that should be used.
 */
export declare function determineReporters(socket: CLISocket, parent: MultiReporter | null): ReporterFactory[];
export declare type MochaStatic = typeof Mocha;
/**
 * Yeah, hideous, but this allows us to be loaded before Mocha, which is handy.
 */
export declare function injectMocha(Mocha: MochaStatic): void;

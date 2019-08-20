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
/**
 * @param {function()} callback A function to call when the active web component
 *     frameworks have loaded.
 */
export declare function whenFrameworksReady(callback: () => void): void;
/**
 * @return {string} '<count> <kind> tests' or '<count> <kind> test'.
 */
export declare function pluralizedStat(count: number, kind: string): string;
/**
 * @param {string} path The URI of the script to load.
 * @param {function} done
 */
export declare function loadScript(path: string, done: (error?: {}) => void): void;
/**
 * @param {string} path The URI of the stylesheet to load.
 * @param {function} done
 */
export declare function loadStyle(path: string, done?: () => void): void;
/**
 * @param {...*} var_args Logs values to the console when the `debug`
 *     configuration option is true.
 */
export declare function debug(...var_args: unknown[]): void;
/**
 * @param {string} url
 * @return {{base: string, params: string}}
 */
export declare function parseUrl(url: string): {
    base: string;
    params: Params;
};
/**
 * Expands a URL that may or may not be relative to `base`.
 *
 * @param {string} url
 * @param {string} base
 * @return {string}
 */
export declare function expandUrl(url: string, base: string): string;
export interface Params {
    [param: string]: string[];
}
/**
 * @param {string=} opt_query A query string to parse.
 * @return {!Object<string, !Array<string>>} All params on the URL's query.
 */
export declare function getParams(query?: string): Params;
/**
 * Merges params from `source` into `target` (mutating `target`).
 *
 * @param {!Object<string, !Array<string>>} target
 * @param {!Object<string, !Array<string>>} source
 */
export declare function mergeParams(target: Params, source: Params): void;
/**
 * @param {string} param The param to return a value for.
 * @return {?string} The first value for `param`, if found.
 */
export declare function getParam(param: string): string | null;
/**
 * @param {!Object<string, !Array<string>>} params
 * @return {string} `params` encoded as a URI query.
 */
export declare function paramsToQuery(params: Params): string;
export declare function basePath(location: Location | string): string;
export declare function relativeLocation(location: Location | string, basePath: string): string;
export declare function cleanLocation(location: Location | string): string;
export declare type Runner = (f: Function) => void;
/**
 * Like `async.parallelLimit`, but our own so that we don't force a dependency
 * on downstream code.
 *
 * @param runners Runners that call their given
 *     Node-style callback when done.
 * @param {number|function(*)} limit Maximum number of concurrent runners.
 *     (optional).
 * @param {?function(*)} done Callback that should be triggered once all runners
 *     have completed, or encountered an error.
 */
export declare function parallel(runners: Runner[], done: (error?: {} | undefined) => void): void;
export declare function parallel(runners: Runner[], limit: number, done: (error?: {} | undefined) => void): void;
/**
 * Finds the directory that a loaded script is hosted on.
 *
 * @param {string} filename
 * @return {string?}
 */
export declare function scriptPrefix(filename: string): string | null;

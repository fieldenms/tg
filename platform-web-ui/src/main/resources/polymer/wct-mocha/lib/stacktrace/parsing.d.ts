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
export interface ParsedLine {
    method: string;
    location: string;
    line: number;
    column: number;
    important?: boolean;
}
export declare function parse(stack: string): ParsedLine[];
export declare function parseGeckoLine(line: string): ParsedLine | null;
export declare function parseV8Line(line: string): ParsedLine | null;
export declare function parseStackyLine(line: string): ParsedLine | null;

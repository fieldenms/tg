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
import { ParsedLine } from './parsing.js';
export declare type FormattingFunction = (text: string) => string;
export interface FormattingOptions {
    showColumns?: boolean;
    maxMethodPadding: number;
    indent: string;
    methodPlaceholder: string;
    locationStrip: (string | RegExp)[];
    unimportantLocation: Array<string>;
    filter: (line: ParsedLine) => boolean;
    styles: {
        method: FormattingFunction;
        location: FormattingFunction;
        line: FormattingFunction;
        column: FormattingFunction;
        unimportant: FormattingFunction;
    };
}
export declare const defaults: FormattingOptions;
export declare function pretty(stackOrParsed: string | Array<ParsedLine | null>, maybeOptions?: Partial<FormattingOptions>): string;

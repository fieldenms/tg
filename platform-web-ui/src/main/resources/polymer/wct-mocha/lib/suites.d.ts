import MultiReporter from './reporters/multi.js';
export declare let htmlSuites: string[];
export declare let jsSuites: string[];
/**
 * Loads suites of tests, supporting both `.js` and `.html` files.
 *
 * @param files The files to load.
 */
export declare function loadSuites(files: string[]): void;
/**
 * @return The child suites that should be loaded, ignoring
 *     those that would not match `GREP`.
 */
export declare function activeChildSuites(): string[];
/**
 * Loads all `.js` sources requested by the current suite.
 */
export declare function loadJsSuites(_reporter: MultiReporter, done: (error: {} | undefined) => void): void;
export declare function runSuites(reporter: MultiReporter, childSuites: string[], done: (error?: {}) => void): void;

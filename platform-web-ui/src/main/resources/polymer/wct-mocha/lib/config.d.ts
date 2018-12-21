/// <reference types="mocha" />
export interface Config {
    /**
     * `.js` scripts to be loaded (synchronously) before WCT starts in earnest.
     *
     * Paths are relative to `scriptPrefix`.
     */
    environmentScripts: string[];
    environmentImports: string[];
    /** Absolute root for client scripts. Detected in `setup()` if not set. */
    root: null | string;
    /** By default, we wait for any web component frameworks to load. */
    waitForFrameworks: boolean;
    /**
     * Alternate callback for waiting for tests.
     * `this` for the callback will be the window currently running tests.
     */
    waitFor: null | Function;
    /** How many `.html` suites that can be concurrently loaded & run. */
    numConcurrentSuites: number;
    /** Whether `console.error` should be treated as a test failure. */
    trackConsoleError: boolean;
    /** Configuration passed to mocha.setup. */
    mochaOptions: MochaSetupOptions;
    /** Whether WCT should emit (extremely verbose) debugging log messages. */
    verbose: boolean;
}
/**
 * The global configuration state for WCT's browser client.
 */
export declare let _config: Config;
/**
 * Merges initial `options` into WCT's global configuration.
 *
 * @param {Object} options The options to merge. See `browser/config.ts` for a
 *     reference.
 */
export declare function setup(options: Partial<Config>): void;
/**
 * Retrieves a configuration value.
 */
export declare function get<K extends keyof Config>(key: K): Config[K];
export declare function deepMerge<V extends {}>(target: V, source: V): void;

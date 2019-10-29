/**
 * Loads all environment scripts ...synchronously ...after us.
 */
export declare function loadSync(): void;
/**
 * We have some hard dependencies on things that should be loaded via
 * `environmentScripts`, so we assert that they're present here; and do any
 * post-facto setup.
 */
export declare function ensureDependenciesPresent(): void;

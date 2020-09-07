/// <reference types="mocha" />
/**
 * A Mocha reporter that logs results out to the web `console`.
 */
export default class Console {
    /**
     * @param runner The runner that is being reported on.
     */
    constructor(runner: Mocha.IRunner);
    /** Prints out a final summary of test results. */
    logSummary(): void;
}
export default interface Console extends Mocha.reporters.Base {
}

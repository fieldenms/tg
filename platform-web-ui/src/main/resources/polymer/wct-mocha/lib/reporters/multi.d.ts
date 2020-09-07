/// <reference types="node" />
/// <reference types="mocha" />
export interface Reporter {
}
export interface ReporterFactory {
    new (parent: MultiReporter): Reporter;
}
/**
 * A Mocha-like reporter that combines the output of multiple Mocha suites.
 */
export default class MultiReporter implements Reporter {
    readonly reporters: ReadonlyArray<Reporter>;
    readonly parent: MultiReporter | undefined | null;
    readonly basePath: string;
    total: number;
    private currentRunner;
    /** Arguments that would be called on emit(). */
    private pendingEvents;
    private complete;
    /**
     * @param numSuites The number of suites that will be run, in order to
     *     estimate the total number of tests that will be performed.
     * @param reporters The set of reporters that
     *     should receive the unified event stream.
     * @param parent The parent reporter, if present.
     */
    constructor(numSuites: number, reporters: ReporterFactory[], parent: MultiReporter | undefined | null);
    /**
     * @return A reporter-like "class" for each child suite
     *     that should be passed to `mocha.run`.
     */
    childReporter(): ReporterFactory;
    /** Must be called once all runners have finished. */
    done(): void;
    epilogue(): void;
    /**
     * Emit a top level test that is not part of any suite managed by this
     * reporter.
     *
     * Helpful for reporting on global errors, loading issues, etc.
     *
     * @param title The title of the test.
     * @param error An error associated with this test. If falsy, test is
     *     considered to be passing.
     * @param suiteTitle Title for the suite that's wrapping the test.
     * @param estimated If this test was included in the original
     *     estimate of `numSuites`.
     */
    emitOutOfBandTest(title: string, error?: {}, suiteTitle?: string, estimated?: boolean): void;
    /**
     * @param {!Location|string} location
     * @return {string}
     */
    suiteTitle(location: Location | string): string;
    /** @param {!Mocha.runners.Base} runner The runner to listen to events for. */
    private bindChildRunner;
    /**
     * Evaluates an event fired by `runner`, proxying it forward or buffering it.
     *
     * @param {string} eventName
     * @param {!Mocha.runners.Base} runner The runner that emitted this event.
     * @param {...*} var_args Any additional data passed as part of the event.
     */
    private proxyEvent;
    /**
     * Cleans or modifies an event if needed.
     *
     * @param eventName
     * @param runner The runner that emitted this event.
     * @param extraArgs
     */
    private cleanEvent;
    /**
     * We like to show the root suite's title, which requires a little bit of
     * trickery in the suite hierarchy.
     *
     * @param {!Mocha.Runnable} node
     */
    private showRootSuite;
    /** @param {!Mocha.runners.Base} runner */
    private onRunnerStart;
    /** @param {!Mocha.runners.Base} runner */
    private onRunnerEnd;
    /**
     * Flushes any buffered events and runs them through `proxyEvent`. This will
     * loop until all buffered runners are complete, or we have run out of
     * buffered events.
     */
    private flushPendingEvents;
}
export default interface MultiReporter extends Mocha.IRunner, NodeJS.EventEmitter {
}

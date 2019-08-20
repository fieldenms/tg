export interface SharedState {
}
/**
 * A Mocha suite (or suites) run within a child iframe, but reported as if they
 * are part of the current context.
 */
export default class ChildRunner {
    private container?;
    private eventListenersToRemoveOnClean;
    private iframe?;
    private onRunComplete;
    share: SharedState | null;
    state: 'initializing' | 'loading' | 'complete';
    private timeoutId?;
    private url;
    parentScope: Window;
    constructor(url: string, parentScope: Window);
    /**
     * Listeners added using this method will be removed on done()
     *
     * @param type event type
     * @param listener object which receives a notification
     * @param target event target
     */
    private addEventListener;
    /**
     * Removes all event listeners added by a method addEventListener defined
     * on an instance of ChildRunner.
     */
    private removeAllEventListeners;
    static loadTimeout: number;
    private static byUrl;
    /**
     * @return {ChildRunner} The `ChildRunner` that was registered for this
     * window.
     */
    static current(): ChildRunner | null;
    /**
     * @param {!Window} target A window to find the ChildRunner of.
     * @param {boolean} traversal Whether this is a traversal from a child window.
     * @return {ChildRunner} The `ChildRunner` that was registered for `target`.
     */
    static get(target: Window, traversal?: boolean): ChildRunner | null;
    /**
     * Loads and runs the subsuite.
     *
     * @param {function} done Node-style callback.
     */
    run(done: (error?: {}) => void): void;
    /**
     * Called when the sub suite's iframe has loaded (or errored during load).
     *
     * @param {*} error The error that occured, if any.
     */
    loaded(error?: {}): void;
    /**
     * Called in mocha/run.js when all dependencies have loaded, and the child is
     * ready to start running tests
     *
     * @param {*} error The error that occured, if any.
     */
    ready(error?: {}): void;
    /**
     * Called when the sub suite's tests are complete, so that it can clean up.
     */
    done(): void;
    signalRunComplete(error?: {}): void;
}

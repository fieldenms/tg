/// <reference types="mocha" />
/**
 * A Mocha reporter that updates the document's title and favicon with
 * at-a-glance stats.
 *
 * @param {!Mocha.Runner} runner The runner that is being reported on.
 */
export default class Title {
    constructor(runner: Mocha.IRunner);
    /** Reports current stats via the page title and favicon. */
    report(): void;
    /** Updates the document title with a summary of current stats. */
    updateTitle(): void;
    /** Updates the document's favicon w/ a summary of current stats. */
    updateFavicon(): void;
    /** Sets the current favicon by URL. */
    setFavicon(url: string): void;
}
export default interface Title extends Mocha.reporters.Base {
}

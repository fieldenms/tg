/**
 * Class to track all detached insertion points, needed to handle back button.
 */
class TgInsertionPointManager {

    constructor() {
        this._insertionPoints = [];
    }

    removeInsertionPoint (insertionPoint) {
        const i = this._insertionPoints.indexOf(insertionPoint);
        if (i >= 0) {
            this._insertionPoints.splice(i, 1);
        }
    }

    addInsertionPoint (insertionPoint) {
        this._insertionPoints.push(insertionPoint);
    }
}

export const InsertionPointManager = new TgInsertionPointManager();
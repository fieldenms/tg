/**
 * Calculates 95% confidence interval from 'data' array of numbers.
 */
export function calcConfidenceInterval(data) {
    const n = data.length;
    const x_ = data.reduce((acc, x) => acc + x, 0.0) / n; // "mean"
    const sigma = Math.sqrt(data.reduce((acc, x) => acc + (x - x_) ** 2, 0.0) / n); // standard deviation

    // The value of z(alfa / 2) is taken from z-table for confidence level of 95%, for example from here (http://www.statisticshowto.com/tables/z-table/).
    // alfa = 0.95, alfa / 2 = 0.475, find that value inside the table and check intersections: 1.9 and 0.6 => z := 1.96
    const z = 1.96;
    const deltaX = z * (sigma / Math.sqrt(n));

    return [+((x_ - deltaX).toFixed(1)), +((x_ + deltaX).toFixed(1))];
};

/**
 * Persists 95% confidence interval from 'data' into local storage with 'load-tests-categoryName: functionName' key.
 * Used padding by '_' instead of ' ' to better faciltate copying from Chrome console view.
 * 
 * @param categoryName -- e.g. 'centre' or 'master'
 * @param functionName -- e.g. 'validate', 'save', 'refresh', 'discard', 'run'
 */
export function persistConfidenceInterval(categoryName, functionName, data) {
    localStorage.setItem(
        `load-tests-${categoryName.padStart(6, '_')}: ${functionName.padStart(8, '_')}`,
        '[' + calcConfidenceInterval(data).map(x => x.toFixed(1).padStart(6, '_')).join('; ') + ']'
    );
};

/**
 * Returns promise for creating testing Entity Centre configuration for load tests.
 */
export const createTestingConfig = (centre) => new Promise((resolve, reject) => {
    // own save-as configuration is needed for load tests that use SAVE button -- not to invoke additional master when calculating saving time
    const newConfigAction = centre.$.dom.$.selectionView.shadowRoot.querySelector('tg-ui-action[element-name="tg-CentreConfigNewAction-master"]');
    const newConfigActionPostActionSuccess = newConfigAction.postActionSuccess;
    newConfigAction.postActionSuccess = (potentiallySavedOrNewEntity, self, master) => {
        newConfigActionPostActionSuccess(potentiallySavedOrNewEntity, self, master);
        const saveAction = centre.$.dom.$.saveAction;
        saveAction._masterReferenceForTestingChanged = function (master) {
            const titleEditor = master.$.editor_4_title;
            titleEditor._editingValue = 'load-tests-configuration';
            titleEditor.commit();
            master.shadowRoot.querySelector('tg-action[role="save"]')._asyncRun();
            saveAction._masterReferenceForTestingChanged = () => {};
        };
        const saveActionPostActionSuccess = saveAction.postActionSuccess;
        saveAction.postActionSuccess = (potentiallySavedOrNewEntity, self, master) => {
            saveActionPostActionSuccess(potentiallySavedOrNewEntity, self, master);
            saveAction.postActionSuccess = saveActionPostActionSuccess;
            resolve('Ok');
        };
        saveAction._run();
        newConfigAction.postActionSuccess = newConfigActionPostActionSuccess;
    };
    newConfigAction._run();
});

/**
 * Returns promise for removing testing Entity Centre configuration for load tests.
 */
export const removeTestingConfig = (centre) => new Promise((resolve, reject) => {
    const deleteConfigAction = centre.$.dom.$.selectionView.shadowRoot.querySelector('tg-ui-action[element-name="tg-CentreConfigDeleteAction-master"]');
    const deleteConfigActionPreAction = deleteConfigAction.preAction;
    deleteConfigAction.preAction = action => {
        deleteConfigAction.preAction = deleteConfigActionPreAction;
        return Promise.resolve(true);
    };
    const deleteConfigActionPostActionSuccess = deleteConfigAction.postActionSuccess;
    deleteConfigAction.postActionSuccess = (potentiallySavedOrNewEntity, self, master) => {
        deleteConfigActionPostActionSuccess(potentiallySavedOrNewEntity, self, master);
        deleteConfigAction.postActionSuccess = deleteConfigActionPostActionSuccess;
        resolve('Ok');
    };
    deleteConfigAction._run();
});
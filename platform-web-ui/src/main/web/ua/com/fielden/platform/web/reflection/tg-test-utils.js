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
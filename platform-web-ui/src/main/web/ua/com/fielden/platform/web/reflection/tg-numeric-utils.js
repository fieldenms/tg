/**
 * Truncates insignificant leading and trailing zeros in number-like string value 'stringVal'.
 *
 * In both decimal / money editors these zeros do not add any significance when editing is performed. So they will be truncated
 * during custom commit value approximations. Integer editor requires only leading zeros to be removed, however this method is used for consistency
 * there too.
 */
export function truncateInsignificantZeros (stringVal) {
    const numberVal = stringVal === '' ? null : (+(stringVal)); // need to be carefull for '' values that convert to 0 when using Number(str) or (+(str))
    return numberVal !== null && !isNaN(numberVal) ? numberVal.toString() : stringVal; // NaNs are most likely filtered using editor capability to filter unwanted chars; the check added here to provide stronger assurance
}

/**
 * Generates a random integer in range [0..max).
 */
export function random (max) {
    return Math.floor(Math.random() * Math.floor(max));
}
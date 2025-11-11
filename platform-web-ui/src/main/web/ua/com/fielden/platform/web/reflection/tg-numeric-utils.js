import { TgAppConfig } from '/app/tg-app-config.js';

const _appConfig = new TgAppConfig();
// A variable that defines a currency symbol, used to represent monetary values as strings.
// This variable is assigned only once.
let currencySymbol = null;

function _getCurrencySymbol () {
    return currencySymbol || '$';
}

// A space used to separate a currency symbol from a numeric part of  when representing monetary value as strings
export const CURRENCY_SYMBOL_SPACE = '\u200A';

/**
 * Used for decimal and money formatting. If the scale value for formatting wasn't specified then the default one is used.
 */
export const DEFAULT_SCALE = 2;

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

/**
 * Formats integer number in to string based on locale. If the value is null then returns empty string.
 *
 * @param locale - custom locale for Number formatting, if present;
 *                 uses app-specific `WebUiBuilder.locale` otherwise.
 */
export function formatInteger (value, locale) {
    if (value !== null) {
        return value.toLocaleString(locale || _appConfig.locale);
    }
    return '';
}

/**
 * Formats number with floating point in to string based on locale. If the value is null then returns empty string.
 *
 * @param locale - custom locale for Number formatting, if present;
 *                 uses app-specific `WebUiBuilder.locale` otherwise.
 */
export function formatDecimal (value, locale, scale, trailingZeros) {
    if (value !== null) {
        const definedScale = typeof scale === 'undefined' || scale === null || scale < 0 || scale > 20 /* 0 and 20 are allowed bounds for scale */ ? DEFAULT_SCALE : scale;
        const options = { maximumFractionDigits: definedScale };
        if (trailingZeros !== false) {
            options.minimumFractionDigits = definedScale;
        }
        return value.toLocaleString(locale || _appConfig.locale, options);
    }
    return '';
}

/**
 * Set the provided currency symbol if previous was empty and provided one is not empty. It means that currency symbol can be set only once.
 *
 * @param {String} newCurrencySymbol - currency symbol to set
 */
export function setCurrencySymbol (newCurrencySymbol) {
    if (!currencySymbol && newCurrencySymbol) {
        currencySymbol = newCurrencySymbol;
    }
}

/**
 * Formats money number in to string based on locale. If the value is null then returns empty string.
 *
 * @param locale - custom locale for Number formatting, if present;
 *                 uses app-specific `WebUiBuilder.locale` otherwise.
 */
export function formatMoney (value, locale, scale, trailingZeros) {
    if (value !== null) {
        const strValue = formatDecimal(Math.abs(value.amount), locale, scale, trailingZeros);
        return (value.amount < 0 ? `-${_getCurrencySymbol()}` : `${_getCurrencySymbol()}`) + CURRENCY_SYMBOL_SPACE + strValue;
    }
    return '';
}
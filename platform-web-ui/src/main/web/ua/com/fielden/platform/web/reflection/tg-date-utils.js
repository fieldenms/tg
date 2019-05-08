import '/resources/components/moment-lib.js';

export const timeZoneFormats = {
    "UTC": {
        "L": "YYYY-MM-DD",
        "L LT": "YYYY-MM-DD HH:mm",
        "L LTS": "YYYY-MM-DD HH:mm:ss.SSS"
    }
}
/**
 * Returns milliseconds from 1-st Jan 1970 in current locale, defined in momentjs.
 * 
 * Please, use this function only in environment with moment(...) function defined.
 * Currently it is used in date-specific unit testing.
 * 
 * This should strictly be used for date properties, which has local time-zone.
 */
export function _millis(fullDateString) { // using strict 'DD/MM/YYYY HH:mm:ss.SSS' format
    return moment(fullDateString, 'DD/MM/YYYY HH:mm:ss.SSS', true).valueOf();
};

export function _millisDateRepresentation(dateMillis, timeZone, portionToDisplay) {
    const millisecondsExist = dateMillis % 1000 !== 0;
    const secondsExist = !millisecondsExist ? (dateMillis / 1000) % 60 !== 0 : true;
    let format;
    if (portionToDisplay) {
        if (portionToDisplay == "DATE") {
            format = "L";
        } else if (portionToDisplay == "TIME") {
            format = millisecondsExist || secondsExist ? "LTS" : "LT";
        } else {
            format = "L " + (millisecondsExist || secondsExist ? "LTS" : "LT");
        }
    } else {
        format = "L " + (millisecondsExist || secondsExist ? "LTS" : "LT");
    }
    return _momentTz(dateMillis, timeZone).format(timeZone ? timeZoneFormats[timeZone][format] : format);
};

/**
 * Performs timeZone-aware momentjs computation.
 *
 * @params -- first parameters need to be specified as for standard moment(...) function
 * @param timeZone -- last parameter is a timeZone, in which momentjs computation will be done (in case of empty timeZone, local timeZone will be used, i.e. simple moment(...) function invoked)
 */
export function _momentTz(input) {
    const args = Array.prototype.slice.call(arguments, 0, -1);
    const timeZone = arguments[arguments.length - 1];
    return timeZone ? moment.tz.apply(null, arguments) : moment.apply(null, args);
};
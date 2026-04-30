import moment from '/resources/polymer/lib/moment-lib.js';

const timeZoneFormats = {
    "UTC": {
        "L": "YYYY-MM-DD",
        "LT": "HH:mm",
        "HH:mm:ss": "HH:mm:ss",
        "LTS": "HH:mm:ss.SSS",
        "L LT": "YYYY-MM-DD HH:mm",
        "L HH:mm:ss": "YYYY-MM-DD HH:mm:ss",
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

/**
 * Calculates 'timeZone' for fixed-tz properties (annotated with @PersistentType(userType = IUtcDateTimeType.class)).
 * Returns empty value for regular date properties.
 *
 * @param prop {Object, likely EntityTypeProp} -- optional meta-property object, optionally containing `timeZone()`.
 */
function _fixedTimeZone(prop) {
    return prop?.timeZone?.();
};

/**
 * Calculates format for concrete 'prop' based on its 'timeZone()' and 'name'.
 *
 * @param prop {Object, likely EntityTypeProp} -- optional meta-property object, optionally containing `timeZone()`.
 */
export function _timeZoneFormat(prop, name) {
    const timeZone = _fixedTimeZone(prop);
    return timeZone ? timeZoneFormats[timeZone][name] : moment.localeData().longDateFormat(name);
};

/**
 * Converts 'dateMillis' to String-based date representation according to property configuration in 'prop' object.
 *
 * @param prop {Object, likely EntityTypeProp} -- optional meta-property object, optionally containing `timeZone()` / `datePortion` / `isDependentTimeZoneMode`.
 * @param portionToDisplay {String} -- "DATE", "TIME" or empty for full date+time representation; this option is taken from 'prop' if not specified
 */
export function _millisDateRepresentation(dateMillis, prop, portionToDisplay) {
    // Create proper 'timeFormat' function based on the 'timeZone' and whether milliseconds / seconds are present.
    const millisecondsExist = dateMillis % 1000 !== 0;
    const secondsExist = !millisecondsExist ? (dateMillis / 1000) % 60 !== 0 : true;
    const timeFormat = () => {
        const fullFormat = _timeZoneFormat(prop, 'LTS');
        const noMillisFormat = fullFormat ? fullFormat.replace('.SSS', '') : 'LT';
        return millisecondsExist ? 'LTS' : secondsExist ? noMillisFormat : 'LT';
    };
    // If 'portionToDisplay' is present and non-empty, use it; otherwise fall back to 'prop.datePortion()'.
    const portion = portionToDisplay || prop?.datePortion?.();
    // Calculate 'format' based on 'portion', or use full format if empty / unknown.
    const format = portion === "DATE" ? "L" : portion === "TIME" ? timeFormat() : "L " + timeFormat();
    const timeZone = _fixedTimeZone(prop);
    return _momentTz(dateMillis, prop).format(timeZone ? timeZoneFormats[timeZone][format] : format);
};

/**
 * In case of independent time-zone mode, enforces real client time-zone for properties with @DependentTimeZoneMode.
 * Returns server time-zone back after `momentComputation()` is completed - for normal operation of all other properties.
 *
 * @param prop {Object, likely EntityTypeProp} -- optional meta-property object, optionally containing `isDependentTimeZoneMode`.
 */
function _enforceDependentTimeZoneModeFor(momentComputation, prop) {
    if (window.TG_APP?.timeZone && prop?.isDependentTimeZoneMode?.()) {
        try {
            moment.tz.setDefault(moment.tz.guess(true));
            return momentComputation();
        }
        finally {
            moment.tz.setDefault(window.TG_APP.timeZone);
        }
    }
    else {
        return momentComputation();
    }
};

/**
 * Performs timeZone-aware momentjs computation.
 *
 * @params -- first parameters need to be specified as for standard moment(...) function
 * @param prop {Object, likely EntityTypeProp} -- optional meta-property object, optionally containing `timeZone()` / `isDependentTimeZoneMode`.
 *                                 May contain a timeZone, in which momentjs computation will be done.
 *                                 In case of empty timeZone, local timeZone will be used, i.e. simple moment(...) function invoked.
 */
export function _momentTz(input) {
    // Drop last argument and save into 'args'.
    const args = Array.prototype.slice.call(arguments, 0, -1);
    // Take last argument, which may contain time-zone.
    const prop = arguments[arguments.length - 1];
    const timeZone = _fixedTimeZone(prop);
    // For the case of fixed-tz property, perform computation in that time-zone.
    if (timeZone) {
        args.push(timeZone);
        return moment.tz.apply(null, args);
    }
    // Otherwise, perform computation in default timezone. Take into account @DependentTimeZoneMode properties.
    return _enforceDependentTimeZoneModeFor(() => moment.apply(null, args), prop);
};

export function _timeZoneHeader () {
    return {"Time-Zone": moment.tz.guess(true)};
};

/**
 * Returns time-zone mode-specific 'now' moment.
 *
 * @param prop {Object, likely EntityTypeProp} -- optional meta-property object, optionally containing `isDependentTimeZoneMode`.
 */
export function now(prop) {
    // In concrete time-zone (e.g. UTC) just use standard method _momentTz for creating 'now' in that time-zone.
    if (_fixedTimeZone(prop)) {
        return _momentTz(prop);
    }
    // In independent time-zone mode we do the following trick:
    // 1. create 'now' moment in current surrogate (equal to server one) time-zone;
    // 2. convert it to real time-zone to be able to format it into our 'real' string;
    // 3. convert it to string that defines moment in our 'real' time-zone;
    // 4. then use that string to create moment in surrogate time-zone.
    // In dependent time-zone mode this trick will return the same moment object as just moment().
    // Take into account @DependentTimeZoneMode properties.
    return _enforceDependentTimeZoneModeFor(() => moment(moment().tz(moment.tz.guess(true)).format('YYYY-MM-DD HH:mm:ss.SSS')), prop);
};
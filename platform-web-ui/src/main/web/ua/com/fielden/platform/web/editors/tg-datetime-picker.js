import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/iron-input/iron-input.js'
import '/resources/polymer/@polymer/iron-icons/iron-icons.js'
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js'
import '/resources/polymer/@polymer/paper-button/paper-button.js'
import '/resources/polymer/@polymer/paper-dialog/paper-dialog.js'
import '/resources/polymer/@polymer/neon-animation/animations/scale-up-animation.js'
import '/resources/polymer/@polymer/neon-animation/animations/fade-out-animation.js'

import '/resources/components/tg-calendar.js';

import {html} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import { TgEditor, createEditorTemplate} from '/resources/editors/tg-editor.js'
import moment from '/resources/polymer/lib/moment-lib.js'; // used for moment.localeData(). ...
import { _momentTz, timeZoneFormats, now } from '/resources/reflection/tg-date-utils.js';
import { tearDownEvent, isTouchEnabled } from '/resources/reflection/tg-polymer-utils.js'

const AFTER = 'AFTER';
const BEFORE = 'BEFORE';

const pickerStyle = html`
    <custom-style>
        <style>
            .date-picker paper-button {
                color: var(--paper-light-blue-500);
                --paper-button-flat-focus-color: var(--paper-light-blue-50);
            }
            .date-picker paper-button:hover {
                background: var(--paper-light-blue-50);
            }
            .date-picker > tg-calendar {
                margin: 0;
                padding: 0;
            }
            .date-picker {
                line-height: normal;
                overflow: auto;
            }
        </style>
    </custom-style>`;
pickerStyle.setAttribute('style', 'display: none;');
document.head.appendChild(pickerStyle.content);

const additionalTemplate = html`
    <style>
        #input[disabled] {
            cursor: text;
        }
       
        .picker-button {
            display: flex;
            width: 24px;
            height: 24px;
            padding: 4px;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>`;
const customInputTemplate = html`
    <iron-input bind-value="{{_editingValue}}" class="custom-input-wrapper">
        <input
            id="input"
            class="custom-input date-input"
            on-change="_onChange"
            on-input="_onInput"
            on-keydown="_onKeydown"
            on-mouseup="_onMouseUp" 
            on-mousedown="_onMouseDown"
            on-focus="_onFocus"
            on-blur="_outFocus"
            disabled$="[[_disabled]]"
            tooltip-text$="[[_getTooltip(_editingValue, _scanAvailable)]]"
            autocomplete="off"/>
    </iron-input>`;
const customIconButtonsTemplate = html`<paper-icon-button class="picker-button custom-icon-buttons" on-tap="_showCalendar" icon="today" disabled$="[[_isCalendarDisabled(_disabled, datePortion)]]" tooltip-text="Show date picker dialog"></paper-icon-buton>`;
const propertyActionTemplate = html`<slot id="actionSlot" name="property-action"></slot>`;

export class TgDatetimePicker extends TgEditor {

    static get template() { 
        return createEditorTemplate(additionalTemplate, html``, customInputTemplate, html``, customIconButtonsTemplate, propertyActionTemplate);
    }

    static get properties () {
        return {
            /**
             * If empty then default timezone should be used for toString and fromString conversions in 'moment()' and 'moment(...)' methods.
             * Otherwise -- the specified timezone should be used in 'moment.tz(timeZone)' and 'moment.tz(..., timeZone)' methods.
             */
            timeZone: {
                type: String,
                value: null
            },
    
            /**
             * Defines what date portion to show: date, time or date & time. If the value is 'DATE' then only date portion of the date property value will be shown,
             * If the value is 'TIME' then only time portion of the date property value will be shown, otherwise date & time wil be shown.
             */
            datePortion: {
                type: String,
                value: null
            },
    
            /**
             * If true -- the empty time portion should be approximated to '23:59:59.999', otherwise -- to '00:00:00.000'.
             */
            timePortionToBecomeEndOfDay: {
                type: Boolean
            },
    
            /////////////////////////////////////////////////////////////////////////////////////////////////////////
            //////////////////////////////////////////// INNER PROPERTIES ///////////////////////////////////////////
            /////////////////////////////////////////////////////////////////////////////////////////////////////////
            // These properties derive from other properties and are considered as 'private' -- need to have '_'   //
            //   prefix and default values specified in 'value' specificator of the property definition (or,       //
            //   alternatively, computing function needs to be specified). 									       //
            /////////////////////////////////////////////////////////////////////////////////////////////////////////
            _isAcceptingDateFromPicker: {
                type: Boolean,
                value: false
            },
    
            /**
             * The formats for approximation. The order of this array is important -- it defines the order in which formats are tried during approximation.
             */
            _timePortionFormats: {
                type: Array,
                value: function () {
                    const fullFormat = moment.localeData().longDateFormat('LTS');
                    const noMillisFormat = fullFormat ? fullFormat.replace('.SSS', '') : 'LTS';
                    return [
                        'h:mm:ss.SSSa',
                        'h:m:ss.SSSa',
                        'H:mm:ss.SSS',
                        'H:m:ss.SSS',
                        'LTS', // e.g. HH:mm:ss.SSS
                        'HH:m:ss.SSS',
                        'h:mm:ssa',
                        'h:m:ssa',
                        'H:mm:ss',
                        'H:m:ss',
                        noMillisFormat, // e.g. HH:mm:ss
                        'HH:m:ss',
                        'LT', // e.g. HH:mm
                        'HH:m',
                        'hmma',
                        'Hmm',
                        'h:mma',
                        'h:ma',
                        'H:mm',
                        'H:m',
                        'ha',
                        'H'
                    ];
                }
            },
    
            /**
             * The property that holds valid approximated 'moment' value in case if approximation has been successful or 'null' otherwise.
             */
            _validMoment: {
                type: Object,
                value: null
            }
        }
    }

    constructor () {
        super();
        moment.parseTwoDigitYear = function (input) {
            var currYear = moment().year();
            return parseInt(input, 10) + ((parseInt(input, 10) < currYear - 2000 + 30) ? 2000 : 1900);
        };
    }
    
    /**
     * Converts the value from string representation (which is used in editing / comm values) into concrete type of this editor component (Number).
     */
    convertFromString (strValue) {
        if (strValue) {
            if (this._validMoment !== null) {
                return this._validMoment.valueOf();
            } else {
                throw 'The entered ' + (this.datePortion === 'TIME' ? 'time' : 'date') + ' is incorrect.';
            }
        } else {
            return null;
        }
    }

    _showCalendar (e) {
        if (!this._isCalendarOpen) {
            this._isCalendarOpen = true;
            var mve = this._createCalendarDialog();
            document.body.appendChild(mve);
            //Opening date picker dialog itself.
            mve.open();
        }
    }

    _isCalendarDisabled (_disabled, datePortion) {
        return _disabled || datePortion === "TIME";
    }

    /**
     * Creates dynamically the 'dom-bind' template, which hold the dialog for the calendar.
     */
    _createCalendarDialog () {
        const self = this;
        const domBind = document.createElement('dom-bind');

        domBind._closedBind = function (e) {
            const dialog = this.$.dateDialog;
            //Removes the registered key down listener.
            document.removeEventListener('keydown', this._onCaptureKeyDown, true);

            document.body.removeChild(dialog);
            document.body.removeChild(this);

            // Setting focus on input -- non-touch devices only.
            if (!isTouchEnabled()) {
                self.decoratedInput().focus();
            }
            self._isCalendarOpen = false;
        }.bind(domBind);

        domBind._acceptDateBind = function () {
            self._isAcceptingDateFromPicker = true;
            const acceptedMoment = _momentTz(this.$.datePicker.selectedDate, self.timeZone)
                .hour(this.$.datePicker.selectedHour)
                .minute(this.$.datePicker.selectedMinute)
                .seconds(this.$.datePicker.seconds)
                .milliseconds(this.$.datePicker.milis);
            self._editingValue = self.convertToString(acceptedMoment.valueOf());
            //self.decoratedInput().focus();
        }.bind(domBind);

        domBind.open = function () {
            var dialog = this.$.dateDialog;
            var datePicker = this.$.datePicker;

            dialog.noCancelOnEscKey = false;

            datePicker.selectedDate = null;
            datePicker.selectedHour = self.timePortionToBecomeEndOfDay ? 23 : 0;
            datePicker.selectedMinute = self.timePortionToBecomeEndOfDay ? 59 : 0;
            datePicker.seconds = self.timePortionToBecomeEndOfDay ? 59 : 0;
            datePicker.milis = self.timePortionToBecomeEndOfDay ? 999 : 0;

            if (self._editingValue && self._editingValue.trim() !== '' && self._validMoment !== null) {
                datePicker.selectedDate = self._validMoment.valueOf();
                datePicker.selectedHour = self._validMoment.hour();
                datePicker.selectedMinute = self._validMoment.minute();
                datePicker.seconds = self._validMoment.seconds();
                datePicker.milis = self._validMoment.milliseconds();
            }

            document.body.appendChild(dialog);
            // let's open the dialog with magical async...
            // this ensures that the dialog is opened after its relocation to body
            setTimeout(function () {
                dialog.open();
            }.bind(this), 1);
        }.bind(domBind);

        domBind._onCaptureKeyDown = function (e) {
            const dialog = this.$.dateDialog;
            if (e.keyCode === 13) {
                this._acceptDateBind();
                dialog.close();
            }
        }.bind(domBind);

        domBind._dialogOpened = function () {
            document.addEventListener('keydown', this._onCaptureKeyDown, true);
            domBind.$.datePicker.addEventListener("tg-accept-date", function (e) {
                this._acceptDateBind();
                this.$.dateDialog.close();
                tearDownEvent(e);
            }.bind(domBind));
        }.bind(domBind);

        domBind.timeZone = self.timeZone;

        const dialogTemplate = document.createElement('template');
        dialogTemplate.innerHTML =
            '<paper-dialog id="dateDialog" class="date-picker layout vertical" modal always-on-top entry-animation="scale-up-animation" exit-animation="fade-out-animation" on-iron-overlay-closed="_closedBind" on-iron-overlay-opened="_dialogOpened">' +
            '<tg-calendar id="datePicker" pick-time time-zone="[[timeZone]]"></tg-calendar>' +
            '<div class="buttons">' +
            '<paper-button dialog-dismiss affirmative>Cancel</paper-button>' +
            '<paper-button dialog-confirm affirmative autofocus on-tap="_acceptDateBind">Ok</paper-button>' +
            '</div>' +
            '</paper-dialog>';

        domBind.appendChild(dialogTemplate);
        return domBind;
    }

    _editingValueChanged (newValue, oldValue) {
        super._editingValueChanged(newValue, oldValue);

        if (this._isAcceptingDateFromPicker) {
            this._isAcceptingDateFromPicker = false;

            this.commit();
        }
    }

    /**
     * Overridden to provide value approximations.
     */
    _commitForDescendants () {
        var approximated = this._approximate(this._editingValue);
        console.debug('COMMIT (value should be approximated): current editingValue = [', this._editingValue, '] approximated = [', approximated, ']');
        if (!this.reflector().equalsEx(approximated, this._editingValue)) {
            console.debug('COMMIT (value should be approximated): change editingValue to [', approximated, ']');
            this._editingValue = approximated;
        }
    }

    /**
     * Approximates the 'dateEditingValue' using formats, generated according to current datePortionFormat, to the standard form like [09/09/2002 11:03:03.003].
     * If approximation is failed -- returns the same 'dateEditingValue'.
     */
    _approximate (dateEditingValue) {
        this._validMoment = null;
        if (dateEditingValue) {
            const valueWithoutSpaces = dateEditingValue.replace(new RegExp(' ', 'g'), '').trim();
            if (valueWithoutSpaces) {
                // at first, check literals:
                this._validMoment = this._tryLiterals(valueWithoutSpaces);
                if (this._validMoment !== null) {
                    return this.convertToString(this._validMoment.valueOf());
                }
                // determine current date portion format for this editor ...
                const datePortionFormat = this.timeZone ? timeZoneFormats[this.timeZone]['L'] : moment.localeData().longDateFormat('L');
                // ... and its separator;
                const separator = datePortionFormat.includes('/') ? '/' : datePortionFormat.includes('-') ? '-' : null;
                // validate separator and ...
                if (!separator) {
                    throw new Error(`Date format [${datePortionFormat}] separator is not supported.`);
                }
                const yearsOnEnding = datePortionFormat[0] !== 'Y';
                // ... placing of the years portion;
                if (yearsOnEnding && datePortionFormat[datePortionFormat.length - 1] !== 'Y') {
                    throw new Error(`Placing of years in date format [${datePortionFormat}] is not supported. Years are only supported on beginning and ending of date format (not in the middle).`);
                }
                const firstSeparatorIndex = dateEditingValue.indexOf(separator);
                if (firstSeparatorIndex > -1) {
                    // if there is at least one separator then date portion is present;
                    //  (please be careful when adding '.' as date portion separator -- it is used to separate seconds from millis in 'ss.SSS')
                    const { secondSeparatorExists, numberOfDigits } = this._calculateNumberOfDigitsAfterLastSeparator(dateEditingValue, separator);
                    if (
                        secondSeparatorExists === true && [1, 2, 4].includes(yearsOnEnding ? numberOfDigits : this._calculateNumberOfDigits(dateEditingValue, BEFORE, firstSeparatorIndex)) // exactly two separators; only 1, 2 and 4 digits for years should be valid
                        || secondSeparatorExists === false && [1, 2].includes(numberOfDigits) // exactly one separator; only 1 and 2 digits for months / days should be valid (numberOfDigitsAfterFirstSeparator)
                    ) {
                        // remove all spaces and insert one after year digits (or [ /2017] or other current year in case of one separator);
                        const valueWithOneSpaceAndYear = this._insertOneSpaceAndYear(valueWithoutSpaces, numberOfDigits, separator, yearsOnEnding).trim(); // never empty -- at least one separator exists
                        // try combined date & time formats for approximation:
                        const datePortionFormats = this._datePortionApproximationFormatsFor(datePortionFormat, separator);
                        this._validMoment = this._tryFormats(valueWithOneSpaceAndYear, this._approximationFormatsFor(datePortionFormat, separator), (stringValue, validMoment, format) => {
                            const indexOfY = format.indexOf('Y');
                            let adjustedMoment = validMoment;
                            if (indexOfY > -1 && format.indexOf('Y', indexOfY + 1) === -1) { // only one Y in the format
                                const strWithDoubleDigitYear = this._convertToDoubleDigitYear(stringValue, indexOfY === 0 ? 0 : this._findSecondSeparatorIndex(stringValue, separator) + 1);
                                adjustedMoment = _momentTz(strWithDoubleDigitYear, format.replace('Y', 'YY'), true, this.timeZone);
                            }
                            if (datePortionFormats.indexOf(format) !== -1 && this.timePortionToBecomeEndOfDay === true) {
                                adjustedMoment.add(1, 'days').subtract(1, 'milliseconds'); // even though original validMoment can be mutated here, it will not be used anywhere else; so it is safe to do this mutation
                            }
                            return adjustedMoment;
                        });
                    }
                } else {
                    // if there is no separator then only time portion is present;
                    // try time formats only for approximation:
                    this._validMoment = this._tryFormats(valueWithoutSpaces, this._timePortionFormats.slice() /* the copy is made  */);
                }
                if (this._validMoment !== null) {
                    return this.convertToString(this._validMoment.valueOf());
                }
            }
        }
        return dateEditingValue;
    }

    /**
     * Generates approximation formats for date portion from default date portion format and its separator.
     * Only 'MM', 'DD' and 'YYYY' parts are supported. Only '/' and '-' separators are supported.
     */
    _datePortionApproximationFormatsFor (datePortionFormat, separator) {
        const parts = datePortionFormat.split(separator);
        const partFormats = part => part.length === 2 ? [part.toUpperCase(), part[0].toUpperCase()] : ['YYYY', 'YY', 'Y']; // converts from 'MM' to ['MM', 'M']; from 'DD' to ['DD', 'D']; and from 'YYYY' to ['YYYY', 'YY', 'Y']
        const first = partFormats(parts[0]);
        const second = partFormats(parts[1]);
        const third = partFormats(parts[2]);
        
        const datePortionFormats = [
            'L'
        ];
        // The order, by which formats will be generated, is not really important.
        // E.g. the very first format will still be widest, either it will be 'YYYY/MM/DD' or 'MM/YYYY/DD' or 'DD/MM/YYYY'.
        // Also, newest (>=2.30.1) 'moment' lib requires, for example, 'MM' to match 09 month ('M' is not sufficient).
        first.forEach(f => second.forEach(s => third.forEach(t => datePortionFormats.push(f + separator + s + separator + t))));
        return datePortionFormats;
    }

    /**
     * Generates full date & time approximation formats from default date portion format and its separator.
     * Only 'MM', 'DD' and 'YYYY' parts are supported. Only '/' and '-' separators are supported.
     * _timePortionFormats are not included.
     */
     _approximationFormatsFor (datePortionFormat, separator) {
        const resultFormats = [];
        const datePortionFormats = this._datePortionApproximationFormatsFor(datePortionFormat, separator);
        this._timePortionFormats.forEach(tpFormat => datePortionFormats.forEach(dpFormat => resultFormats.push(dpFormat + ' ' + tpFormat))); // date and time combined
        datePortionFormats.forEach(dpFormat => resultFormats.push(dpFormat));  // date only
        return resultFormats;
    }

    _tryLiterals (editingValue) {
        const upperCasedValue = editingValue[0].toUpperCase();
        // In concrete time-zone (e.g. UTC) just use standard method _momentTz for creating 'now' in that time-zone.
        // Otherwise use standard now() function.
        const convertedMoment = this.timeZone ? _momentTz(this.timeZone) : now();

        if ('T' === upperCasedValue) {
            const todayMoment = convertedMoment.startOf("day");
            return this.timePortionToBecomeEndOfDay === true ? todayMoment.add(1, 'days').subtract(1, 'milliseconds') : todayMoment;
        } else if ('N' === upperCasedValue) {
            return convertedMoment.startOf('minute');
        }
        return null;
    }

    /**
     * Tries the formats one-by-one and returns the valid 'moment' object in case where some format has been successed.
     * In case of all formats failure -- returns 'null'.
     * 
     * @param adjustValidMoment -- function with parameters (stringValue, validMoment, format) to adjust 'validMoment', that is valid in 'format', to some other custom valid moment
     */
    _tryFormats (stringValue, formats, adjustValidMoment) {
        if (formats.length === 0) {
            return null;
        } else {
            const firstFormat = formats[0];
            let tryingMoment = _momentTz(stringValue, firstFormat, true, this.timeZone);
            if (tryingMoment.isValid()) {
                if (adjustValidMoment) {
                    return adjustValidMoment(stringValue, tryingMoment, firstFormat);
                }
                return tryingMoment;
            } else {
                formats.shift(); // first element is removed
                return this._tryFormats(stringValue, formats, adjustValidMoment);
            }
        }
    }

    _convertToDoubleDigitYear (oneDigitYearString, indexOfY) {
        return oneDigitYearString.slice(0, indexOfY) + '0' + oneDigitYearString.slice(indexOfY);
    }

    _findSecondSeparatorIndex (str, separator) {
        const firstSeparatorIndex = str.indexOf(separator);
        if (firstSeparatorIndex === -1) {
            return -1;
        } else {
            return str.indexOf(separator, firstSeparatorIndex + 1);
        }
    }

    _calculateNumberOfDigitsAfterLastSeparator (str, separator) {
        const secondSeparatorIndex = this._findSecondSeparatorIndex(str, separator);
        let numberOfDigits;
        if (secondSeparatorIndex === -1) {
            numberOfDigits = this._calculateNumberOfDigits(str, AFTER, str.indexOf(separator));
        } else {
            numberOfDigits = this._calculateNumberOfDigits(str, AFTER, secondSeparatorIndex);
        }
        return {
            secondSeparatorExists: secondSeparatorIndex > -1,
            numberOfDigits: numberOfDigits
        };
    }

    /**
     * Calculates number of digits before / after separator meaning that we count from first / last separator in appropriate direction until the space or end of string.
     */
    _calculateNumberOfDigits (str, afterOrBefore, separatorIndex) {
        if (separatorIndex === -1) {
            throw `_calculateNumberOfDigits[${afterOrBefore} separator index]: index of separator should not be -1.`;
        }
        const strWithSpaces = (afterOrBefore === AFTER ? str.substring(separatorIndex + 1) : str.substring(0, separatorIndex)).trim();
        const split = strWithSpaces.split(' ');
        const strWithoutSpaces = split[afterOrBefore === AFTER ? 0 : split.length - 1];
        return strWithoutSpaces.length;
    }

    /**
     * Inserts one space after last digits after last separator; inserts 'currYear' with space if years are not present (one separator).
     */
    _insertOneSpaceAndYear (strWithoutSpaces, numberOfDigitsAfterLastSeparator, separator, yearsOnEnding) {
        if (numberOfDigitsAfterLastSeparator === 0) {
            return strWithoutSpaces;
        } else {
            const secondSeparatorIndex = this._findSecondSeparatorIndex(strWithoutSpaces, separator);
            if (secondSeparatorIndex === -1) { // in this case, one separator could exist. Then need to insert current year.
                const firstSeparatorIndex = strWithoutSpaces.indexOf(separator);
                if (firstSeparatorIndex === -1) {
                    return strWithoutSpaces;
                } else {
                    const insertionPoint = firstSeparatorIndex + numberOfDigitsAfterLastSeparator + 1;
                    const currYearStr = _momentTz(this.timeZone).format('YYYY');
                    return yearsOnEnding // years are only supported on beginning and ending of datePortionFormat (not in the middle, like MM-YYYY-DD)
                        ? strWithoutSpaces.slice(0, insertionPoint) + separator + currYearStr + ' ' + strWithoutSpaces.slice(insertionPoint)
                        : currYearStr + separator + strWithoutSpaces.slice(0, insertionPoint) + ' ' + strWithoutSpaces.slice(insertionPoint)
                }
            }
            const insertionPoint = secondSeparatorIndex + numberOfDigitsAfterLastSeparator + 1;
            return strWithoutSpaces.slice(0, insertionPoint) + ' ' + strWithoutSpaces.slice(insertionPoint);
        }
    }
}

customElements.define('tg-datetime-picker', TgDatetimePicker);
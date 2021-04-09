import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/iron-input/iron-input.js'
import '/resources/polymer/@polymer/iron-icons/iron-icons.js'
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js'
import '/resources/polymer/@polymer/paper-button/paper-button.js'
import '/resources/polymer/@polymer/paper-dialog/paper-dialog.js'
import '/resources/polymer/@polymer/paper-checkbox/paper-checkbox.js'
import '/resources/polymer/@polymer/neon-animation/animations/scale-up-animation.js'
import '/resources/polymer/@polymer/neon-animation/animations/fade-out-animation.js'

import '/resources/components/tg-calendar.js';

import {html} from '/resources/polymer/@polymer/polymer/polymer-element.js';

import { TgEditor, createEditorTemplate} from '/resources/editors/tg-editor.js'
import { _momentTz, timeZoneFormats } from '/resources/reflection/tg-date-utils.js';
import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js'
import { TgAppConfig } from '/app/tg-app-config.js';

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
                -webkit-overflow-scrolling: touch;
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
            disabled$="[[_disabled]]"
            tooltip-text$="[[_getTooltip(_editingValue)]]"
            autocomplete="off"/>
    </iron-input>`;
const customIconButtonsTemplate = html`<paper-icon-button class="picker-button custom-icon-buttons" on-tap="_showCalendar" icon="today" disabled$="[[_isCalendarDisabled(_disabled, datePortion)]]" tooltip-text="Show date picker dialog"></paper-icon-buton>`;
const propertyActionTemplate = html`<slot name="property-action"></slot>`;

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
                    return [
                        'h:m:ss.SSSa',
                        'H:m:ss.SSS',
                        'LTS', // e.g. HH:mm:ss.SSS
                        'LT', // e.g. HH:mm
                        'hmma',
                        'Hmm',
                        'h:ma',
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
        this._appConfig = new TgAppConfig();
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

            //Setting focus on input.
            self.decoratedInput().focus();
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
                this._validMoment = this._tryLiterals(valueWithoutSpaces);
                if (this._validMoment !== null) {
                    return this.convertToString(this._validMoment.valueOf());
                }
                // determine current date portion format for this editor;
                const datePortionFormat = this.timeZone ? timeZoneFormats[this.timeZone]['L'] : this._appConfig.dateFormat;
                const separator = datePortionFormat.includes('/') ? '/' : datePortionFormat.includes('-') ? '-' : null;
                if (!separator) {
                    throw new Error(`Date format [${datePortionFormat}] separator is not supported.`);
                }
                if (dateEditingValue.indexOf(separator) > -1) {
                    const numberOfDigitsAfterLastSeparator = this._calculateNumberOfDigitsAfterLastSeparator(dateEditingValue, separator);
                    const numberOfDigits = numberOfDigitsAfterLastSeparator.number;
                    if (
                        numberOfDigitsAfterLastSeparator.secondSeparatorExists === true && (numberOfDigits === 1 || numberOfDigits === 2 || numberOfDigits === 4) // exactly two slashes; if numberOfDigitsAfterSecondSlash equals 3, 5 or more -- the string could be potentially valid in formats like '../../Y ...'; but they should not be valid -- only 1, 2 and 4 digits for years should be valid.
                        || numberOfDigitsAfterLastSeparator.secondSeparatorExists === false && (numberOfDigits === 1 || numberOfDigits === 2) // exactly one slash; if numberOfDigitsAfterFirstSlash equals 1 or 2 -- it could be represented as valid digits for month.
                    ) {
                        // remove all spaces and insert one after year digits (or [ /2017] or other current year in case of one slash)
                        const valueWithoutSpacesWithYear = this._insertOneSpaceAndYear(dateEditingValue.replace(new RegExp(' ', 'g'), ''), numberOfDigits, separator, datePortionFormat).trim();
                        if (valueWithoutSpacesWithYear) {
                            this._validMoment = this._tryFormats(valueWithoutSpacesWithYear, this._formatsFor(datePortionFormat, separator), this._datePortionFormatsFor(datePortionFormat, separator), separator);
                        }
                    }
                } else {
                    this._validMoment = this._tryFormats(valueWithoutSpaces, this._timePortionFormats.slice() /* the copy is made  */, [] /* not needed */, separator);
                }
                if (this._validMoment !== null) {
                    return this.convertToString(this._validMoment.valueOf());
                }
            }
        }
        return dateEditingValue;
    }

    _datePortionFormatsFor (datePortionFormat, separator) {
        const parts = datePortionFormat.split(separator);
        const partFormats = part => part.length === 2 ? part[0].toUpperCase() : ['YYYY', 'YY', 'Y'];
        const first = partFormats(parts[0]);
        const second = partFormats(parts[1]);
        const third = partFormats(parts[2]);
        
        const datePortionFormats = [
            'L'
        ];
        if (Array.isArray(first)) {
            first.forEach(f => datePortionFormats.push(f + separator + second + separator + third));
        } else if (Array.isArray(second)) {
            second.forEach(s => datePortionFormats.push(first + separator + s + separator + third));
        } else {
            third.forEach(t => datePortionFormats.push(first + separator + second + separator + t));
        }
        return datePortionFormats;
    }

    _formatsFor (datePortionFormat, separator) {
        const resultFormats = [];
        const datePortionFormats = this._datePortionFormatsFor(datePortionFormat, separator);
        this._timePortionFormats.forEach(tpFormat => datePortionFormats.forEach(dpFormat => resultFormats.push(dpFormat + ' ' + tpFormat)));
        datePortionFormats.forEach(dpFormat => resultFormats.push(dpFormat));
        this._timePortionFormats.forEach(tpFormat => resultFormats.push(tpFormat));
        return resultFormats;
    }

    _tryLiterals (editingValue) {
        const upperCasedValue = editingValue[0].toUpperCase();
        // in concrete time-zone (e.g. UTC) just use standard method _momentTz for creating 'now' in that time-zone;
        // otherwise in independent time-zone mode we do the following trick:
        // 1. create 'now' moment in current surrogate (equal to server one) time-zone
        // 2. convert it to real time-zone to be able to format it into our 'real' string
        // 3. convert it to string that defines moment in our 'real' time-zone
        // 4. than use that string to create moment in surrogate time-zone;
        // in dependent time-zone mode this trick will return the same moment object as just moment().
        const convertedMoment = this.timeZone ? _momentTz(this.timeZone) : moment(moment().tz(moment.tz.guess(true)).format('YYYY-MM-DD HH:mm:ss.SSS'));

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
     */
    _tryFormats (stringValue, formats, datePortionFormats, separator) {
        if (formats.length === 0) {
            return null;
        } else {
            const firstFormat = formats[0];
            let tryingMoment = _momentTz(stringValue, firstFormat, true, this.timeZone);
            if (tryingMoment.isValid()) {
                const indexOfY = firstFormat.indexOf('Y');
                if (indexOfY > -1 && firstFormat.indexOf('Y', indexOfY + 1) === -1) { // only one Y in the format
                    tryingMoment = _momentTz(this._convertToDoubleDigitYear(stringValue, indexOfY === 0 ? 0 : this._findSecondSeparatorIndex(stringValue, separator) + 1), firstFormat.replace('Y', 'YY'), true, this.timeZone);
                }
                if (datePortionFormats.indexOf(firstFormat) !== -1 && this.timePortionToBecomeEndOfDay === true) {
                    tryingMoment.add(1, 'days').subtract(1, 'milliseconds');
                }
                return tryingMoment;
            } else {
                formats.shift(); // first element is removed
                return this._tryFormats(stringValue, formats, datePortionFormats, separator);
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
            numberOfDigits = this._calculateNumberOfDigitsAfterIndex(str, str.indexOf(separator));
        } else {
            numberOfDigits = this._calculateNumberOfDigitsAfterIndex(str, secondSeparatorIndex);
        }
        return {
            firstSeparatorExists: str.indexOf(separator) > -1,
            secondSeparatorExists: secondSeparatorIndex > -1,
            number: numberOfDigits
        };
    }

    _calculateNumberOfDigitsAfterIndex (str, separatorIndex) {
        if (separatorIndex === -1) {
            throw '_calculateNumberOfDigitsAfterIndex: index of separator should not be -1.';
        }
        let numberOfDigits = 0;
        for (let index = separatorIndex + 1; index < str.length; index++) {
            if (' ' === str[index]) {
                if (numberOfDigits > 0) {
                    return numberOfDigits;
                }
            } else {
                // assuming that not ' ' is a digit
                numberOfDigits++;
            }
        }
        return numberOfDigits;
    }

    /**
     * Inserts one space after year digits (two slashes exist) or '/currYear ' after month digits (one slash exists).
     */
    _insertOneSpaceAndYear (strWithoutSpaces, numberOfDigitsAfterLastSeparator, separator, datePortionFormat) {
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
                    return datePortionFormat[0] !== 'Y' // years are only supported on beginning and ending of datePortionFormat (not in the middle, like MM-YYYY-DD)
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
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
import { _momentTz, _millisDateRepresentation, timeZoneFormats } from '/resources/reflection/tg-date-utils.js';
import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js'

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
            _formats: {
                type: Array,
                value: function () {
                    return [
                        'L LTS',
                        'D/M/YYYY h:m:ss.SSSa',
                        'D/M/YY h:m:ss.SSSa',
                        'D/M/Y h:m:ss.SSSa',
                        'D/M/YYYY H:m:ss.SSS',
                        'D/M/YY H:m:ss.SSS',
                        'D/M/Y H:m:ss.SSS',
                        'L LT',
                        'D/M/YYYY hmma',
                        'D/M/YY hmma',
                        'D/M/Y hmma',
                        'D/M/YYYY Hmm',
                        'D/M/YY Hmm',
                        'D/M/Y Hmm',
                        'D/M/YYYY h:ma',
                        'D/M/YY h:ma',
                        'D/M/Y h:ma',
                        'D/M/YYYY H:m',
                        'D/M/YY H:m',
                        'D/M/Y H:m',
                        'D/M/YYYY ha',
                        'D/M/YY ha',
                        'D/M/Y ha',
                        'D/M/YYYY H',
                        'D/M/YY H',
                        'D/M/Y H',
                        'D/M/YYYY',
                        'D/M/YY',
                        'D/M/Y'
                    ];
                }
            },
    
            /**
             * The formats for approximation. The order of this array is important -- it defines the order in which formats are tried during approximation.
             */
            _timePortionFormats: {
                type: Array,
                value: function () {
                    return [
                        'LTS',
                        'h:m:ss.SSSa',
                        'H:m:ss.SSS',
                        'LT',
                        'hmma',
                        'Hmm',
                        'h:ma',
                        'H:m',
                        'ha',
                        'H',
                        'T', //Means today,
                        't', //Means today
                        'N',  //Means now
                        'n' //Means now
                    ];
                }
            },
    
            /**
             * The date picker literals for date approximaton.
             */
            _literals: {
                type: Array,
                value: function () {
                    return [
                        'T',
                        'N'
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
            console.debug('parseTwoDigitYear: [', input, '] currYear ', currYear);
            return parseInt(input, 10) + ((parseInt(input, 10) < currYear - 2000 + 30) ? 2000 : 1900);
        };
    }
    
    /**
     * Converts the value from string representation (which is used in editing / comm values) into concrete type of this editor component (Number).
     */
    convertFromString (strValue) {
        // console.log(moment.localeData().longDateFormat("LT").toLowerCase().indexOf("a"));
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

    /**
     * Converts the value into string representation (which is used in editing / comm values).
     */
    convertToString (value) {
        if (value === null) {
            return "";
        } else {
            return _millisDateRepresentation(value, this.timeZone, this.datePortion);
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
            self._editingValue = _millisDateRepresentation(acceptedMoment.valueOf(), self.timeZone, self.datePortion);
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
            '<paper-dialog id="dateDialog" class="date-picker layout vertical" modal entry-animation="scale-up-animation" exit-animation="fade-out-animation" on-iron-overlay-closed="_closedBind" on-iron-overlay-opened="_dialogOpened">' +
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
     * Approximates the 'dateEditingValue' using registered '_formats' to the standard form like [09/09/2002 11:03 AM].
     * If approximation is failed -- returns the same 'dateEditingValue'.
     */
    _approximate (dateEditingValue) {
        this._validMoment = null;
        if (dateEditingValue) {
            const firstSlashIndex = dateEditingValue.indexOf('/');
            if (firstSlashIndex > -1) {
                // remove all spaces and insert one after year digits (or [ /2017] or other current year in case of one slash)
                const numberOfDigitsAfterLastSlash = this._calculateNumberOfDigitsAfterLastSlash(dateEditingValue);
                const numberOfDigits = numberOfDigitsAfterLastSlash.number;
                let aproximated = undefined;
                if (numberOfDigitsAfterLastSlash.secondSlashExists === true) { // exactly two slashes
                    // If numberOfDigitsAfterSecondSlash equals 3, 5 or more -- the string could be potentially valid in formats like '../../Y ...'
                    // But they should not be valid. Only 1, 2 and 4 digits for years should be valid.
                    if (numberOfDigits === 1 || numberOfDigits === 2 || numberOfDigits === 4) {
                        aproximated = this._approximateWithNumberOfDigits(dateEditingValue, numberOfDigits);
                    }
                } else { // exactly one slash
                    // If numberOfDigitsAfterFirstSlash equals 1 or 2 -- it could be represented as valid digits for month.
                    if (numberOfDigits === 1 || numberOfDigits === 2) {
                        aproximated = this._approximateWithNumberOfDigits(dateEditingValue, numberOfDigits);
                    }
                }
                if (aproximated !== undefined) {
                    return aproximated;
                }
            } else if (this._isLiteral(dateEditingValue)) {
                this._validMoment = this._tryLiterals(dateEditingValue);
                if (this._validMoment !== null) {
                    return _millisDateRepresentation(this._validMoment.valueOf(), this.timeZone, this.datePortion);
                }
            } else if (this.timeZone === 'UTC' && dateEditingValue.indexOf('-') >= 0) { //As the last resort try utc formatting
                this._validMoment = this._tryUTCFormats(dateEditingValue);
                if (this._validMoment !== null) {
                    return _millisDateRepresentation(this._validMoment.valueOf(), this.timeZone, this.datePortion);
                }
            } else {
                const value = dateEditingValue.replace(new RegExp(' ', 'g'), '').trim();
                if (value) {
                    this._validMoment = this._tryTimePortionFormats(value, this._timePortionFormats.slice() /* the copy is made  */);
                    if (this._validMoment !== null) {
                        return _millisDateRepresentation(this._validMoment.valueOf(), this.timeZone, this.datePortion);
                    }
                }
            }
        }
        return dateEditingValue;
    }

    _isLiteral (value) {
        const upperCasedValue = value[0].toUpperCase();
        return this._literals.indexOf(upperCasedValue) >= 0;
    }

    _tryLiterals (editingValue) {
        const upperCasedValue = editingValue[0].toUpperCase();
        if ('T' === upperCasedValue) {
            const todayMoment = moment().startOf("day");
            return this.timePortionToBecomeEndOfDay === true ? todayMoment.add(1, 'days').subtract(1, 'milliseconds') : todayMoment;
        } else if ('N' === upperCasedValue) {
            return moment().startOf('minute');
        }
        return null;
    }

    _approximateWithNumberOfDigits (dateEditingValue, numberOfDigits) {
        const value = this._insertOneSpace(dateEditingValue.replace(new RegExp(' ', 'g'), ''), numberOfDigits).trim();
        if (value) {
            this._validMoment = this._tryFormats(value, this._formats.slice() /* the copy is made  */);
            if (this._validMoment !== null) {
                return _millisDateRepresentation(this._validMoment.valueOf(), this.timeZone, this.datePortion);
            }
        }
        return undefined;
    }

    /**
     * Tries UTC formats one by one and returns valid moment or null if there are no appropriate format.
     */
    _tryUTCFormats (stringValue) {
        const timeZone = this.timeZone;
        const tryingMoment = Object.values(timeZoneFormats['UTC'])
            .map(f => _momentTz(stringValue, f, true, timeZone))
            .find(m => m.isValid());
        return tryingMoment ? tryingMoment : null;
    }

    /**
     * Tries the formats one-by-one and returns the valid 'moment' object in case where some format has been successed.
     * In case of all formats failure -- returns 'null'.
     */
    _tryFormats (stringValue, formats) {
        if (formats.length === 0) {
            return null;
        } else {
            console.debug('formats', formats);
            const firstFormat = formats[0];
            let tryingMoment = _momentTz(stringValue, firstFormat, true, this.timeZone);
            if (tryingMoment.isValid()) {
                if (['D/M/Y H:m:ss.SSS', 'D/M/Y h:m:ss.SSSa', 'D/M/Y hmma', 'D/M/Y Hmm', 'D/M/Y h:ma', 'D/M/Y H:m', 'D/M/Y ha', 'D/M/Y H', 'D/M/Y'].indexOf(firstFormat) !== -1) {
                    tryingMoment = _momentTz(this._convertToDoubleDigitYear(stringValue), firstFormat.replace('D/M/Y', 'D/M/YY'), true, this.timeZone);
                }
                if (['D/M/YYYY', 'D/M/YY', 'D/M/Y'].indexOf(firstFormat) !== -1 && this.timePortionToBecomeEndOfDay === true) {
                    tryingMoment.add(1, 'days').subtract(1, 'milliseconds');
                }
                return tryingMoment;
            } else {
                formats.shift(); // first element is removed
                return this._tryFormats(stringValue, formats);
            }
        }
    }

    /**
     * Tries time portion formats one-by-one and returns the valid 'moment' object in case where some format has been successed.
     * In case of all formats failure -- returns 'null'.
     */
    _tryTimePortionFormats (stringValue, formats) {
        if (formats.length === 0) {
            return null;
        } else {
            console.debug('time portion formats', formats);
            var firstFormat = formats[0];
            var tryingMoment = _momentTz(stringValue, firstFormat, true, this.timeZone);
            if (tryingMoment.isValid()) {
                return tryingMoment;
            } else {
                formats.shift(); // first element is removed
                return this._tryFormats(stringValue, formats);
            }
        }
    }

    _convertToDoubleDigitYear (oneDigitYearString) {
        var insertionPoint = this._findSecondSlashIndex(oneDigitYearString) + 1;
        return oneDigitYearString.slice(0, insertionPoint) + "0" + oneDigitYearString.slice(insertionPoint);
    }

    _findSecondSlashIndex (str) {
        var firstSlashIndex = str.indexOf('/');
        if (firstSlashIndex === -1) {
            return -1;
        } else {
            var secondSlashIndex = str.indexOf('/', firstSlashIndex + 1);
            if (secondSlashIndex === -1) {
                return -1;
            }
            return secondSlashIndex;
        }
    }

    _calculateNumberOfDigitsAfterLastSlash (str) {
        var secondSlashIndex = this._findSecondSlashIndex(str);
        var numberOfDigits = null;
        if (secondSlashIndex === -1) {
            var firstSlashIndex = str.indexOf('/');
            numberOfDigits = this._calculateNumberOfDigitsAfterIndex(str, firstSlashIndex);
        } else {
            numberOfDigits = this._calculateNumberOfDigitsAfterIndex(str, secondSlashIndex);
        }
        return {
            secondSlashExists: secondSlashIndex > -1,
            number: numberOfDigits
        };
    }

    _calculateNumberOfDigitsAfterIndex (str, slashIndex) {
        if (slashIndex === -1) {
            throw '_calculateNumberOfDigitsAfterIndex: index of slash should not be -1.';
        }
        var numberOfDigits = 0;
        for (var index = slashIndex + 1; index < str.length; index++) {
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
    _insertOneSpace (strWithoutSpaces, numberOfDigitsAfterLastSlash) {
        if (numberOfDigitsAfterLastSlash === 0) {
            return strWithoutSpaces;
        } else {
            var secondSlashIndex = this._findSecondSlashIndex(strWithoutSpaces);
            if (secondSlashIndex === -1) { // in this case, one slash could exist. Then need to insert current year after it.
                var firstSlashIndex = strWithoutSpaces.indexOf('/');
                if (firstSlashIndex === -1) {
                    return strWithoutSpaces;
                } else {
                    var insertionPoint = firstSlashIndex + numberOfDigitsAfterLastSlash + 1;
                    var currYearStr = _momentTz(this.timeZone).format('YYYY');
                    return strWithoutSpaces.slice(0, insertionPoint) + '/' + currYearStr + ' ' + strWithoutSpaces.slice(insertionPoint);
                }
            }
            var insertionPoint = secondSlashIndex + numberOfDigitsAfterLastSlash + 1;
            return strWithoutSpaces.slice(0, insertionPoint) + ' ' + strWithoutSpaces.slice(insertionPoint);
        }
    }
}

customElements.define('tg-datetime-picker', TgDatetimePicker);
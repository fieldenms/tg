import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-media-query/iron-media-query.js'
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/polymer/@polymer/neon-animation/neon-animated-pages.js';
import '/resources/polymer/@polymer/neon-animation/animations/fade-in-animation.js';
import '/resources/polymer/@polymer/neon-animation/animations/fade-out-animation.js';

import '/app/tg-app-config.js';
import '/resources/components/tg-number-selector.js';
import '/resources/components/tg-month-selector.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import {_momentTz} from '/resources/reflection/tg-date-utils.js';

const template = html`
    <style>
        .col {
            width: 14em;
            height: 15em;
            /*font-size: 24px;*/
        }
        .day-name {
            background-color: #0288D1;
            color: white;
        }
        .header {
            height: 2.2em;
            font-size: 14px;
        }
        .selected-date {
            background-color: #03A9F4;
            color: white !important;
        }
        .selected-month {
            font-size: 24px;
            cursor: pointer;
        }
        .selected-day {
            font-size: 56px;
            cursor: pointer;
        }
        .selected-year {
            font-size: 24px;
            color: #81D4FA;
            cursor: pointer;
        }
        .time-picker {
            background-color: #03A9F4;
            color: white;
            padding-bottom: 16px;
        }
        .time {
            font-size: 18px;
            height: 18px;
            margin: 0 1px;
            cursor: pointer;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <tg-app-config id="appConfig"></tg-app-config>
    <iron-media-query query="[[_calcMobileQuery()]]" query-matches="{{_phoneScreen}}"></iron-media-query>
    <div class$="[[_calcCalendarLayout(_phoneScreen)]]">
        <div class="col layout vertical">
            <div class="header day-name layout vertical center-center">[[selectedDayName]]</div>
            <div class="selected-date layout vertical center justified" style="padding: 10px;">
                <div class="selected-month" on-tap="_showMonthSelector">[[selectedMonthName]]</div>
                <div class="selected-day" on-tap="_showDefaultPage">[[selectedDay]]</div>
                <div class="selected-year" on-tap="_showYearSelector">[[selectedYear]]</div>
            </div>
            <template is="dom-if" if="[[pickTime]]">
                <div class="time-picker layout horizontal center-center">
                    <div class="time" on-tap="_showHourSelector">[[_formatHours(selectedHour, showMeridian)]]</div>
                    <div class="time">:</div>
                    <div class="time" on-tap="_showMinuteSelector">[[_formatMinute(selectedMinute)]]</div>
                    <div class="time" on-tap="_changeMeridan" hidden$="[[!showMeridian]]">[[meridian]]</div>
                </div>
            </template>
        </div>
        <div class="col relative">
            <neon-animated-pages class="fit" id="pages" selected=[[_selectedPage]] attr-for-selected="page-name" entry-animation="fade-in-animation" exit-animation="fade-out-animation" on-iron-select="_onPageChange">
                <tg-month-selector id="defaultPage" page-name="defaultPage" selected-date="{{selectedDate}}" time-zone="[[timeZone]]"></tg-month-selector>
                <tg-number-selector page-name="yearSelector" selected-number="{{selectedYear}}" on-number-selected="_showDefaultPage"></tg-number-selector>
                <tg-number-selector page-name="monthSelector" selected-number="{{selectedMonth}}" lower-bound="0" upper-bound="11" formatter="[[formatMonth]]" on-number-selected="_showDefaultPage"></tg-number-selector>
                <tg-number-selector page-name="hourSelector" selected-number="{{selectedHour}}" lower-bound="[[_getHourLowerBound(showMeridian)]]" upper-bound="[[_getHourUpperBound(showMeridian)]]" on-number-selected="_timeSelected"></tg-number-selector>
                <tg-number-selector page-name="minuteSelector" selected-number="{{selectedMinute}}" lower-bound="0" upper-bound="59" on-number-selected="_timeSelected"></tg-number-selector>
            </neon-animated-pages>
        </div>
    </div>`;

template.setAttribute('strip-whitespace', '');

(function () {
    var meridians = ["AM", "PM"];
    var zeroPad = function (str, num) {
        str = (str && str.toString()) || "";
        return str.length < num ? zeroPad("0" + str, num) : str;
    };
    Polymer({
        _template: template,

        is: "tg-calendar",

        properties: {
            /**
             * If empty then default timezone should be used for toString and fromString conversions in 'moment()' and 'moment(...)' methods.
             * Otherwise -- the specified timezone should be used in 'moment.tz(timeZone)' and 'moment.tz(..., timeZone)' methods.
             */
            timeZone: {
                type: String
            },

            selectedDate: {
                type: Number,
                notify: true,
                observer: "_selectedDateChanged"
            },
            selectedYear: {
                type: Number
            },
            selectedMonth: {
                type: Number
            },
            selectedHour: {
                type: Number,
                notify: true
            },
            selectedMinute: {
                type: Number,
                notify: true
            },
            pickTime: {
                type: Boolean,
                reflectToAttribute: true,
                value: false
            },
            meridian: {
                type: String,
                notify: true,
                value: "AM"
            },
            showMeridian: {
                type: Boolean,
                value: moment.localeData().longDateFormat("LT").toLowerCase().indexOf("a") >= 0
            },
            seconds: Number,
            milis: Number,
            _months: Array
        },

        observers: ["_selectYearMonthDay(selectedYear, selectedMonth, selectedDay)"],

        ready: function () {
            this._selectedPage = 'defaultPage';
            this.formatMonth = (function(monthNumber) {
                return this._months[monthNumber];
            }).bind(this);
            this._months = moment.monthsShort();
        },

        attached: function () {
            var todayDate = _momentTz(this.timeZone).startOf('minute');
            this.selectedDate = this.selectedDate || todayDate.valueOf();
            this.selectedHour = (typeof this.selectedHour !== 'undefined' && this.selectedHour !== null) ? this.selectedHour : todayDate.hour();
            this.selectedMinute = (typeof this.selectedMinute !== 'undefined' && this.selectedMinute !== null) ? this.selectedMinute : todayDate.minutes();
            this.seconds = this.seconds || 0;
            this.milis = this.milis || 0;
        },

        /**
         * Updates the calendar information according to specified date.
         */
        _adjustDate: function (newMoment) {
            this.selectedDayName = newMoment.format("dddd");
            this.selectedMonthName = newMoment.format("MMM").toUpperCase();
            this.selectedDay = null;
            this.selectedMonth = null;
            this.selectedYear = null;
            this.selectedDay = newMoment.date();
            this.selectedMonth = newMoment.month();
            this.selectedYear = newMoment.year();
        },

        /**
         * Changes selected date according to changed year and month.
         */
        _selectYearMonthDay: function (newYear, newMonth, newDay) {
            if (newYear !== null && newMonth !== null && newDay !== null) {
                const yearMonth = { year: newYear, month: newMonth, day: 1 };
                const lastDayOfTheMonth = _momentTz(yearMonth, this.timeZone).daysInMonth();

                const computedDay = newDay > lastDayOfTheMonth ? lastDayOfTheMonth : newDay;
                const yearMonthDay = { year: newYear, month: newMonth, day: computedDay };
                this.selectedDate = _momentTz(yearMonthDay, this.timeZone).valueOf();
            }
        },

        /**
         * Listens when the selected date changes.
         */
        _selectedDateChanged: function (newValue, oldValue) {
            if (newValue !== null) {
                var newMoment = _momentTz(newValue, this.timeZone);
                this._adjustDate(newMoment);
            }
        },

        _showYearSelector: function () {
            this._selectedPage = "yearSelector";
        },

        _showMonthSelector: function () {
            this._selectedPage = "monthSelector";
        },

        _showDefaultPage: function () {
            this._selectedPage = "defaultPage";
        },

        _timeSelected: function() {
            this._showDefaultPage();
            this.seconds = 0;
            this.milis = 0;
        },

        _showHourSelector: function () {
            this._selectedPage = "hourSelector";
        },

        _showMinuteSelector: function () {
            this._selectedPage = "minuteSelector";
        },

        _changeMeridan: function () {
            var indexOfMeridian = meridians.indexOf(this.meridian);
            this.meridian = meridians[indexOfMeridian ? 0 : 1];
        },

        /**
         * Calculates the media query for mobile devices.
         */
        _calcMobileQuery: function () {
            return "max-width: " + (this.$.appConfig.minTabletWidth - 1) + "px";
        },

        /**
         * Calculates the calendar layout depending on phoneScreen.
         */
        _calcCalendarLayout: function (phoneScreen) {
            return "layout " + (phoneScreen ? "vertical" : "horizontal");
        },

        _onPageChange: function (e, detail) {
            if (detail.item.getAttribute('page-name') !== 'defaultPage' && typeof detail.item._resize === 'function') {
                detail.item._resize();
            }
        },

        _formatHours: function (value, showMeridian) {
            if (showMeridian) {
                return zeroPad(value % 12 === 0 ? 12 : value % 12, 2);
            } else {
                return zeroPad(value, 2);
            }
        },

        _getHourLowerBound: function (showMeridian) {
            return showMeridian ? 1 : 0;
        },

        _getHourUpperBound: function (showMeridian) {
            return showMeridian ? 12 : 23;
        },

        _formatMinute: function (value) {
            return zeroPad(value, 2);
        },
    });
})();
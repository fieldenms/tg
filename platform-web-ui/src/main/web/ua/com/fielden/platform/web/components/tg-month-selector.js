import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';

import {_momentTz} from '/resources/reflection/tg-date-utils.js'

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const template = html`
    <style>
        .header {
            height: 2.2em;
            font-size: 14px;
        }
        .month-shown {
            font-weight: 600;
            margin: 0px 1.1em;
        }
        .selected-date {
            background-color: #03A9F4;
            color: white !important;
        }
        .day {
            height: 2.2em;
            width: 2.2em;
            font-size: 12px; /*24px;*/
            border-radius: 1.1em;
            cursor: pointer;
        }
        .outside-day {
            color: #CDCDCD;
        }
        .day-short-name {
            color: #757575;
            pointer-events: none;
            cursor: auto;
        }
        .today-date {
            color: #03A9F4;
        }
        paper-icon-button {
            padding: 0px;
            margin: 0px;
            width: 24px;
            height: 24px;
        }
    </style>
    <style is="custom-style" include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <div class="layout vertical">
    <div class="header month-shown layout horizontal justified center">
        <div>
            <paper-icon-button icon="chevron-left" on-tap="_prevMonth"></paper-icon-button>
        </div>
        <div class="layout vertical center-justified">
            <span>
                    <span>[[monthName]]</span>&nbsp;&nbsp;<span>[[year]]</span>
            </span>
        </div>
        <div>
            <paper-icon-button icon="chevron-right" on-tap="_nextMonth"></paper-icon-button>
        </div>
    </div>
    <div class="calendar layout vertical">
        <div class="layout horizontal center-justified">
            <div class="day day-short-name layout vertical center-center"><span>S</span>
            </div>
            <div class="day day-short-name layout vertical center-center"><span>M</span>
            </div>
            <div class="day day-short-name layout vertical center-center"><span>T</span>
            </div>
            <div class="day day-short-name layout vertical center-center"><span>W</span>
            </div>
            <div class="day day-short-name layout vertical center-center"><span>T</span>
            </div>
            <div class="day day-short-name layout vertical center-center"><span>F</span>
            </div>
            <div class="day day-short-name layout vertical center-center"><span>S</span>
            </div>
        </div>
        <template is="dom-repeat" items="[[weeks]]" as="week">
            <div class="layout horizontal center-justified">
                <template is="dom-repeat" items="[[week]]" as="day">
                    <div on-tap="_selectDay" class$="[[_calcDayClass(day, month, year, selectedDay, selectedMonth, selectedYear)]]"><span>[[_absolute(day)]]</span>
                    </div>
                </template>
            </div>
        </template>
    </div>
</div>`;

(function () {
    const getDaysInMonth = function (year, month) {
        const yearMonth = { year: year, month: month, day: 1 };
        return _momentTz(yearMonth, this.timeZone).daysInMonth();
    };
    const getDaysInPreviousMonth = function (year, month) {
        const yearMonth = { year: year, month: month, day: 1 };
        return _momentTz(yearMonth, this.timeZone).add(-1, 'M').daysInMonth();
    };
    const createMonth = function (year, month) {
        const weeks = [];
        let week = [];
        let day = 1;
        const days = getDaysInMonth.bind(this)(year, month);
        while (day > 0) {
            week = createWeek.bind(this)(year, month, day);
            weeks.push(week);
            day = week[week.length - 1].day > 0 && week[week.length - 1].day < days ? week[week.length - 1].day + 1 : 0;
        }
        return weeks;
    };
    const createWeek = function (year, month, day) {
        const yearMonthDay = { year: year, month: month, day: day };
        const firstDay = _momentTz(yearMonthDay, this.timeZone).day();
        const week = [];
        const previousMonthDays = getDaysInPreviousMonth.bind(this)(year, month);
        let _i = 0;
        // In case when the first day of the month is not a sanday then add 0 to the first days of the week.
        for (; _i < firstDay; _i++) {
            week.push({monthIncrementor: -1, day: -1 * (previousMonthDays - (firstDay - (_i + 1)))});
        }
        // Add day numbers to the week.
        const days = getDaysInMonth.bind(this)(year, month);
        for (; _i < 7 && day <= days; _i++) {
            week.push({monthIncrementor: 0, day: day});
            day += 1;
        }
        // In case when the last day of the month is not saturday then add 0 to the last days of the week.
        let nextMonthDay = -1;
        for (; _i < 7; _i++) {
            week.push({monthIncrementor: 1, day: nextMonthDay});
            nextMonthDay -= 1;
        }
        return week;
    };
    Polymer({
        _template: template,

        is: "tg-month-selector",

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
            selectedDay: Number,
            selectedMonth: Number,
            selectedYear: Number,
            todayDay: Number,
            todayMonth: Number,
            todayYear: Number,
            monthName: String,
            month: Number,
            year: Number
        },

        ready: function () {
            var today = _momentTz(this.timeZone);
            this.todayDay = today.date();
            this.todayMonth = today.month();
            this.todayYear = today.year();
            this.selectedDay = null;
            this.selectedMonth = null;
            this.selectedYear = null;
            this.month = null;
            this.year = null;
        },

        attached: function () {
            var momentToShow = this.selectedDate ? 
                    _momentTz(this.selectedDate, this.timeZone) :
                    _momentTz(this.timeZone);
            this._adjustMonth(momentToShow);
        },

        /**
         * Goes to the previous month.
         */
        _prevMonth: function () {
            this._selectMonth(-1);
        },

        /**
         * Goes to the next month.
         */
        _nextMonth: function () {
            this._selectMonth(1);
        },

        /**
         * Selects month depending on increment (the increment might be positive or negetive).
         */
        _selectMonth: function (inc) {
            var yearMonth = { year: this.year, month: this.month, day: 1 };
            var momentToShow = _momentTz(yearMonth, this.timeZone).add(inc, 'M');
            this._adjustMonth(momentToShow);
        },

        /**
         * Updates the calendar information depending on which month was choosen.
         */
        _adjustMonth: function (momentToShow) {
            if (momentToShow.isValid() && (this.month !== momentToShow.month() || this.year !== momentToShow.year())) {
                this.monthName = momentToShow.format("MMMM");
                this.month = momentToShow.month();
                this.year = momentToShow.year();
                this.weeks = createMonth.bind(this)(this.year, this.month);
            }
        },

        /**
         * Selects the date.
         */
        _selectDay: function (event, detail, el) {
            var yearMonthDay = { year: this.year, month: this.month + event.model.day.monthIncrementor, day: Math.abs(event.model.day.day) };
            this.selectedDate = _momentTz(yearMonthDay, this.timeZone).valueOf();
            if (event.detail.sourceEvent.detail > 1) {
                this.fire("tg-accept-date");
            }
        },

        /**
         * Listens when the selected date changes.
         */
        _selectedDateChanged: function (newValue, oldValue) {
            if (newValue !== null) {
                var newMoment = _momentTz(newValue, this.timeZone);
                if (newMoment.isValid()) {
                    this.selectedYear = newMoment.year();
                    this.selectedMonth = newMoment.month();
                    this._adjustMonth(newMoment);
                    this.selectedDay = newMoment.date();
                }
            }
        },

        /**
         * Calcualtes class names for specified day.
         */
        _calcDayClass: function (day, month, year, selectedDay, selectedMonth, selectedYear) {
            var dayClasses = "day layout vertical center-center";
            if (day.day < 0) {
                dayClasses += " outside-day";
            }
            if (day.day === this.todayDay && month === this.todayMonth && year === this.todayYear) {
                dayClasses += " today-date";
            }
            if (day.day === selectedDay && month === selectedMonth && year === selectedYear) {
                dayClasses += " selected-date";
            }
            return dayClasses;
        },

        /**
         * Returns the absolute value of the day
         */
        _absolute: function (day) {
            return Math.abs(day.day);
        }
    });
})();
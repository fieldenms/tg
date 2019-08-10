import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/centre/criterion/tg-abstract-criterion.js';
import { TgAbstractRangeCriterionBehavior } from '/resources/centre/criterion/multi/range/tg-abstract-range-criterion-behavior.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/paper-styles/color.js';

import '/resources/centre/criterion/multi/range/tg-date-range-criterion-config.js';

const template = html`
    <style>
        .truncate {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .exclusive-mnemonic {
            background-color: var(--paper-blue-200);
            opacity: 0.5;
            pointer-events: none;
            margin-bottom: 8px;
            margin-top: 28px;
        }
    </style>
    <style is="custom-style" include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <tg-abstract-criterion id="dom"
        crit-only="[[critOnly]]"
        _cancel-meta-values="[[_cancelMetaValuesForBinding]]"
        _accept-meta-values="[[_acceptMetaValuesForBinding]]"
        _show-meta-values-editor="[[_showMetaValuesEditor]]"
        _compute-icon-button-style="[[_computeIconButtonStyleForBinding]]"
        _or-null="{{_orNull}}"
        _not="{{_not}}"
        _exclusive="{{_exclusive}}"
        _exclusive2="{{_exclusive2}}">
        <div slot="criterion-editors" class="layout horizontal flex relative criterion-editors" style="margin-right:20px;">
            <slot name="range-criterion-editor-1"></slot>
            <div class="fit mnemonic-layer exclusive-mnemonic" hidden$="[[!_exclusive]]"></div>
        </div>
        <div slot="criterion-editors" class="layout horizontal flex relative criterion-editors">
            <slot name="range-criterion-editor-2"></slot>
            <div class="fit mnemonic-layer exclusive-mnemonic" hidden$="[[!_exclusive2]]"></div>
        </div>
        <div slot="date-mnemonic" class="fit layout horizontal center center-justified mnemonic-layer date-mnemonic" hidden$="[[!_hasDateMetaValueFor(_datePrefix)]]">
            <span class="truncate">[[_calcText(_datePrefix, _dateMnemonic, _andBefore)]]</span>
        </div>
    </tg-abstract-criterion>
`;

Polymer({
    _template: template,

    is: 'tg-date-range-criterion',

    observers: [
        '_updateIconButtonStyle(orNull, not, exclusive, exclusive2, datePrefix, dateMnemonic, andBefore)'
    ],

    behaviors: [ TgAbstractRangeCriterionBehavior ],

    _dom: function () {
        return this.$.dom;
    },

    properties: {
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////// EXTERNAL PROPERTIES //////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These mandatory properties must be specified in attributes, when constructing <tg-*-editor>s.       //
        // No default values are allowed in this case.														   //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        /**
         * This published property specifies the 'date prefix' for this date criterion.
         *
         * values: 'PREV', 'CURR', 'NEXT'. (see DateRangePrefixEnum.java)
         */
        datePrefix: {
            type: String,
            notify: true, // TODO val: null
            observer: '_datePrefixChanged'
        },

        /**
         * This published property specifies the 'date mnemonic' for this date criterion.
         *
         * values: 'DAY', 'MONTH', 'WEEK', 'YEAR' etc. (see MnemonicEnum.java)
         */
        dateMnemonic: {
            type: String,
            notify: true, // TODO val: null
            observer: '_dateMnemonicChanged'
        },

        /**
         * This published property specifies the 'and before' for this date criterion.
         *
         * values: 'null', 'true', 'false'
         */
        andBefore: {
            // please, note that specifying Boolean as a type is slightly dangerous -- 'null' value serialisation in that case can give us unpredictable results.
            type: Object,
            notify: true, // TODO val: null
            observer: '_andBeforeChanged'
        },

        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////// INNER PROPERTIES, THAT GOVERN CHILDREN /////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These properties derive from other properties and are considered as 'private' -- need to have '_'   //
        //   prefix. 																				           //
        // Also, these properties are designed to be bound to children element properties -- it is necessary to//
        //   populate their default values in ready callback (to have these values populated in children)!     //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        _datePrefix: String,
        _dateMnemonic: String,
        _andBefore: Object
    },

    ready: function () {
        this._datePrefix = null;
        this._dateMnemonic = null;
        this._andBefore = null;
    },

    _acceptMetaValues: function (validate) {
        this.datePrefix = this._datePrefix;
        this.dateMnemonic = this._dateMnemonic;
        this.andBefore = this._andBefore;

        TgAbstractRangeCriterionBehavior[1]._acceptMetaValues.call(this, validate);
    },

    /**
     * Calculates date layer's text based on applied mnemonic.
     */
    _calcText: function (_datePrefix, _dateMnemonic, _andBefore) {
        /* this._datePrefixes = ["PREV", "CURR", "NEXT"];
        this._dateMnemonics = ["DAY", "WEEK", "MONTH", "QRT1", "QRT2", "QRT3", "QRT4", "YEAR", "OZ_FIN_YEAR"];
        this._andBefores = ["THIS", "BEFORE", "AFTER"];
        this._prefixTitles = ["Previous", "Current", "Next"];
        this._mnemonicTitles = ["Day", "Week", "Month", "1-st quarter", "2-nd quarter", "3-rd quarter", "4-th quarter", "Year", "Financial year"];
        this._andBeforeTitles = ["", " and before", " and after"]; */
        const _mnemonicIds = ['WEEK', 'MONTH', 'QRT1', 'QRT2', 'QRT3', 'QRT4', 'YEAR', 'OZ_FIN_YEAR'];
        const _mnemonicTitles = ['week', 'month', 'year\'s 1-st quarter', 'year\'s 2-nd quarter', 'year\'s 3-rd quarter', 'year\'s 4-th quarter', 'year', 'financial year'];
        const _andBeforeSuffix = _andBefore === true ? ' and before' : (_andBefore === false ? ' and after' : '');
        if ('DAY' === _dateMnemonic) {
            const day = 'PREV' === _datePrefix ? 'Yesterday' : ('CURR' === _datePrefix ? 'Today' : 'Tomorrow');
            return day + _andBeforeSuffix;
        } else {
            const prefix = 'PREV' === _datePrefix ? 'Previous' : ('CURR' === _datePrefix ? 'Current' : 'Next');
            const period = _mnemonicTitles[_mnemonicIds.indexOf(_dateMnemonic)];
            return prefix + ' ' + period + _andBeforeSuffix;
        }
    },

    /**
     * Returns 'true' if date specific meta value exists and date layer should be present, 'false' otherwise.
     */
    _hasDateMetaValueFor: function (_datePrefix) {
        return _datePrefix !== null && this._iconButtonVisible();
    },

    _cancelMetaValues: function () {
        this._datePrefix = this.datePrefix;
        this._dateMnemonic = this.dateMnemonic;
        this._andBefore = this.andBefore;

        TgAbstractRangeCriterionBehavior[1]._cancelMetaValues.call(this);
    },

    /**
     * Creates the string representation for meta value editors DOM (to be inserted into dynamic meta-value dialog).
     */
    _createMetaValueEditors: function () {
        console.log("tg-date-range-criterion: _createMetaValueEditors");
        return TgAbstractRangeCriterionBehavior[1]._createMetaValueEditors.call(this) +
            '<tg-date-range-criterion-config _date-prefix="{{_datePrefixBind}}" _date-mnemonic="{{_dateMnemonicBind}}" _and-before="{{_andBeforeBind}}"></tg-date-range-criterion-config>';
    },

    _datePrefixChanged: function (newValue, oldValue) {
        this._datePrefix = newValue;
    },

    _dateMnemonicChanged: function (newValue, oldValue) {
        this._dateMnemonic = newValue;
    },

    _andBeforeChanged: function (newValue, oldValue) {
        this._andBefore = newValue;
    },

    /**
     * Returns 'true' if criterion has no meta values assigned, 'false' otherwise.
     */
    _hasNoMetaValues: function (orNull, not, exclusive, exclusive2, datePrefix, dateMnemonic, andBefore) {
        return TgAbstractRangeCriterionBehavior[1]._hasNoMetaValues.call(this, orNull, not, exclusive, exclusive2, datePrefix, dateMnemonic, andBefore) &&
            datePrefix === null && dateMnemonic === null && andBefore === null;
    }
});
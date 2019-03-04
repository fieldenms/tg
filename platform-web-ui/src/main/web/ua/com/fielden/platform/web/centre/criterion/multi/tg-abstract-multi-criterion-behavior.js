import { TgAbstractCriterionBehavior } from '/resources/centre/criterion/tg-abstract-criterion-behavior.js';
import '/resources/centre/criterion/multi/tg-multi-criterion-config.js';

const TgAbstractMultiCriterionBehaviorImpl = {

    properties: {
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////// EXTERNAL PROPERTIES //////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These mandatory properties must be specified in attributes, when constructing <tg-*-editor>s.       //
        // No default values are allowed in this case.														   //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * This published property specifies whether 'missing value' will be also considered in search queries.
         */
        orNull: {
            type: Boolean,
            notify: true, // TODO val: false
            observer: '_orNullChanged'
        },

        /**
         * This published property specifies whether the criterion should be negated.
         */
        not: {
            type: Boolean,
            notify: true, // TODO val: false
            observer: '_notChanged'
        },

        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////// INNER PROPERTIES, THAT GOVERN CHILDREN /////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These properties derive from other properties and are considered as 'private' -- need to have '_'   //
        //   prefix. 																				           //
        // Also, these properties are designed to be bound to children element properties -- it is necessary to//
        //   populate their default values in ready callback (to have these values populated in children)!     //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        _orNull: {
            type: Boolean
        },
        _not: {
            type: Boolean
        }
    },

    ready: function () {
        this._orNull = false;
        this._not = false;
    },

    _acceptMetaValues: function (validate) {
        this.orNull = this._orNull;
        this.not = this._not;

        TgAbstractCriterionBehavior[1]._acceptMetaValues.call(this, validate);
    },
    _cancelMetaValues: function () {
        this._orNull = this.orNull;
        this._not = this.not;

        TgAbstractCriterionBehavior[1]._cancelMetaValues.call(this);
    },

    /**
     * Creates the string representation for meta value editors DOM (to be inserted into dynamic meta-value dialog).
     */
    _createMetaValueEditors: function () {
        console.log("tg-abstract-multi-criterion-behavior: _createMetaValueEditors");
        return TgAbstractCriterionBehavior[1]._createMetaValueEditors.call(this) +
            '<tg-multi-criterion-config class="layout vertical" _or-null="{{_orNullBind}}" _not="{{_notBind}}"></tg-multi-criterion-config>';
    },

    _orNullChanged: function (newValue, oldValue) {
        this._orNull = newValue;
    },
    _notChanged: function (newValue, oldValue) {
        this._not = newValue;
    },

    /**
     * Returns 'true' if criterion has no meta values assigned, 'false' otherwise.
     */
    _hasNoMetaValues: function (orNull, not, exclusive, exclusive2, datePrefix, dateMnemonic, andBefore) {
        return TgAbstractCriterionBehavior[1]._hasNoMetaValues.call(this, orNull, not, exclusive, exclusive2, datePrefix, dateMnemonic, andBefore) &&
            orNull === false && not === false;
    }
};

export const TgAbstractMultiCriterionBehavior = [
    TgAbstractCriterionBehavior,
    TgAbstractMultiCriterionBehaviorImpl
];
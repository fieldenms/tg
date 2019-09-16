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

        /**
         * Number of the group of conditions [glued together through logical OR] that this criterion belongs to.
         * 'null' if this criterion does not belong to any group.
         */
        orGroup: {
            type: Number,
            notify: true,
            observer: '_orGroupChanged'
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
        },
        _orGroup: {
            type: Number
        }
    },

    ready: function () {
        this._orNull = false;
        this._not = false;
        this._orGroup = null;
    },

    _acceptMetaValues: function (validate) {
        this.orNull = this._orNull;
        this.not = this._not;
        this.orGroup = this._orGroup;

        TgAbstractCriterionBehavior[1]._acceptMetaValues.call(this, validate);
    },
    _cancelMetaValues: function () {
        this._orNull = this.orNull;
        this._not = this.not;
        this._orGroup = this.orGroup;

        TgAbstractCriterionBehavior[1]._cancelMetaValues.call(this);
    },

    /**
     * Creates the string representation for meta value editors DOM (to be inserted into dynamic meta-value dialog).
     */
    _createMetaValueEditors: function () {
        console.log("tg-abstract-multi-criterion-behavior: _createMetaValueEditors");
        return TgAbstractCriterionBehavior[1]._createMetaValueEditors.call(this) +
            '<tg-multi-criterion-config class="layout vertical" _exclude-missing="[[_excludeMissingBind]]" _or-null="{{_orNullBind}}" _not="{{_notBind}}" _or-group="{{_orGroupBind}}"></tg-multi-criterion-config>';
    },

    _orNullChanged: function (newValue) {
        this._orNull = newValue;
    },
    _notChanged: function (newValue) {
        this._not = newValue;
    },
    _orGroupChanged: function (newValue) {
        this._orGroup = newValue;
    },

    /**
     * Returns 'true' if criterion has no meta values assigned, 'false' otherwise.
     */
    _hasNoMetaValues: function (orNull, not, orGroup, exclusive, exclusive2, datePrefix, dateMnemonic, andBefore) {
        return TgAbstractCriterionBehavior[1]._hasNoMetaValues.call(this, orNull, not, orGroup, exclusive, exclusive2, datePrefix, dateMnemonic, andBefore) &&
            orNull === false && not === false && orGroup === null;
    }
};

export const TgAbstractMultiCriterionBehavior = [
    TgAbstractCriterionBehavior,
    TgAbstractMultiCriterionBehaviorImpl
];
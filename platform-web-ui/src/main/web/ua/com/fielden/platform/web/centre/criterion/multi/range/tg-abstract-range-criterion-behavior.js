import { TgAbstractMultiCriterionBehavior } from '/resources/centre/criterion/multi/tg-abstract-multi-criterion-behavior.js';
import '/resources/centre/criterion/multi/range/tg-range-criterion-config.js';

const TgAbstractRangeCriterionBehaviorImpl = {

    properties: {
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////// EXTERNAL PROPERTIES //////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These mandatory properties must be specified in attributes, when constructing <tg-*-editor>s.       //
        // No default values are allowed in this case.														   //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * This published property specifies whether left operand of 'range' criterion should be 'exclusive'.
         */
        exclusive: {
            type: Boolean,
            notify: true, // TODO val: false
            observer: '_exclusiveChanged'
        },

        /**
         * This published property specifies whether right operand of 'range' criterion should be 'exclusive'.
         */
        exclusive2: {
            type: Boolean,
            notify: true, // TODO val: false
            observer: '_exclusive2Changed'
        },

        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////// INNER PROPERTIES, THAT GOVERN CHILDREN /////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These properties derive from other properties and are considered as 'private' -- need to have '_'   //
        //   prefix. 																				           //
        // Also, these properties are designed to be bound to children element properties -- it is necessary to//
        //   populate their default values in ready callback (to have these values populated in children)!     //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        _exclusive: {
            type: Boolean
        },
        _exclusive2: {
            type: Boolean
        }
    },

    ready: function () {
        this._exclusive = false;
        this._exclusive2 = false;
    },

    _acceptMetaValues: function (validate) {
        this.exclusive = this._exclusive;
        this.exclusive2 = this._exclusive2;

        TgAbstractMultiCriterionBehavior[1]._acceptMetaValues.call(this, validate);
    },
    _cancelMetaValues: function () {
        this._exclusive = this.exclusive;
        this._exclusive2 = this.exclusive2;

        TgAbstractMultiCriterionBehavior[1]._cancelMetaValues.call(this);
    },

    /**
     * Creates the string representation for meta value editors DOM (to be inserted into dynamic meta-value dialog).
     */
    _createMetaValueEditors: function () {
        console.log("tg-abstract-range-criterion-behavior: _createMetaValueEditors");
        return TgAbstractMultiCriterionBehavior[1]._createMetaValueEditors.call(this) +
            '<tg-range-criterion-config class="layout vertical" _exclusive="{{_exclusiveBind}}" _exclusive2="{{_exclusive2Bind}}"></tg-range-criterion-config>';
    },

    _exclusiveChanged: function (newValue, oldValue) {
        this._exclusive = newValue;
    },
    _exclusive2Changed: function (newValue, oldValue) {
        this._exclusive2 = newValue;
    },

    /**
     * Returns 'true' if criterion has no meta values assigned, 'false' otherwise.
     */
    _hasNoMetaValues: function (orNull, not, orGroup, exclusive, exclusive2, datePrefix, dateMnemonic, andBefore) {
        return TgAbstractMultiCriterionBehavior[1]._hasNoMetaValues.call(this, orNull, not, orGroup, exclusive, exclusive2, datePrefix, dateMnemonic, andBefore) &&
            exclusive === false && exclusive2 === false;
    }
};

export const TgAbstractRangeCriterionBehavior = [
    TgAbstractMultiCriterionBehavior,
    TgAbstractRangeCriterionBehaviorImpl
];
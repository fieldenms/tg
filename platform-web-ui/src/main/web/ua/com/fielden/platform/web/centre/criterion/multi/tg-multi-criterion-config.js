/**
 * The `tg-multi-criterion-config` contains just the DOM part of all non-single `tg-criterion`s -- 
 * 'Missing value' and 'Not' editors.
 */
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/paper-checkbox/paper-checkbox.js';
import '/resources/polymer/@polymer/paper-input/paper-input.js';

const template = html`
    <style>
        paper-checkbox {
            margin-bottom: 20px;
            font-family: 'Roboto', 'Noto', sans-serif;
            --paper-checkbox-checked-color: #0288D1;
            --paper-checkbox-checked-ink-color: #0288D1;
        }
    </style>
    <paper-checkbox checked="{{_orNull}}" hidden$="[[_excludeMissing]]">Missing</paper-checkbox>
    <paper-checkbox checked="{{_not}}">Not</paper-checkbox>
    <paper-input value="{{_orGroupStr}}" label="Group"></paper-input>
`;

Polymer({
    _template: template,

    is: 'tg-multi-criterion-config',

    properties: {
        _orNull: {
            type: Boolean,
            notify: true
        },
        _not: {
            type: Boolean,
            notify: true
        },
        /**
         * Number of the group of conditions [glued together through logical OR] that this criterion belongs to.
         * 'null' if this criterion does not belong to any group.
         */
        _orGroup: {
            type: Number,
            notify: true,
            observer: '_orGroupChanged'
        },
        /**
         * String representation of '_orGroup'.
         */
        _orGroupStr: {
            type: String,
            observer: '_orGroupStrChanged'
        },

        _excludeMissing: {
            type: Boolean
        }
    },

    _orGroupStrChanged: function (newValue) {
        if (newValue && newValue !== '') {
            this._orGroup = +newValue;
        } else {
            this._orGroup = null;
        }
    },

    _orGroupChanged: function (newValue) {
        this._orGroupStr = newValue === null ? '' : (newValue + '');
    }
});
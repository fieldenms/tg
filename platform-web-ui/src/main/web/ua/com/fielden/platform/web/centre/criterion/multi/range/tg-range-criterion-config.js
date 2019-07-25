/**
 * The `tg-multi-criterion-config` contains just the DOM part of all non-single `tg-criterion`s -- 
 * 'Missing value' and 'Not' editors.
 */
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/paper-checkbox/paper-checkbox.js';

const template = html`
    <style>
        paper-checkbox {
            margin-bottom: 20px;
            font-family: 'Roboto', 'Noto', sans-serif;
            --paper-checkbox-checked-color: #0288D1;
            --paper-checkbox-checked-ink-color: #0288D1;
        }
    </style>
    <paper-checkbox checked="{{_exclusive}}">Exclude From</paper-checkbox>
    <paper-checkbox checked="{{_exclusive2}}">Exclude To</paper-checkbox>
`;

Polymer({
    _template: template,

    is: 'tg-range-criterion-config',

    properties: {
        _exclusive: {
            type: Boolean,
            notify: true
        },
        _exclusive2: {
            type: Boolean,
            notify: true
        }
    }
});
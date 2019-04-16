import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';

Polymer({
    is: "tg-summary-property",

    properties: {
        property: String,
        type: String,
        columnTitle: String,
        columnDesc: String
    }
});
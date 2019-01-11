import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

Polymer({
    _template: html`<slot></slot>`,

    is: 'tg-dom-stamper',
 
    properties: {
        /**
         * DOM text that is needed to be stamped as inner DOM elements.
         */
        domText: {
            type: String,
            observer: '_domTextChanged'
        }
    },

    _domTextChanged: function (newDomText) {
        this.innerHTML = newDomText;
    }
});
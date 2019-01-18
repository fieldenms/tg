import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/layout/tg-flex-layout.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const template = html`
   <tg-flex-layout when-desktop="[[whenDesktop]]" when-tablet="[[whenTablet]]" when-mobile="[[whenMobile]]">
        <slot></slot>
    </tg-flex-layout>`;

template.setAttribute('strip-whitespace', '');

Polymer({
    _template: template,

    is: "tg-content-layout",

    properties: {
        whenDesktop: Array,
        whenTablet: Array,
        whenMobile: Array,
    },

    ready: function() {
        this.whenDesktop=[[['flex'],['flex']],[['flex'],['flex']]];
        this.whenTablet=[[['flex'],['flex']],[['flex'],['flex']]];
        this.whenMobile=[[['flex'],['flex']],[['flex'],['flex']]];
    }
});
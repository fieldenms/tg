import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const template = html`
    <slot name="input"></slot>`;
    
template.setAttribute('strip-whitespace', '');

Polymer({
    _template: template,

    is: 'grand-slot-parent',

    attached: function () {
        console.log("grand-slot-parent: ", this.querySelector("input"));
    }
});
import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import './grand-slot-parent.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const template = html`
    <grand-slot-parent>
        <slot name="input-child" slot="input"></slot>
    </grand-slot-parent>`;
    
template.setAttribute('strip-whitespace', '');

Polymer({
    _template: template,

    is: 'slot-parent',

    attached: function () {
        console.log("slot-parent: ", this.querySelector("input"));
    }
});
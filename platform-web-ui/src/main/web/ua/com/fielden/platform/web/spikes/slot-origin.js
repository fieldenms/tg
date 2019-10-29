import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import './slot-parent.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const template = html`
    <slot-parent>
        <slot slot="input-child" name="input-origin"></slot>
    </slot-parent>`;
    
template.setAttribute('strip-whitespace', '');

Polymer({
    _template: template,

    is: 'slot-origin',

    attached: function () {
        console.log("slot-origin", this.querySelector("input"));
    }
});
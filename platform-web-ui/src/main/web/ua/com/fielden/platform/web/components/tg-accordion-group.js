import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import '/resources/components/tg-accordion.js';

const template = html`
    <slot id="accordions" name="accordion"></slot>
`;

Polymer({
    _template: template,

    is: 'tg-accordion-group',

    properties: {
        singleOpen: {
            type: Boolean,
            reflectToAttribute: true,
            value: false
        }
    },

    attached: function () {
        const self = this;
        this.async(function () {
            self.accordions = [];
            Array.prototype.forEach.call(self.$.accordions.assignedNodes({flatten: true}), function (item) {
                this.accordions.push(item);
                item.addEventListener('accordion-toggled', function (e) {
                    if (this.singleOpen) {
                        if (e.detail) {
                            this.accordions.forEach(function (accordion) {
                                const target = e.target || e.srcElement;
                                if (accordion !== target) {
                                    accordion.opened = false;
                                }
                            });
                        }
                    }
                }.bind(self));
            }.bind(self));
        }, 1);
    }
});
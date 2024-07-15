import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/paper-styles/paper-styles-classes.js';

const template = html`
    <style>
        :host {
            @apply --layout-horizontal;
            @apply --layout-justified;
        }
    </style>
    <slot id="resultSlots"></slot>
`;

Polymer({
    _template: template,

    is: 'tg-centre-result-view',

    ready: function () {
        this.addEventListener("tg-centre-page-was-selected", () => {
            this.$.resultSlots.assignedNodes({ flatten: true }).forEach(node => {
                this._fireEventToNode(node);
            });
        })
    },

    _fireEventToNode: function (node) {
        if (node.shadowRoot && typeof node.fire === 'function') {
            node.dispatchEvent(new CustomEvent("tg-centre-page-was-selected", {bubbles: false, composed: true}));
        } else if (node.tagName === 'SLOT') {
            node.assignedNodes().forEach(assignedNode => {
                this._fireEventToNode(assignedNode);
            });
        } else if (node.nodeType === Node.ELEMENT_NODE && node.children.length > 0) {
            Array.from(node.children).forEach(child => {
                this._fireEventToNode(child);
            });
        }
    },

    attached: function () {
        this.classList.add('centreResultView');
    },

    /**
     * Finds parent centre for this result view. It should exist, otherwise exception will be thrown.
     */
    _findParentCentre: function () {
        let parent = this;
        while (parent && !parent.classList.contains('generatedCentre')) {
            parent = parent.parentElement || parent.getRootNode().host;
        }
        if (!parent) {
            throw 'No parent centre exists for this result view.';
        } else if (parent === this) {
            throw 'This result view somehow contains generatedCentre class.';
        }
        return parent;
    }
});
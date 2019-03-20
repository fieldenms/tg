import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/paper-styles/paper-styles-classes.js';

const template = html`
    <style>
        :host {
            min-height: 100%;
            @apply --layout-horizontal;
            @apply --layout-justified;
        }
    </style>
    <slot></slot>
`;

Polymer({
    _template: template,

    is: 'tg-centre-result-view',

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
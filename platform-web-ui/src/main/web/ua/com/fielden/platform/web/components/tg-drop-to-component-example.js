import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { TgDropToBehavior } from '/resources/components/tg-drop-to-behavior.js';
import { tearDownEvent} from '/resources/reflection/tg-polymer-utils.js';

const template = html`
    <style>
        .drop-to-rect {
            width: 100px;
            height: 50px;
            background-color: red;
            @apply --layout-vertical;
            @apply --layout-center;
            @apply --layout-center-justified;

        }
    </style>
    <div class="drop-to-rect layout vertical center-center" on-drop="dropTo" on-dragover="canDropTo">
        <div>[[data.name]]</div>
        <div>[[data.value]]</div>
    </div>`;

template.setAttribute('strip-whitespace', '');

Polymer({
    _template: template,

    is: 'tg-drop-to-component-example',

    behaviors: [TgDropToBehavior],

    properties: {
        data: {
            type: Object
        }
    },

    ready: function () {
        this.data = {
            name: "drop-to",
            value: 10
        };
    },

    canDropTo: function (e) {
        if (this.canDropType(e.dataTransfer.types, "text/plain")) {
            e.dataTransfer.dropEffect = "copy";
            tearDownEvent(e);
        }
    },

    dropTo: function (e) {
        this.data = JSON.parse(e.dataTransfer.getData("text/plain"));
        tearDownEvent(e);
    }
});
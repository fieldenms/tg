import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { TgDragFromBehavior } from '/resources/components/tg-drag-from-behavior.js';

const template = html`
    <style>
        .drag-from-rect {
            width: 100px;
            height: 50px;
            background-color: blue;
        }
    </style>
    <div class="drag-from-rect" draggable="true"></div>`;

template.setAttribute('strip-whitespace', '');

Polymer({
    _template: template,

    is: 'tg-drag-from-component-example',

    behaviors: [TgDragFromBehavior],

    getElementToDragFrom: function (target) {
        const elem = document.createElement('div');
        elem.style.width = "50px";
        elem.style.height = "25px";
        elem.style.backgroundColor = "green";
        return elem;
    },

    getDataToDragFrom: function () {
        return {
            "text/plain": JSON.stringify({
                name: "drag-from",
                value: 12
            })
        };
    }
});
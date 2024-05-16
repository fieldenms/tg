import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import './tg-element-loader.js';

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const template = html`
     Hello this is test for element loader prop1=<span>[[test.prop1]]</span> and prop2=<span>[[test.prop2]]</span>`;

Polymer({
    _template: template,

    is: 'tg-element-loader-test1',

    properties: {
        test:Object
    }
});
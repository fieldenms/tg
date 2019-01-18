import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';

import '/resources/layout/tg-flex-layout.js'

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import {TgTooltipBehavior} from '/resources/components/tg-tooltip-behavior.js'

const template = html`
    <style>
        div, span {
            cursor: default;
            border: 1px dashed black;
        }
    </style>
    <custom-style>
        <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    </custom-style>
    <tg-flex-layout when-desktop="[[_whenDesktop]]" , when-tablet="[[_whenDesktop]]" , when-mobile="[[_whenDesktop]]">
        <div id="div_text" tooltip-text="Tooltip text for div">
            <span tooltip-text="Tooltip text for embeded span">Embeded component</span>
            Component with activated tooltip
        </div>
        <template is="dom-repeat" items="[[_tooltipItems]]">
            <div id="[[item.id]]" tooltip-text$="[[item.tooltip]]">[[item.text]]</div>
        </template>
    </tg-flex-layout>
    <div>Component's tooltip goes here.</div>`;

template.setAttribute('strip-whitespace', '');

Polymer({
    _template: template,
    
    is: 'tg-component-with-tooltip',

    behaviors: [TgTooltipBehavior],

    properties: {
        _whenDesktop: Array,
        _tooltipItems: Array
    },
    
    hostAttributes: {
        'tooltip-text': "The component's tooltip works!"
    },

    ready: function () {
        this._whenDesktop = ['horizontal', ['flex'], ['flex']];
        this._tooltipItems = [{id: "div_html_text", text: "Another componsnt with activated tooltips", tooltip: '<iron-icon icon="attachment"></iron-icon><i>Test italic</i> html tooltip text'}];
    }
});
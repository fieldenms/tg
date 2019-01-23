import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-collapse/iron-collapse.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';

const template = html`
    <style>
        :host {
            position: relative;
            display: block;
        }
        .iron-collapse-closed {
            display: block !important;
        }
        .panel {
            background-color: #fff;
            border: 1px solid #ddd;
            border-radius: 6px;
            overflow: hidden;
        }
        .heading {
            background-color: #f5f5f5;
            border-color: #ddd;
            padding: 6px 15px;
            cursor: pointer;
        }
        a {
            margin-top: 0;
            margin-bottom: 0;
            font-size: 16px;
            font-weight: 400;
            color: #333;
        }
        a:hover {
            text-decoration: underline;
        }
        iron-collapse {
            border-top: 1px solid #ddd;
        }
        iron-icon {
            cursor: pointer;
        }
    </style>
    <div class="panel layout vertical">
        <div class="heading layout horizontal center justified" on-tap="_toggle">
            <a>[[heading]]</a>
            <iron-icon icon="[[_calcIcon(opened)]]"></iron-icon>
        </div>
        <iron-collapse opened="{{opened}}" id="collapse">
            <slot></slot>
        </iron-collapse>
    </div>
`;
Polymer({
    _template: template,

    is: 'tg-accordion',

    properties: {
        opened: {
            type: Boolean,
            reflectToAttribute: true,
            value: function () {
                return false;
            },
            observer: '_openedChanged'
        },
        dontCloseOnTap: {
            type: Boolean,
            reflectToAttribute: true,
            value: function () {
                return false;
            }
        },
        heading: String
    },

    ready: function () {
        this.$.collapse.__defineGetter__('_isDisplayed', function () {
            const rect = this.getBoundingClientRect();
            return !!Object.entries(Object.getOwnPropertyDescriptors(Object.getPrototypeOf(rect)))
                .filter(([key, descriptor]) => typeof descriptor.get === 'function').map(([key]) => key)
                .find(prop => rect[prop] !== 0);
        }.bind(this.$.collapse));
    },

    _calcIcon: function (isOpened) {
        return 'icons:' + (this.opened ? 'expand-less' : 'expand-more');
    },

    _toggle: function (e, detail, source) {
        if ((this.opened && !this.dontCloseOnTap) || !this.opened) {
            this.$.collapse.toggle();
        }
    },

    _openedChanged: function (newValue, oldValue) {
        if (newValue !== undefined) {
            this.fire('accordion-toggled', newValue);
        }
    }
});
import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-icons/av-icons.js';
import '/resources/polymer/@polymer/paper-styles/paper-styles-classes.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const template = html`
    <style>
        :host {
            @apply --layout-vertical;
        }
        .line {
            border-top: 1px solid rgba(0, 0, 0, .12);
            box-sizing: border-box;
            padding-top: 16px;
            -webkit-font-smoothing: antialiased;
            font-size: 1rem;
            font-weight: 500;
            color: rgba(0, 0, 0, 0.55);
        }
        .title[collapsible],
        iron-icon[collapsible] {
            cursor: pointer;
        }
        iron-icon {
            transform: translate(0, -1px);
            --iron-icon-width: 1.2em;
            --iron-icon-height: 1.2em;
        }
        iron-icon:not([collapsible]) {
            visibility: hidden;
        }
        iron-icon[open] {
            fill: none;
            stroke: rgba(0, 0, 0, 0.55);
            stroke-width: 2;
            stroke-linecap: round;
            stroke-linejoin: round;
            transform: translate(0, -1.5px)rotate(90deg);
        }
    </style>
    <custom-style>
        <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    </custom-style>
    <div class="line layout horizontal center">
        <iron-icon icon="av:play-arrow" open$="[[!closed]]" collapsible$="[[collapsible]]"></iron-icon>
        <div hidden$="[[!subheaderTitle]]" class="title layout horizontal center" collapsible$="[[collapsible]]">[[subheaderTitle]]</div>
    </div>`;

template.setAttribute('strip-whitespace', '');

Polymer({
    _template: template,

    is: 'tg-subheader',

    properties: {
        subheaderTitle: String,

        collapsible: {
            type: Boolean,
            reflectToAttribute: true,
            value: false
        },

        closed: {
            type: Boolean,
            reflectToAttribute: true,
            value: false
        },

        relativeElements: {
            type: Array
        }
    },

    ready: function () {
        this.relativeElements = [];
        this.toggle = this.toggle.bind(this);

        this.addEventListener('click', this.toggle);
    },

    addRelativeElement: function (element) {
        this.relativeElements.push(element);
        element.$$relativeSubheader$$ = this;
        if (this.collapsible && this.closed) {
            element.classList.toggle('hidden-with-subheader', true);
        }
    },

    removeAllRelatedComponents: function () {
        this.relativeElements.forEach(function (relativeElement) {
            delete relativeElement.$$relativeSubheader$$;
            relativeElement.classList.toggle("hidden-with-subheader", false);
        });
    },

    toggle: function () {
        if (this.collapsible) {
            this.closed = !this.closed;
            this._syncRelativeElements();
            this.fire("iron-resize", {
                node: this,
                bubbles: true,
            });
        }
    },

    open: function () {
        if (this.collapsible && this.closed) {
            this.toggle();
        }
    },

    close: function () {
        if (this.collapsible && !this.closed) {
            this.toggle();
        }
    },

    _syncRelativeElements: function () {
        this.relativeElements.forEach(function (relativeElement) {
            relativeElement.classList.toggle("hidden-with-subheader", this.closed);
        }.bind(this));
    }
});
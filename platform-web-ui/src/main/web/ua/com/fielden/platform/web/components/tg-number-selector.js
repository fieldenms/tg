import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/iron-scroll-threshold/iron-scroll-threshold.js';
import '/resources/polymer/@polymer/iron-list/iron-list.js'

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const template = html`
    <style>
        .number-item {
            height: 42px;
            width: 42px;
            font-size: 14px;
            border-radius: 21px;
            cursor: pointer;
        }
        .number-item[selected] {
            background-color: #03A9F4;
            color: white;
        }
    </style>
    <style is="custom-style" include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <iron-scroll-threshold class="fit" on-lower-threshold="_lowerThreshold" lower-threshold="10" upper-threshold="10" on-upper-threshold="_upperThreshold" id="threshold">
        <iron-list class="fit" id="list" items="[]" scroll-target="threshold">
            <template>
                <div class="layout horizontal center-center">
                    <div class="number-item layout vertical center-center" selected$="[[selected]]" on-tap="_select">[[formatter(item)]]</div>
                </div>
            </template>
        </iron-list>
    </iron-scroll-threshold>`;

template.setAttribute('strip-whitespace', '');

(function () {
    var numbersToLoadAtOnce = 50;
    var cellSize = 32;
    Polymer({
        _template: template,

        is: "tg-number-selector",

        properties: {
            selectedNumber: {
                type: Number,
                notify: true,
                observer: "_selectedNumberChanged"
            },
            upperBound: {
                type: Number,
                observer: "_upperBoundChanged"
            },
            lowerBound: {
                type: Number,
                observer: "_lowerBoundChanged"
            },
            formatter: {
                type: Function,
                value: function () {
                    return (function (item) {
                        return item;
                    }).bind(this);
                }
            },
            _wasResized: {
                type: Boolean,
                value: false
            }
        },

        _resize: function () {
            if (!this._wasResized) {
                this.$.list.fire("iron-resize");
                this._wasResized = true;
            } else {
                this.$.list._updateMetrics();
                this.$.list._positionItems();
            }
            if (typeof this.selectedNumber !== undefined && this.selectedNumber !== null) {
                var selectedIndex = this.$.list.items.indexOf(this.selectedNumber);
                if (selectedIndex < this.$.list.firstVisibleIndex - 1 || selectedIndex > this.$.list.lastVisibleIndex + 1) {
                    this.$.list.scrollToIndex(selectedIndex);
                }
            }
        },

        _isSelected: function (selectedNumber, item) {
            return selectedNumber === item;
        },

        _select: function (e, detail) {
            if (e.model.item !== this.selectedNumber) {
                this.selectedNumber = e.model.item;
                this.fire("number-selected", this.selectedNumber);
            }
        },

        _selectedNumberChanged: function (newValue, oldValue) {
            if (newValue !== null) {
                //Just opened
                if (this.$.list.items.length === 0) {
                    this.$.list.push("items", newValue);
                    this._loadPreviousNumbers();
                    this._loadNextNumbers();
                } else if (newValue <= this.$.list.items[0]) {
                    this._loadPreviousNumbers(newValue);
                } else if (newValue >= this.$.list.items[this.$.list.items.length - 1]) {
                    this._loadNextNumbers(newValue);
                }
                if (newValue < this.$.list.items[0] || newValue > this.$.list.items[this.$.list.items.length - 1]) {
                    throw "The selected element is out of bounds or it wasn't loaded for some reason.";
                }
                if (typeof oldValue !== undefined && oldValue !== null && oldValue >= this.$.list.items[0] && oldValue <= this.$.list.items[this.$.list.items.length - 1]) {
                    this.$.list.deselectItem(oldValue);
                }
                this.$.list.selectItem(newValue);
            }
        },

        _lowerBoundChanged: function (newValue, oldValue) {
            if (this.$.list.items.length > 0 && newValue > this.$.list.items[0]) {
                var lastIndex = this.$.list.items.indexOf(newValue);
                this.$.list.splice("items", 0, lastIndex);
                if (this.selectedNumber < newValue) {
                    this.selectedNumber = newValue;
                }
            }
        },

        _upperThreshold: function () {
            this._loadPreviousNumbers();
        },

        _loadPreviousNumbers: function (toNumber) {
            if (this.$.list.items.length > 0) {
                var startFrom = this.$.list.items[0];
                var calcTo = toNumber || startFrom - numbersToLoadAtOnce;
                calcTo = calcTo < this.lowerBound ? this.lowerBound : calcTo;
                if (calcTo < startFrom) {
                    for (startFrom -= 1; startFrom >= calcTo; startFrom--) {
                        this.$.list.unshift("items", startFrom);
                    }
                    this.$.threshold.scroll(0, numbersToLoadAtOnce * cellSize);
                }
                this.$.threshold.clearTriggers();
            }
        },

        _upperBoundChanged: function (newValue, oldValue) {
            if (this.$.list.items.length > 0 && newValue < this.$.list.items[this.$.list.items.length - 1]) {
                var lastIndex = this.$.list.items.indexOf(newValue);
                this.$.list.splice("items", lastIndex + 1, this.$.list.items.length - lastIndex - 1);
                if (this.selectedNumber > newValue) {
                    this.selectedNumber = newValue;
                }
            }
        },

        _lowerThreshold: function () {
            this._loadNextNumbers();
        },

        _loadNextNumbers: function (toNumber) {
            if (this.$.list.items.length > 0) {
                var startFrom = this.$.list.items[this.$.list.items.length - 1];
                var calcTo = toNumber || startFrom + numbersToLoadAtOnce;
                calcTo = calcTo > this.upperBound ? this.upperBound : calcTo;
                if (calcTo > startFrom) {
                    for (startFrom += 1; startFrom <= calcTo; startFrom++) {
                        this.$.list.push("items", startFrom);
                    }
                }
                this.$.threshold.clearTriggers();
            }
        }
    });
})();
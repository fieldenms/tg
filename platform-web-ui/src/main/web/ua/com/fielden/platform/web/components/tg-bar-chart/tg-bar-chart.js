import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import  '/resources/components/tg-bar-chart/d3-bar-chart.js';

import {IronResizableBehavior} from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';
import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const template = html`
    <style>
        :host {
            @apply --layout-vertical;
        }
        [hidden] {
            display: none !important;
        }
        #chartContainer, svg {
            min-height: 0;
            @apply --layout-horizontal;
            @apply --layout-flex;
        }
        .chart-background {
            fill: white;
        }
        .grid path,
        .grid line {
            fill: none;
        }
        .grid path {
            stroke: none;
        }
        .grid line {
            stroke: #e3e3e3;
        }
        .bar {
            fill: steelblue;
            vector-effect: non-scaling-stroke;
        }
        .bar:hover {
            stroke: white;
            stroke-width: 1px;
            cursor: pointer;
        }
        .chart-label,
        .x-axis-label,
        .y-axis-label {
            font-family: sans-serif;
            font-size: 12px;
            stroke: none;
            fill: #000;
            pointer-events: none;
        }
        .chart-label {
            font-weight: bold;
        }
        .marker[selected] .mark-rect {
            fill: white;
        }
        .mark-text {
            font-family: sans-serif;
            font-size: 10px;
            stroke: none;
            fill: #000;
            pointer-events: none;
        }
        .mark-rect {
            fill: #d8d7ba;
            stroke: #e3e3e3;
            vector-effect: non-scaling-stroke;
            pointer-events: none;
        }
        /*Legend related style*/
        .legend-panel {
            margin-top: 8px;
            margin-bottom: 25px;
            @apply --layout-horizontal;
            @apply --layout-center-justified;
            @apply --layout-wrap;
        }
        .legend-rect {
            min-width: 18px;
            min-height: 18px;
            border: 1px solid black;
            margin-right: 8px;
            @apply --layout-horizontal;
            @apply --layout-center-center;
        }
        .legend-item {
            font-size: smaller;
            margin-right: 16px;
            @apply --layout-horizontal;
            @apply --layout-center;
        }
        .legend-item-line {
            border: none;
            margin: 0 2px;
            @apply --layout-flex;
        }
        .line {
            stroke-width: 1;
            fill: none;
            vector-effect: non-scaling-stroke;
        }
    </style>

    <!-- local DOM for your element -->
    <div id="chartContainer">
        <svg id="chart" class="bar-chart"></svg>
    </div>
    <!-- data bindings in local DOM -->
    <div id="legend" class="legend-panel" style$="[[_calcLegendStyle(_chartStyles)]]" hidden$="[[!showLegend]]">
        <template is="dom-repeat" items="[[legendItems]]" on-dom-change="_legendItemsChanged">
            <div class="legend-item">
                <div class="legend-rect" style$="[[_calcLegendItemStyle(item)]]"></div>
                <div>[[item.title]]</div>
            </div>
        </template>
        <template is="dom-repeat" items="[[lineLegendItems]]" on-dom-change="_legendItemsChanged">
            <div class="legend-item">
                <div class="legend-rect">
                    <hr class="legend-item-line" style="[[_calculateLineLegendStyle(item)]]">
                </div>
                <div>[[item.title]]</div>
            </div>
        </template>
    </div>`;

template.setAttribute('strip-whitespace', '');

Polymer({
    _template: template,

    is: "tg-bar-chart",

    // add properties and methods on the element's prototype

    properties: {
        // Declare properties for the element's public API
        data: {
            type: Array,
            observer: "_dataChanged"
        },

        options: {
            type: Object,
            observer: "_chartOptionsChanged"
        },
        
        //Determines whether legend should be visible or not
        showLegend: {
            type: Boolean,
            value: false
        },
        
        //The legend items whish represents title and colour
        legendItems: {
            type: Array,
            value: function () {
                return [];
            }
        },
        
        lineLegendItems: {
            type: Array,
            value: function () {
                return [];
            }
        },

        _chartStyle: Object,
        _chart: Object
    },

    behaviors: [IronResizableBehavior],

    ready: function () {
        this._chart = d3.barChart(this.$.chart);
        if (this.data) {
            this._chart.data(this.data);
        }
        if (this.options) {
            this._chart.options(this.options);
        }
        this.scopeSubtree(this.$.chart, true);
        this.addEventListener("iron-resize", this._resizeEventListener.bind(this));
    },

    attached: function () {
        this.async(() => {
            const width = this.$.chartContainer.offsetWidth;
            const height = this.$.chartContainer.offsetHeight;
            this._chart.options({
                width: width,
                height: height
            });
        }, 1);
    },
    
    selectEntity: function (entity, select) {
        if (this._chart) {
            this._chart.selectEntity(entity, select);
        }
    },
    
    repaint: function () {
        if (this._chart) {
            this._chart.repaint();
        }
    },

    _dataChanged: function (newData, oldData) {
        if (this._chart) {
            this._chart.data(newData);
        }
    },

    _chartOptionsChanged: function (newOptions, oldOptions) {
        if (this._chart) {
            this._chart.options(newOptions);
            const updatedOptions = this._chart.options();
            this._chartStyles = {
                left: updatedOptions.margin.left,
                right: updatedOptions.margin.right
            };
        }
    },
    
    _calcLegendStyle: function (_chartStyles) {
        return "margin-left: " + _chartStyles.left + "px; margin-right: " + _chartStyles.right + "px;";
    },

    _calcLegendItemStyle: function (item) {
        return "background-color: " + item.colour + ";";
    },
    
    _calculateLineLegendStyle: function (item) {
        return "border-top: 2px solid " + item.colour + ";";
    },

    _legendItemsChanged: function () {
        this.notifyResize();
    },

    _resizeEventListener: function (event, details) {
        const width = this.$.chartContainer.offsetWidth;
        const height = this.$.chartContainer.offsetHeight;
        if (this._chart && width && height) {
            this._chart.options({
                width: width,
                height: height
            });
        }
    }
});
        
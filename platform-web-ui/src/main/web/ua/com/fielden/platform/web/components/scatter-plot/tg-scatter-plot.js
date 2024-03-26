import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { mixinBehaviors } from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import {IronResizableBehavior} from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

import  '/resources/components/scatter-plot/d3-scatter-plot.js';

const template = html`
    <style>
        :host {
            width: 100%;
            height: 100%;
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
        .dot {
            fill: steelblue;
            vector-effect: non-scaling-stroke;
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
    </style>

    <!-- local DOM for your element -->
    <div id="chartContainer">
        <svg id="chart" class="scatter-plot"></svg>
    </div>
    <template is="dom-repeat" items="[[legendItems]]" on-dom-change="_legendItemsChanged">
        <div class="legend-item">
            <svg><path d="[[_calcLegendItemPath(item.style)]]" style$="[[_calcLegendItemStyle(item.style)]]"></svg>
            <div>[[item.title]]</div>
        </div>
    </template>`;

template.setAttribute('strip-whitespace', '');


export class TgScatterPlot extends mixinBehaviors([IronResizableBehavior], PolymerElement) {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            // Declare properties for the element's public API
            chartData: {
                type: Array,
                observer: "_chartDataChanged"
            },
    
            options: {
                type: Object,
                observer: "_chartOptionsChanged"
            },

            renderingHints: {
                type: Object,
                observer: "_renderingHintsChanged"
            },

            legendItems: {
                type: Array,
                value: () => []
            },
            
            _chart: Object
        };
    }

    ready () {
        super.ready();
        this._chart = d3.scatterPlot(this.$.chart);
        if (this.data) {
            this._chart.data(this.data);
        }
        if (this.options) {
            this._chart.options(this.options);
        }
        if (this.renderingHints) {
            this._chart.renderingHints(this.renderingHints);
        }
        this.scopeSubtree(this.$.chart, true);
        this.addEventListener("iron-resize", this._resizeEventListener.bind(this));
    }

    connectedCallback () {
        super.connectedCallback();
        this._waitForDimensions();
    }

    repaint () {
        if (this._chart) {
            this._chart.repaint();
        }
    }

    _waitForDimensions (time) {
        this.async(() => {
            const width = this.$.chartContainer.offsetWidth
            const height = this.$.chartContainer.offsetHeight
            if (width && height && this._chart) {
                this._chart.options({
                    width: width,
                    height: height
                });
            } else {
                this._waitForDimensions(100);
            }
        }, time);
    }

    _chartDataChanged (newData, oldData) {
        if (this._chart) {
            this._chart.data(newData);
        }
    }

    _chartOptionsChanged (newOptions, oldOptions) {
        if (this._chart) {
            this._chart.options(newOptions);
        }
    }

    _renderingHintsChanged (newHints, oldHints) {
        if (this._chart) {
            this._chart.renderingHints(newHints);
        }
    }
    
    _resizeEventListener (event, details) {
        const width = this.$.chartContainer.offsetWidth;
        const height = this.$.chartContainer.offsetHeight;
        if (this._chart && width && height) {
            const oldOptions = this._chart.options();
            if (oldOptions.width !== width || oldOptions.height !== height) {
                this._chart.options({
                    width: width,
                    height: height
                });
            }
        }
    }

    _legendItemsChanged () {
        this.notifyResize();
    }

    _calcLegendStyle (options) {
        return "margin-left: " + options.margin.left + "px; margin-right: " + options.margin.right + "px;";
    }

    _calcLegendItemPath (style) {
        return d3.scatterPlot.shapes[style.shape];
    } 
    
    _calcLegendItemStyle (style) {
        let styles = "";
        Object.keys(style).forEach(styleKey => {
            styles += `${styleKey}: ${style[styleKey]};`;
        })
        return styles;
    }
}

customElements.define('tg-scatter-plot', TgScatterPlot);
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
            stroke: black;
            stroke-width: 1px;
            fill: steelblue;
            vector-effect: non-scaling-stroke;
        }
        .dot:not(.legend-path):hover {
            stroke: white;
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
        .legend-panel {
            /* margin-top: 8px;
            margin-bottom: 25px; */
            @apply --layout-horizontal;
            @apply --layout-center-justified;
            @apply --layout-wrap;
        }
        .legend-item {
            font-size: smaller;
            margin-right: 16px;
            @apply --layout-horizontal;
            @apply --layout-center;
        }
        .legend-shape {
            width: 30px;
            height: 30px;
            margin-right: 8px;
            @apply --layout-center-center;
        }
        .legend-path {
            transform: translate(15px, 15px);
        }
    </style>

    <!-- local DOM for your element -->
    <div id="chartContainer">
        <svg id="chart" class="scatter-plot"></svg>
    </div>
    <div id="legend" class="legend-panel" style$="[[_calcLegendStyle(_chartStyles, margins)]]">
        <template is="dom-repeat" items="[[legendItems]]" on-dom-change="_legendItemsChanged">
            <div class="legend-item">
                <svg class="legend-shape"><path class="dot legend-path" d$="[[_calcLegendItemPath(item.style)]]" style$="[[_calcLegendItemStyle(item.style)]]"></svg>
                <div>[[item.title]]</div>
            </div>
        </template>
    </div>`;

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

            margins: {
                type: Object,
                value: () => {return {top: 0, right: 0, bottom: 0, left: 0}}
            },
            
            _chart: Object
        };
    }

    static get observers() {
        return [
          "_stylesChaged(options, margins)"
        ]
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
        this.$.chart.addEventListener("scatter-plot-left-margin-changed", e => this._stylesChaged(this._chart.options(), this.margins));
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

    _stylesChaged (options, margins) {
        if (options && options.margin && margins) {
            this.$.chartContainer.style.marginTop = `${margins.top || 0}px`;
            this.$.chartContainer.style.marginLeft = `${margins.left || 0}px`;
            this.$.chartContainer.style.marginRight = `${margins.right || 0}px`;
            this.$.legend.style.marginLeft = `${options.margin.left + (margins && margins.left ? margins.left : 0)}px`;
            this.$.legend.style.marginRight = `${options.margin.right + (margins && margins.right ? margins.right : 0)}px`;
            this.$.legend.style.marginBottom = `${margins && margins.bottom ? margins.bottom : 0}px`;
            this.$.legend.style.marginTop = `10px`;
            this.notifyResize();
        }
    }

    _calcLegendItemPath (style) {
        return d3.symbol().type(d3.scatterPlot.shapes[style.shape]).size(style.size)();
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
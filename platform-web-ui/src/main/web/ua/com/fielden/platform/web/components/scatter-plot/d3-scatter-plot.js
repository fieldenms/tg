import '/resources/components/d3-lib.js';
import { generateUUID } from '/resources/reflection/tg-polymer-utils.js';

//Symbols enum
const Symbols = {
    circle: d3.symbolCircle,
    cross: d3.symbolCross,
    diamond: d3.symbolDiamond,
    square: d3.symbolSquare,
    star: d3.symbolStar,
    triangle: d3.symbolTriangle,
    wye: d3.symbolWye
}

//Merges two objects up to two 
const merge2LevelData = function (oldOptions, newOptions) {
    const newObj = mergeData(oldOptions, newOptions);
    Object.keys(newObj).filter(key => typeof newObj[key] === 'object' && !Array.isArray(newObj[key])).forEach(key => {
        newObj[key] = mergeData(oldOptions[key], newOptions[key]);
    });
    return newObj;
};
const mergeData = function (oldData, newData) {
    return Object.assign({}, oldData, newData)
};

/**
 * Gets or sets the value for entity if value is undefined the it gets the value ortherwise it sets the value. Also obj might be a function it means that user has provided formatter for value.
 */
const value = (obj, data) => {
    if (typeof obj === 'function') {
        return obj(data);
    } else {
        return data.get ? data.get(obj.toString()) : data[obj.toString()];
    }
};

const getRange = function (rangeValue) {
    if (typeof rangeValue === 'function') {
        return rangeValue();
    }
    return rangeValue;
};

class ScatterPlot {

    constructor(container, options) {
        //////////////////////Private variables can be configured through the api//////////////////////////
        //Most common configuration properties.
        this._options = {
            width: 35,
            height: 85,
            margin: {
                top: 0,
                right: 0,
                bottom: 0,
                left: 0
            },
            label: "",
            xAxis: {
                label: "",
                range: []
            },
            yAxis: {
                label: "",
                range: []
            },
            dataPropertyNames: {
                id: "id",
                categoryProp: "categoryProp",
                valueProp: "valueProp",
                styleProp: "styleProp"
            },
            tooltip: (d) => {return ""},
            click: (d) => { },
        };
        this._options = options ? merge2LevelData(this._options, options) : this._options;
        this._data = [];
        this._renderingHints = [];

        //////////////////////Functional private variables///////////////////
        this._actualWidth = this._options.width - this._options.margin.left - this._options.margin.right;
        this._actualHeight = this._options.height - this._options.margin.bottom - this._options.margin.top;

        //Holds the current transform 
        this._currentTransform = d3.zoomIdentity;
        // Holds the list of elements those can't be scaled and is used after current transforamation changed to unscale these elements.
        this._elemToUnscale = [];

        //Creating coordinate system.
        this._xs = this._xScale();
        this._ys = this._yScale();
        // Createing x and y axis they also should be modifiable.
        this._xAxis = this._createXAxis();
        this._yAxis = this._createYAxis();
        // Creat grids that should be modifiable.
        this._xGrid = this._createXGrid();
        this._yGrid = this._createYGrid();
        //create clip path to cut off the chart area.
        const clipPathId = generateUUID();
        this._clipPath = this._createClipPath(container, clipPathId);
        //Create area for zooming and panning that will contain axes, grids and chart area with dots.
        this._chartArea = this._createChartArea(container);
        //Create background area
        this._background = this._createBackground();

        //Draw grid
        this._xGridGroup = this._drawXGrid();
        this._yGridGroup = this._drawYGrid();

        //Creating dots container.
        this._dataContainer = this._createDataContainer(clipPathId);

        //Draw axes
        this._xAxisGroup = this._drawXAxes();
        this._yAxisGroup = this._drawYAxes();

        //Draw labels
        this._chartLabel = this._drawChartLabel(container);
        this._xAxisLabel = this._drawXAxisLabel(container);
        this._yAxisLabel = this._drawYAxisLabel(container);

        //Now draw data with dots
        this._drawData();

        this._zoom = d3.zoom()
            .scaleExtent([1, 10])
            .translateExtent([[0, 0], [this._actualWidth, 0]])
            .extent([[0, 0], [this._actualWidth, 0]])
            .filter(() => {
                return !d3.event.button && (d3.event.type !== "wheel" || d3.event.altKey);
            })
            .on("zoom", () => {
                this._currentTransform = d3.event.transform;
                this._xAxisGroup.call(this._xAxis.scale(this._currentTransform.rescaleX(this._xs)));
                this._xGridGroup.call(this._xGrid.scale(this._currentTransform.rescaleX(this._xs)));
                this._zoomDataContainer();
                //this._dataContainer.attr("transform", "translate(" + this._currentTransform.x + ", 0)scale(" + this._currentTransform.k + ", 1)");
            });
        this._chartArea.call(this._zoom).on("dblclick.zoom", null).on("wheel", function () { d3.event.altKey && d3.event.preventDefault(); });
    }

    repaint(resetState) {
        //The old position of origin point.Needed to update the position of viewpoint after resizing.
        let oldX = this._xAxis.scale().invert(0);
        //Calculate new width and height without margins.
        this._actualWidth = this._options.width - this._options.margin.left - this._options.margin.right;
        this._actualHeight = this._options.height - this._options.margin.bottom - this._options.margin.top;
        if (this._actualWidth < 0) {
            this._options.width = this._options.margin.left + this._options.margin.right
            this._actualWidth = 0;
        }
        if (this._actualHeight < 0) {
            this._options.height = this._options.margin.bottom + this._options.margin.top;
            this._actualHeight = 0;
        }

        // Create new scales acoording to new width and height.
        this._xs = this._xScale();
        this._ys = this._yScale();
        // Update clip path according to new width and height.
        this._clipPath.attr("width", this._actualWidth).attr("height", this._actualHeight);
        // Update chart group node according to new margins. 
        this._chartArea.attr("transform", "translate(" + this._options.margin.left + "," + this._options.margin.top + ")");
        // Update background rectangle according to new width and height
        this._background.attr("width", this._actualWidth).attr("height", this._actualHeight);

        this._xGridGroup
            .attr("transform", "translate(0," + this._actualHeight + ")")
            .call(this._xGrid.tickSize(-this._actualHeight).scale(this._currentTransform.rescaleX(this._xs)));
        this._yGridGroup.call(this._yGrid.tickSize(-this._actualWidth).scale(this._ys));
        this._xAxisGroup.attr("transform", "translate(0," + this._actualHeight + ")").call(this._xAxis.scale(this._currentTransform.rescaleX(this._xs)));
        this._yAxisGroup.call(this._yAxis.scale(this._ys));

        // Update chart and axis captioins
        this._chartLabel.call(this._setChartLabelData.bind(this));
        this._xAxisLabel.call(this._setXAxisLabelData.bind(this));
        this._yAxisLabel.call(this._setYAxisLabelData.bind(this));

        // Update geometry of data.
        this._drawData();
        //Update zoom behavior
        this._zoom
            .translateExtent([[0, 0], [this._actualWidth, 0]])
            .extent([[0, 0], [this._actualWidth, 0]]);

        //Update the container translation in order to remain current translate position when window size was changed
        let newW = this._xAxis.scale()(oldX);
        if (resetState) {
            this._chartArea.call(this._zoom.transform, d3.zoomIdentity);
        } else if (newW) {
            this._chartArea.call(this._zoom.translateBy, (0 - newW) / this._currentTransform.k, 0);
        }
    }

    set options(val) {
        this._options = val ? merge2LevelData(this._options, val) : this._options;
        this.repaint();
    }

    get options() {
        return this._options;
    }

    set data(data) {
        this._data = data || this._data;
        this.repaint(true);
    }

    get data() {
        return this._data;
    }

    set renderingHints(hints) {
        this._renderingHints = hints || this._renderingHints;
        this.repaint();
    }

    get renderingHints() {
        return this._renderingHints;
    }

    _xScale() {
        const range = getRange(this._options.xAxis.range);
        if (range.length > 0) {
            range[0] = d3.timeDay.offset(range[0], -1);
            range[1] = d3.timeDay.offset(range[1], 1);
        }
        return d3.scaleTime().domain(range).range([0, this._actualWidth]);
    }

    _yScale() {
        return d3.scalePoint().domain(getRange(this._options.yAxis.range)).range([0, this._actualHeight]).padding(0.1);
    }

    _createXAxis() {
        return d3.axisBottom(this._xs);
    }

    _createXGrid() {
        return this._createXAxis().tickSize(-this._actualHeight).tickFormat("").tickSizeOuter(0);
    }

    _createYAxis() {
        return d3.axisLeft(this._ys);
    }

    _createYGrid() {
        return this._createYAxis().tickSize(-this._actualWidth).tickFormat("").tickSizeOuter(0);
    }

    _createClipPath(container, clipPathId) {
        return d3.select(container).append('defs').append("clipPath").attr("id", clipPathId)
            .append("rect").attr("width", this._actualWidth).attr("height", this._actualHeight);
    }

    _createChartArea(container) {
        return d3.select(container).append("g")
            .attr("class", "chart-area")
            .attr("transform", "translate(" + this._options.margin.left + "," + this._options.margin.top + ")");
    }

    _createBackground() {
        return this._chartArea.append("rect").attr("class", "chart-background").attr("width", this._actualWidth).attr("height", this._actualHeight);
    }

    _drawXGrid() {
        return this._chartArea.append("g").attr("class", "grid x-grid")
            .attr("transform", "translate(0, " + this._actualHeight + ")").call(this._xGrid);
    }

    _drawYGrid() {
        return this._chartArea.append("g").attr("class", "grid y-grid").call(this._yGrid);
    }

    _createDataContainer(clipPathId) {
        return this._chartArea.append("g").attr("clip-path", "url(#" + clipPathId + ")").append("g").attr("class", "data-container");
    }

    _drawXAxes() {
        return this._chartArea.append("g").attr("class", "axis x-axis")
            .attr("transform", "translate(0," + this._actualHeight + ")").call(this._xAxis);
    }

    _drawYAxes() {
        return this._chartArea.append("g").attr("class", "axis y-axis").call(this._yAxis);
    }

    _drawChartLabel(container) {
        const lable = d3.select(container)
            .append("text")
            .attr("class", "chart-label")
            .style("text-anchor", "middle")
            .style("alignment-baseline", "baseline");
        setTimeout(() => lable.call(this._setChartLabelData.bind(this)), 1);
        return lable;
    }

    _setChartLabelData(chartLable) {
        chartLable.text(this._options.label)
            .attr("x", this._options.margin.left + this._actualWidth / 2)
            .attr("y", this._options.margin.top - 10);
        this._adjustTopMargin(chartLable);
    }

    _adjustTopMargin(label) {
        const labelBox = label.node().getBBox();
        if ((!label.text() || (labelBox.width !== 0 && labelBox.height !== 0)) && this._options.margin.top < labelBox.height + 20) {
            this.options = { margin: { top: labelBox.height + 20 } };
        }
    }

    _setXAxisLabelPosition(xAxisLabel) {
        const axisBox = this._xAxisGroup.node().getBBox();
        if (axisBox.width !== 0 && axisBox.height !== 0) {
            xAxisLabel
                .attr("x", this._options.margin.left + this._actualWidth / 2)
                .attr("y", this._actualHeight + this._options.margin.top + axisBox.height + 10);
            this._adjustBottomMargin(xAxisLabel, axisBox);
        } else if (this._actualWidth !== 0 && this._actualHeight !== 0) {
            setTimeout(() => this._setXAxisLabelPosition(xAxisLabel), 100);
        }
    }

    _adjustBottomMargin(label, xAxisBox) {
        const labelBox = label.node().getBBox();
        if ((!label.text() || (labelBox.width !== 0 && labelBox.height !== 0)) && this._options.margin.bottom < xAxisBox.height + labelBox.height + 20) {
            this.options = { margin: { bottom: xAxisBox.height + labelBox.height + 20 } };
        }
    }

    _setXAxisLabelData(xAxisLabel) {
        xAxisLabel.text(this._options.xAxis.label).call(this._setXAxisLabelPosition.bind(this));
    }

    _drawXAxisLabel(container) {
        const lable = d3.select(container)
            .append("text")
            .attr("class", "x-axis-label")
            .style("text-anchor", "middle")
            .style("alignment-baseline", "hanging");
        setTimeout(() => lable.call(this._setXAxisLabelData.bind(this)), 1);
        return lable;
    }

    _setYAxisLabelData(yAxisLabel) {
        yAxisLabel.text(this._options.yAxis.label).call(this._setYAxisLabelPosition.bind(this));
    }

    _setYAxisLabelPosition(yAxisLabel) {
        const axisBox = this._yAxisGroup.node().getBBox();
        if (axisBox.width !== 0 && axisBox.height !== 0) {
            yAxisLabel
                .attr("x", -this._options.margin.top - this._actualHeight / 2)
                .attr("y", this._options.margin.left - axisBox.width - 10);
            this._adjustLeftMargin(yAxisLabel, axisBox);
        } else if (this._actualWidth !== 0 && this._actualHeight !== 0) {
            setTimeout(() => this._setYAxisLabelPosition(yAxisLabel), 100);
        }
    }

    _adjustLeftMargin(label, yAxisBox) {
        const labelBox = label.node().getBBox();
        if ((!label.text() || (labelBox.width !== 0 && labelBox.height !== 0)) && this._options.margin.left < yAxisBox.width + labelBox.height + 20) {
            this.options = { margin: { left: yAxisBox.width + labelBox.height + 20 } };
        }
    }

    _drawYAxisLabel(container) {
        const lable = d3.select(container)
            .append("text")
            .attr("class", "y-axis-label")
            .style("text-anchor", "middle")
            .style("alignment-baseline", "baseline")
            .attr("transform", "rotate(-90)");
        setTimeout(() => lable.call(this._setYAxisLabelData.bind(this)), 1);
        return lable;
    }

    _drawData() {
        if (this._xs.domain() && this._xs.domain().length > 0 && this._ys.domain() && this._ys.domain().length > 0) {
            const rendHints = (i) => {
                const renderingHints = this._renderingHints[i] || {};
                const propStyle = value(this._options.dataPropertyNames.styleProp, renderingHints) || {};
                return propStyle.backgroundStyles || propStyle || {};
            };

            const dataModel = this._data.map((d, i) => {
                return {
                    data: d,
                    renderingHints: rendHints(i)
                }
            });
            dataModel.sort((a, b) => {
                const aZIndex = +a.renderingHints["z-index"] || 0;
                const bZIndex = +b.renderingHints["z-index"] || 0;
                return aZIndex - bZIndex;
            });

            const updateSelection = this._dataContainer.selectAll(".dot").data(dataModel);
            const insertSelection = updateSelection.enter();
            const removeSelection = updateSelection.exit();

            //update data groups
            updateSelection.call(this._updateData.bind(this));
            //insert new data groups
            insertSelection.call(this._insertNewData.bind(this));
            //removed unnedded data groups
            removeSelection.remove();
        }
    }

    _insertNewData(selection) {
        selection.append("path").attr("class", "dot").on("click", d => {
            if (d3.event.button === 0 ) {
                this._options.click(d.data);
            }
        }).call(this._updateData.bind(this));
    }

    _updateData(selection) {
        selection
            .attr("transform", d => `translate(${this._xs(value(this._options.dataPropertyNames.valueProp, d.data))}, ${this._ys(value(this._options.dataPropertyNames.categoryProp, d.data))})`)
            .attr("tooltip-text", d => this._options.tooltip(d.data))
            .attr("d", d3.symbol().type(d => {
                return Symbols[d.renderingHints.shape] || Symbols.circle; //Default shape is circle
            }).size(d => {
                return d.renderingHints.size || 50; //Default size is 50
            })
            ).each(function (d) {
                const dot = d3.select(this);
                Object.keys(d.renderingHints).forEach(key => {
                    dot.style(key, d.renderingHints[key]);
                });
            });
    }

    _zoomDataContainer() {
        const rescaledX = this._currentTransform.rescaleX(this._xs);
        this._dataContainer
            .selectAll(".dot")
            .attr("transform", d => `translate(${rescaledX(value(this._options.dataPropertyNames.valueProp, d.data))}, ${this._ys(value(this._options.dataPropertyNames.categoryProp, d.data))})`)
    }
}

d3.scatterPlot = (container, options) => {
    const scatterPlot = new ScatterPlot(container, options);

    const chart = {
        _chartForDebugging: scatterPlot,
        options: o => {
            if (o) {
                scatterPlot.options = o;
                return chart;
            } else {
                return scatterPlot.options;
            }
        },
        data: d => {
            if (d) {
                scatterPlot.data = d;
                return chart;
            } else {
                return scatterPlot.data;
            }
        },
        renderingHints: h => {
            if (h) {
                scatterPlot.renderingHints = h;
                return chart;
            } else {
                return scatterPlot.renderingHints;
            }
        },
        repaint: () => scatterPlot.repaint()
    };
    return chart;
};

d3.scatterPlot.shapes = Symbols;
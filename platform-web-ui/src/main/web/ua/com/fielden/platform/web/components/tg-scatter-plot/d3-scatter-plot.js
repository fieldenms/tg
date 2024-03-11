import '/resources/components/d3-lib.js';
import { generateUUID} from '/resources/reflection/tg-polymer-utils.js';


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
const value = (obj, data, value) => {
    if (typeof obj === 'function') {
        return obj(data, value);
    } else {
        if (typeof value === 'undefined') {
            return data.get(obj.toString());
        } else {
            const lastDotIndex = obj.lastIndexOf(".");
            const rest = lastDotIndex > -1 ? obj.slice(lastDotIndex + 1) : obj;
            const firstVal = lastDotIndex > -1 ? data.get(obj.slice(0, lastDotIndex)) : data;
            if (firstVal && rest) {
                firstVal.set(rest, value);
            }
        }
    }
};

//TODO might not be needed
const copyMatrix = function(from, to) {
    to.a = from.a;
    to.b = from.b;
    to.c = from.c;
    to.d = from.d;
    to.e = from.e;
    to.f = from.f;
    return to;
}

/**
 * Fires the select event for provided entity and select parameter.
 */
const fireSelectEvent = (entity, select, chartArea) => {
    const event = new CustomEvent("bar-entity-selected", {
        detail: {
            shouldScrollToSelected: false,
            entities: [{
                entity: entity,
                select: select
            }]
        },
        bubbles: true,
        composed: true,
        cancelable: true
    });
    chartArea.dispatchEvent(event);
};

const fireLabelPlacedEvent = (eventName, newRequiredMargin, chartArea) => {
    const event = new CustomEvent(eventName, {
        detail: newRequiredMargin,
        bubbles: true,
        composed: true,
        cancelable: true
    });
    chartArea.dispatchEvent(event);
}

class ScatterPlot {

    constructor(container, options) {
        //////////////////////Private variables can be configured through the api//////////////////////////
        //Most common configuration properties.
        this._options = {
            width: 35,
            height: 85,
            margin: {
                top: 40,
                right: 35,
                bottom: 45,
                left: 0
            },
            label: "",
            xAxis: {
                label: "" 
            },
            yAxis: {
                label: ""
            },
            dataPropertyNames: {
                id: "id",
                categoryProp: "categoryProp",
                valueProp: "valueProp",
            },
            click: (d, idx) => {},
        };
        this._options = options ? merge2LevelData(this._options, options) : this._options;
        this._data = [];

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
                this._dataContainer.attr("transform", "translate(" + this._currentTransform.x + ", 0)scale(" + this._currentTransform.k + ", 1)");
            });
        this._chartArea.call(this._zoom).on("dblclick.zoom", null).on("wheel", function() { d3.event.altKey && d3.event.preventDefault(); });
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

        // Reset visualy selected and activated bars.
        if (resetState) {
            this._selectedData = [];
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

        // Update bars.
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

    _xDomain() {
        const vals = data.map(d => value(options.dataPropertyNames.valueProp));
        const minValue = Math.min(...vals);
        const maxValue = Math.max(...vals);
        return [minValue === Infinity ? new Date().getMilliseconds() : minValue, maxValue === Infinity ? new Date().getMilliseconds() : maxValue];
    }

    _yDomain() {
        return data.map(d => value(options.dataPropertyNames.categoryProp, d));
    }

    _xScale() {
        return d3.scaleTime().domain(this._xDomain()).range([0, this._actualWidth]);
    }

    _yScale() {
        return d3.scalePoint().domain(this._yDomain()).range([0, this._actualHeight]).padding(0.1);
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
        return this._chartArea.append("g").attr("clip-path", "url(#" + clipPathId +")").append("g").attr("class", "bar-container");
    }

    _drawXAxes() {
        return this._chartArea.append("g").attr("class", "axis x-axis")
            .attr("transform", "translate(0," + this._actualHeight + ")").call(this._xAxis);
    }
    
    _drawYAxes() {
        return this._chartArea.append("g").attr("class", "axis y-axis").call(this._yAxis);
    }


    _setChartLabelData(chartLabel) {
        chartLabel.text(this._options.label)
            .attr("x", this._options.margin.left + this._actualWidth / 2)
            .attr("y", this._options.margin.top - 10);
    }

    _drawChartLabel(container) {
        return d3.select(container)
            .append("text")
            .attr("class", "chart-label")
            .style("text-anchor", "middle")
            .style("alignment-baseline", "baseline")
            .call(this._setChartLabelData.bind(this));
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
            if ((!label.text() || (labelBox.width !== 0 && labelBox.height !== 0)) && this._options.margin.bottom !== xAxisBox.height + labelBox.height + 20) {
                this.options = {margin: {bottom: xAxisBox.height + labelBox.height + 20}};
            }
        }

    _setXAxisLabelData(xAxisLabel) {
        xAxisLabel.text(this._options.xAxis.label).call(this._setXAxisLabelPosition.bind(this));
    }

    _drawXAxisLabel(container) {
        return d3.select(container)
            .append("text")
            .attr("class", "x-axis-label")
            .style("text-anchor", "middle")
            .style("alignment-baseline", "hanging")
            .call(this._setXAxisLabelData.bind(this));
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
            this._fireYAxisLabelWidthChanged(yAxisLabel, axisBox);
        } else if (this._actualWidth !== 0 && this._actualHeight !== 0) {
            setTimeout(() => this._setYAxisLabelPosition(yAxisLabel), 100);
        }
    }

    _fireYAxisLabelWidthChanged(label, yAxisBox) {
        const labelBox = label.node().getBBox();
        if (!label.text() || (labelBox.width !== 0 && labelBox.height !== 0)) {
            fireLabelPlacedEvent("y-axis-label-positioned", yAxisBox.width + labelBox.height + 20, this._chartArea.node());
        }
    }

    _drawYAxisLabel(container) {
        return d3.select(container)
            .append("text")
            .attr("class", "y-axis-label")
            .style("text-anchor", "middle")
            .style("alignment-baseline", "baseline")
            .call(this._setYAxisLabelData.bind(this))
            .attr("transform", "rotate(-90)");
    }

    _drawData() {
        const self = this;
        const updateSelection = self._dataContainer.selectAll(".bar-group").data(this._data);
        const insertSelection = updateSelection.enter();
        const removeSelection = updateSelection.exit();

        //update bar groups
        updateSelection.call(self._updateBarGroup.bind(self));
        //insert new bar groups
        insertSelection.call(self._insertNewBarGroup.bind(self));
        //removed unnedded bar groups
        removeSelection.remove();
    }

    _insertNewBarGroup(selection) {
        const self = this;
        selection.append("g").attr("class", "bar-group").call(self._updateBarGroup.bind(self));
    }
    
    _updateBarGroup(selection) {
        const self = this;
        selection.attr("transform", d => "translate(" + this._xs(value(this._options.dataPropertyNames.groupKeyProp, d)) + ",0)");
        const updateBarSelection = selection.selectAll(".bar").data(d => this._options.dataPropertyNames.valueProps.map((val, idx) => {return {data: d, idx: idx};}));
        const insertBarSelection = updateBarSelection.enter();
        const removeBarSelection = updateBarSelection.exit();
        
        //update bars
        updateBarSelection.call(self._updateBar.bind(self));
        //insert bars
        insertBarSelection.call(self._insertNewBar.bind(self));
        //removed unnedded bars
        removeBarSelection.remove();
    }
    
    _insertNewBar(selection) {
        const self = this;
        const newBars = selection.append("rect").attr("class", "bar").on("click", function (d) {
                if (d3.event.button == 0 && (d3.event.ctrlKey || d3.event.metaKey)) {
                    const val = value(self._options.dataPropertyNames.valueProps[d.idx], d.data);
                    const markerIdEnding = self._options.mode === BarMode.GROUPED ?  d.idx : (val >= 0 ? 1 : -1);
                    const markerId = "#marker_" + value(self._options.dataPropertyNames.id, d.data) + "_" + markerIdEnding;
                    self._selectEntity(this, self._markerContainer.select(markerId).node(), d, !self._isEntitySelected(d), true);
                } else {
                    self._options.click(d.data, d.idx);
                }
            })
            .call(self._updateBar.bind(self));
    }

    _updateBar(selection) {
        selection.attr("id", d => "bar_" + value(this._options.dataPropertyNames.id, d.data) + "_" + d.idx);

        selection.style("fill", d => this._isEntitySelected(d) ? this._options.selectedColour(d.data, d.idx) : this._options.barColour(d.data, d.idx))
            .attr("selected", d => this._isEntitySelected(d) ? "" : null)
            .attr("x", d => this._options.mode === BarMode.GROUPED ? this._xs1(d.idx) : 0)
            .attr("y", d => {
                let y = 0;
                if (this._options.mode === BarMode.GROUPED) {
                    const yValue = value(this._options.dataPropertyNames.valueProps[d.idx], d.data);
                    if (yValue > 0) {
                        y = yValue
                    }
                } else {
                    const prevYValues = this._options.dataPropertyNames.valueProps.slice(0, d.idx).map(valueProp => value(valueProp, d.data));
                    const currYValue = value(this._options.dataPropertyNames.valueProps[d.idx], d.data);
                    if (currYValue > 0) {
                        y = prevYValues.filter(val => val > 0).reduce((a, b) => a + b, 0) + currYValue;
                    } else {
                        y = prevYValues.filter(val => val < 0).reduce((a, b) => a + b, 0);
                    }
                }
                return this._ys(y);
            })
            .attr("height", d => {
                return Math.abs(this._ys(value(this._options.dataPropertyNames.valueProps[d.idx], d.data)) - this._ys(0));
            })
            .attr("width", d => this._xs1.bandwidth())
            .attr("tooltip-text", d => this._options.tooltip(d.data, d.idx));
    }
    
    _drawLines() {
        const self = this;
        const updateSelection = self._lineContainer.selectAll(".line").data(this._options.lines);
        const insertSelection = updateSelection.enter();
        const removeSelection = updateSelection.exit();

        //update line paths
        updateSelection.call(self._updateLines.bind(self));
        //insert new line paths
        insertSelection.call(self._insertNewLine.bind(self));
        //removed unnedded line paths
        removeSelection.remove();
    }
    
    _insertNewLine(selection) {
        selection.append("path").attr("class", "line").call(this._updateLines.bind(this));
    }
    
    _updateLines(selection) {
        const line = d3.line().x(d => d.x).y(d => d.y);
        selection.style("stroke", d => d.colour).attr("d", d => line(this._generateLineData(d)));
    }
    
    _generateLineData(line) {
        const lineData = this._data.map(e => {
            const x = this._xs(value(this._options.dataPropertyNames.groupKeyProp, e)) + this._xs.bandwidth() / 2;
            const y = this._ys(value(line.property, e));
            return {x: x, y: y};
        });
        if (lineData.length < 2) {
            const yVal = lineData[0] ? lineData[0].y : 0;
            lineData.unshift({x: 0, y: yVal});
            lineData.push({x: this._actualWidth, y: yVal});
        } else {
            const deltaX1 = lineData[1].x - lineData[0].x;
            const k1 = deltaX1 ? (lineData[1].y - lineData[0].y) / deltaX1 : 0;
            const lastIdx = lineData.length - 1;
            const deltaX2 = lineData[lastIdx].x - lineData[lastIdx - 1].x
            const k2 = deltaX2 ? (lineData[lastIdx].y - lineData[lastIdx - 1].y) / deltaX2 : 0;
            const b1 = lineData[0].y - k1 * lineData[0].x;
            const b2 = lineData[lastIdx].y - k2 * lineData[lastIdx].x
            lineData.unshift({x: 0, y: b1});
            lineData.push({x: this._actualWidth, y: k2 * this._actualWidth + b2});
        }
        return lineData;
    }
    
    _drawBarLabels() {
        const self = this;
        const newData = [];
        this._data.forEach(elem => {
            if (this._options.mode === BarMode.GROUPED) {
                this._options.dataPropertyNames.valueProps.forEach((val, idx) => {
                    newData.push({data: elem, idx: idx});
                });
            } else {
                newData.push({data: elem, idx: 1});
                newData.push({data: elem, idx: -1});
            }
        });
        const updateSelection = self._markerContainer.selectAll(".marker").data(newData);
        const insertSelection = updateSelection.enter();
        const removeSelection = updateSelection.exit();

        //update markers
        updateSelection.call(self._updateMarker.bind(self));
        //insert new markers
        insertSelection.call(self._insertNewMarker.bind(self));
        //remove markers
        removeSelection.remove();
    }

    _insertNewMarker(selection) {
        const self = this;
        const markerGroup = selection.append("g")
            .attr("class", "marker");
        markerGroup.append("text").attr("class", "mark-text").style("text-anchor", "middle");
        //Observing style and class changes
        markerGroup.each(function () {
            //Add to unsclable node in order to prevent zooming
            self._elemToUnscale.push(this);
        });
        markerGroup.call(this._updateMarker.bind(this));
    }

    _updateMarker(selection) {
        const self = this;
        selection.attr("id", d => "marker_" + value(this._options.dataPropertyNames.id, d.data) + "_" + d.idx);
        selection.attr("transform", d => {
            const newY = self._calculateNewMarkerY(d);
            const increment = newY < 0 ? 10 : -10;
            const translateX = this._xs(value(this._options.dataPropertyNames.groupKeyProp, d.data))
                                + this._xs1(this._options.mode === BarMode.GROUPED ? d.idx : 0) + this._xs1.bandwidth() / 2;
            return "translate(" + translateX + ", " + (this._ys(newY) + increment) + ")";
        }).attr("selected", d => this._isEntityGroupSelected(d) ? "" : null);
        selection.each(function (d) {
            let text = d3.select(this).select(".mark-text").style("alignment-baseline", d => {
                const newY = self._calculateNewMarkerY(d);
                if (newY < 0) {
                    return "hanging";
                }
                return "auto";
            }).text(d => self._options.barLabel(d.data, d.idx));
            d3.select(this).style("display", !!text.text() ? "initial" : "none");
        });
    }
    
    _isEntityGroupSelected(d) {
        if (this._options.mode === BarMode.GROUPED) {
            return this._isEntitySelected(d);
        } else {
            let pred;
            if (d.idx < 0) {
                pred = val => value(this._options.dataPropertyNames.valueProps[val.idx], val.data) < 0;
            } else {
                pred = val => value(this._options.dataPropertyNames.valueProps[val.idx], val.data) >= 0;
            }
            return this._options.dataPropertyNames.valueProps.map((valueProp, idx) => {return {data: d.data, idx: idx};}).filter(pred).some(d => this._isEntitySelected(d));
        }
    }
    
    _calculateNewMarkerY(d) {
        let newY = 0;
        if (this._options.mode === BarMode.GROUPED) {
            newY = value(this._options.dataPropertyNames.valueProps[d.idx], d.data);
        } else {
            const newYValues = this._options.dataPropertyNames.valueProps.map(valueProp => value(valueProp, d.data));
            const predicate = d.idx >= 0 ? val => val > 0 : val => val < 0;
            newY = newYValues.filter(predicate).reduce((a, b) => a + b, 0);
        }
        return newY;
    }

    _isEntitySelected(entity) {
        return !!this._selectedEntities.find(selectedEntity => {
            return value(this._options.dataPropertyNames.id, selectedEntity.data) === value(this._options.dataPropertyNames.id, entity.data) && selectedEntity.idx === entity.idx
        });
    }
    
    _isAnyEntitySelected (entity) {
        return this._options.dataPropertyNames.valueProps.some((prop, i) => this._isEntitySelected({data: entity, idx: i}));
    }

    _selectEntity(bar, marker, entity, select, fireEvent) {
        if (entity) {
            const entityIndex = this._selectedEntities.findIndex(selectedEntity => {
                return value(this._options.dataPropertyNames.id, selectedEntity.data) === value(this._options.dataPropertyNames.id, entity.data)
                        && selectedEntity.idx === entity.idx;
            });                                                  
            if (select) {
                // add entity to selected if it is not there yet
                if (entityIndex < 0) {
                    const wasPreviousSelection = this._isAnyEntitySelected(entity.data)
                    this._selectedEntities.push(entity);
                    if (fireEvent && !wasPreviousSelection) {
                        fireSelectEvent(entity.data, select, this._chartArea.node());
                    }
                }
            } else {
                // remove entity from selected if it is there
                if (entityIndex >= 0) {
                    this._selectedEntities.splice(entityIndex, 1);
                    if (fireEvent && !this._isAnyEntitySelected(entity.data)) {
                        fireSelectEvent(entity.data, select, this._chartArea.node());
                    }
                }
            }
            if (bar && marker) {
                this._selectBar(bar, marker, entity, select);
            }
        }
    }

    _selectBar(bar, marker, entity, select) {
        const indexOfBar = this._selectedBars.indexOf(bar);
        d3.select(bar).style("fill", d => select ? this._options.selectedColour(d.data, d.idx) : this._options.barColour(d.data, d.idx))
            .attr("selected", select ? "" : null);
        d3.select(marker).attr("selected", d => select || this._isEntityGroupSelected(d3.select(marker).datum()) ? "" : null);

        if (select) {
            if (indexOfBar < 0) {
                this._selectedBars.push(bar);
            }
        } else {
            if (indexOfBar >= 0) {
                this._selectedBars.splice(indexOfBar, 1);
            }
        }
    }

    _unscale() {
        const elementsToRemove = [];
        const m = this._chartArea.node().getTransformToElement(this._markerContainer.node());
        this._elemToUnscale.forEach(el => {
            const elementToRemove = this._unscaleElement(el, m);
            if (elementToRemove) {
                elementsToRemove.push(el);
            }
        });
        elementsToRemove.forEach(el => {
            const elIndex = this._elemToUnscale.indexOf(el);
            if (elIndex >= 0) {
                this._elemToUnscale.splice(elIndex, 1);
            }
        });
    }

    _unscaleElement(el, m) {
        const dataValue = this._calculateNewMarkerY(d3.select(el).datum());
        const translationFromTopOfBar = dataValue < 0 ? -10 : 10;
        if (el.parentElement) {
            let xf;
            if (el.transform.baseVal.length < 2) {
                // Keep a single transform matrix in the stack for fighting transformations
                // Be sure to apply this transformation after existing transformations (translate)
                xf = this._chartArea.node().ownerSVGElement.createSVGTransform();
                el.transform.baseVal.appendItem(xf);
            } else {
                xf = el.transform.baseVal[1];
            }
            const copyM = copyMatrix(m, this._chartArea.node().ownerSVGElement.createSVGMatrix());
            copyM.e = 0; // Ignore (preserve) any translations done up to this point
            copyM.f = translationFromTopOfBar * (1 - m.d);
            xf.setMatrix(copyM);
        } else {
            return el;
        }
    }
}

d3.barChart = (container, options, data) => {
    const barChart = new BarChart(container, options, data);

    const chart = {
        _chartForDebugging: barChart,
        options: o => {
            if (o) {
                barChart.options = o;
                return chart;
            } else {
                return barChart.options;
            }
        },
        data: d => {
            if (d) {
                barChart.data = d;
                return chart;
            } else {
                return barChart.data;
            }
        },
        selectEntity: (entity, select) => {
            barChart.selectEntity(entity, select);
            return chart
        },
        repaint: () => barChart.repaint()
    };
    return chart;
};

d3.barChart.BarMode = BarMode;
d3.barChart.LabelOrientation = LabelOrientation;
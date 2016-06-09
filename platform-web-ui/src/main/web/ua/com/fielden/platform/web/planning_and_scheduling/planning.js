(function () {

    d3.planning = function (barWidth, stretchWidth, now, selectColor, leftBound, rightBound, isOverCompleted, completedClass, overCompletedClass) {
        //Most common configuration properties.
        var width = 640,
            height = 480,
            margin = {
                top: 35,
                right: 35,
                bottom: 35,
                left: 35
            },
            //The minimal duration of the task.
            minWidth = 24 * 60 * 60 * 1000;
        //Variables needed for indicating selected, activated tasks.
        var selectedTasks = [];
        var activatedTasks = [];
        //Needed for tracking drag actions.
        var currentDragEvent;
        //The canvas to draw chart on.
        var svg;
        //The x and y axises accordingly.
        var x, y;
        //The x and y axis grids accordingly.
        var xGrid, yGrid;
        //The chart's data.Task names, task data, markers e.t.c.
        var tskTypes = [];
        var tskStatuses = [];
        var markers = [
            {where: now, style: "nowMarker"}
        ];
        //The containers for chart's graphic elements
        var graphContainer;
        var elemToUnscale = [];

        var timeRange = function () {
            var timeDomainStart = 0, timeDomainEnd = 0;

            if (!tskStatuses || tskStatuses.length < 1) {
                return {
                    from: d3.time.day.offset(now, -3),
                    to: d3.time.hour.offset(now, +3)
                };
            }

            tskStatuses.forEach(function (elem) {
                timeDomainStart = timeDomainStart || elem.entity.from;
                timeDomainEnd = timeDomainEnd || elem.entity.to;
                timeDomainStart = timeDomainStart.getTime() - elem.entity.from.getTime() > 0 ? elem.entity.from : timeDomainStart;
                timeDomainEnd = timeDomainEnd.getTime() - elem.entity.to.getTime() < 0 ? elem.entity.to : timeDomainEnd;
            });
            return {
                from: timeDomainStart,
                to: timeDomainEnd
            };
        };

        var xScale = function () {
            var tRange = timeRange();
            return d3.time.scale().domain([tRange.from, tRange.to]).range([ 0, width]);
        };

        var yScale = function () {
            return d3.scale.ordinal().domain(tskTypes).rangeRoundBands([ 0, height], .1, .1);
        };

        var xAxis = function (xs) {
            return d3.svg.axis().scale(xs).orient("bottom").ticks(6);
        };

        var yAxis = function (ys) {
            return d3.svg.axis().scale(ys).orient("left");
        };

        var drawBackground = function () {
            svg.append("rect")
                .attr("class", "background")
                .attr("width", width)
                .attr("height", height);
        };

        var drawGrid = function (xs, ys) {
            xGrid = xAxis(xs).tickSize(-height, 0, 0).tickFormat("");
            yGrid = yAxis(ys).tickSize(-width, 0, 0).tickFormat("");
            //Creates the x axis grid
            svg.append("g")
                .attr("class", "x grid")
                .attr("transform", "translate(0," + height + ")")
                .call(xGrid);
            //Creates the y axis grid
            svg.append("g")
                .attr("class", "y grid")
                .call(yGrid);
        };

        var drawAxis = function (xs, ys) {
            var format = d3.time.format.multi([
                ["%I:%M %p", function (d) {
                    return d.getMilliseconds() || d.getSeconds() || d.getMinutes() || d.getHours();
                }],
                ["%d/%m/%Y", function () {
                    return true;
                }]
            ]);
            x = xAxis(xs).tickFormat(format);
            y = yAxis(ys);
            //Draws the x axis with ticks.
            svg.append("g")
                .attr("class", "x axis")
                .attr("transform", "translate(0," + height + ")")
                .call(x)
                .selectAll(".tick text");
            //Draws the y axis with ticks.
            svg.append("g")
                .attr("class", "y axis")
                .call(y);
        };

        var drawMarkers = function (xs) {
            var dataSelection = graphContainer.selectAll(".marker").data(markers)
                .attr("class", function (d) {
                    return "marker " + d.style;
                })
                .attr("style", "vector-effect: non-scaling-stroke")
                .attr("x1", function (d) {
                    return xP(d.where, xs);
                })
                .attr("x2", function (d) {
                    return xP(d.where, xs);
                })
                .attr("y2", height);
            dataSelection.enter().append("line")
                .attr("class", function (d) {
                    return "marker " + d.style;
                })
                .attr("style", "vector-effect: non-scaling-stroke")
                .attr("x1", function (d) {
                    return xP(d.where, xs);
                })
                .attr("x2", function (d) {
                    return xP(d.where, xs);
                })
                .attr("y2", height);
            dataSelection.exit().remove();
        };

        var mouseMoveOverTask = function (bar, d) {
            var d3elem = d3.select(bar);
            var mousePos = d3.mouse(bar);
            var isSelected = selectedTasks.indexOf(bar) >= 0;
            if (isSelected && mousePos[0] < d.dimension.width * stretchWidth && d.entity.allowedActions.indexOf(d3.planning.TaskAction.LEFT) >= 0) {
                d3elem.style("cursor", "w-resize");
            } else if (isSelected && mousePos[0] > d.dimension.width * (1 - stretchWidth) && d.entity.allowedActions.indexOf(d3.planning.TaskAction.RIGHT) >= 0) {
                d3elem.style("cursor", "e-resize");
            } else if (isSelected && d.entity.allowedActions.indexOf(d3.planning.TaskAction.MOVE) >= 0){
                d3elem.style("cursor", "move");
            } else {
                d3elem.style("cursor", "auto");
            }
        };

        var selectTask = function (task, d) {
            var indexOfTask = selectedTasks.indexOf(task);
            if (indexOfTask >= 0) {
                d3.select(task)
                    .classed(d.entity.style, true).classed(selectColor, false)
                    .selectAll(".mark-rect").classed(selectColor, false);
                selectedTasks.splice(indexOfTask, 1);
            } else {
                selectedTasks.push(task);
                d3.select(task)
                    .classed(d.entity.style, false).classed(selectColor, true)
                    .selectAll(".mark-rect").classed(selectColor, true)
            }
        };

        var taskDragStarted = function (bar, d) {
            var d3elem = d3.select(bar);
            var mousePos = d3.mouse(bar);
            var isSelected = selectedTasks.indexOf(bar) >= 0;
            var taskAction;
            if (isSelected && mousePos[0] < d.dimension.width * stretchWidth && d.entity.allowedActions.indexOf(d3.planning.TaskAction.LEFT) >= 0) {
                d3elem.style("cursor", "w-resize");
                taskAction = d3.planning.TaskAction.LEFT;
            } else if (isSelected && mousePos[0] > d.dimension.width * (1 - stretchWidth) && d.entity.allowedActions.indexOf(d3.planning.TaskAction.RIGHT) >= 0) {
                d3elem.style("cursor", "e-resize");
                taskAction = d3.planning.TaskAction.RIGHT;
            } else if (isSelected && d.entity.allowedActions.indexOf(d3.planning.TaskAction.MOVE) >= 0) {
                d3elem.style("cursor", "move");
                taskAction = d3.planning.TaskAction.MOVE;
            }
            currentDragEvent = taskAction && {pos: d3.mouse(bar.parentNode), action: taskAction};
        };

        var taskDragged = function (bar, d, delta, xs) {
            switch (currentDragEvent.action) {
                case(d3.planning.TaskAction.LEFT) : handleStretchTaskLeftSide(bar, d, xs, delta); break;
                case(d3.planning.TaskAction.MOVE) : handleMoveTask(bar, d, xs, delta); break;
                case(d3.planning.TaskAction.RIGHT) : handleStretchTaskRightSide(bar, d, xs, delta); break;
            }
        };

        var updateBar = function(bar, d, xs) {
            var barElem = d3.select(bar);
            d.pos.x = xP(d.entity.from, xs);
            d.dimension.width = xP(d.entity.to, xs) - xP(d.entity.from, xs);
            if (+d.entity.completedMark) {
                d.midPos = Math.min(xP(now, xs), d.pos.x + d.dimension.width) - d.pos.x;
            }
            barElem.attr("transform", "translate(" + d.pos.x +", " + d.pos.y + ")");
            barElem.select(".uncompleted-part").attr("width", d.dimension.width);
            barElem.select(".task-divider").style("visibility", d.midPos > 0 && d.midPos < d.dimension.width)
                .attr("x1", d.midPos).attr("x2", d.midPos);
            barElem.select(".completed-part").style("visibility", d.midPos > 0).attr("width", d.midPos);
            barElem.select(".completed-mark").call(function(selection) {
               var translateMatrix = selection.node().transform.baseVal.getItem(0).matrix;
                translateMatrix.e = d.midPos / 2;
            });
            barElem.select(".uncompleted-mark").call(function(selection) {
                var translateMatrix = selection.node().transform.baseVal.getItem(0).matrix;
                translateMatrix.e = (d.midPos + d.dimension.width) / 2;
            });
        };

        var activateTask = function(task, activate) {
            d3.select(task).classed("activated-task", activate);
        };

        var handleStretchTaskLeftSide = function (bar, d, xs, delta) {
            if (delta < 0) {
                delta = d.entity.from < now ? 0 : (ixP(d.pos.x + delta, xs) < now ? xP(now, xs) - xP(d.entity.from, xs) : delta);
            } else {
                delta = d.entity.to - ixP(d.pos.x + delta, xs) < minWidth ? xP(d.entity.to.getTime() - minWidth, xs) - xP(d.entity.from, xs) : delta;
            }
            if(delta) {
                d.entity.from = ixP(d.pos.x + delta, xs);
                updateBar(bar, d, xs);
                activateTask(bar, true);
            }
        };

        var handleMoveTask = function (bar, d, xs, delta) {
            var deltaLeft = delta, deltaRight = delta;
            if (delta < 0) {
                deltaLeft = ixP(d.pos.x, xs) < now ? 0 : deltaLeft;
                deltaLeft = deltaLeft && ixP(d.pos.x + deltaLeft, xs) < now ? xP(now, xs) - xP(d.entity.from, xs) : deltaLeft;
                deltaRight = ixP(d.pos.x + d.dimension.width + deltaRight, xs) - ixP(d.pos.x + deltaLeft, xs) < minWidth ?
                    xP(minWidth + ixP(d.pos.x + deltaLeft, xs).getTime() ,xs) - xP(d.entity.to , xs) : deltaRight;
                deltaRight = d.entity.to >= now && ixP(d.pos.x + d.dimension.width + deltaRight, xs) <= now ?
                    xP(now, xs) - (d.pos.x + d.dimension.width) : deltaRight;
            }
            if(deltaLeft || deltaRight) {
                d.entity.from = ixP(d.pos.x + deltaLeft, xs);
                d.entity.to = ixP(d.pos.x + d.dimension.width + deltaRight, xs);
                updateBar(bar, d, xs);
                activateTask(bar, true);
            }
        };

        var handleStretchTaskRightSide = function (bar, d, xs, delta) {
            if (delta < 0) {
                delta = ixP(d.pos.x + d.dimension.width + delta, xs) - d.entity.from < minWidth ?
                    xP(d.entity.from.getTime() + minWidth, xs) - (d.pos.x + d.dimension.width) : delta;
                delta = d.entity.to >= now && ixP(d.pos.x + d.dimension.width + delta, xs) <= now ?
                    xP(now, xs) - (d.pos.x + d.dimension.width) : delta;
            }
            if(delta) {
                d.entity.to = ixP(d.pos.x + d.dimension.width + delta, xs);
                updateBar(bar, d, xs);
                activateTask(bar, true);
            }
        };

        var xP = function(x, xs) {
            var point = svg.node().ownerSVGElement.createSVGPoint();
            point.x = xs(x);
            return point.matrixTransform(graphContainer.node().parentNode.getTransformToElement(graphContainer.node())).x;
        };

        var ixP = function(x, xs) {
            var point = svg.node().ownerSVGElement.createSVGPoint();
            point.x = x;
            return xs.invert(point.matrixTransform(graphContainer.node().getTransformToElement(graphContainer.node().parentNode)).x);
        };

        var yP = function(y, ys) {
            var point = svg.node().ownerSVGElement.createSVGPoint();
            point.y = ys(y);
            return point.matrixTransform(graphContainer.node().parentNode.getTransformToElement(graphContainer.node())).y;
        };

        var iyP = function(y, ys) {
            var point = svg.node().ownerSVGElement.createSVGPoint();
            point.y = y;
            return ys.invert(point.matrixTransform(graphContainer.node().getTransformToElement(graphContainer.node().parentNode)).y);
        };

        var taskDragEnded = function (bar, d) {
            currentDragEvent = undefined;
        };

        var drawBars = function (xs, ys) {
            //calculate the completion percentage.
            tskStatuses.forEach(calculateTasks(xs, ys));
            var updateSelection = graphContainer.selectAll(".planning-task").data(tskStatuses.reverse());
            var insertSelection = updateSelection.enter();
            var removeSelection = updateSelection.exit();

            //update tasks
            updateSelection.each(updateTasks(xs, ys));
            //insert new tasks
            insertSelection.call(insertNewTasks, xs);
            //removed unnedded tasks
            removeSelection.remove();
        };

        var calculateTasks = function (xs, ys) {
            return function (tsk) {
                tsk.pos = {x: xP(tsk.entity.from, xs), y: yP(tsk.entity.task, ys) + ys.rangeBand() / 2 * (1 - barWidth)};
                tsk.dimension = {width: xP(tsk.entity.to, xs) - xP(tsk.entity.from, xs), height: ys.rangeBand() * barWidth};
                tsk.midPos = xP(new Date(tsk.entity.from.getTime() + tsk.entity.completedPos), xs) - xP(tsk.entity.from, xs);
                if (tsk.midPos > tsk.dimension.width) {
                    throw {message: "The completion duration can not last longer then the job itself"};
                }
            };
        };

        var configureDragEvent = function(xs) {
            var timeout;
            return d3.behavior.drag()
                .on("dragstart", function (d) {
                    var bar = this;
                    switch (d3.event.sourceEvent.type) {
                        case ("mousedown"):
                            if (d3.event.sourceEvent.button == 0 && d3.event.sourceEvent.ctrlKey) {
                                selectTask(bar, d);
                            }
                            taskDragStarted(bar, d);
                            break;
                        case ("touchstart") :
                            taskDragStarted(bar, d);
                            timeout = setTimeout(function () {
                                selectTask(bar, d);
                                taskDragStarted(bar, d);
                            }, 700);
                            break;
                    }
                    d3.event.sourceEvent.stopPropagation();
                }).on("drag", function (d, i) {
                    if(d3.event.sourceEvent.type === "touchmove") {
                        clearTimeout(timeout);
                    }
                    if(currentDragEvent) {
                        var mousePos = d3.mouse(this.parentNode);
                        var delta = mousePos[0] - currentDragEvent.pos[0];
                        currentDragEvent.pos = mousePos;
                        selectedTasks.forEach(function (elem) {
                            var elemData = d3.select(elem).datum();
                            if (elemData.entity.allowedActions.indexOf(currentDragEvent.action) >= 0) {
                                taskDragged(elem, elemData, delta, xs);
                            }
                        });
                    }
                    d3.event.sourceEvent.preventDefault();
                }).on("dragend", function (d, i) {
                    if(d3.event.sourceEvent.type === "touchend") {
                        clearTimeout(timeout);
                    }
                    taskDragEnded(this, d);
                });
        };

        var insertNewTasks = function (selection, xs) {
            var taskGroups = selection.append("g")
                .attr("class", function (d) {
                    return "planning-task " + d.entity.style;
                })
                .attr("transform", function (d) {
                    return "translate(" + d.pos.x + ", " + d.pos.y + ")";
                })
                .on("mousemove", function (d) {
                    mouseMoveOverTask(this, d);
                })
                .call(configureDragEvent(xs));
            taskGroups.append("rect").style("vector-effect", "non-scaling-stroke")
                .attr("class", "uncompleted-part")
                .attr("width", function (d) {
                    return d.dimension.width;
                })
                .attr("height", function (d) {
                    return d.dimension.height;
                });
            taskGroups.append("line").attr("class", "task-divider")
                .style({
                    "visibility": function (d) {
                        return d.midPos < d.dimension.width && d.midPos > 0 ? "visible" : "hidden";
                    },
                    "vector-effect": "non-scaling-stroke"
                })
                .attr("x1", function (d) {
                    return d.midPos;
                })
                .attr("x2", function (d) {
                    return d.midPos;
                })
                .attr("y2", function (d) {
                    return d.dimension.height;
                });
            taskGroups.append("rect")
                .style({
                    "visibility": function (d) {
                        return d.midPos > 0 ? "visible" : "hidden";
                    },
                    "stroke": "none"
                })
                .attr("class", function (d) {
                    return "completed-part " + (isOverCompleted(d.entity) ? overCompletedClass : completedClass);
                })
                .attr("width", function (d) {
                    return d.midPos;
                })
                .attr("height", function (d) {
                    return d.dimension.height
                });
            taskGroups.append("g")
                .attr("class", "completed-mark")
                .attr("transform", function(d) {
                    return "translate(" + d.midPos / 2 + ", -10)";
                })
                .style("visibility", function (d) {
                    return +d.entity.completedMark != 0 ? "visible" : "hidden";
                }).each(function(d) {
                    var thisElem = d3.select(this);
                    var rect = thisElem.append("rect").attr("class", "mark-rect");
                    var text = thisElem.append("text").attr("class", "mark-text")
                        .style("text-anchor", "middle").text(d.entity.completedMark);
                    var textBox = text.node().getBBox();
                    rect.attr("x", textBox.x - 5).attr("y", textBox.y - 1).attr("width", textBox.width + 10).attr("height", textBox.height + 2);
                }).each(setUnscalable);
            taskGroups.append("g")
                .attr("class", "uncompleted-mark")
                .attr("transform", function(d) {
                    return "translate(" + (d.midPos + d.dimension.width) / 2 + ", -10)";
                })
                .style("visibility", function (d) {
                    return +d.entity.uncompletedMark != 0 ? "visible" : "hidden";
                }).each(function(d) {
                    var thisElem = d3.select(this);
                    var rect = thisElem.append("rect").attr("class", "mark-rect");
                    var text = thisElem.append("text").attr("class", "mark-text")
                        .style("text-anchor", "middle").text(d.entity.uncompletedMark);
                    var textBox = text.node().getBBox();
                    rect.attr("x", textBox.x - 5).attr("y", textBox.y - 1).attr("width", textBox.width + 10).attr("height", textBox.height + 2);
                }).each(setUnscalable);
        };

        var setUnscalable = function () {
            elemToUnscale.push(this);
        };

        var updateTasks = function (xs, ys) {
//            return function(d, i) {
//                var bar = d3.select(this)
//                    .attr("class", "planning-task " + d.style)
//                    .attr("style", "vector-effect: non-scaling-stroke")
//                    .attr("transform", "translate(" + xs(d.from) + ", " + (ys(d.task) + ys.rangeBand() / 2 * (1 - d.width)) + ")");
//                bar.select(".task-whole")
//                    .attr("width", xs(d.to) - xs(d.from))
//                    .attr("height", ys.rangeBand() * d.width);
            // bar.select(".task-completed")
            //     .style("visibility", d.completedPercentage > 0 ? "visible" : "hidden")
            //     .attr("width", (xs(d.to) - xs(d.from)) * d.completedPercentage)
            //     .attr("height", ys.rangeBand() * d.width);
            // bar.select(".task-uncompleted")
            //     .style("visibility", d.completedPercentage < 1 ? "visible" : "hidden")
            //     .attr("x", (xs(d.to) - xs(d.from)) * d.completedPercentage)
            //     .attr("width", xs(d.to) - (xs(d.to) - xs(d.from))*d.completedPercentage)
            //     .attr("height", ys.rangeBand() * d.width);
//            };
        };

        var zoomed = function () {
            svg.select(".x.axis").call(x);
            svg.select(".x.grid").call(xGrid);
            graphContainer.attr("transform", "translate(" + d3.event.translate[0] + ", 0)scale(" + d3.event.scale + ", 1)");
            elemToUnscale.forEach(unscale);
        };

        // Counteract all transforms applied above an element.
        // Apply a translation to the element to have it remain at a local position
        var unscale = function (el) {
            var xf = el.scaleIndependentXForm;
            if (!xf) {
                // Keep a single transform matrix in the stack for fighting transformations
                // Be sure to apply this transform after existing transforms (translate)
                xf = el.scaleIndependentXForm = svg.node().ownerSVGElement.createSVGTransform();
                el.transform.baseVal.appendItem(xf);
            }
            var m = svg.node().getTransformToElement(el.parentNode);
            m.e = m.f = 0; // Ignore (preserve) any translations done up to this point
            xf.setMatrix(m);
        };

        var wrapTickText = function (text) {
            text.each(function () {
                var text = d3.select(this),
                    words = text.text().split('<br>'),
                    lineNumber = 0,
                    lineHeight = 1.1//em;
                yp = text.attr("y"),
                    dy = parseFloat(text.attr("dy")),
                    text.text(null);
                words.forEach(function (word) {
                    text.append("tspan").attr("x", 0).attr("y", yp).attr("dy", lineNumber * lineHeight + dy + "em").text(word);
                    lineNumber += 1;
                });
            });
        };

        var createClipPath = function (definitions) {
            definitions.append("clipPath").attr("id", "chart-area")
                .append("rect").attr("width", width).attr("height", height);
        };

        //TODO Implement special ordinal scale see http://stackoverflow.com/questions/20758373/d3-js-inversion-with-ordinal-scale
        //TODO and this http://stackoverflow.com/questions/13342149/pan-zoom-ordinal-var
        //TODO implement your own time interval in order to support evenly distributed dates on OX axis see https://github.com/mbostock/d3/issues/1593
        var planning = function (selection) {
            //Creating coordinate system.
            var xs = xScale();
            var ys = yScale();
            //Creating zoom&pan behaviour.
            var zoom = d3.behavior.zoom()
                .x(xs)
                .scaleExtent([1, 10])
                .on("zoom", zoomed);
            //SVG container.
            svg = selection.append("svg")
                .attr("shape-rendering", "crispEdges")
                .attr("class", "planning-chart")
                .attr("width", margin.left + width + margin.right)
                .attr("height", margin.top + height + margin.bottom);
            //Creating filters and other definitions.
            var definitions = svg.append('defs');
            createClipPath(definitions);
            svg = svg.append("g")
                .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
                .call(zoom)
                .on("dblclick.zoom", null);

            //Drawing chart's background and grid.
            drawBackground();
            drawGrid(xs, ys);
            
            graphContainer = svg.append("g").attr("clip-path", "url(#chart-area)").append("g").attr("class", "chart");
            //Drawing the chart itself
            drawBars(xs, ys);
            drawMarkers(xs, ys);
            //Draw axis
            drawAxis(xs, ys);
        };

        planning.setMarkers = function (mrks) {
            if (!arguments.length) {
                return markers;
            }
            markers.splice(1, markers.length - 1);
            mrks.forEach(function (elem) {
                markers.push(elem);
            });
            return planning;
        };

        planning.taskTypes = function (types) {
            if (!arguments.length) {
                return tskTypes;
            }
            tskTypes = [];
            types.forEach(function (elem) {
                tskTypes.push(elem);
            });
            return planning;
        };

        planning.taskStatuses = function (statuses) {
            if (!arguments.length) {
                return tskStatuses;
            }
            tskStatuses = [];
            statuses.forEach(function (elem) {
                tskStatuses.push({entity: elem});
            });
            return planning;
        };

        planning.width = function (w) {
            if (!arguments.length) {
                return width;
            }
            width = +w;
            return planning;
        };

        planning.height = function (h) {
            if (!arguments.length) {
                return height;
            }
            height = +h;
            return planning;
        };

        planning.margin = function (m) {
            if (!arguments.length) {
                return margin;
            }
            margin.top = m.hasOwnProperty('top') ? m.top : margin.top;
            margin.right = m.hasOwnProperty('right') ? m.right : margin.right;
            margin.bottom = m.hasOwnProperty('bottom') ? m.bottom : margin.bottom;
            margin.left = m.hasOwnProperty('left') ? m.left : margin.left;
            return planning;
        };

        return planning;
    };

    //Available task action.
    d3.planning.TaskAction = {
        MOVE: "MOVE",
        LEFT: "LEFT",
        RIGHT: "RIGHT"
    };
})();
(function () {

    var dayDuration  = 24 * 60 * 60 * 1000;
    var weekDuration = 7 * dayDuration;

    var BarList = function(workUnit) {
        this.workUnit = workUnit;
        this.inlineBars = [];
        this.horizontalBars = [];
    };

    BarList.prototype.contains = function(bar) {
        var index;
        for(index = 0; index < this.inlineBars.length; index += 1) {
            if (this.inlineBars[index].entity == bar) {
                return this.inlineBars[index];
            }
        }
        for(index = 0; index < this.horizontalBars.length; index += 1) {
            if (this.horizontalBars[index].entity == bar) {
                return this.horizontalBars[index];
            }
        }
        return null;
    };

    BarList.prototype.intersectsWithOthers = function(tick) {
        if(this.inlineBars.length == 0) {
            return null;
        }
        return binarySearch(tickPredicate(tick), this.inlineBars, 0, this.inlineBars.length - 1);
    };

    BarList.prototype.insert = function(bar) {
        var startTick = this.workUnit.weeks().findClosestTickWithOuterBounds(bar.start),
            endTick = tickPlusN(startTick, hoursToTickNumber(bar.duration) - 1),
            barToInsert = new Bar(bar, this, startTick, endTick);
        try {
            return this.insertInlineBar(barToInsert);
        } catch (ex) {
            this.horizontalBars.push(barToInsert);
            return barToInsert;
        }
    };

    BarList.prototype.insertOutsideBar = function(bar) {
        var startTick = this.workUnit.weeks().findClosestTickWithOuterBounds(bar.start),
            endTick = tickPlusN(startTick, hoursToTickNumber(bar.duration) - 1),
            barToInsert = new Bar(bar, this, startTick, endTick);
        this.horizontalBars.push(barToInsert);
        return barToInsert;
    };

    BarList.prototype.insertInlineBar = function(bar) {
        var currentTick = bar.startTick,
            intersectBar =  this.intersectsWithOthers(currentTick),
            prevBar, nextBar,
            tickNumber = howManyTicksBetween(bar.startTick, bar.endTick),
            indexToInsert;
        if(intersectBar) {
            currentTick = findClosestTickAfter(intersectBar);
        }
        if(!currentTick) {
            throw {msg : "There is no where to place this bar with start at :" + bar.startTick + " and duration: " + tickNumber * 15 / 60 + " hours."};
        }
        bar.startTick = currentTick;
        intersectBar = null;
        while(tickNumber > 0 && !intersectBar && currentTick.next) {
            intersectBar = this.intersectsWithOthers(currentTick.next);
            if (!intersectBar) {
                currentTick = currentTick.next;
                tickNumber -= 1;
            }
        }
        if (intersectBar) {
            intersectBar.push(tickNumber);
            currentTick = intersectBar.startTick.prev;
        }
        bar.endTick = currentTick;
        indexToInsert = this.findPlaceToInsert(bar);
        this.inlineBars.splice(indexToInsert, 0, bar);
        if (indexToInsert > 0) {
            prevBar = this.inlineBars[indexToInsert - 1];
        }
        if (indexToInsert < this.inlineBars.length - 1) {
            nextBar = this.inlineBars[indexToInsert + 1];
        }
        if (nextBar) {
            bar.next = nextBar;
            nextBar.prev = bar;
        }
        if (prevBar) {
            bar.prev = prevBar;
            prevBar.next = bar;
        }
        return bar;
    };

    BarList.prototype.findPlaceToInsert = function(bar) {
        var index = 0;
        while (index < this.inlineBars.length && this.inlineBars[index].endTick.time < bar.startTick.time) {
            index += 1;
        }
        return index;
    };

    var tickPredicate = function(tick) {
        return function(testBar) {
            return testBar.testSide(tick);
        };
    };

    var binarySearch = function(predicate, bars, startIndex, endIndex) {
        var midIndex, predRes;
        if (startIndex == endIndex) {
            return predicate(bars[startIndex]) == 0 && bars[startIndex];
        } else {
            midIndex = Math.floor((endIndex + startIndex) / 2);
            predRes = predicate(bars[midIndex]);
            if(predRes < 0) {
                return binarySearch(predicate, bars, midIndex + 1, endIndex);
            } else if (predRes > 0) {
                return midIndex == startIndex ? null : binarySearch(predicate, bars, startIndex, midIndex - 1);
            } else {
                return bars[midIndex];
            }
        }
    };

    var findClosestTickAfter = function(bar) {
        var nextTick = bar.endTick.next;
        var nextBar = bar.next;
        while (nextBar && nextTick == nextBar.startTick) {
            nextTick = nextBar.endTick.next;
            nextBar = nextBar.next;
        }
        return nextTick;
    };

    var nextTick = function(tick, sign) {
        if  (sign > 0) {
            return tick.next;
        } else if (sign < 0) {
            return tick.prev;
        } else {
            return tick;
        }
    };

    var tickPlusN = function(startTick, n) {
        var increment = Math.floor(n) < 0 ? 1 : -1,
            currentTick = startTick;
        while (n != 0 && nextTick(currentTick, n)) {
            currentTick = nextTick(currentTick, n);
            n += increment;
        }
        return currentTick;
    };

    var howManyTicksBetween = function(startTick, endTick) {
        var sign = startTick.time < endTick.time ? 1 : (startTick.time > endTick.time ? -1 : 0),
            tickCount = 0,
            currentTick = startTick;
        while (currentTick != endTick) {
            currentTick = nextTick(currentTick, sign);
            tickCount += sign;
        }
        return tickCount;
    };

    var Bar = function(entity, barList, startTick, endTick) {
        this.entity = entity;
        this.barList = barList;
        this.startTick = startTick;
        this.endTick = endTick;
        this.redrawCallback = function(bar){};
    };

    Bar.prototype.joinWith = function(job) {
        var inlineIndex = this.barList.inlineBars.indexOf(job);
        if (job.next) {
            this.next = job.next;
            job.next.prev = this;
            job.next = null;
            job.prev = null;
        }
        this.endTick = job.endTick;
        this.barList.inlineBars.splice(inlineIndex, 1);
        this.redrawCallback(this);
    };

    Bar.prototype.splitJobAt = function(date) {
        var inlineBarIndex = this.barList.inlineBars.indexOf(this),
            horizontalBarIndex = this.barList.horizontalBars.indexOf(this),
            splitTick = this.barList.workUnit.weeks().findClosestTickWithOuterBounds(date),
            ticksBetween = howManyTicksBetween(this.startTick, splitTick),
            newBar;
        if (this.entity.activatable) {
            if (ticksBetween > 0) {
                newBar = new Bar(JSON.parse(JSON.stringify(this.entity)), this.barList, splitTick, this.endTick);
                this.endTick = splitTick.prev;
            } else if (splitTick != this.endTick) {
                newBar = new Bar(JSON.parse(JSON.stringify(this.entity)), this.barList, splitTick.next, this.endTick);
                this.endTick = splitTick;
            }
            if (newBar) {
                if (inlineBarIndex >= 0) {
                    this.barList.inlineBars.splice(inlineBarIndex + 1, 0, newBar);
                    if (this.next) {
                        newBar.next = this.next;
                        this.next.prev = newBar;
                    }
                    newBar.prev = this;
                    this.next = newBar;
                } else if (horizontalBarIndex >= 0) {
                    this.barList.horizontalBars.push(newBar);
                }
            }
            this.redrawCallback(this);
        }
        return newBar;
    };

    Bar.prototype.moveToOutline = function() {
        var inlineBarIndex = this.barList.inlineBars.indexOf(this);
        if (inlineBarIndex >= 0) {
            this.barList.inlineBars.splice(inlineBarIndex, 1);
            this.barList.horizontalBars.push(this);
            if (this.prev) {
                this.prev.next = this.next;
            }
            if (this.next) {
                this.next.prev = this.prev;
            }
            this.next = null;
            this.prev = null;
        }
    };

    Bar.prototype.moveToInline = function() {
        var horizontalBarIndex = this.barList.horizontalBars.indexOf(this);
        if (horizontalBarIndex >= 0) {
            this.barList.insertInlineBar(this);
            this.barList.horizontalBars.splice(horizontalBarIndex, 1);
        }
    };

    Bar.prototype.moveToBarList = function(newBarList, inline) {
        var jobDuration = howManyTicksBetween(this.startTick, this.endTick),
            horizontalBarIndex = this.barList.horizontalBars.indexOf(this),
            inlineBarIndex = this.barList.inlineBars.indexOf(this);
        this.startTick = newBarList.workUnit.weeks().findClosestTickWithOuterBounds(this.startTick.time);
        this.endTick = tickPlusN(this.startTick, jobDuration);
        if (horizontalBarIndex >= 0) {
            this.barList.horizontalBars.splice(horizontalBarIndex, 1);
        } else if (inlineBarIndex >= 0) {
            this.barList.inlineBars.splice(inlineBarIndex, 1);
            if (this.prev) {
                this.prev.next = this.next;
            }
            if (this.next) {
                this.next.prev = this.prev;
            }
            this.next = null;
            this.prev = null;
        }
        this.barList = newBarList;
        if (inline) {
            newBarList.insertInlineBar(this);
        } else {
            newBarList.horizontalBars.push(this);
        }
    };

    Bar.prototype.testSide = function(tick) {
        if (!tick) {
            throw {msg : "The tick must not be null or undefined!"}
        } else if (this.startTick.time > tick.time) {
            return 1;
        } else if (this.endTick.time < tick.time) {
            return -1;
        } else {
            return 0;
        }
    };

    Bar.prototype.stretchLeft = function(tickNumber) {
        var canStretchToTick = function(bar ,tick, sign) {
            return bar.entity.activatable &&
                bar.entity.strechability.indexOf(d3.scheduling.TaskAction.LEFT) >= 0 && !(sign < 0 && tick.isBeforeNow || sign > 0 && tick.time > bar.endTick.time);
            },
            increment = tickNumber < 0 ? -1 : (tickNumber > 0 ? 1 : 0),
            howManyManagedToStretch = 0,
            currentTick = this.startTick,
            nextTickValue = nextTick(currentTick, increment),
            prevBarValue = increment < 0 ? this.prev : null,
            prevBarToPush;
        while (howManyManagedToStretch != tickNumber && nextTickValue && canStretchToTick(this, nextTickValue, increment) && !prevBarToPush) {
            if (prevBarValue && prevBarValue.endTick == nextTickValue) {
                prevBarToPush = prevBarValue;
            } else {
                currentTick = nextTickValue;
                nextTickValue = nextTick(nextTickValue, increment);
                howManyManagedToStretch += increment;
            }
        }
        if(howManyManagedToStretch != tickNumber && prevBarToPush) {
            howManyManagedToStretch += prevBarToPush.push(tickNumber - howManyManagedToStretch);
        }
        this.startTick = tickPlusN(this.startTick, howManyManagedToStretch);
        if (typeof this.redrawCallback == 'function' && howManyManagedToStretch) {
            this.redrawCallback(this);
        }
        return howManyManagedToStretch;
    };

    Bar.prototype.stretchRight = function(tickNumber) {
        var canStretchToTick = function(bar ,tick, sign) {
                return bar.entity.activatable &&
                    bar.entity.strechability.indexOf(d3.scheduling.TaskAction.RIGHT) &&
                    !(sign < 0 && (tick.time < bar.startTick.time || (tick.isBeforeNow && !(tick.next && tick.next.isNow))));
            },
            increment = tickNumber < 0 ? -1 : (tickNumber > 0 ? 1 : 0),
            howManyManagedToStretch = 0,
            currentTick = this.endTick,
            nextTickValue = nextTick(currentTick, increment),
            nextBarValue = increment > 0 ? this.next : null,
            nextBarToPush;
        while (howManyManagedToStretch != tickNumber && nextTickValue && canStretchToTick(this, nextTickValue, increment) && !nextBarToPush) {
            if (nextBarValue && nextBarValue.startTick == nextTickValue) {
                nextBarToPush = nextBarValue;
            } else {
                currentTick = nextTickValue;
                nextTickValue = nextTick(nextTickValue, increment);
                howManyManagedToStretch += increment;
            }
        }
        if(howManyManagedToStretch != tickNumber && nextBarToPush) {
            howManyManagedToStretch += nextBarToPush.push(tickNumber - howManyManagedToStretch);
        }
        this.endTick = tickPlusN(this.endTick, howManyManagedToStretch);
        if (typeof this.redrawCallback == 'function' && howManyManagedToStretch) {
            this.redrawCallback(this);
        }
        return howManyManagedToStretch;
    };

    Bar.prototype.push = function(tickNumber) {
        var barEndTick = function(bar, sign) {
                return bar && (sign < 0 ? bar.startTick : (sign > 0 ? bar.endTick : null));
            },
            canPushToTick = function(bar, tick, sign) {
                return bar.entity.activatable &&
                    bar.entity.strechability.indexOf(d3.scheduling.TaskAction.MOVE) >= 0 && !(sign < 0 && tick.isBeforeNow);
            },
            increment = tickNumber < 0 ? -1 : (tickNumber > 0 ? 1 : 0),
            howManyManagedToPush = 0,
            currentTick = barEndTick(this, increment),
            nextTickValue = nextTick(currentTick, increment),
            nextBarValue = increment < 0 ? this.prev : (increment> 0 ? this.next : this),
            nextBarToPush;
        while (tickNumber != howManyManagedToPush && nextTickValue && canPushToTick(this, nextTickValue, increment) && !nextBarToPush) {
            if (nextBarValue && barEndTick(nextBarValue, -1 * increment) == nextTickValue) {
                nextBarToPush = nextBarValue;
            } else {
                currentTick = nextTickValue;
                nextTickValue = nextTick(nextTickValue, increment);
                howManyManagedToPush += increment;
            }
        }
        if(howManyManagedToPush != tickNumber && nextBarToPush) {
            howManyManagedToPush += nextBarToPush.push(tickNumber - howManyManagedToPush);
        }
        this.startTick = tickPlusN(this.startTick, howManyManagedToPush);
        this.endTick = tickPlusN(this.endTick, howManyManagedToPush);
        if (typeof this.redrawCallback == 'function') {
            this.redrawCallback(this);
        }
        return howManyManagedToPush;
    };

    var createWeeks = function(firstWeekStart, productiveHoursForEachWeek) {
        var weeks = [],
            prevWeek,
            weekStart = firstWeekStart,
            weekEnd = new Date(weekStart.getTime() + weekDuration);

        productiveHoursForEachWeek.forEach(function(elem) {
            prevWeek = createWeek(weekStart, weekEnd, prevWeek, elem);
            weeks.push(prevWeek);
            weekStart = weekEnd;
            weekEnd = new Date(weekStart.getTime() + weekDuration)
        });

        return {
            first : weeks[0],
            last : weeks[weeks.length - 1],
            size : weeks.length,
            firstTick : weeks[0].firstTick,
            lastTick : weeks[weeks.length - 1].lastTick,
            firstWeekStart : weeks[0].weekStart,
            lastWeekEnd : weeks[weeks.length - 1].weekEnd,
            week : function(index) { return weeks[index]; },
            contains : function(testDate) {
                return this.firstWeekStart <= testDate && this.lastWeekEnd > testDate;
            },
            findClosestTick : function(to) {
                var weekIndex;
                for(weekIndex = 0; weekIndex < weeks.length; weekIndex += 1) {
                    if (weeks[weekIndex].contains(to)) {
                        return weeks[weekIndex].findClosestTick(to);
                    }
                }
                return null;
            },
            findClosestTickWithOuterBounds : function(to) {
                if (to < firstWeekStart) {
                   return weeks[0].firstTick;
                } else if (to >= weekStart) {
                    return weeks[weeks.length - 1].lastTick;
                } else {
                    return this.findClosestTick(to);
                }
            }
        };
    };

    var createWeek = function(weekStart, weekEnd, prevWeek, productiveHoursForWeek) {
        var days = [],
            prevDay = prevWeek && prevWeek.lastDay,
            dayIndex,
            thisWeek;

        for(dayIndex = 0; dayIndex < 7; dayIndex += 1) {
            prevDay = createDay(new Date(weekStart.getTime() + dayIndex * dayDuration), prevDay, productiveHoursForWeek);
            days.push(prevDay);
        }

        thisWeek = {
            firstDay : days[0],
            lastDay : days[days.length - 1],
            size : days.length,
            weekStart : weekStart,
            weekEnd : weekEnd,
            firstTick : days[0].firstTick,
            lastTick : days[days.length - 1].lastTick,
            day : function(index) { return days[index]; },
            contains : function(testDate) {
                return weekStart <= testDate && weekEnd > testDate;
            },
            findClosestTick : function(to) {
                for(dayIndex = 0; dayIndex < days.length; dayIndex += 1){
                    if (days[dayIndex].contains(to)) {
                        return days[dayIndex].findClosestTick(to);
                    }
                }
                return null;
            }
        };
        if (prevWeek) {
            prevWeek.next = thisWeek;
            thisWeek.prev = prevWeek;
        }
        return thisWeek;
    };

    var createDay = function(date, prevDay, productiveHoursForDay) {
        var ticks = [],
            prevTick = prevDay && prevDay.lastTick,
            tickNumber = hoursToTickNumber(productiveHoursForDay),
            tickDuration = dayDuration / tickNumber,
            tickIndex,
            thisDay;

        for(tickIndex = 0; tickIndex < tickNumber; tickIndex += 1) {
            prevTick = createTick(new Date(date.getTime() + tickDuration * tickIndex), tickDuration, prevTick);
            ticks.push(prevTick);
        }
        thisDay = {
            date : date,
            firstTick : ticks[0],
            lastTick : ticks[ticks.length - 1],
            size : ticks.length,
            tick : function(index) {return ticks[index]; },
            contains : function(testDate) {
                return testDate >= date && testDate < new Date(date.getTime() + dayDuration);
            },
            findClosestTick : function(to) {
                for(tickIndex = 0; tickIndex < ticks.length; tickIndex += 1) {
                    if (ticks[tickIndex].time <= to
                        && to < ((ticks[tickIndex].next && ticks[tickIndex].next.time) || new Date(date.getTime() + dayDuration))) {
                        return ticks[tickIndex];
                    }
                }
                return null;
            }
        };
        if (prevDay) {
            prevDay.next = thisDay;
            thisDay.prev = prevDay;
        }

        return thisDay;
    };

    var createTick = function(tickDate, duration, prevTick) {
        var thisTick = {
            time : tickDate,
            duration : duration
        };

        if (prevTick) {
            prevTick.next = thisTick;
            thisTick.prev = prevTick;
        }
        return thisTick;
    };

    var hoursToTickNumber = function(hours) {
        return Math.ceil(hours * 60 / 15);
    };

    d3.scheduling = function (stretchWidth, now, selectColor, workUnits, holidays) {
        //Most common configuration properties.
        var width = 640,
            height = 480,
            margin = {
                top: 35,
                right: 35,
                bottom: 35,
                left: 35
            },
            barWidth = 0.2,
            selectedJobs = [],
            activeJob,
            markVisible = true,
            markVisibleForSelected = false,
            //Needed for tracking drag actions.
            currentDragEvent,
            //The canvas to draw chart on.
            svg,
            //The x and y axises accordingly.
            x, y,
            //The x and y axis grids accordingly.
            xGrid, yGrid,
            //The containers for chart's graphic elements.
            graphContainer,
            //The collection of elements that shouldn't be scaled.
            unscalableElems = [],
            //The job container.
            jobs = {},
            //The job parts.
            jobParts = [],
            //Work unit map implemented in order to have effective search by name
            wUBarList = function() {
                var _ = {};
                workUnits.forEach(function(elem) {
                    _[elem.name()] = new BarList(elem);
                });
                return _;
            }(),
            //The left and right time bounds
            timeBounds = function () {
                var timeDomainStart = 0, timeDomainEnd = 0,
                    addDays = function (date, days) {
                        var res = new Date(date.getTime());
                        res.setDate(res.getDate() + days);
                        return res;
                    };

                if (!workUnits || workUnits.length < 1) {
                    return {
                        from: d3.time.day.offset(now, -3),
                        to: d3.time.hour.offset(now, +3)
                    };
                }

                workUnits.forEach(function (elem) {
                    var endOfTime = addDays(elem.firstWeekStart(), elem.numberOfWeeks() * 7);
                    timeDomainStart = timeDomainStart || elem.firstWeekStart();
                    timeDomainEnd = timeDomainEnd || endOfTime;
                    timeDomainStart = timeDomainStart > elem.firstWeekStart() ? elem.firstWeekStart() : timeDomainStart;
                    timeDomainEnd = timeDomainEnd < endOfTime ? endOfTime : timeDomainEnd;
                });
                return {
                    from: timeDomainStart,
                    to: timeDomainEnd
                };
            }();

        var xScale = function () {
            return d3.time.scale().domain([timeBounds.from, timeBounds.to]).range([ 0, width]);
        };

        var yScale = function () {
            return d3.scale.ordinal().domain(workUnits.map(function(elem) {return elem.name();})).rangeRoundBands([ 0, height]);
        };

        var xAxis = function (xs) {
            return d3.svg.axis().scale(xs).orient("bottom").ticks(d3.time.day, 1);
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
            var format = d3.time.format("%Y-%m-%d<br>(%A)");
            x = xAxis(xs).tickFormat(format);
            y = yAxis(ys);
            //Draws the x axis with ticks.
            svg.append("g")
                .attr("class", "x axis")
                .attr("transform", "translate(0," + height + ")")
                .call(x)
                .selectAll(".tick text").call(wrapTickText);
            //Draws the y axis with ticks.
            svg.append("g")
                .attr("class", "y axis")
                .call(y);
        };

        var drawWeekends = function(xs) {
            var weekends = [],
                startDate = new Date(timeBounds.from.getTime());
            //Gather the weekend days.
            while(startDate <= timeBounds.to) {
                if(startDate.getDay() == 6 || startDate.getDay() == 0) {
                    weekends.push(new Date(startDate.getTime()));
                }
                startDate.setDate(startDate.getDate() + 1);
            }
            //Draw the weekend days - gray rectangles.
            graphContainer.selectAll(".weekend-mark").data(weekends).enter()
                .append("rect")
                .attr("x", function(d) {return xP(d, xs);})
                .attr("width", function(d) {
                    var nextDay = new Date(d.getTime());
                    nextDay.setDate(nextDay.getDate() + 1);
                    return xP(nextDay, xs) - xP(d, xs);
                })
                .attr("height", height)
                .attr("class", "weekend-mark");
        };

        var drawHolidays = function(xs) {
            graphContainer.selectAll(".holiday-mark").data(holidays).enter()
                .append("rect")
                .attr("x", function(d) {return xP(d, xs);})
                .attr("width", function(d) {
                    var nextDay = new Date(d.getTime());
                    nextDay.setDate(nextDay.getDate() + 1);
                    return xP(nextDay, xs) - xP(d, xs);
                })
                .attr("height", height)
                .attr("class", "holiday-mark");
        };

        var drawNowMarker = function (xs) {
            graphContainer.append("line")
                .style("vector-effect", "non-scaling-stroke")
                .attr("class", "nowMarker")
                .attr("x1", xP(now, xs))
                .attr("x2", xP(now, xs))
                .attr("y2", height);
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
            return invertY(point.matrixTransform(graphContainer.node().getTransformToElement(graphContainer.node().parentNode)).y, ys);
        };

        var invertY = function(y, ys) {
            var domain = ys.domain(),
                startY,
                index;
            for (index = 0; index < domain.length; index += 1) {
                startY = ys(domain[index]);
                if (startY <= y && startY + ys.rangeBand() >= y) {
                    return domain[index];
                }
            }
            return undefined;
        };

        var mouseMoveOverJob = function (bar, d) {
            var d3elem = d3.select(bar);
            var mousePos = d3.mouse(bar);
            var isSelected = selectedJobs.indexOf(bar) >= 0;
            if (!currentDragEvent){
                if (isSelected && mousePos[0] < d.geomData.dimension.width * stretchWidth && d.entity.strechability.indexOf(d3.scheduling.TaskAction.LEFT) >= 0) {
                    d3elem.style("cursor", "w-resize");
                } else if (isSelected && mousePos[0] > d.geomData.dimension.width * (1 - stretchWidth) && d.entity.strechability.indexOf(d3.scheduling.TaskAction.RIGHT) >= 0) {
                    d3elem.style("cursor", "e-resize");
                } else if (isSelected && d.entity.strechability.indexOf(d3.scheduling.TaskAction.MOVE) >= 0){
                    d3elem.style("cursor", "move");
                } else {
                    d3elem.style("cursor", "");
                }
            }
        };

        var selectJob = function (job, d) {
            if (!d.entity.activatable) {
                return;
            }
            var indexOfTask = selectedJobs.indexOf(job);
            if (indexOfTask >= 0) {
                d3.select(job)
                    .classed(jobs[d.entity.job], true).classed(selectColor, false);
                selectedJobs.splice(indexOfTask, 1);
                if (activeJob == job) {
                    activateJob(selectedJobs[selectedJobs.length - 1]);
                }
            } else {
                selectedJobs.push(job);
                d3.select(job)
                    .classed(jobs[d.entity.job], false).classed(selectColor, true);
                activateJob(job);
            }
        };

        var activateJob = function(job) {
            if (job != activeJob) {
                d3.select(activeJob).selectAll(".mark-rect").classed(selectColor, false);
                d3.select(job).selectAll(".mark-rect").classed(selectColor, true);
                activeJob = job;
            }
        };

        var taskDragStarted = function (bar, d, xs) {
            var cursor,
                mousePos = d3.mouse(bar),
                mousePosForParent = d3.mouse(bar.parentNode),
                isSelected = selectedJobs.indexOf(bar) >= 0,
                taskAction;
            if (isSelected) {
                activateJob(bar);
                if (mousePos[0] < d.geomData.dimension.width * stretchWidth && d.entity.strechability.indexOf(d3.scheduling.TaskAction.LEFT) >= 0) {
                    cursor = "w-resize";
                    taskAction = d3.scheduling.TaskAction.LEFT;
                } else if (mousePos[0] > d.geomData.dimension.width * (1 - stretchWidth) && d.entity.strechability.indexOf(d3.scheduling.TaskAction.RIGHT) >= 0) {
                    cursor ="e-resize";
                    taskAction = d3.scheduling.TaskAction.RIGHT;
                } else if (d.entity.strechability.indexOf(d3.scheduling.TaskAction.MOVE) >= 0) {
                    cursor = "move";
                    taskAction = d3.scheduling.TaskAction.MOVE;
                }
                d3.select("html").style("cursor", cursor);
            }
            if (taskAction) {
                $(bar).detach().insertBefore(".nowMarker");
                currentDragEvent = {
                    barMousePos : mousePos,
                    pos: mousePosForParent,
                    action: taskAction
                };
            }
        };

        var taskDragged = function (d, xs, ys) {
            switch (currentDragEvent.action) {
                case(d3.scheduling.TaskAction.LEFT) : handleStretchTaskLeftSide(d, xs); break;
                case(d3.scheduling.TaskAction.MOVE) : handleMoveTask(d, xs, ys); break;
                case(d3.scheduling.TaskAction.RIGHT) : handleStretchTaskRightSide(d, xs); break;
            }
        };
        var taskDragEnded = function (d) {
            var isOutside = d.barList.horizontalBars.indexOf(d) >= 0,
                lineRanges = d.barList.geomData;
            if (currentDragEvent.action == d3.scheduling.TaskAction.MOVE && isOutside) {
                if (currentDragEvent.pos[1] >= lineRanges.snapRange[0] && currentDragEvent.pos[1] <= lineRanges.snapRange[1]) {
                    try {
                        d.moveToInline();
                        d.geomData.pos.y = lineRanges.inlineY;
                        removeTick(d);
                    } catch (ex) {
                        d.geomData.pos.y = lineRanges.lowerRange[0];
                    }
                } else {
                    d.geomData.pos.y = getInBopundPos(currentDragEvent.pos[1]) - currentDragEvent.barMousePos[1];
                }
                d.redrawCallback(d);
            }
            d3.select("html").style("cursor", "");
            currentDragEvent = null;
        };

        var removeTick = function(d) {
            if(d.geomData.tick) {
                d.geomData.tick.axisTick.remove();
                d.geomData.tick.gridTick.remove();
                d.geomData.tick = null;
            }
        };

        var handleStretchTaskLeftSide = function (d, xs) {
            var newTick = d.barList.workUnit.weeks().findClosestTickWithOuterBounds(ixP(currentDragEvent.pos[0], xs)),
                ticksBetween = howManyTicksBetween(d.startTick, newTick);
            d.stretchLeft(ticksBetween);
        };

        var handleStretchTaskRightSide = function (d, xs) {
            var newTick = d.barList.workUnit.weeks().findClosestTickWithOuterBounds(ixP(currentDragEvent.pos[0], xs)),
                ticksBetween = howManyTicksBetween(d.endTick, newTick);
            d.stretchRight(ticksBetween);
        };

        var handleMoveTask = function (d, xs, ys) {
            var isInline = d.barList.inlineBars.indexOf(d) >= 0,
                isOutside = d.barList.horizontalBars.indexOf(d) >= 0,
                lineRanges  = d.barList.geomData,
                newBarList = wUBarList[iyP(currentDragEvent.pos[1], ys)],
                jobDuration = howManyTicksBetween(d.startTick, d.endTick),
                firstTick, secondTick;
            if (newBarList != d.barList) {
                d.geomData.pos.y = getInBopundPos(currentDragEvent.pos[1]) - currentDragEvent.barMousePos[1];
                if (newBarList) {
                    d.moveToBarList(newBarList, false);
                } else if (isInline) {
                    d.moveToOutline();
                }
            } else if (isInline && currentDragEvent.pos[1] > lineRanges.snapRange[1] || currentDragEvent.pos[1] < lineRanges.snapRange[0]) {
                d.geomData.pos.y = getInBopundPos(currentDragEvent.pos[1]) - currentDragEvent.barMousePos[1];
                d.moveToOutline();
            } else if (isOutside) {
                d.geomData.pos.y = getInBopundPos(currentDragEvent.pos[1]) - currentDragEvent.barMousePos[1];
            }
            firstTick = d.barList.workUnit.weeks().findClosestTickWithOuterBounds(ixP(currentDragEvent.previousPos[0], xs));
            secondTick = d.barList.workUnit.weeks().findClosestTickWithOuterBounds(ixP(currentDragEvent.pos[0], xs));
            createTickIfNeeded(d);
            d.push(howManyTicksBetween(firstTick, secondTick));
        };

        var createTickIfNeeded = function(d) {
            var axisTick, axisGrid;
            if (d.barList.inlineBars.indexOf(d) < 0 && !d.geomData.tick) {
                axisTick = $(".y.axis>.tick:first-child", $(svg.node()));
                axisGrid = $(".y.grid>.tick:first-child", $(svg.node()));
                d.geomData.tick = {};
                d.geomData.tick.axisTick = axisTick.clone();
                d.geomData.tick.gridTick = axisGrid.clone();
                $("text", d.geomData.tick.axisTick).text("~");
                d.geomData.tick.axisTick.attr("transform", "translate(0, " + (d.geomData.pos.y + d.geomData.dimension.height / 2) + ")");
                d.geomData.tick.gridTick.attr("transform", "translate(0, " + (d.geomData.pos.y + d.geomData.dimension.height / 2) + ")");
                d.geomData.tick.axisTick.appendTo(".y.axis");
                d.geomData.tick.gridTick.appendTo(".y.grid");
            }
        };

        var getInBopundPos = function(y) {
            if (y < 0) {
                return 0;
            } else if(y > height) {
                return height;
            } else {
                return y;
            }
        };

        var configureDragEvent = function(xs, ys) {
            var timeout;
            return d3.behavior.drag()
                .on("dragstart", function (d) {
                    var bar = this;
                    console.log(d3.event.sourceEvent);
                    if (d3.event.sourceEvent.detail == 2) {
                        splitJob(bar, d, xs, ys);
                    }
                    switch (d3.event.sourceEvent.type) {
                        case ("mousedown"):
                            if (d3.event.sourceEvent.button == 0 && d3.event.sourceEvent.ctrlKey) {
                                selectJob(bar, d);
                            }
                            taskDragStarted(bar, d, xs);
                            break;
                        case ("touchstart") :
                            taskDragStarted(bar, d, xs);
                            timeout = setTimeout(function () {
                                selectJob(bar, d);
                                taskDragStarted(bar, d, xs);
                            }, 700);
                            break;
                    }
                    d3.event.sourceEvent.stopPropagation();
                }).on("drag", function (d) {
                    if(d3.event.sourceEvent.type === "touchmove") {
                        clearTimeout(timeout);
                    }
                    if(currentDragEvent) {
                        var mousePos = d3.mouse(this.parentNode);
                        currentDragEvent.previousPos = currentDragEvent.pos;
                        currentDragEvent.pos = mousePos;
                        if (d.entity.strechability.indexOf(currentDragEvent.action) >= 0) {
                            taskDragged(d, xs, ys);
                        }
                    }
                }).on("dragend", function (d) {
                    if(d3.event.sourceEvent.type === "touchend") {
                        clearTimeout(timeout);
                    }
                    if(currentDragEvent) {
                        var mousePos = d3.mouse(this.parentNode);
                        currentDragEvent.previousPos = currentDragEvent.pos;
                        currentDragEvent.pos = mousePos;
                        taskDragEnded(d);
                    }
                });
        };

        var drawBars = function (jobBars, xs, ys) {
            jobBars.forEach(function(elem) {
                var barList = wUBarList[elem.groupTitle],
                    bar = barList.contains(elem);
                if (!bar) {
                    bar = barList.insert(elem);
                }
                if (!bar.geomData) {
                    bar.geomData = {};
                    bar.geomData.node = graphContainer.append("g").datum(bar);
                }
                drawBar(bar, xs, ys);
            });
        };

        var drawBar = function(bar, xs, ys) {
            var geometry = bar.geomData,
                barNode = geometry.node,
                x = xP(bar.startTick.time, xs),
                actualBarWidth = xP(new Date(bar.endTick.time.getTime() + bar.endTick.duration), xs) - x,
                y = bar.barList.horizontalBars.indexOf(bar) >= 0 ? bar.barList.geomData.upperRange[0] + 1 : bar.barList.geomData.inlineY;
            bar.redrawCallback = updateBar(xs);
            if (geometry.pos && geometry.pos.y) {
                geometry.pos.x = x;
            } else {
                geometry.pos = {x: x, y: y};
            }
            geometry.dimension = {width: actualBarWidth, height:  bar.barList.geomData.actualBarHeight};
            barNode.node().getToolTipText = function() {
                return "Part " + bar.entity.mark +
                    ":<ul style='margin:0.1em'>" +
                    "<li>duration " + ((howManyTicksBetween(bar.startTick, bar.endTick) + 1) * 15 / 60) + " hrs</li>" +
                    "<li>allocated craft is " + bar.barList.workUnit.name() + "</li>" +
                    "</ul>";
            };
            barNode
                .attr("class", function(d) {return "job " + jobs[d.entity.job];})
                .attr("transform", function (d) {return "translate(" + d.geomData.pos.x + ", " + d.geomData.pos.y + ")";})
                .on("mousemove", function (d) {
                    mouseMoveOverJob(this, d);
                })
                .call(configureDragEvent(xs, ys))
                .append("rect")
                    .style("vector-effect", "non-scaling-stroke")
                    .attr("class", "job-rect")
                    .attr("width", function(d) {return d.geomData.dimension.width})
                    .attr("height", function(d) {return d.geomData.dimension.height});
            unscale(barNode.append("g")
                .attr("class", "mark")
                .attr("transform", function(d) {
                    return "translate(" + d.geomData.dimension.width / 2 + ", -10)";
                }).call(drawMarker, bar).call(setUnscalable).node());
            createTickIfNeeded(bar);
        };

        var splitJob = function(bar, d, xs, ys) {
            var newBar = d.splitJobAt(ixP(d3.mouse(bar.parentNode)[0], xs));
            if (newBar) {
                newBar.geomData = {};
                newBar.geomData.node = graphContainer.insert("g", ".nowMarker").datum(newBar);
                if(newBar.barList.horizontalBars.indexOf(newBar) >= 0) {
                    newBar.geomData.pos = {y : d.geomData.pos.y};
                }
                drawBar(newBar, xs, ys);
                if (selectedJobs.indexOf(bar) >= 0) {
                    selectJob(newBar.geomData.node.node(), newBar);
                }
            }
        };

        var updateBar = function(xs) {
            return function(bar) {
                if(!bar.geomData.node) {
                    return;
                }
                bar.geomData.pos.x = xP(bar.startTick.time, xs);
                bar.geomData.dimension.width = xP(new Date(bar.endTick.time.getTime() + bar.endTick.duration), xs) - bar.geomData.pos.x;
                bar.geomData.node.attr("transform", "translate(" + bar.geomData.pos.x + ", " + bar.geomData.pos.y + ")");
                bar.geomData.node.select(".job-rect").attr("width", bar.geomData.dimension.width);
                bar.geomData.node.select(".mark").call(function(selection) {
                    var translateMatrix = selection.node().transform.baseVal.getItem(0).matrix;
                    translateMatrix.e =  bar.geomData.dimension.width / 2;
                });
                if (bar.geomData.tick) {
                    bar.geomData.tick.axisTick.attr("transform", "translate(0, " + (bar.geomData.pos.y + bar.geomData.dimension.height / 2) +")");
                    bar.geomData.tick.gridTick.attr("transform", "translate(0, " + (bar.geomData.pos.y + bar.geomData.dimension.height / 2) +")");
                }
            };
        };

        var drawMarker = function(barNode, barData) {
            var rect = barNode.append("rect").attr("class", "mark-rect");
            var text = barNode.append("text").attr("class", "mark-text")
                .style("text-anchor", "middle").text(barData.entity.mark);
            var textBox = text.node().getBBox();
            rect.attr("x", textBox.x - 5).attr("y", textBox.y - 1).attr("width", textBox.width + 10).attr("height", textBox.height + 2);
        };

        var setUnscalable = function (barNode) {
            unscalableElems.push(barNode.node());
        };

        var zoomed = function () {
            svg.select(".x.axis").call(x).selectAll(".tick text").call(wrapTickText);
            svg.select(".x.grid").call(xGrid);
            graphContainer.attr("transform", "translate(" + d3.event.translate[0] + ", 0)scale(" + d3.event.scale + ", 1)");
            unscalableElems.forEach(unscale);
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
                    lineHeight = 1.1,//em;
                    yp = text.attr("y"),
                    dy = parseFloat(text.attr("dy"));
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

        var calculateBarListGeometry = function(barList, ys) {
            var _ = {},
                actualBarHeight = ys.rangeBand() * barWidth,
                startY = yP(barList.workUnit.name(), ys),
                inlineY = startY + ys.rangeBand() / 2  - actualBarHeight / 2;
            _.upperRange = [startY, inlineY - actualBarHeight];
            _.lowerRange = [inlineY + 2 * actualBarHeight, startY + ys.rangeBand()];
            _.snapRange = [inlineY - actualBarHeight, inlineY + 2 * actualBarHeight];
            _.inlineY = inlineY;
            _.actualBarHeight = actualBarHeight;
            return _;
        };

        var point = function(container, x, y) {
            var svg = container.ownerSVGElement || container;
            if (svg.createSVGPoint) {
                var point = svg.createSVGPoint();
                point.x = x; point.y = y;
                point = point.matrixTransform(container.getScreenCTM().inverse());
                return [point.x, point.y];
            }
            var rect = container.getBoundingClientRect();
            return [ e.clientX - rect.left - container.clientLeft, e.clientY - rect.top - container.clientTop ];
        };

        //TODO Implement special ordinal scale see http://stackoverflow.com/questions/20758373/d3-js-inversion-with-ordinal-scale
        //TODO and this http://stackoverflow.com/questions/13342149/pan-zoom-ordinal-var
        //TODO implement your own time interval in order to support evenly distributed dates on OX axis see https://github.com/mbostock/d3/issues/1593
        var scheduling = function (selection) {
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
                .attr("class", "scheduling-chart")
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
            //Drawing the chart itself
            graphContainer = svg.append("g").attr("clip-path", "url(#chart-area)").append("g").attr("class", "chart");
            drawWeekends(xs);
            drawHolidays(xs);
            workUnits.forEach(function(elem) {
                var barList = wUBarList[elem.name()];
                if (!barList.geomData) {
                    barList.geomData = calculateBarListGeometry(barList, ys);
                }
            });
            drawBars(jobParts, xs, ys);
            drawNowMarker(xs);
            //Draw axis
            drawAxis(xs, ys);
        };

        scheduling.joinSelectedJobs = function() {
            var jobsToRemove = [],
                job,
                index;
            for(index = 0; index < selectedJobs.length; index += 1) {
                job = d3.select(selectedJobs[index]).datum();
                while (job.next &&
                    job.endTick.next === job.next.startTick &&
                    job.entity.job === job.next.entity.job &&
                    selectedJobs.indexOf(job.next.geomData.node.node()) >= 0)
                {
                    jobsToRemove.push(job.next);
                    job.joinWith(job.next);
                }
            }
            jobsToRemove.forEach(function (elem) {
                index = selectedJobs.indexOf(elem.geomData.node.node());
                selectedJobs.splice(index, 1);
                elem.geomData.node.remove();
            });
        };

        scheduling.setJobs = function (_) {
            if (!arguments.length) {
                return jobs;
            }
            jobs = _;
            return scheduling;
        };

        scheduling.addJobPart = function (jobPart, event) {
            var pos,time, group, barList, newBar;
            if (graphContainer && jobPart) {
                pos = point(graphContainer.node(), event.clientX, event.clientY);
                time = ixP(pos[0], x.scale());
                group = iyP(pos[1], y.scale());
                barList = wUBarList[group];
                jobPart.start = time;
                jobPart.groupTitle = group;
                if (barList.geomData.snapRange[0] <= pos[1] && barList.geomData.snapRange[1] >= pos[1]) {
                    newBar = barList.insert(jobPart);
                    newBar.geomData = {};
                } else {
                    newBar = barList.insertOutsideBar(jobPart);
                    newBar.geomData = {};
                    newBar.geomData.pos = {y: pos[1] - barList.geomData.actualBarHeight / 2};
                }
                newBar.geomData.node = graphContainer.insert("g", ".nowMarker").datum(newBar);
                drawBar(newBar, x.scale(), y.scale());
            }
        };

        scheduling.setJobParts = function (_) {
            if (!arguments.length) {
                return jobParts;
            }
            jobParts = [];
            _.forEach(function (elem) {
                jobParts.push(elem);
            });
            return scheduling;
        };

        scheduling.width = function (w) {
            if (!arguments.length) {
                return width;
            }
            width = +w;
            return scheduling;
        };

        scheduling.height = function (h) {
            if (!arguments.length) {
                return height;
            }
            height = +h;
            return scheduling;
        };

        scheduling.markVisible = function(_) {
            if (!arguments.length) {
                return markVisible;
            }
            markVisible = !!_;
            return scheduling;
        };

        scheduling.markVisibleForSelected = function(_) {
            if (!arguments.length) {
                return markVisible;
            }
            markVisibleForSelected = !!_;
            return scheduling;
        };

        scheduling.margin = function (m) {
            if (!arguments.length) {
                return margin;
            }
            margin.top = m.hasOwnProperty('top') ? m.top : margin.top;
            margin.right = m.hasOwnProperty('right') ? m.right : margin.right;
            margin.bottom = m.hasOwnProperty('bottom') ? m.bottom : margin.bottom;
            margin.left = m.hasOwnProperty('left') ? m.left : margin.left;
            return scheduling;
        };

        return scheduling;
    };

    //Available task action.
    d3.scheduling.TaskAction = {
        MOVE: "MOVE",
        LEFT: "LEFT",
        RIGHT: "RIGHT"
    };

    //Utility method that allows to create work unit.
    d3.scheduling.workUnit = function(name, zero, firstWeekStart, numberOfWeeks, productiveHoursForEachWeek) {
        if (!name) {
            throw {msg: "Work unit name is required."};
        }
        if (firstWeekStart.getDay() != 1){
            throw {msg: "Week should start on Monday."};
        }
        if (numberOfWeeks < 1) {
            throw {msg: "Scheduling should be performed for the duration of at least one week."};
        }
        if (productiveHoursForEachWeek.length != numberOfWeeks) {
            throw {msg: "The values for working hours should be specified for all scheduling weeks."};
        }
        var weeks = createWeeks(firstWeekStart, productiveHoursForEachWeek),
            zeroTick = weeks.findClosestTickWithOuterBounds(zero),
            nextTick;
        zeroTick.isNow = true;
        nextTick = zeroTick.prev;
        while (nextTick) {
            nextTick.isBeforeNow = true;
            nextTick = nextTick.prev;
        }
        nextTick = zeroTick.next;
        while (nextTick) {
            nextTick.isAfterNow = true;
            nextTick = nextTick.next;
        }
        return {
            name : function() { return name; },
            firstWeekStart : function() { return firstWeekStart; },
            numberOfWeeks : function() { return numberOfWeeks; },
            productiveHoursForEachWeek : function() { return productiveHoursForEachWeek; },
            weeks : function() { return weeks;}
        };
    };
})();
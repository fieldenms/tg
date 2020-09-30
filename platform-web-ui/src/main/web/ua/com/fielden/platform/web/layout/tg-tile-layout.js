import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-media-query/iron-media-query.js'
import '/app/tg-app-config.js'

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { FlattenedNodesObserver } from '/resources/polymer/@polymer/polymer/lib/utils/flattened-nodes-observer.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const template = html`
    <style>
        :host ::slotted(.tile) {
            position: absolute;
        }
    </style>
    <tg-app-config id="appConfig"></tg-app-config>
    <iron-media-query query="[[_calcMobileQuery()]]" on-query-matches-changed="_mobileChanged"></iron-media-query>
    <iron-media-query query="[[_calcTabletQuery()]]" on-query-matches-changed="_tabletChanged"></iron-media-query>
    <iron-media-query query="[[_calcDesktopQuery()]]" on-query-matches-changed="_desktopChanged"></iron-media-query>
    <slot id="items" name="tile"></slot>`;

template.setAttribute('strip-whitespace', '');

(function () {
    // Set the layout for the given layout specification.
    const setLayout = function (layoutSpec) {
        if (typeof layoutSpec === "string") {
            layoutSpec = JSON.parse(layoutSpec);
        }
        var layout = [],
            componentIndex = 0,
            layoutGrid = buildLayoutGrid(layoutSpec),
            rows = layoutGrid.length,
            cols = layoutGrid[0] && layoutGrid[0].length;
        // Creating a data structure that corresponds to layout specification.
        layoutSpec.forEach(function (rowElement) {
            rowElement.forEach(function (colElement) {
                layout.push(getComponentBoundingBox(componentIndex, layoutGrid));
                componentIndex += 1;
            });
        });
        this.rows = rows;
        this.cols = cols;
        this.$.items.assignedNodes().forEach(function (item, itemIndex) {
            item.style.top = 100 * layout[itemIndex].y / rows + "%";
            item.style.left = 100 * layout[itemIndex].x / cols + "%";
            item.style.width = 100 * layout[itemIndex].width / cols + "%";
            item.style.height = 100 * layout[itemIndex].height / rows + "%";
            item.rows = layout[itemIndex].height;
            item.cols = layout[itemIndex].width;
        });
        setMinDimensions.bind(this)();
        this.fire('layout-finished', layoutSpec);
    };
    const setMinDimensions = function () {
        var self = this;
        if (self.minCellHeight && self.rows) {
            self.style.minHeight = "calc(" + self.minCellHeight + " * " + self.rows + ")";
        }
        if (self.minCellWidth && self.cols) {
            self.style.minWidth = "calc(" + self.minCellWidth + " * " + self.cols + ")";
        }
        this.$.items.assignedNodes().forEach(function (item, itemIndex) {
            if (self.minCellHeight && item.rows) {
                item.style.minHeight = "calc(" + self.minCellHeight + " * " + item.rows + ")";
            }
            if (self.minCellWidth && item.cols) {
                item.style.minWidth = "calc(" + self.minCellWidth + " * " + item.cols + ")";
            }
        });
    };
    // Returns the component's bounding box in index units; Component for which bounding box must be calculated is specified with componentIndex.
    const getComponentBoundingBox = function (componentIndex, layoutGrid) {
        var rowIndex,
            colIndex,
            boundingBox = {};

        for (rowIndex = 0; rowIndex < layoutGrid.length; rowIndex++) {
            for (colIndex = 0; colIndex < layoutGrid[rowIndex].length; colIndex++) {
                if (layoutGrid[rowIndex][colIndex].index === componentIndex) {
                    return {
                        x: colIndex,
                        y: rowIndex,
                        width: calculateComponentWidth(componentIndex, layoutGrid, rowIndex, colIndex),
                        height: calculateComponentHeight(componentIndex, layoutGrid, rowIndex, colIndex)
                    };
                }
            }
        }
    };
    // Calculates the component width in index units.
    const calculateComponentWidth = function (componentIndex, layoutGrid, row, col) {
        var width = 0;
        while (col < layoutGrid[row].length && layoutGrid[row][col].index === componentIndex) {
            width += 1;
            col += 1;
        }
        if (!width) {
            throw new Error(`The component at: ${componentIndex} index has width equal to 0.`);
        }
        return width;
    };
    // Calculates the component height in index units;
    const calculateComponentHeight = function (componentIndex, layoutGrid, row, col) {
        var height = 0;
        while (row < layoutGrid.length && layoutGrid[row][col].index === componentIndex) {
            height += 1;
            row += 1;
        }
        if (!height) {
            throw new Error(`The component at ${componentIndex} index has height equal to 0.`);
        }
        return height;
    };
    // Builds the grid (two dimensional array) that represents the given layoutSpec.
    const buildLayoutGrid = function (layoutSpec) {
        var layoutGrid = [],
            rows = 0,
            cols = 0,
            rowIndex = 0,
            colIndex = 0,
            componentIndex = 0;
        // Calculate the number of rows.
        while (rowIndex < layoutSpec.length) {
            rows += maxRowSpan(layoutSpec[rowIndex])
            rowIndex = rows;
        }
        // Throw exception if the specified layout spec doesn't have rows.
        if (rows === 0) {
            throw new Error(`The layout ${JSON.stringify(layoutSpec)} must have at least one row.`);
        }
        // Calculate the number of columns.
        layoutSpec[0].forEach(function (colElement) {
            if (colElement[0] && colElement[0].hasOwnProperty("colspan") && colElement[0].colspan > 1) {
                cols += colElement[0].colspan;
            } else {
                cols += 1;
            }
        });
        // Throw exception if the specified layout spec doesn't have columns.
        if (cols === 0) {
            throw new Error(`The layout ${JSON.stringify(layoutSpec)} must have at least one column.`);
        }
        // Create the two dimensional array that represents the layout and fill it with object that has only one property named index equal to -1.
        // That indicates the free grid cell.
        for (rowIndex = 0; rowIndex < rows; rowIndex++) {
            layoutGrid.push([]);
            for (colIndex = 0; colIndex < cols; colIndex++) {
                layoutGrid[rowIndex].push({
                    index: -1
                });
            }
        }
        // Fill the layout grid with component indexes. The component index in the layout grid will indicate the position of component and it's size.
        layoutSpec.forEach(function (rowElement, rowInd) {
            rowElement.forEach(function (colElement, colInd) {
                var rowSpan = 1,
                    colSpan = 1,
                    elementPos = null;
                if (colElement[0] && colElement[0].hasOwnProperty("rowspan") && colElement[0].rowspan > 1) {
                    rowSpan = colElement[0].rowspan;
                }
                if (colElement[0] && colElement[0].hasOwnProperty("colspan") && colElement[0].colspan > 1) {
                    colSpan = colElement[0].colspan;
                }
                elementPos = findFreePlace(layoutGrid);
                if (elementPos) {
                    placeElementAt(componentIndex, layoutGrid, elementPos[0], elementPos[1], colSpan, rowSpan);
                } else {
                    throw new Error(`There is no free space for element at index: ${componentIndex}.`);
                }
                componentIndex += 1;
            });
        });

        return layoutGrid;
    };
    // Place the element specified with componentIndex into layoutGrid at row and col with specified width and height.
    // Element is placed in the layout grid when layout grid has cell or cells with the specified component index.
    const placeElementAt = function (componentIndex, layoutGrid, row, col, width, height) {
        var rowIndex,
            colIndex;
        for (rowIndex = row; rowIndex < row + height; rowIndex++) {
            for (colIndex = col; colIndex < col + width; colIndex++) {
                if (!layoutGrid[rowIndex][colIndex]) {
                    throw new Error(`The component at index ${componentIndex} doesn't have enough space to be placed into layout.`);
                }
                if (layoutGrid[rowIndex][colIndex].index !== -1) {
                    throw new Error(`The components at indices ${componentIndex} and ${layoutGrid[rowIndex][colIndex].index} are overlapping.`);
                } else {
                    layoutGrid[rowIndex][colIndex].index = componentIndex;
                }
            }
        }
    };
    // Finds first free place in the layout grid. Free place - it is a cell in the two dimensional array that has object with index property equal to -1.
    // Returns the array of two elements. The first element - is a row index of the first free cell. The second element - is a column index of the first free cell.
    // If the layoutGrid doesn't have free place then it returns null.
    const findFreePlace = function (layoutGrid) {
        var rowIndex = 0,
            colIndex = 0;
        for (rowIndex = 0; rowIndex < layoutGrid.length; rowIndex++) {
            for (colIndex = 0; colIndex < layoutGrid[rowIndex].length; colIndex++) {
                if (layoutGrid[rowIndex][colIndex].index === -1) {
                    return [rowIndex, colIndex];
                }
            }
        }

        return null;
    };
    // Returns the largest number of row span among specified columns. 
    const maxRowSpan = function (cols) {
        var rowSpan = 1;

        cols.forEach(function (colElement) {
            if (colElement[0] && colElement[0].hasOwnProperty("rowspan") && colElement[0].rowspan > rowSpan) {
                rowSpan = colElement[0].rowspan;
            }
        });

        return rowSpan;
    };

    Polymer({
        _template: template,

        is: "tg-tile-layout",

        properties: {
            whenDesktop: {
                type: Array,
                observer: "_whenDesktopChanged"
            },
            whenTablet: {
                type: Array,
                observer: "_whenTabletChanged"
            },
            whenMobile: {
                type: Array,
                observer: "_whenMobileChanged"
            },
            minCellHeight: {
                type: String,
                observer: "_minCellHeightChanged"
            },
            minCellWidth: {
                type: String,
                observer: "_minCellWidthChanged"
            },
            desktopScreen: {
                type: Boolean,
                readOnly: true,
                observer: "_handleDesktopScreen"
            },
            tabletScreen: {
                type: Boolean,
                readOnly: true,
                observer: "_handleTabletScreen"
            },
            mobileScreen: {
                type: Boolean,
                readOnly: true,
                observer: "_handleMobileScreen"
            },
            contentLoaded: {
                type: Boolean,
                readOnly: true,
                observer: "_handleContentLoading",
                value: false
            }
        },

        attached: function () {
            this._nodeObserver = new FlattenedNodesObserver(this.$.items, this._childNodesChanged.bind(this));
            this.async(function () {
                this._setContentLoaded(true);
            }, 1);
        },
        detached: function () {
            this._nodeObserver.disconnect();
        },
        _childNodesChanged: function (info) {
            this._setAppropriateScreen();
        },
        _mobileChanged: function (e, detail) {
            this._setMobileScreen(detail.value);
        },
        _tabletChanged: function (e, detail) {
            this._setTabletScreen(detail.value);
        },
        _desktopChanged: function (e, detail) {
            this._setDesktopScreen(detail.value);
        },
        _handleMobileScreen: function (newValue, oldValue) {
            if (newValue && this.whenMobile && this.contentLoaded) {
                setLayout.bind(this)(this.whenMobile);
            }
        },
        _handleTabletScreen: function (newValue, oldValue) {
            if (newValue && this.whenTablet && this.contentLoaded) {
                setLayout.bind(this)(this.whenTablet);
            }
        },
        _handleDesktopScreen: function (newValue, oldValue) {
            if (newValue && this.whenDesktop && this.contentLoaded) {
                setLayout.bind(this)(this.whenDesktop);
            }
        },
        _handleContentLoading: function (newValue, oldValue) {
            if (newValue) {
                this._setAppropriateScreen();
            }
        },
        _setAppropriateScreen: function () {
            if (this.desktopScreen && this.whenDesktop) {
                setLayout.bind(this)(this.whenDesktop);
            } else if (this.tabletScreen && this.whenTablet) {
                setLayout.bind(this)(this.whenTablet);
            } else if (this.mobileScreen && this.whenMobile) {
                setLayout.bind(this)(this.whenMobile);
            }
        },
        _whenMobileChanged: function (newValue, oldValue) {
            if (this.mobileScreen && newValue && this.contentLoaded) {
                setLayout.bind(this)(this.whenMobile);
            }
        },
        _whenTabletChanged: function (newValue, oldValue) {
            if (this.tabletScreen && newValue && this.contentLoaded) {
                setLayout.bind(this)(this.whenTablet);
            }
        },
        _whenDesktopChanged: function (newValue, oldValue) {
            if (this.desktopScreen && newValue && this.contentLoaded) {
                setLayout.bind(this)(this.whenDesktop);
            }
        },
        _minCellHeightChanged: function (newValue, oldValue) {
            setMinDimensions.bind(this)();
        },
        _minCellWidthChanged: function (newValue, oldValue) {
            setMinDimensions.bind(this)();
        },
        _calcMobileQuery: function () {
            return "max-width: " + (this.$.appConfig.minTabletWidth - 1) + "px";
        },
        _calcTabletQuery: function () {
            return "(min-width: " + this.$.appConfig.minTabletWidth + "px) and (max-width: " + (this.$.appConfig.minDesktopWidth - 1) + "px)";
        },
        _calcDesktopQuery: function () {
            return "min-width: " + this.$.appConfig.minDesktopWidth + "px";
        }
    });
})()
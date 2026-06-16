import '/resources/polymer/@polymer/iron-media-query/iron-media-query.js';

import '/resources/components/tg-subheader.js';

import '/resources/polymer/@polymer/polymer/lib/elements/dom-bind.js';

import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { mixinBehaviors } from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';

import { TgLayoutBehavior, layoutMediaQueryTemplate, stampLayoutTemplate } from '/resources/layout/tg-layout-behavior.js';

const Widgets = {
    CELL: "cell",
    SKIP: "skip",
    SUBHEADER: "subheader",
    HTML: "html"
};

const template = html`
    <style>
        :host {
            display: grid;
        }
        .hidden-with-subheader,
        .hidden-with-filter {
            display: none !important;
        }
    </style>
    ${layoutMediaQueryTemplate}`;
template.setAttribute('strip-whitespace', '');

// The number of logical tracks represented by a column or row definition, accounting for `repeat`.
const trackSpan = function (track) {
    return track.repeat && track.repeat > 1 ? track.repeat : 1;
};

// A track definition's contribution to a `grid-template-*` declaration: `repeat(n, size)` when repeated, otherwise the bare size.
const trackToken = function (track) {
    return track.repeat && track.repeat > 1 ? `repeat(${track.repeat}, ${track.size})` : track.size;
};

// The total number of logical tracks across the given definitions.
const trackCount = function (tracks) {
    return tracks.reduce((sum, track) => sum + trackSpan(track), 0);
};

// One emulated-style object per logical track, expanding repeated tracks so the array is indexable by 0-based track position.
const expandTrackStyles = function (tracks) {
    const styles = [];
    tracks.forEach(track => {
        for (let i = 0; i < trackSpan(track); i += 1) {
            styles.push(track.style || {});
        }
    });
    return styles;
};

// Marks every position covered by a (possibly spanning) cell as occupied.
const markOccupied = function (occupied, row, col, colSpan, rowSpan) {
    for (let r = row; r < row + rowSpan; r += 1) {
        for (let c = col; c < col + colSpan; c += 1) {
            occupied.add(r + ',' + c);
        }
    }
};

class TgGridLayout extends mixinBehaviors([TgLayoutBehavior], PolymerElement) {

    static get template() {
        return template;
    }

    // Lays out the slotted editors over a CSS Grid according to the wire format `{container?, columns, rows?, cells?}`.
    // Explicitly configured cells (spans, overrides, subheaders, skips) are placed at their coordinates; the remaining editors auto-flow into the empty positions, in order.
    _setLayout (layout) {
        if (!layout.columns) {
            throw new Error("The provided layout is not grid layout");
        }
        if (this.currentLayout === layout) {
            return;
        }

        // 1. Create list of slotted elements to layout them. this will be done only once, before first layout will be set.
        this._slotChildrenOnce();

        // 2. Clear anything appended by a previous layout then
        // reset the host's grid styles and styles for slotted elements setted by previous layout.
        this._clearAppendedElements();
        this._resetSubheaders();
        this._resetStyles();

        //3. Apply styles to container
        this._applyContainer(layout);

        const columns = layout.columns || [];
        const rows = layout.rows || [];
        const columnCount = trackCount(columns) || 1;
        // When `rows` are explicitly specified, the grid has a fixed height; an editor that does not fit within those rows is left unslotted (and so unrendered), rather than spilling into implicit rows.
        const rowCount = rows.length > 0 ? trackCount(rows) : Infinity;
        const columnStyles = expandTrackStyles(columns);
        const rowStyles = expandTrackStyles(rows);

        // Index the explicit cells by position and bind any `select` cells to their specific editor, removing those editors from the auto-flow pool.
        const cellAt = {};
        (layout.cells || []).forEach(cell => {
            cellAt[cell.row + ',' + cell.col] = Object.assign({}, cell);
        });
        const pool = this.componentsToLayout.slice();
        Object.values(cellAt).forEach(cell => {
            if (cell.select) {
                const eqIndex = cell.select.indexOf('=');
                const attribute = cell.select.substring(0, eqIndex).trim();
                const value = cell.select.substring(eqIndex + 1).trim();
                const index = pool.findIndex(slotName => this.slottedElements[slotName].getAttribute(attribute) === value);
                if (index >= 0) {
                    cell.boundSlot = pool.splice(index, 1)[0];
                }
            }
        });

        // Walk positions row-major: place explicit cells, bind/auto-flow editors into the rest.
        const occupied = new Set();
        const remainingCells = new Set(Object.keys(cellAt));
        let currentSubheader = null;
        let row = 1;
        while ((pool.length > 0 || remainingCells.size > 0) && row <= rowCount) {
            for (let col = 1; col <= columnCount; col += 1) {
                const key = row + ',' + col;
                if (occupied.has(key)) {
                    continue;
                }
                const cell = cellAt[key];
                let placed = null;
                let subheader = false;
                if (cell) {
                    remainingCells.delete(key);
                    const colSpan = cell.colSpan === 'all' ? columnCount : (cell.colSpan || 1);
                    const rowSpan = cell.rowSpan || 1;
                    markOccupied(occupied, row, col, colSpan, rowSpan);
                    if (cell.widget === Widgets.SKIP) {
                        placed = document.createElement('div');
                    } else if (cell.widget && cell.widget.indexOf(Widgets.SUBHEADER) === 0) {
                        placed = this._createSubheader(cell.widget);
                        subheader = true;
                    } else if (cell.widget && cell.widget.indexOf(Widgets.HTML) === 0) {
                        placed = this._createHtmlElement(cell.widget);
                    } else {
                        placed = this._createCellElement(cell.boundSlot || pool.shift());
                    }
                    this._placeItem(placed, row, col, colSpan, rowSpan, columnStyles, rowStyles, cell.style);
                } else if (pool.length > 0) {
                    placed = this._createCellElement(pool.shift());
                    markOccupied(occupied, row, col, 1, 1);
                    this._placeItem(placed, row, col, 1, 1, columnStyles, rowStyles, null);
                }
                if (placed) {
                    if (subheader) {
                        currentSubheader = placed;
                        this._subheaders.push(placed);
                    } else if (currentSubheader) {
                        currentSubheader.addRelativeElement(placed);
                    }
                    this.shadowRoot.appendChild(placed);
                    this.appendedElements.push(placed);
                }
            }
            row += 1;
        }

        this._setCurrentLayout(layout);
        this._filterLayout(this.filter);
        this.fire('layout-finished', this);
    }

    _filterLayout (filter) {
        if (this.currentLayout) {
            this._filterElement(this.shadowRoot);
        }
    }

    // Slots the host's light-DOM children once, naming each slot so it can be projected into a grid cell.
    _slotChildrenOnce () {
        if (!this.componentsToLayout) {
            this.componentsToLayout = [];
            this.slottedElements = {};
            Array.from(this.children).forEach((item, idx) => {
                const slotName = 'layout_element_' + idx;
                item.setAttribute('slot', slotName);
                this.componentsToLayout.push(slotName);
                this.slottedElements[slotName] = item;
            });
        }
    }

    _clearAppendedElements () {
        (this.appendedElements || []).forEach(element => this.shadowRoot.removeChild(element));
        this.appendedElements = [];
        this._htmlElements = {};
    }

    _resetSubheaders () {
        this._subheaders.forEach(subheader => subheader.removeAllRelatedComponents());
        this._subheaders = [];
    }

    _resetStyles () {
        // 1. Clear the grid styles the previous layout applied to the host.
        this.style.removeProperty('grid-template-columns');
        this.style.removeProperty('grid-template-rows');
        (this._appliedContainerProps || []).forEach(property => this.style.removeProperty(property));
        this._appliedContainerProps = [];
        // 2. Clear the per-editor state left on the slotted elements, so each starts the new layout from a clean slate.
        // The cell/column/row declarations live on the wrapper grid-items, which are recreated each layout and so reset themselves;
        // the only state carried on a slotted editor is the `hidden-with-filter` class, so it is cleared here. The new layout and filter
        // re-apply it as needed — this also clears it from an editor that is now clipped, which the filter no longer revisits.
        (this.componentsToLayout || []).forEach(slotName => {
            this.toggleClass('hidden-with-filter', false, this.slottedElements[slotName]);
        });
    }

    // Applies `display: grid`, the column/row templates and the container-level declarations to the host.
    _applyContainer (layout) {
        this._appliedContainerProps = [];
        this.style.display = 'grid';
        const columnTemplate = (layout.columns || []).map(trackToken).join(' ');
        if (columnTemplate) {
            this.style.gridTemplateColumns = columnTemplate;
        }
        const rowTemplate = (layout.rows || []).map(trackToken).join(' ');
        if (rowTemplate) {
            this.style.gridTemplateRows = rowTemplate;
        }
        if (layout.container) {
            Object.entries(layout.container).forEach(([property, value]) => {
                this.style.setProperty(property, value);
                this._appliedContainerProps.push(property);
            });
        }
    }


    // A grid item that projects the editor of the given slot (or an empty item when no editor is available).
    _createCellElement (slotName) {
        const wrapper = document.createElement('div');
        if (slotName) {
            const slot = document.createElement('slot');
            slot.setAttribute('name', slotName);
            wrapper.appendChild(slot);
        }
        return wrapper;
    }

    // A `tg-subheader` for a `subheader` / `subheader-open` / `subheader-closed` widget descriptor.
    _createSubheader (widget) {
        const colonIndex = widget.indexOf(':');
        const keyword = widget.substring(0, colonIndex);
        const subheader = document.createElement('tg-subheader');
        subheader.subheaderTitle = widget.substring(colonIndex + 1);
        if (keyword === 'subheader-open') {
            subheader.collapsible = true;
            subheader.closed = false;
        } else if (keyword === 'subheader-closed') {
            subheader.collapsible = true;
            subheader.closed = true;
        }
        return subheader;
    }

    // A grid item that stamps an inline html snippet (`html:<markup>`) into a `dom-bind`, kept in sync with the layout `context`.
    _createHtmlElement (widget) {
        const template = document.createElement('template');
        template.innerHTML = widget.substring(widget.indexOf(':') + 1).trim();
        const domBind = document.createElement('dom-bind');
        domBind.appendChild(template);
        stampLayoutTemplate(domBind, this.context);
        (this._htmlElements[widget] = this._htmlElements[widget] || []).push(domBind);
        const wrapper = document.createElement('div');
        wrapper.appendChild(domBind);
        return wrapper;
    }

    // Places a grid item at `(row, col)`, applying its span and the cascade of column, row and cell declarations.
    _placeItem (element, row, col, colSpan, rowSpan, columnStyles, rowStyles, cellStyle) {
        element.style.gridColumn = colSpan > 1 ? `${col} / span ${colSpan}` : `${col}`;
        element.style.gridRow = rowSpan > 1 ? `${row} / span ${rowSpan}` : `${row}`;
        [columnStyles[col - 1], rowStyles[row - 1], cellStyle].forEach(styles => {
            if (styles) {
                Object.entries(styles).forEach(([property, value]) => element.style.setProperty(property, value));
            }
        });
    }

    // Recursively toggles `hidden-with-filter` according to `this.filter`, mirroring the flex layout's filtering (including subheader sections).
    _filterElement (element) {
        if (element.hasAttribute && element.hasAttribute('filterable')) {
            this.toggleClass('hidden-with-filter', this.filter && !this.filter(element), element);
        } else {
            const children = [...element.children].flatMap(child => child.tagName === 'SLOT' ? [...child.assignedNodes()] : [child]);
            children.forEach(child => this._filterElement(child));
            const filterableChildren = children.filter(child => child.hasAttribute('filterable') || child.hasAttribute('has-filterable-children'));
            if (filterableChildren.length > 0) {
                element.setAttribute && element.setAttribute('has-filterable-children', '');
            } else {
                element.removeAttribute && element.removeAttribute('has-filterable-children');
            }
            element.classList && this.toggleClass('hidden-with-filter', filterableChildren.length > 0 && filterableChildren.every(child => child.classList.contains('hidden-with-filter')), element);

            children.filter(child => child.tagName === 'TG-SUBHEADER').forEach(subheader => {
                const filterableElements = subheader.relativeElements
                        .flatMap(relativeElement => relativeElement.tagName === 'SLOT' ? [...relativeElement.assignedNodes()] : [relativeElement])
                        .filter(relativeElement => relativeElement.hasAttribute('filterable') || relativeElement.hasAttribute('has-filterable-children'));
                const isHidden = filterableElements.length > 0 && filterableElements.every(filterableElement => filterableElement.classList.contains('hidden-with-filter'));
                this.toggleClass('hidden-with-filter', isHidden, subheader);
                if (!isHidden && this.filter) {
                    subheader.open();
                }
                subheader.relativeElements.forEach(relativeElement => {
                    if (filterableElements.indexOf(relativeElement) < 0) {
                        this.toggleClass('hidden-with-filter', isHidden, relativeElement);
                    }
                });
            });
        }
    }
}

customElements.define('tg-grid-layout', TgGridLayout);

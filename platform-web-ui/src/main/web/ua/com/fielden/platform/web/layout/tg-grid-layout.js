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
        .hidden-with-filter,
        ::slotted(.hidden-with-subheader),
        ::slotted(.hidden-with-filter) {
            display: none !important;
        }
    </style>
    ${layoutMediaQueryTemplate}`;
template.setAttribute('strip-whitespace', '');

// The number of logical tracks represented by a column or row definition, accounting for `repeat`.
const trackSpan = function (track) {
    return track.repeat && track.repeat > 1 ? track.repeat : 1;
};

// A track definition's contribution to a `grid-template-*` declaration:
// `repeat(n, size)` for a fixed repeat, `repeat(auto-fit|auto-fill, size)` for an auto-track (repeat is a keyword string), otherwise the bare size.
const trackToken = function (track) {
    const repeat = track.repeat;
    return repeat && (typeof repeat === 'string' || repeat > 1) ? `repeat(${repeat}, ${track.size})` : track.size;
};

// The total number of logical tracks across the given definitions.
const trackCount = function (tracks) {
    return tracks.reduce((sum, track) => sum + trackSpan(track), 0);
};

// One emulated-style object per logical track, expanding repeated tracks so the array is indexable by 0-based track position.
const expandTrackStyles = function (tracks) {
    return tracks.flatMap(track => Array.from({ length: trackSpan(track) }, () => track.style || {}));
};

// Whether the column tracks use auto-tracking (`repeat(auto-fit|auto-fill, …)`): the browser then determines the track count and every editor auto-flows.
const isAutoFlow = function (columns) {
    return columns.some(track => typeof track.repeat === 'string');
};

// Whether a cell's `widget` descriptor is of the given kind. Matched by prefix, so `subheader-open:Title` is a `subheader`.
const isWidget = function (widget, kind) {
    return typeof widget === 'string' && widget.indexOf(kind) === 0;
};

// Splits a `select` descriptor `attribute=value` into its trimmed `[attribute, value]`.
const splitSelect = function (select) {
    const eq = select.indexOf('=');
    return [select.substring(0, eq).trim(), select.substring(eq + 1).trim()];
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
        this._slotChildrenOnce();
        this._resetForLayout();

        const indent = this._subheaderIndent(layout);
        this._applyContainer(layout, indent);

        // Auto-tracking columns yield a browser-determined track count, so coordinate-based placement (cells, rows, spans) does not apply — every editor simply auto-flows.
        this._autoFlow = isAutoFlow(layout.columns);
        if (this._autoFlow) {
            this._layoutAutoFlow(layout.columns);
        } else {
            this._layoutFixedTracks(layout, indent);
        }

        this._finishLayout(layout);
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

    // Clears everything a previous layout left behind: appended shadow elements, subheaders, host/editor styles, and the per-row index.
    _resetForLayout () {
        this._clearAppendedElements();
        this._resetSubheaders();
        this._resetStyles();
        this._rowElements = {};
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
        // 2. Clear the per-editor state left on the slotted editors, so each starts the new layout from a clean slate.
        // Editor cells place the slotted editor itself (not a recreated wrapper), so the placement and style declarations applied
        // to it persist between layouts and are removed here, along with the visibility classes. The new layout and filter re-apply
        // them as needed — this also clears an editor that is now clipped, which the filter no longer revisits.
        (this.componentsToLayout || []).forEach(slotName => {
            const editor = this.slottedElements[slotName];
            (editor._appliedLayoutStyles || []).forEach(property => editor.style.removeProperty(property));
            editor._appliedLayoutStyles = [];
            this.toggleClass('hidden-with-filter', false, editor);
            this.toggleClass('hidden-with-subheader', false, editor);
        });
    }

    // The subheader-indentation gutter size, but only when the layout actually has a subheader; otherwise `null` (no gutter).
    _subheaderIndent (layout) {
        const hasSubheader = (layout.cells || []).some(cell => isWidget(cell.widget, Widgets.SUBHEADER));
        return hasSubheader ? layout.subheaderIndentation : null;
    }

    // Applies `display: grid`, the column/row templates and the container-level declarations to the host.
    // When `indent` is given, a fixed gutter track is prepended; the developer's columns then occupy grid columns 2..N+1.
    _applyContainer (layout, indent) {
        this._appliedContainerProps = [];
        this.style.display = 'grid';
        const columnTemplate = (layout.columns || []).map(trackToken).join(' ');
        const fullTemplate = indent ? `${indent} ${columnTemplate}` : columnTemplate;
        if (fullTemplate) {
            this.style.gridTemplateColumns = fullTemplate;
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

    // Auto-flow placement: editors flow across the browser-generated tracks of the lone auto-tracking column (reflowing on resize), so that column's style applies uniformly to every editor.
    // Auto-tracking is supported only as a single column — it cannot be combined with other (static or auto) columns, since the browser-determined track count would make per-column styling indeterminate — which is enforced here.
    _layoutAutoFlow (columns) {
        if (columns.length > 1) {
            throw new Error("An auto-tracking column (repeat(auto-fit|auto-fill, …)) must be the only column; it cannot be mixed with other columns.");
        }
        const cellStyle = columns[0].style || {};
        this.componentsToLayout.forEach(slotName => {
            this._applyStyles(this.slottedElements[slotName], cellStyle);
            this._append(this._createCellElement(slotName));
        });
    }

    // Fixed-track placement: resolve the grid geometry, bind any `select` cells, then walk the cells row-major.
    _layoutFixedTracks (layout, indent) {
        const columns = layout.columns;
        const rows = layout.rows || [];
        const grid = {
            indent,
            columnCount: trackCount(columns) || 1,
            // Explicit rows fix the grid height: an editor that does not fit is left unslotted (and so unrendered), rather than spilling into implicit rows.
            rowCount: rows.length > 0 ? trackCount(rows) : Infinity,
            columnStyles: expandTrackStyles(columns),
            rowStyles: expandTrackStyles(rows)
        };
        const pool = this.componentsToLayout.slice();
        this._placeCells(this._indexCells(layout.cells || [], pool), pool, grid);
    }

    // Indexes the explicit cells by `row,col` and binds any `select` cells to their matching editor, removing those editors from the auto-flow `pool`.
    _indexCells (cells, pool) {
        const cellAt = {};
        cells.forEach(cell => { cellAt[cell.row + ',' + cell.col] = Object.assign({}, cell); });
        Object.values(cellAt).forEach(cell => {
            if (cell.select) {
                const [attribute, value] = splitSelect(cell.select);
                const index = pool.findIndex(slotName => this.slottedElements[slotName].getAttribute(attribute) === value);
                if (index >= 0) {
                    cell.boundSlot = pool.splice(index, 1)[0];
                }
            }
        });
        return cellAt;
    }

    // Walks positions row-major, placing the explicit cell at each coordinate or auto-flowing the next editor into an empty one.
    _placeCells (cellAt, pool, grid) {
        const { columnCount, rowCount, indent, columnStyles, rowStyles } = grid;
        const occupied = new Set();
        const remaining = new Set(Object.keys(cellAt));
        // The highest row any explicit cell sits on. Past it, a cell still left in `remaining` is unreachable (its position was
        // occupied by a span, or its column is out of range), so it must not keep the loop alive — otherwise an implicit-row grid
        // (rowCount === Infinity) would spin forever. The server rejects such layouts (GridLayoutBuilder.validateCells); this is a backstop.
        const maxCellRow = Object.keys(cellAt).reduce((max, key) => Math.max(max, parseInt(key, 10)), 0);
        let currentSubheader = null;
        let row = 1;
        while ((pool.length > 0 || (remaining.size > 0 && row <= maxCellRow)) && row <= rowCount) {
            for (let col = 1; col <= columnCount; col += 1) {
                const key = row + ',' + col;
                const cell = cellAt[key];
                if (occupied.has(key) || (!cell && pool.length === 0)) {
                    continue;
                }
                remaining.delete(key);
                const spanAll = !!cell && cell.colSpan === 'all';
                const colSpan = spanAll ? columnCount : ((cell && cell.colSpan) || 1);
                const rowSpan = (cell && cell.rowSpan) || 1;
                markOccupied(occupied, row, col, colSpan, rowSpan);
                const placement = this._createPlacement(cell, pool);
                this._placeItem(placement.target, this._gridColumn(col, colSpan, spanAll, placement.subheader, indent), row, col, rowSpan, columnStyles, rowStyles, cell && cell.style);
                currentSubheader = this._registerPlacement(placement, row, currentSubheader);
            }
            row += 1;
        }
    }

    // Builds the DOM for a cell, returning `{ placed, target, subheader }`: `placed` is appended to the shadow root; `target` is the grid item that receives placement and styles.
    // A skip is a placeholder div, a subheader a `tg-subheader`, an html cell a stamped snippet; otherwise it is an editor projected via a `<slot>` — explicitly bound, or (for an absent/unbound cell) the next one auto-flowed from `pool`.
    _createPlacement (cell, pool) {
        if (cell && cell.widget === Widgets.SKIP) {
            const div = document.createElement('div');
            return { placed: div, target: div, subheader: false };
        }
        if (cell && isWidget(cell.widget, Widgets.SUBHEADER)) {
            const subheader = this._createSubheader(cell.widget);
            return { placed: subheader, target: subheader, subheader: true };
        }
        if (cell && isWidget(cell.widget, Widgets.HTML)) {
            const wrapper = this._createHtmlElement(cell.widget);
            return { placed: wrapper, target: wrapper, subheader: false };
        }
        // A select/withProp cell whose editor was not found leaves the cell empty (and warns) rather than silently consuming an unrelated editor from the pool.
        if (cell && cell.select && !cell.boundSlot) {
            console.warn(`tg-grid-layout: no editor matched select "${cell.select}" for the cell at row ${cell.row}, column ${cell.col} — leaving it empty.`);
            const div = document.createElement('div');
            return { placed: div, target: div, subheader: false };
        }
        const slotName = (cell && cell.boundSlot) || pool.shift();
        const slot = this._createCellElement(slotName);
        return { placed: slot, target: slotName ? this.slottedElements[slotName] : slot, subheader: false };
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

    // Projects the editor of the given slot via a bare <slot>. A <slot> is `display: contents`, so the slotted editor itself
    // becomes the grid item and receives the placement and styles directly — this is what lets a cell give the editor a specific size.
    // When no slot is given (an explicitly configured cell with no editor to fill it), an empty placeholder div is returned instead.
    _createCellElement (slotName) {
        if (slotName) {
            const slot = document.createElement('slot');
            slot.setAttribute('name', slotName);
            return slot;
        }
        return document.createElement('div');
    }

    // Places a grid item at the given resolved `grid-column` and at `(row)`, applying its row span and the cascade of column, row and cell declarations.
    // The grid item is the slotted editor itself for editor cells, so the declarations (including a specific width) take effect on the editor.
    // `col` is the developer column (1-based, gutter-agnostic), used only to index the column-level emulated styles.
    _placeItem (element, gridColumn, row, col, rowSpan, columnStyles, rowStyles, cellStyle) {
        this._applyStyles(element, {
            'grid-column': gridColumn,
            'grid-row': rowSpan > 1 ? `${row} / span ${rowSpan}` : `${row}`
        });
        [columnStyles[col - 1], rowStyles[row - 1], cellStyle].forEach(styles => {
            if (styles) {
                this._applyStyles(element, styles);
            }
        });
    }

    // Resolves a cell's `grid-column`, accounting for the implicit subheader-indentation gutter.
    // Without indentation it is the plain developer column and span. When a subheader is present, a fixed gutter occupies grid column 1:
    // a subheader spans it (`1 / -1`, staying flush), while all other content is indented into grid columns 2..N+1
    // (developer column c maps to grid column c+1, and `spanAll` resolves to `2 / -1`).
    _gridColumn (col, colSpan, spanAll, isSubheader, indent) {
        if (!indent) {
            return spanAll ? '1 / -1' : (colSpan > 1 ? `${col} / span ${colSpan}` : `${col}`);
        }
        if (isSubheader) {
            return '1 / -1';
        }
        if (spanAll) {
            return '2 / -1';
        }
        return colSpan > 1 ? `${col + 1} / span ${colSpan}` : `${col + 1}`;
    }

    // Applies the given CSS declarations to a grid item and records the property names on it, so the next layout can reset them.
    // This matters because the grid items for editor cells are the slotted editors, which persist between layouts (unlike the recreated shadow-DOM cells).
    _applyStyles (element, styles) {
        const applied = element._appliedLayoutStyles || (element._appliedLayoutStyles = []);
        Object.entries(styles).forEach(([property, value]) => {
            element.style.setProperty(property, value);
            applied.push(property);
        });
    }

    // Records a placed grid item: a subheader becomes the current section; any other item is attached to the current section (if any) and indexed by its row for row-based filtering.
    // Returns the (possibly updated) current subheader.
    _registerPlacement ({ placed, target, subheader }, row, currentSubheader) {
        if (subheader) {
            this._subheaders.push(target);
            currentSubheader = target;
        } else {
            if (currentSubheader) {
                currentSubheader.addRelativeElement(target);
            }
            (this._rowElements[row] = this._rowElements[row] || []).push(target);
        }
        this._append(placed);
        return currentSubheader;
    }

    // Appends an element to the shadow root and tracks it so the next layout can remove it.
    _append (element) {
        this.shadowRoot.appendChild(element);
        this.appendedElements.push(element);
    }

    // Commits the layout: remember it, apply the current filter, and notify listeners.
    _finishLayout (layout) {
        this._setCurrentLayout(layout);
        this._filterLayout(this.filter);
        this.fire('layout-finished', this);
    }

    _filterLayout (filter) {
        if (!this.currentLayout) {
            return;
        }
        if (this._autoFlow) {
            this._filterAutoFlow();
        } else {
            this._filterByRow();
        }
    }

    // Row-based filtering: a row is hidden only when it has filterable elements and every one of them is filtered out.
    // If at least one remains visible, the whole row — every element in it — stays visible. A subheader is hidden only
    // when every filterable element in its section is filtered out, and is reopened while a non-empty section is being filtered.
    // This reproduces the all-or-nothing row semantics of the flex layout, which the grid cannot get structurally because its
    // items are positioned individually rather than wrapped in per-row containers.
    _filterByRow () {
        const isFilteredOut = element => this.filter && !this.filter(element);
        Object.values(this._rowElements || {}).forEach(elements => {
            const filterable = elements.filter(element => element.hasAttribute('filterable'));
            const hidden = filterable.length > 0 && filterable.every(isFilteredOut);
            elements.forEach(element => this.toggleClass('hidden-with-filter', hidden, element));
        });
        (this._subheaders || []).forEach(subheader => {
            const filterable = subheader.relativeElements.filter(element => element.hasAttribute('filterable'));
            const hidden = filterable.length > 0 && filterable.every(isFilteredOut);
            this.toggleClass('hidden-with-filter', hidden, subheader);
            if (!hidden && this.filter) {
                subheader.open();
            }
        });
    }

    // Per-element filtering for auto-flow mode: the shadow root holds a flat list of slotted editors — no rows or subheaders — so each filterable editor is simply hidden on its own (and reflows out, leaving no gaps).
    _filterAutoFlow () {
        this.componentsToLayout.forEach(slotName => {
            const editor = this.slottedElements[slotName];
            if (editor.hasAttribute('filterable')) {
                this.toggleClass('hidden-with-filter', this.filter && !this.filter(editor), editor);
            }
        });
    }
}

customElements.define('tg-grid-layout', TgGridLayout);
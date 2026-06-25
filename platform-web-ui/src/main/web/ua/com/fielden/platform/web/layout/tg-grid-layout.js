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
        // Reset the per-row index of placed grid items; row-based filtering is rebuilt from it below.
        this._rowElements = {};

        //3. Apply styles to container
        // A subheader indentation reserves an implicit leading "gutter" column, but only when the layout actually has a subheader.
        const hasSubheader = (layout.cells || []).some(cell => cell.widget && cell.widget.indexOf(Widgets.SUBHEADER) === 0);
        const indent = hasSubheader ? layout.subheaderIndentation : null;
        this._applyContainer(layout, indent);

        const columns = layout.columns || [];

        // 3a. Auto-tracking columns — repeat(auto-fit|auto-fill, …) — produce a browser-determined number of tracks,
        // so coordinate-based placement does not apply: every editor auto-flows across the generated tracks, reflowing on resize.
        // Explicit cells and rows are not used in this mode. Per-column-index styling is likewise meaningless, so the styles
        // declared on the column track(s) are applied uniformly to every cell.
        if (columns.some(track => typeof track.repeat === 'string')) {
            // Auto-flow mode has no fixed rows — editors reflow across browser-determined tracks — so filtering stays per-element: hiding one reflows the rest, leaving no gaps.
            this._autoFlow = true;
            const autoCellStyle = columns.reduce((style, track) => Object.assign(style, track.style || {}), {});
            this.componentsToLayout.forEach(slotName => {
                const slot = this._createCellElement(slotName);
                this._applyStyles(this.slottedElements[slotName], autoCellStyle);
                this.shadowRoot.appendChild(slot);
                this.appendedElements.push(slot);
            });
            this._setCurrentLayout(layout);
            this._filterLayout(this.filter);
            this.fire('layout-finished', this);
            return;
        }
        // Fixed-track mode: editors are placed at explicit rows, so filtering is row-based (all-or-nothing per row).
        this._autoFlow = false;
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
                let placed = null;   // the element appended to the shadow root — a bare <slot> for an editor cell
                let target = null;   // the grid item that receives placement and styles — the slotted editor itself for editor cells
                let subheader = false;
                if (cell) {
                    remainingCells.delete(key);
                    const colSpan = cell.colSpan === 'all' ? columnCount : (cell.colSpan || 1);
                    const rowSpan = cell.rowSpan || 1;
                    markOccupied(occupied, row, col, colSpan, rowSpan);
                    if (cell.widget === Widgets.SKIP) {
                        placed = target = document.createElement('div');
                    } else if (cell.widget && cell.widget.indexOf(Widgets.SUBHEADER) === 0) {
                        placed = target = this._createSubheader(cell.widget);
                        subheader = true;
                    } else if (cell.widget && cell.widget.indexOf(Widgets.HTML) === 0) {
                        placed = target = this._createHtmlElement(cell.widget);
                    } else {
                        const slotName = cell.boundSlot || pool.shift();
                        placed = this._createCellElement(slotName);
                        target = slotName ? this.slottedElements[slotName] : placed;
                    }
                    this._placeItem(target, this._gridColumn(col, colSpan, cell.colSpan === 'all', subheader, indent), row, col, rowSpan, columnStyles, rowStyles, cell.style);
                } else if (pool.length > 0) {
                    const slotName = pool.shift();
                    placed = this._createCellElement(slotName);
                    target = this.slottedElements[slotName];
                    markOccupied(occupied, row, col, 1, 1);
                    this._placeItem(target, this._gridColumn(col, 1, false, false, indent), row, col, 1, columnStyles, rowStyles, null);
                }
                if (placed) {
                    if (subheader) {
                        currentSubheader = target;
                        this._subheaders.push(target);
                    } else {
                        if (currentSubheader) {
                            currentSubheader.addRelativeElement(target);
                        }
                        // Index every non-subheader grid item by its row, so row-based filtering can hide or reveal the row as a whole.
                        (this._rowElements[row] = this._rowElements[row] || []).push(target);
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
        if (!this.currentLayout) {
            return;
        }
        if (this._autoFlow) {
            // No fixed rows in auto-flow mode — filter per element (hidden items reflow out, leaving no gaps).
            this._filterElement(this.shadowRoot);
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

    // Per-element filtering for auto-flow mode: recursively toggles `hidden-with-filter` according to `this.filter`.
    // Fixed-track layouts filter by row instead (see `_filterByRow`); auto-flow has no fixed rows and reflows hidden items out, so each filterable element is hidden on its own.
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

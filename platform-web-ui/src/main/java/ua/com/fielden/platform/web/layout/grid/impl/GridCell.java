package ua.com.fielden.platform.web.layout.grid.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.fielden.platform.web.layout.grid.IGridCell;

/// Implementation of a single non-conforming cell of a grid layout, placed at an explicit `(row, column)`.
///
/// A cell is one of three kinds:
/// an ordinary cell (configures the editor that auto-flows to its position, or an explicitly bound editor),
/// a skip (an empty placeholder cell),
/// or a subheader (an inserted, optionally collapsible section title that spans all columns by default).
///
public class GridCell implements IGridCell {

    enum Kind {
        CELL, SKIP, SUBHEADER
    }

    /// Sentinel for [#colSpan] meaning "span all columns" (`grid-column: 1 / -1`).
    ///
    static final int SPAN_ALL = -1;

    private final int row;
    private final int col;
    private final Kind kind;

    private Integer colSpan;
    private Integer rowSpan;

    private String title;
    private boolean collapsible;
    private boolean open;

    private String selectAttribute;
    private String selectValue;

    private final Map<String, String> styles = new LinkedHashMap<>();

    private GridCell(final int row, final int col, final Kind kind) {
        this.row = row;
        this.col = col;
        this.kind = kind;
    }

    public static GridCell cell(final int row, final int col) {
        return new GridCell(row, col, Kind.CELL);
    }

    public static GridCell skip(final int row, final int col) {
        return new GridCell(row, col, Kind.SKIP);
    }

    public static GridCell subheader(final int row, final String title) {
        return subheaderOf(row, title, false, true);
    }

    public static GridCell subheaderOpen(final int row, final String title) {
        return subheaderOf(row, title, true, true);
    }

    public static GridCell subheaderClosed(final int row, final String title) {
        return subheaderOf(row, title, true, false);
    }

    private static GridCell subheaderOf(final int row, final String title, final boolean collapsible, final boolean open) {
        final GridCell cell = new GridCell(row, 1, Kind.SUBHEADER);
        cell.title = title;
        cell.collapsible = collapsible;
        cell.open = open;
        cell.colSpan = SPAN_ALL;
        return cell;
    }

    @Override
    public GridCell spanAllCols() {
        colSpan = SPAN_ALL;
        return this;
    }

    @Override
    public GridCell spanCols(final int columns) {
        colSpan = columns;
        return this;
    }

    @Override
    public GridCell spanRows(final int rows) {
        rowSpan = rows;
        return this;
    }

    @Override
    public GridCell span(final int columns, final int rows) {
        colSpan = columns;
        rowSpan = rows;
        return this;
    }

    @Override
    public GridCell justify(final String value) {
        styles.put("justify-self", value);
        return this;
    }

    @Override
    public GridCell align(final String value) {
        styles.put("align-self", value);
        return this;
    }

    @Override
    public GridCell style(final String property, final String value) {
        styles.put(property, value);
        return this;
    }

    @Override
    public GridCell select(final String attribute, final String value) {
        this.selectAttribute = attribute;
        this.selectValue = value;
        return this;
    }

    /// Renders this cell as a JavaScript object literal, e.g. `{row:4,col:1,colSpan:"all",widget:"subheader-open:Asset",style:{"padding-left":"0"}}`.
    ///
    String render() {
        final StringBuilder sb = new StringBuilder("{row:").append(row).append(",col:").append(col);
        if (colSpan != null) {
            if (colSpan == SPAN_ALL) {
                sb.append(",colSpan:\"all\"");
            } else if (colSpan > 1) {
                sb.append(",colSpan:").append(colSpan);
            }
        }
        if (rowSpan != null && rowSpan > 1) {
            sb.append(",rowSpan:").append(rowSpan);
        }
        if (kind == Kind.SKIP) {
            sb.append(",widget:\"skip\"");
        } else if (kind == Kind.SUBHEADER) {
            sb.append(",widget:\"").append(subheaderKeyword()).append(":").append(title).append("\"");
        }
        if (selectAttribute != null) {
            sb.append(",select:\"").append(selectAttribute).append("=").append(selectValue).append("\"");
        }
        if (!styles.isEmpty()) {
            sb.append(",style:").append(GridStyles.object(styles));
        }
        return sb.append("}").toString();
    }

    private String subheaderKeyword() {
        if (!collapsible) {
            return "subheader";
        }
        return open ? "subheader-open" : "subheader-closed";
    }
}
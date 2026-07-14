package ua.com.fielden.platform.web.layout.grid.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.fielden.platform.web.layout.grid.IGridCell;
import ua.com.fielden.platform.web.layout.grid.ISubheader;
import ua.com.fielden.platform.web.layout.grid.exceptions.GridLayoutConfigurationException;

import static ua.com.fielden.platform.web.layout.grid.impl.GridStyles.escape;

/// Implementation of a single non-conforming cell of a grid layout, placed at an explicit `(row, column)`.
///
/// A cell is one of four kinds:
/// an ordinary cell (configures the editor that auto-flows to its position, or an explicitly bound editor),
/// a skip (an empty placeholder cell),
/// a subheader (an inserted, optionally collapsible section title that always spans all columns),
/// or an inline `html` snippet (stamped with the layout's `context`).
///
public class GridCell implements IGridCell, ISubheader {

    enum Kind {
        CELL, SKIP, SUBHEADER, HTML
    }

    /// Sentinel for [#colSpan] meaning "span all columns" (`grid-column: 1 / -1`).
    ///
    static final int SPAN_ALL = -1;

    public static final String
        ERR_INVALID_COLUMN_SPAN = "Column span must be at least 1, but was %s.",
        ERR_INVALID_ROW_SPAN = "Row span must be at least 1, but was %s.";

    /// The HTML attribute that property-editor elements expose to carry the name of the property they are bound to.
    /// Editors set it through `AbstractWidget`, and a few hand-written insertion-point masters set it directly; the `tg-grid-layout` client matches on it to place an editor into a cell.
    /// [#withProp(CharSequence)] uses it to bind a cell to the editor of a given property.
    /// This is a shared client-side contract rather than a value owned here, so it must stay in step with the emitter side and the client if it is ever renamed.
    ///
    private static final String PROPERTY_NAME_ATTRIBUTE = "property-name";

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

    private String html;

    private final Map<String, String> styles = new LinkedHashMap<>();

    private GridCell(final int row, final int col, final Kind kind) {
        this.row = row;
        this.col = col;
        this.kind = kind;
    }

    int row() {
        return row;
    }

    int col() {
        return col;
    }

    /// The leftmost column this cell occupies — column 1 for a `spanAllCols` cell, otherwise its declared column.
    ///
    int firstColumn() {
        return colSpan != null && colSpan == SPAN_ALL ? 1 : col;
    }

    /// The number of columns this cell occupies — all `columnCount` of them for a `spanAllCols` cell, otherwise its declared column span (default 1).
    ///
    int occupiedColumns(final int columnCount) {
        return colSpan != null && colSpan == SPAN_ALL ? columnCount : (colSpan != null && colSpan > 1 ? colSpan : 1);
    }

    /// The number of rows this cell occupies — its declared row span (default 1).
    ///
    int occupiedRows() {
        return rowSpan != null && rowSpan > 1 ? rowSpan : 1;
    }

    /// Whether this cell is a subheader.
    ///
    boolean isSubheader() {
        return kind == Kind.SUBHEADER;
    }

    /// Folds the given declarations into this cell's own styles as defaults: a property the cell already sets keeps its value (the cell wins), an absent property is appended in the given map's order.
    /// Used to apply container-level subheader-style defaults while a per-subheader `style(...)` overrides them; idempotent, so a repeated application is harmless.
    ///
    void addDefaultStyles(final Map<String, String> defaults) {
        defaults.forEach(styles::putIfAbsent);
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

    public static GridCell html(final int row, final int col, final String html) {
        final GridCell cell = new GridCell(row, col, Kind.HTML);
        cell.html = html;
        return cell;
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
        if (columns < 1) {
            throw new GridLayoutConfigurationException(ERR_INVALID_COLUMN_SPAN.formatted(columns));
        }
        colSpan = columns;
        return this;
    }

    @Override
    public GridCell spanRows(final int rows) {
        if (rows < 1) {
            throw new GridLayoutConfigurationException(ERR_INVALID_ROW_SPAN.formatted(rows));
        }
        rowSpan = rows;
        return this;
    }

    @Override
    public GridCell span(final int columns, final int rows) {
        spanCols(columns);
        spanRows(rows);
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

    @Override
    public GridCell withProp(final CharSequence propertyName) {
        return select(PROPERTY_NAME_ATTRIBUTE, propertyName.toString());
    }

    /// Renders this cell as a JavaScript object literal, e.g. `{row:4,col:1,colSpan:"all",widget:"subheader-open:Asset",style:{"padding-left":"0"}}`.
    ///
    String render() {
        final StringBuilder sb = new StringBuilder("{row:").append(row).append(",col:").append(firstColumn());
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
        final String widget = widget();
        if (widget != null) {
            sb.append(",widget:\"").append(escape(widget)).append("\"");
        }
        if (selectAttribute != null) {
            sb.append(",select:\"").append(escape(selectAttribute + "=" + selectValue)).append("\"");
        }
        if (!styles.isEmpty()) {
            sb.append(",style:").append(GridStyles.object(styles));
        }
        return sb.append("}").toString();
    }

    /// The widget descriptor for this cell, or `null` for an ordinary editor cell.
    ///
    private String widget() {
        return switch (kind) {
            case SKIP -> "skip";
            case SUBHEADER -> subheaderKeyword() + ":" + title;
            case HTML -> "html:" + html;
            case CELL -> null;
        };
    }

    private String subheaderKeyword() {
        if (!collapsible) {
            return "subheader";
        }
        return open ? "subheader-open" : "subheader-closed";
    }
}
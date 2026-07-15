package ua.com.fielden.platform.web.layout;

import ua.com.fielden.platform.web.layout.grid.IGridElement;
import ua.com.fielden.platform.web.layout.grid.exceptions.GridLayoutConfigurationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static ua.com.fielden.platform.web.layout.grid.AutoRepeat.AUTO_FILL;
import static ua.com.fielden.platform.web.layout.grid.AutoRepeat.AUTO_FIT;
import static ua.com.fielden.platform.web.layout.grid.impl.GridLayoutBuilder.cell;
import static ua.com.fielden.platform.web.layout.grid.impl.GridLayoutBuilder.content;
import static ua.com.fielden.platform.web.layout.grid.impl.GridLayoutBuilder.grid;
import static ua.com.fielden.platform.web.layout.grid.impl.GridLayoutBuilder.hidden;
import static ua.com.fielden.platform.web.layout.grid.impl.GridLayoutBuilder.html;
import static ua.com.fielden.platform.web.layout.grid.impl.GridLayoutBuilder.skip;
import static ua.com.fielden.platform.web.layout.grid.impl.GridLayoutBuilder.subheaderOpen;
import static ua.com.fielden.platform.web.layout.grid.impl.GridLayoutBuilder.ERR_COLUMN_OUT_OF_BOUNDS;
import static ua.com.fielden.platform.web.layout.grid.impl.GridLayoutBuilder.ERR_NON_POSITIVE_ROW;
import static ua.com.fielden.platform.web.layout.grid.impl.GridLayoutBuilder.ERR_OVERLAPPING_CELLS;
import static ua.com.fielden.platform.web.layout.grid.impl.GridLayoutBuilder.ERR_ROW_OUT_OF_BOUNDS;
import static ua.com.fielden.platform.web.layout.grid.impl.GridLayoutBuilder.ERR_COLUMN_SPAN_OUT_OF_BOUNDS;
import static ua.com.fielden.platform.web.layout.grid.impl.GridLayoutBuilder.ERR_ROW_SPAN_OUT_OF_BOUNDS;
import static ua.com.fielden.platform.web.layout.grid.impl.GridLayoutBuilder.ERR_UNSUPPORTED_ELEMENT;
import static ua.com.fielden.platform.web.layout.grid.impl.GridCell.ERR_INVALID_COLUMN_SPAN;
import static ua.com.fielden.platform.web.layout.grid.impl.GridCell.ERR_INVALID_ROW_SPAN;
import static ua.com.fielden.platform.web.layout.grid.impl.GridCell.ERR_SPAN_ALL_COLUMN;

import org.junit.Test;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;

/// Tests for the grid layout fluent API and its serialisation to the `tg-grid-layout` wire format, and for the [GridLayout] per-breakpoint manager.
///
public class GridLayoutTest {

    @Test
    public void import_path_points_to_the_grid_layout_client() {
        assertEquals("layout/tg-grid-layout", new GridLayout("test").importPath());
    }

    @Test
    public void manager_renders_media_attributes_and_emits_layout_assignment() {
        final GridLayout gridLayout = new GridLayout("test");
        final String desktop = grid()
                .content(content().withGaps("0px", "20px"))
                .columns()
                    .addColumn("1fr")
                    .addColumn("1fr")
                .elements(skip(1, 1))
                .toString();
        gridLayout.whenMedia(Device.DESKTOP).set(desktop);

        final DomElement rendered = gridLayout.render();
        assertEquals("the number of children is incorrect", 0, rendered.childCount());
        assertEquals("the desktop layout constraint is incorrect", "[[_desktopLayout_test]]", rendered.getAttr("when-desktop").value);
        assertEquals("the emitted layout assignment is incorrect", "this._desktopLayout_test = " + desktop + ";\n", gridLayout.code().toString());
    }

    @Test
    public void simple_layout_serialises_to_the_expected_wire_string() {
        final String wire = grid()
                .content(content().withGaps("0px", "20px").style("padding", "20px"))
                .columns()
                    .addColumn("1fr").style("padding-left", "32px")
                    .addColumn("1fr")
                .elements(
                    cell(2, 1).spanAllCols(),
                    subheaderOpen(3, "Asset").style("padding-left", "0"),
                    skip(4, 2))
                .toString();

        assertEquals(
                "{container:{\"row-gap\":\"0px\",\"column-gap\":\"20px\",\"padding\":\"20px\"},"
                + "columns:[{size:\"1fr\",style:{\"padding-left\":\"32px\"}},{size:\"1fr\"}],"
                + "cells:[{row:2,col:1,colSpan:\"all\"},"
                + "{row:3,col:1,colSpan:\"all\",widget:\"subheader-open:Asset\",style:{\"padding-left\":\"0\"}},"
                + "{row:4,col:2,widget:\"skip\"}]}",
                wire);
    }

    @Test
    public void row_alignment_is_emulated_onto_its_track() {
        final String wire = grid()
                .content(content().withGaps("10px", "10px"))
                .columns()
                    .addColumn("1fr")
                    .addColumn("auto")
                .rows()
                    .addRow("auto").align("center")
                    .addRow("min-content")
                .elements(
                    cell(1, 1).align("start").style("padding", "4px"),
                    cell(2, 1).spanCols(2))
                .toString();

        assertEquals(
                "{container:{\"row-gap\":\"10px\",\"column-gap\":\"10px\"},"
                + "columns:[{size:\"1fr\"},{size:\"auto\"}],"
                + "rows:[{size:\"auto\",style:{\"align-self\":\"center\"}},{size:\"min-content\"}],"
                + "cells:[{row:1,col:1,style:{\"align-self\":\"start\",\"padding\":\"4px\"}},"
                + "{row:2,col:1,colSpan:2}]}",
                wire);
    }

    @Test
    public void work_order_main_layout_serialises_to_the_expected_wire_string() {
        final String wire = grid()
                .content(content()
                    .withGaps("0px", "20px")
                    .style("padding", "20px"))
                .columns()
                    .addColumn("1fr").style("padding-left", "32px")
                    .addColumn("1fr")
                    .addColumn("1fr")
                .rows()
                    .addRow("auto").repeat(22)
                .elements(
                    cell(2, 1).spanAllCols(),
                    subheaderOpen(4, "Asset").style("padding-left", "0"),
                    subheaderOpen(6, "Preventive Maintenance").style("padding-left", "0"),
                    skip(9, 3),
                    subheaderOpen(10, "Allocation").style("padding-left", "0"),
                    subheaderOpen(13, "Date and Time").style("padding-left", "0"),
                    skip(15, 3),
                    subheaderOpen(18, "Financials and Accounting").style("padding-left", "0"),
                    subheaderOpen(21, "Notes").style("padding-left", "0"),
                    cell(22, 1).spanAllCols())
                .toString();

        assertEquals(
                "{container:{\"row-gap\":\"0px\",\"column-gap\":\"20px\",\"padding\":\"20px\"},"
                + "columns:[{size:\"1fr\",style:{\"padding-left\":\"32px\"}},{size:\"1fr\"},{size:\"1fr\"}],"
                + "rows:[{size:\"auto\",repeat:22}],"
                + "cells:[{row:2,col:1,colSpan:\"all\"},"
                + "{row:4,col:1,colSpan:\"all\",widget:\"subheader-open:Asset\",style:{\"padding-left\":\"0\"}},"
                + "{row:6,col:1,colSpan:\"all\",widget:\"subheader-open:Preventive Maintenance\",style:{\"padding-left\":\"0\"}},"
                + "{row:9,col:3,widget:\"skip\"},"
                + "{row:10,col:1,colSpan:\"all\",widget:\"subheader-open:Allocation\",style:{\"padding-left\":\"0\"}},"
                + "{row:13,col:1,colSpan:\"all\",widget:\"subheader-open:Date and Time\",style:{\"padding-left\":\"0\"}},"
                + "{row:15,col:3,widget:\"skip\"},"
                + "{row:18,col:1,colSpan:\"all\",widget:\"subheader-open:Financials and Accounting\",style:{\"padding-left\":\"0\"}},"
                + "{row:21,col:1,colSpan:\"all\",widget:\"subheader-open:Notes\",style:{\"padding-left\":\"0\"}},"
                + "{row:22,col:1,colSpan:\"all\"}]}",
                wire);
    }

    @Test
    public void addColumn_without_size_defaults_to_one_fraction() {
        final String wire = grid()
                .columns()
                    .addColumn()
                    .addColumn()
                    .addColumn()
                .elements(skip(1, 1))
                .toString();

        assertEquals(
                "{columns:[{size:\"1fr\"},{size:\"1fr\"},{size:\"1fr\"}],"
                + "cells:[{row:1,col:1,widget:\"skip\"}]}",
                wire);
    }

    @Test
    public void addRow_without_size_defaults_to_auto() {
        assertEquals(
                "{columns:[{size:\"1fr\"}],rows:[{size:\"auto\"}],cells:[]}",
                grid().columns().addColumn().rows().addRow().layout());
    }

    @Test
    public void html_cell_serialises_as_a_widget_and_escapes_the_snippet() {
        assertEquals(
                "{columns:[{size:\"1fr\"}],cells:[{row:1,col:1,widget:\"html:<b>note</b>\"}]}",
                grid().columns().addColumn().elements(html(1, 1, "<b>note</b>")).toString());

        // double quotes in the snippet are escaped for the JS wire string
        assertEquals(
                "{columns:[{size:\"1fr\"}],cells:[{row:1,col:1,widget:\"html:<i class=\\\"x\\\"></i>\"}]}",
                grid().columns().addColumn().elements(html(1, 1, "<i class=\"x\"></i>")).toString());
    }

    @Test
    public void elements_are_optional_and_the_chain_is_itself_a_configuration() {
        // after columns — the declared column tracks alone form a complete, auto-flowing layout
        assertEquals(
                "{columns:[{size:\"1fr\"},{size:\"1fr\"}],cells:[]}",
                grid().columns().addColumn("1fr").addColumn("1fr").layout());

        // after rows — likewise a terminal configuration without an explicit elements(...) call
        assertEquals(
                "{columns:[{size:\"1fr\"}],rows:[{size:\"auto\"}],cells:[]}",
                grid().columns().addColumn().rows().addRow("auto").layout());
    }

    @Test
    public void hidden_elements_are_kept_out_of_the_grid_and_serialised_separately() {
        // a hidden element is not a positioned cell — it serialises into the top-level `hidden` array, leaving `cells` empty
        assertEquals(
                "{columns:[{size:\"1fr\"}],cells:[],hidden:[\"property-name=secret\"]}",
                grid().columns().addColumn().elements(hidden("secret")).layout());
        // hidden elements coexist with positioned cells and preserve declaration order
        assertEquals(
                "{columns:[{size:\"1fr\"},{size:\"1fr\"}],cells:[{row:1,col:1,widget:\"skip\"}],hidden:[\"property-name=a\",\"property-name=b\"]}",
                grid().columns().addColumn().addColumn().elements(skip(1, 1), hidden("a"), hidden("b")).layout());
    }

    @Test
    public void auto_tracking_columns_serialise_with_a_repeat_keyword() {
        // auto-fit — the browser fits as many tracks of the given size as it can; editors auto-flow
        assertEquals(
                "{columns:[{size:\"minmax(220px, 1fr)\",repeat:\"auto-fit\"}],cells:[]}",
                grid().columns().addAutoColumn("minmax(220px, 1fr)", AUTO_FIT).layout());

        // auto-fill — like auto-fit, but empty trailing tracks are preserved
        assertEquals(
                "{columns:[{size:\"minmax(180px, 1fr)\",repeat:\"auto-fill\"}],cells:[]}",
                grid().columns().addAutoColumn("minmax(180px, 1fr)", AUTO_FILL).layout());

        // a style declared on an auto-tracked column is serialised — the client applies it uniformly to every cell
        assertEquals(
                "{columns:[{size:\"minmax(220px, 1fr)\",repeat:\"auto-fit\",style:{\"padding\":\"8px\"}}],cells:[]}",
                grid().columns().addAutoColumn("minmax(220px, 1fr)", AUTO_FIT).style("padding", "8px").layout());

        // `justify` on an auto-tracked column is emitted as a `justify-self` declaration the client applies to every cell
        assertEquals(
                "{columns:[{size:\"minmax(220px, 1fr)\",repeat:\"auto-fit\",style:{\"justify-self\":\"center\"}}],cells:[]}",
                grid().columns().addAutoColumn("minmax(220px, 1fr)", AUTO_FIT).justify("center").layout());
    }

    @Test
    public void subheader_indentation_serialises_as_a_top_level_field() {
        final String wire = grid()
                .content(content().withGaps("0px", "20px").withSubheaderIndentation("32px"))
                .columns()
                    .addColumn("1fr")
                    .addColumn("1fr")
                .elements(
                    subheaderOpen(1, "Asset"),
                    skip(2, 2))
                .toString();

        assertEquals(
                "{container:{\"row-gap\":\"0px\",\"column-gap\":\"20px\"},"
                + "subheaderIndentation:\"32px\","
                + "columns:[{size:\"1fr\"},{size:\"1fr\"}],"
                + "cells:[{row:1,col:1,colSpan:\"all\",widget:\"subheader-open:Asset\"},"
                + "{row:2,col:2,widget:\"skip\"}]}",
                wire);
    }

    @Test
    public void with_prop_binds_an_editor_by_its_property_name_attribute() {
        // withProp(name) is shorthand for select("property-name", name)
        assertEquals(
                "{columns:[{size:\"1fr\"}],cells:[{row:2,col:1,select:\"property-name=desc\"}]}",
                grid().columns().addColumn().elements(cell(2, 1).withProp("desc")).toString());

        // it composes with further cell configuration, e.g. spanning all columns
        assertEquals(
                "{columns:[{size:\"1fr\"},{size:\"1fr\"}],cells:[{row:3,col:1,colSpan:\"all\",select:\"property-name=bio\"}]}",
                grid().columns().addColumn().addColumn().elements(cell(3, 1).withProp("bio").spanAllCols()).toString());
    }

    @Test
    public void a_cell_with_a_column_beyond_the_declared_columns_is_rejected() {
        final var ex = assertThrows(GridLayoutConfigurationException.class, () ->
                grid().columns().addColumn().addColumn().elements(cell(2, 3).withProp("desc")));
        assertEquals(ERR_COLUMN_OUT_OF_BOUNDS.formatted(2, 3, 2), ex.getMessage());
    }

    @Test
    public void a_cell_with_a_non_positive_column_is_rejected() {
        final var ex = assertThrows(GridLayoutConfigurationException.class, () ->
                grid().columns().addColumn().addColumn().elements(skip(1, 0)));
        assertEquals(ERR_COLUMN_OUT_OF_BOUNDS.formatted(1, 0, 2), ex.getMessage());
    }

    @Test
    public void a_cell_with_a_non_positive_row_is_rejected() {
        final var ex = assertThrows(GridLayoutConfigurationException.class, () ->
                grid().columns().addColumn().elements(skip(0, 1)));
        assertEquals(ERR_NON_POSITIVE_ROW.formatted(1, 0), ex.getMessage());
    }

    @Test
    public void a_cell_with_a_row_beyond_the_declared_explicit_rows_is_rejected() {
        final var ex = assertThrows(GridLayoutConfigurationException.class, () ->
                grid().columns().addColumn().rows().addRow("auto").addRow("auto").elements(skip(3, 1)));
        assertEquals(ERR_ROW_OUT_OF_BOUNDS.formatted(3, 1, 2), ex.getMessage());
    }

    @Test
    public void in_bounds_cells_are_accepted_and_implicit_rows_have_no_upper_bound() {
        // within the declared 2 columns — no exception
        grid().columns().addColumn().addColumn().elements(cell(2, 2).withProp("desc"));
        // a high row is fine when rows are implicit (no explicit rows())
        grid().columns().addColumn().elements(cell(100, 1).withProp("desc"));
        // non-overlapping spans are accepted: a full-width row 1, then a cell in row 2
        grid().columns().addColumn().addColumn().elements(cell(1, 1).spanCols(2), skip(2, 1));
    }

    @Test
    public void a_cell_overlapping_an_earlier_cells_column_span_is_rejected() {
        // the first cell spans columns 1-2 of row 1; the skip at (1,2) lands inside that span
        final var ex = assertThrows(GridLayoutConfigurationException.class, () ->
                grid().columns().addColumn().addColumn().elements(cell(1, 1).spanCols(2), skip(1, 2)));
        assertEquals(ERR_OVERLAPPING_CELLS.formatted(1, 2, 1, 1), ex.getMessage());
    }

    @Test
    public void a_cell_overlapping_an_earlier_cells_row_span_is_rejected() {
        // the first cell spans rows 1-2 of column 1; the skip at (2,1) lands inside that span
        final var ex = assertThrows(GridLayoutConfigurationException.class, () ->
                grid().columns().addColumn().elements(cell(1, 1).spanRows(2), skip(2, 1)));
        assertEquals(ERR_OVERLAPPING_CELLS.formatted(2, 1, 1, 1), ex.getMessage());
    }

    @Test
    public void a_cell_in_a_full_width_subheaders_row_is_rejected() {
        // the subheader at row 1 spans every column; a cell in row 1 overlaps it
        final var ex = assertThrows(GridLayoutConfigurationException.class, () ->
                grid().columns().addColumn().addColumn().elements(subheaderOpen(1, "Section"), skip(1, 1)));
        assertEquals(ERR_OVERLAPPING_CELLS.formatted(1, 1, 1, 1), ex.getMessage());
    }

    @Test
    public void developer_values_are_escaped_for_the_javascript_wire_format() {
        // a double quote in a CSS value is backslash-escaped so the generated JS string literal stays valid
        assertEquals(
                "{columns:[{size:\"1fr\",style:{\"content\":\"\\\"x\\\"\"}}],cells:[]}",
                grid().columns().addColumn().style("content", "\"x\"").layout());
    }

    @Test
    public void a_non_positive_column_span_is_rejected() {
        assertEquals(ERR_INVALID_COLUMN_SPAN.formatted(0),
                assertThrows(GridLayoutConfigurationException.class, () -> cell(1, 1).spanCols(0)).getMessage());
        // -1 is the internal span-all sentinel — it must be rejected, not silently treated as span-all
        assertEquals(ERR_INVALID_COLUMN_SPAN.formatted(-1),
                assertThrows(GridLayoutConfigurationException.class, () -> cell(1, 1).spanCols(-1)).getMessage());
        // span(columns, rows) validates the column span too
        assertEquals(ERR_INVALID_COLUMN_SPAN.formatted(0),
                assertThrows(GridLayoutConfigurationException.class, () -> cell(1, 1).span(0, 2)).getMessage());
    }

    @Test
    public void a_non_positive_row_span_is_rejected() {
        assertEquals(ERR_INVALID_ROW_SPAN.formatted(0),
                assertThrows(GridLayoutConfigurationException.class, () -> cell(1, 1).spanRows(0)).getMessage());
        // span(columns, rows) validates the row span too
        assertEquals(ERR_INVALID_ROW_SPAN.formatted(0),
                assertThrows(GridLayoutConfigurationException.class, () -> cell(1, 1).span(2, 0)).getMessage());
    }

    @Test
    public void a_span_all_cell_declared_off_column_1_is_rejected() {
        // spanAllCols() always anchors at column 1, so declaring it at another column is a mistake, not a silent no-op
        assertEquals(ERR_SPAN_ALL_COLUMN.formatted(2),
                assertThrows(GridLayoutConfigurationException.class, () -> cell(1, 2).spanAllCols()).getMessage());
        // column 1 is accepted
        cell(1, 1).spanAllCols();
    }

    @Test
    public void a_foreign_grid_element_is_rejected() {
        // elements(...) accepts IGridElement, but the only concrete kind is GridCell (from the factories);
        // a foreign implementation is rejected with a clear message rather than a raw ClassCastException.
        final IGridElement foreign = new IGridElement() { };
        assertEquals(ERR_UNSUPPORTED_ELEMENT.formatted(foreign.getClass().getName()),
                assertThrows(GridLayoutConfigurationException.class,
                        () -> grid().columns().addColumn().elements(foreign)).getMessage());
    }

    @Test
    public void a_cell_spanning_past_the_last_column_is_rejected() {
        // anchored at column 2 of a 2-column grid and spanning 3 columns reaches column 4, which would expand the grid with implicit tracks
        final var ex = assertThrows(GridLayoutConfigurationException.class, () ->
                grid().columns().addColumn().addColumn().elements(cell(1, 2).spanCols(3)));
        assertEquals(ERR_COLUMN_SPAN_OUT_OF_BOUNDS.formatted(1, 2, 4, 2), ex.getMessage());
    }

    @Test
    public void a_cell_spanning_past_the_last_explicit_row_is_rejected() {
        final var ex = assertThrows(GridLayoutConfigurationException.class, () ->
                grid().columns().addColumn().rows().addRow("auto").addRow("auto").elements(cell(1, 1).spanRows(3)));
        assertEquals(ERR_ROW_SPAN_OUT_OF_BOUNDS.formatted(1, 1, 3, 2), ex.getMessage());
    }

    @Test
    public void a_cell_spanning_exactly_to_the_last_column_is_accepted() {
        assertEquals(
                "{columns:[{size:\"1fr\"},{size:\"1fr\"}],cells:[{row:1,col:1,colSpan:2}]}",
                grid().columns().addColumn().addColumn().elements(cell(1, 1).spanCols(2)).toString());
    }

    @Test
    public void an_image_cell_spanning_five_rows_builds_via_the_java_api() {
        // Example 5 of the change overview: two 1fr columns with an image pinned to column 2 spanning rows 2-6,
        // sized (absolutely positioned + object-fit) to fill its span without enlarging the rows.
        assertEquals(
                "{container:{\"row-gap\":\"12px\",\"column-gap\":\"20px\",\"align-items\":\"start\",\"padding\":\"20px\"},"
                + "columns:[{size:\"1fr\"},{size:\"1fr\"}],"
                + "cells:[{row:2,col:2,rowSpan:5,"
                + "widget:\"html:<img src='{{photo}}' style='position:absolute;inset:0;width:100%;height:100%;object-fit:cover'>\","
                + "style:{\"align-self\":\"stretch\",\"position\":\"relative\",\"min-height\":\"0\",\"overflow\":\"hidden\"}}]}",
                grid()
                    .content(content().withGaps("12px", "20px").alignItems("start").style("padding", "20px"))
                    .columns()
                        .addColumn()
                        .addColumn()
                    .elements(
                        html(2, 2, "<img src='{{photo}}' style='position:absolute;inset:0;width:100%;height:100%;object-fit:cover'>")
                            .spanRows(5)
                            .style("align-self", "stretch")
                            .style("position", "relative")
                            .style("min-height", "0")
                            .style("overflow", "hidden"))
                    .toString());
    }

    @Test
    public void container_subheader_style_is_a_default_that_each_subheader_can_override() {
        final String wire = grid()
                .content(content()
                    .withGaps("0px", "20px")
                    .withSubheaderStyle("padding-left", "0")
                    .withSubheaderStyle("font-weight", "bold"))
                .columns()
                    .addColumn("1fr").style("padding-left", "32px")
                    .addColumn("1fr")
                .elements(
                    subheaderOpen(1, "Asset"),
                    subheaderOpen(2, "Details").style("padding-left", "8px"),
                    skip(3, 2))
                .toString();

        // Asset inherits both container defaults; Details keeps its own padding-left and inherits font-weight.
        // Container subheader styles do NOT appear in the container:{...} block -- they ride each subheader cell's style.
        assertEquals(
                "{container:{\"row-gap\":\"0px\",\"column-gap\":\"20px\"},"
                + "columns:[{size:\"1fr\",style:{\"padding-left\":\"32px\"}},{size:\"1fr\"}],"
                + "cells:[{row:1,col:1,colSpan:\"all\",widget:\"subheader-open:Asset\",style:{\"padding-left\":\"0\",\"font-weight\":\"bold\"}},"
                + "{row:2,col:1,colSpan:\"all\",widget:\"subheader-open:Details\",style:{\"padding-left\":\"8px\",\"font-weight\":\"bold\"}},"
                + "{row:3,col:2,widget:\"skip\"}]}",
                wire);
    }

    @Test
    public void container_subheader_style_affects_only_subheaders() {
        final String wire = grid()
                .content(content().withSubheaderStyle("padding-left", "0"))
                .columns()
                    .addColumn("1fr")
                    .addColumn("1fr")
                .elements(
                    cell(1, 1).withProp("desc"),
                    subheaderOpen(2, "Asset"),
                    skip(3, 2))
                .toString();

        // The ordinary cell and the skip are untouched; only the subheader carries the container default.
        // content holds no container styles, so there is no container:{...} block.
        assertEquals(
                "{columns:[{size:\"1fr\"},{size:\"1fr\"}],"
                + "cells:[{row:1,col:1,select:\"property-name=desc\"},"
                + "{row:2,col:1,colSpan:\"all\",widget:\"subheader-open:Asset\",style:{\"padding-left\":\"0\"}},"
                + "{row:3,col:2,widget:\"skip\"}]}",
                wire);
    }

    @Test
    public void subheader_indentation_and_subheader_style_combine_independently() {
        // subheaderIndentation is a top-level layout field (the gutter); withSubheaderStyle rides each subheader cell's style.
        // They are orthogonal: the indentation appears once at the top level, the style is folded into the subheader cell.
        final String wire = grid()
                .content(content()
                    .withGaps("0px", "20px")
                    .withSubheaderIndentation("32px")
                    .withSubheaderStyle("padding-left", "0")
                    .withSubheaderStyle("font-weight", "bold"))
                .columns()
                    .addColumn("1fr")
                    .addColumn("1fr")
                .elements(
                    subheaderOpen(1, "Asset"),
                    skip(2, 2))
                .toString();

        assertEquals(
                "{container:{\"row-gap\":\"0px\",\"column-gap\":\"20px\"},"
                + "subheaderIndentation:\"32px\","
                + "columns:[{size:\"1fr\"},{size:\"1fr\"}],"
                + "cells:[{row:1,col:1,colSpan:\"all\",widget:\"subheader-open:Asset\",style:{\"padding-left\":\"0\",\"font-weight\":\"bold\"}},"
                + "{row:2,col:2,widget:\"skip\"}]}",
                wire);
    }
}
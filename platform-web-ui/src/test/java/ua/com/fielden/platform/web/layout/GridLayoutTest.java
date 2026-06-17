package ua.com.fielden.platform.web.layout;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.web.layout.grid.impl.GridLayoutBuilder.cell;
import static ua.com.fielden.platform.web.layout.grid.impl.GridLayoutBuilder.content;
import static ua.com.fielden.platform.web.layout.grid.impl.GridLayoutBuilder.grid;
import static ua.com.fielden.platform.web.layout.grid.impl.GridLayoutBuilder.html;
import static ua.com.fielden.platform.web.layout.grid.impl.GridLayoutBuilder.skip;
import static ua.com.fielden.platform.web.layout.grid.impl.GridLayoutBuilder.subheaderOpen;

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
                    cell(2, 2).spanCols(2))
                .toString();

        assertEquals(
                "{container:{\"row-gap\":\"10px\",\"column-gap\":\"10px\"},"
                + "columns:[{size:\"1fr\"},{size:\"auto\"}],"
                + "rows:[{size:\"auto\",style:{\"align-self\":\"center\"}},{size:\"min-content\"}],"
                + "cells:[{row:1,col:1,style:{\"align-self\":\"start\",\"padding\":\"4px\"}},"
                + "{row:2,col:2,colSpan:2}]}",
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
}
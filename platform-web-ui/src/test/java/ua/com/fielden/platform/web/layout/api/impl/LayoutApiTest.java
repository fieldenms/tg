package ua.com.fielden.platform.web.layout.api.impl;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.cell;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.html;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutCellBuilder.layout;

import org.junit.Test;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;

public class LayoutApiTest {

    @Test
    public void test_whether_default_layout_direction_works() {
        final ContainerConfig layout = cell(
                cell(cell(cell().repeat(2).withGapBetweenCells(30)).repeat(2).withGapBetweenCells(20)).repeat(2).withGapBetweenCells(10));

        final String expectedLayout = "["
                + "[\"margin-bottom:10px\", [\"margin-right:20px\", [\"margin-bottom:30px\"], []], [[\"margin-bottom:30px\"], []]], "
                + "[[\"margin-right:20px\", [\"margin-bottom:30px\"], []], [[\"margin-bottom:30px\"], []]]"
                + "]";

        assertEquals(expectedLayout, layout.toString());
    }

    @Test
    public void test_whether_specified_layout_direction_works() {
        final FlexLayoutConfig vertical = layout().vertical().end();
        final FlexLayoutConfig horizontal = layout().horizontal().end();

        final ContainerConfig layout = cell(
                cell(cell(cell().repeat(2).withGapBetweenCells(30), horizontal).repeat(2).withGapBetweenCells(20), vertical).repeat(2).withGapBetweenCells(10), horizontal);

        final String expectedLayout = "[\"horizontal\", "
                + "[\"vertical\", \"margin-right:10px\", [\"horizontal\", \"margin-bottom:20px\", [\"margin-right:30px\"], []], [\"horizontal\", [\"margin-right:30px\"], []]], "
                + "[\"vertical\", [\"horizontal\", \"margin-bottom:20px\", [\"margin-right:30px\"], []], [\"horizontal\", [\"margin-right:30px\"], []]]"
                + "]";

        assertEquals(expectedLayout, layout.toString());
    }

    @Test
    public void test_whether_semi_specified_layout_direction_works() {
        final FlexLayoutConfig horizontal = layout().horizontal().end();

        final ContainerConfig layout = cell(
                cell(cell(cell().repeat(2).withGapBetweenCells(30), horizontal).repeat(2).withGapBetweenCells(20)).repeat(2).withGapBetweenCells(10), horizontal);

        final String expectedLayout = "[\"horizontal\", "
                + "[\"margin-right:10px\", [\"horizontal\", \"margin-right:20px\", [\"margin-right:30px\"], []], [\"horizontal\", [\"margin-right:30px\"], []]], "
                + "[[\"horizontal\", \"margin-right:20px\", [\"margin-right:30px\"], []], [\"horizontal\", [\"margin-right:30px\"], []]]"
                + "]";

        assertEquals(expectedLayout, layout.toString());
    }

    @Test
    public void test_whether_all_inline_widgets_works() {
        final ContainerConfig layout = cell(
                html(new DomElement("span").attr("src", "test_src").add(new InnerTextElement("This is test text"))).
                skip().
                select("prop", "name1").
                subheader("Subheader").
                subheaderClosed("Subheader closed").
                        subheaderOpen("Subheader open").withGapBetweenCells(20));

        final String expectedLayout = "["
                + "[\"html:<span src='test_src'>\nThis is test text\n</span>\", \"margin-bottom:20px\"], "
                + "[\"skip\", \"margin-bottom:20px\"], "
                + "[\"select:prop=name1\", \"margin-bottom:20px\"], "
                + "[\"subheader:Subheader\", \"margin-bottom:20px\"], "
                + "[\"subheader-closed:Subheader closed\", \"margin-bottom:20px\"], "
                + "[\"subheader-open:Subheader open\"]"
                + "]";

        assertEquals(expectedLayout, layout.toString());
    }

    @Test
    public void test_whether_styling_for_each_cell_works() {
        final FlexLayoutConfig empty = layout().end();
        final FlexLayoutConfig cell = layout().withClass("spec-class").withStyle("opacity", "0.3").withStyle("margin-bottom", "40px").
                horizontal().
                aroundJustified().
                centerAligned().
                flex(2).end();
        final ContainerConfig layout = cell(cell().cell(empty).layoutForEach(cell).withGapBetweenCells(20));

        final String expectedLayout = "[[\"spec-class\", \"horizontal\", \"around-justified\", \"center\", \"flex-2\", \"opacity:0.3\", \"margin-bottom:20px\"], []]";

        assertEquals(expectedLayout, layout.toString());
    }

}

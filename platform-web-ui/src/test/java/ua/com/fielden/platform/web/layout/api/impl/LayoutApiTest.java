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
    public void layout_direction_can_be_default() {
        final ContainerConfig layout = cell(
                cell(cell(cell().repeat(2).withGapBetweenCells(30)).repeat(2).withGapBetweenCells(20)).repeat(2).withGapBetweenCells(10));

        final String expectedLayout = "["
                + "[\"margin-bottom:10px\", [\"margin-right:20px\", [\"margin-bottom:30px\"], []], [[\"margin-bottom:30px\"], []]], "
                + "[[\"margin-right:20px\", [\"margin-bottom:30px\"], []], [[\"margin-bottom:30px\"], []]]"
                + "]";

        assertEquals(expectedLayout, layout.toString());
    }

    @Test
    public void layout_direction_can_be_specified() {
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
    public void layout_direction_can_be_semi_specified() {
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
    public void layout_can_have_inline_widgets() {
        final ContainerConfig layout = cell(
                html(new DomElement("span").attr("src", "test_src").add(new InnerTextElement("This is test text"))).
                html("<span src='raw_src'>This is raw html</span>").
                skip().
                select("prop", "name1").
                subheader("Subheader").
                subheaderClosed("Subheader closed").
                        subheaderOpen("Subheader open").withGapBetweenCells(20));

        final String expectedLayout = "["
                + "[\"html:<span src='test_src'>\nThis is test text\n</span>\", \"margin-bottom:20px\"], "
                + "[\"html:<span src='raw_src'>This is raw html</span>\", \"margin-bottom:20px\"], "
                + "[\"skip\", \"margin-bottom:20px\"], "
                + "[\"select:prop=name1\", \"margin-bottom:20px\"], "
                + "[\"subheader:Subheader\", \"margin-bottom:20px\"], "
                + "[\"subheader-closed:Subheader closed\", \"margin-bottom:20px\"], "
                + "[\"subheader-open:Subheader open\"]"
                + "]";

        assertEquals(expectedLayout, layout.toString());
    }

    @Test
    public void layout_can_be_styled() {
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

    @Test
    public void layout_can_be_composed_with_predefined_parts() {
        final FlexLayoutConfig centerCenter = layout().centerJustified().centerAligned().end();
        final FlexLayoutConfig flex = layout().flex().end();
        final ContainerConfig rowWithSkip = cell().repeat(2).skip().cell().layoutForEach(flex);
        final ContainerConfig cell3 = cell(flex).repeat(3);

        final ContainerConfig layout = cell(
                cell(cell3).
                cell(rowWithSkip).
                cell(cell3).repeat(2).layoutForEach(centerCenter).withGapBetweenCells(20));

        final String expectedLayout = "["
                + "[\"center-justified\", \"center\", \"margin-bottom:20px\", [\"flex\"], [\"flex\"], [\"flex\"]], "
                + "[\"center-justified\", \"center\", \"margin-bottom:20px\", [\"flex\"], [\"flex\"], [\"skip\", \"flex\"], [\"flex\"]], "
                + "[\"center-justified\", \"center\", \"margin-bottom:20px\", [\"flex\"], [\"flex\"], [\"flex\"]], "
                + "[\"center-justified\", \"center\", [\"flex\"], [\"flex\"], [\"flex\"]]]";

        assertEquals(expectedLayout, layout.toString());
    }

}

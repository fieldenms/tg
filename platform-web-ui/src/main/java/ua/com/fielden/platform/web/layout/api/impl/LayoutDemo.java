package ua.com.fielden.platform.web.layout.api.impl;

import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.cell;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.html;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.layoutContainer;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutCellBuilder.layout;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.web.layout.api.IFlexLayout;

public class LayoutDemo {

    public static void main(final String[] args) {
        final LayoutBuilder layout1 = layoutContainer(
                cell(cell().repeat(2).skip().cell().layoutForEach(layout().flex().end()).withGapBetweenCells(20)).repeat(2).withGapBetweenCells(10));

        System.out.println("layout 1: " + layout1);

        final IFlexLayout cellLayout = layout().flex().end();

        final LayoutBuilder layout2 = layoutContainer(
                cell(cell().repeat(2).skip().cell().layoutForEach(cellLayout).withGapBetweenCells(20)).
                cell(cell().skip().cell().repeat(2).layoutForEach(cellLayout).withGapBetweenCells(20)).withGapBetweenCells(10),
                layout().withStyle("padding", "20px").end());

        System.out.println("layout 2: " + layout2);

        final IFlexLayout rowLayout = layout().withStyle("padding-left", "32px").end();
        final IFlexLayout subheaderLayout = layout().withStyle("padding-top", "16px").withStyle("padding-bottom", "16px").end();
        final IFlexLayout bottomSubheaderLayout = layout().withStyle("padding-top", "24px").withStyle("padding-bottom", "16px").end();

        final LayoutBuilder waLayout  = layoutContainer(
                cell(cell().repeat(3).layoutForEach(cellLayout).withGapBetweenCells(20)).
                cell(cell().layoutForEach(cellLayout)).
                cell(cell().repeat(3).layoutForEach(cellLayout).withGapBetweenCells(20)).
                cell(cell().repeat(2).skip().layoutForEach(cellLayout).withGapBetweenCells(20)).
                cell(cell().repeat(3).layoutForEach(cellLayout).withGapBetweenCells(20)).
                subheader("Financials and Accounting", subheaderLayout).
                cell(cell().layoutForEach(cellLayout)).
                cell(cell().repeat(3).layoutForEach(cellLayout).withGapBetweenCells(20)).
                cell(cell().layoutForEach(cellLayout)).
                subheader("Allocation", subheaderLayout).
                cell(cell().repeat(3).layoutForEach(cellLayout).withGapBetweenCells(20)).
                cell(cell().skip().repeat(2).layoutForEach(cellLayout).withGapBetweenCells(20)).
                subheader("Date and Time", subheaderLayout).
                cell(cell().repeat(3).layoutForEach(cellLayout).withGapBetweenCells(20)).
                cell(cell().repeat(2).skip().layoutForEach(cellLayout).withGapBetweenCells(20)).repeat(2).
                cell(cell().repeat(3).layoutForEach(cellLayout).withGapBetweenCells(20)).
                subheader("Knowledge Tree", bottomSubheaderLayout).
                cell(cell().repeat(3).layoutForEach(cellLayout).withGapBetweenCells(20)).layoutForEach(rowLayout),
                layout().withStyle("padding", "20px").end());

        System.out.println("WA Layout: " + waLayout);

        final IFlexLayout verticalCell = layout().vertical().end();

        final LayoutBuilder layout3 = layoutContainer(
                cell(cell(layout().withStyle("margin-right", "30px").flex().end()).repeat(2).skip().cell().layoutForEach(cellLayout).withGapBetweenCells(20)).
                cell(cell(layout().withStyle("margin-right", "30px").flex().end()).skip().cell().repeat(2).layoutForEach(cellLayout)).layoutForEach(verticalCell).withGapBetweenCells(10),
                layout().withStyle("padding", "20px").horizontal().end());

        System.out.println("layout 3: " + layout3.toString());

        final LayoutBuilder layout4 = layoutContainer(
                html(new DomElement("span").attr("src", "test").style("color:blue").add(new InnerTextElement("That is test span element"))).
                cell().withGapBetweenCells(20));

        System.out.println("layout 4: " + layout4.toString());
    }

}

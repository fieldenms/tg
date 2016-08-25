package ua.com.fielden.platform.web.layout.api.impl;

import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.cell;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.layoutContainer;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutCellBuilder.layout;
import ua.com.fielden.platform.web.layout.api.IFlexLayout;

public class LayoutDemo {

    LayoutBuilder layout1 = layoutContainer(
            cell(cell().repeat(2).skip().cell().forEach(layout().flex().end()).withGap(20)).repeat(2));

    /* --------- -------- skip ---------
     * --------- -------- skip ---------
     */

    IFlexLayout cellLayout = layout().flex().end();

    LayoutBuilder layout2 = layoutContainer(
            cell(cell().repeat(2).skip().cell().forEach(cellLayout).withGap(20)).
            cell(cell().skip().cell().repeat(2).forEach(cellLayout).withGap(20)),
            layout().withStyle("padding", "20px").end());

    /*
     * --------- ----------   skip    --------
     * ---------    skip    --------- --------
     */
}

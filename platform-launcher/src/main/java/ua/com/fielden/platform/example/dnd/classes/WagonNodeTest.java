package ua.com.fielden.platform.example.dnd.classes;

import java.awt.geom.RoundRectangle2D;

public class WagonNodeTest extends AbstractWagonWidget<BogieNodeTest> {

    @Override
    public boolean canDrag() {
        return true;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public WagonNodeTest() {
        super(BogieNodeTest.class, new RoundRectangle2D.Double(0., 0., 10., 10., 2, 2), 2, WidgetOrientation.HORIZONTAL);
    }

    @Override
    protected boolean canAccept(int slotIndex, BogieNodeTest widgetToTest) {

        return true;
    }

}

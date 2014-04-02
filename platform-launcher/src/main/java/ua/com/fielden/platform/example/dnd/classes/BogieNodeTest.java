package ua.com.fielden.platform.example.dnd.classes;

import ua.com.fielden.platform.example.entities.Rotable;

public class BogieNodeTest extends AbstractBogieWidget<WheelsetNodeTest> {

    @Override
    public boolean canDrag() {
        return true;
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public BogieNodeTest() {
        super(WheelsetNodeTest.class, "bogie");
    }

    @Override
    protected boolean canAccept(final int slotIndex, final WheelsetNodeTest widgetToTest) {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Rotable getRotable() {
        return null;
    }

}

package ua.com.fielden.platform.example.dnd.classes;

import ua.com.fielden.platform.example.entities.Rotable;

public class WheelsetNodeTest extends AbstractWheelsetWidget {

    @Override
    public boolean canDrag() {
	return true;
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public WheelsetNodeTest() {
	super("caption");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Rotable getRotable() {
	return null;
    }

}

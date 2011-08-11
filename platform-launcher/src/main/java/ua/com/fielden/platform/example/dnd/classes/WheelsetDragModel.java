package ua.com.fielden.platform.example.dnd.classes;

import ua.com.fielden.platform.example.entities.Rotable;
import ua.com.fielden.platform.example.entities.Wheelset;

public class WheelsetDragModel extends AbstractWheelsetWidget {

    private Rotable rotable;

    @Override
    public boolean canDrag() {
	return true;
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public WheelsetDragModel(final Wheelset rotable) {
	super("caption");
	setRotable(rotable);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Rotable getRotable() {
	return rotable;
    }

    public void setRotable(final Rotable rotable) {
	this.rotable = rotable;
    }

}

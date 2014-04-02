package ua.com.fielden.platform.example.dnd.classes;

import ua.com.fielden.platform.example.entities.Rotable;
import ua.com.fielden.platform.example.entities.Wheelset;

public class WheelsetModel extends AbstractWheelsetWidget {

    private static final long serialVersionUID = -7813711598532296285L;
    private Wheelset wheelset;

    public WheelsetModel(final Wheelset rotable) {
        super(rotable.getKey());
        setWheelset(rotable);
        fill(rotable.getStatus().getColor());
    }

    @Override
    public boolean canDrag() {
        return true;
    }

    public Wheelset getWheelset() {
        return wheelset;
    }

    public void setWheelset(final Wheelset wheelset) {
        this.wheelset = wheelset;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Rotable getRotable() {
        return wheelset;
    }
}

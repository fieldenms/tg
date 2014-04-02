package ua.com.fielden.platform.swing.chartscroll;

import java.util.EventObject;

import org.jfree.data.Range;

public class AutoRangeChangedEvent extends EventObject {

    private static final long serialVersionUID = 1637176546305138388L;

    private final Range newRange;
    private final int axisIndex;

    public AutoRangeChangedEvent(final Object source, final Range newRange, final int axisIndex) {
        super(source);
        this.newRange = newRange;
        this.axisIndex = axisIndex;
    }

    public Range getNewRange() {
        return newRange;
    }

    public int getAxisIndex() {
        return axisIndex;
    }
}

package ua.com.fielden.platform.swing.chartscroll;

import javax.swing.event.EventListenerList;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.util.ObjectList;

public class ScrollableCategoryPlot extends CategoryPlot {

    private static final long serialVersionUID = -1483979004278976652L;

    private final ObjectList autoRanges = new ObjectList();

    private final EventListenerList listenerList = new EventListenerList();

    public ScrollableCategoryPlot() {

    }

    public ScrollableCategoryPlot(final CategoryDataset dataset, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final CategoryItemRenderer renderer) {
	super(dataset, domainAxis, rangeAxis, renderer);
    }

    public Range getAutoRange(final int index) {
	if (index < 0) {
	    return null;
	}
	if (autoRanges.get(index) == null) {
	    autoRanges.set(index, super.getDataRange(getRangeAxis(index)));
	}
	return (Range) autoRanges.get(index);
    }

    public void combineAutoRange(final int index, final Range range) {
	if (index < 0) {
	    return;
	}
	autoRanges.set(index, Range.combine(getAutoRange(index), range));
	fireAutoRangeChangedEvent(new AutoRangeChangedEvent(this, (Range) autoRanges.get(index), index));
	getRangeAxis(index).configure();
    }

    @Override
    public Range getDataRange(final ValueAxis axis) {
	final Range newRange = Range.combine(super.getDataRange(axis), getAutoRange(getRangeAxisIndex(axis)));
	if (getRangeAxisIndex(axis) >= 0) {
	    autoRanges.set(getRangeAxisIndex(axis), newRange);
	    fireAutoRangeChangedEvent(new AutoRangeChangedEvent(this, newRange, getRangeAxisIndex(axis)));
	}
	return newRange;
    }

    public void addAutoRangeChangedListener(final IAutoRangeChangedListener listener) {
	listenerList.add(IAutoRangeChangedListener.class, listener);
    }

    public void removeAutoRangeChangedListener(final IAutoRangeChangedListener listener) {
	listenerList.remove(IAutoRangeChangedListener.class, listener);
    }

    protected void fireAutoRangeChangedEvent(final AutoRangeChangedEvent event) {
	final Object[] listenersArray = listenerList.getListenerList();
	for (int i = listenersArray.length - 2; i >= 0; i -= 2) {
	    if (listenersArray[i] == IAutoRangeChangedListener.class) {
		((IAutoRangeChangedListener) listenersArray[i + 1]).autoRangeChanged(event);
	    }
	}
    }
}

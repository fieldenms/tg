package ua.com.fielden.platform.swing.categorychart;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.ui.RectangleEdge;

/**
 * This temporary class was implemented in order to fix bug that hides CategoryLabelEntity. This class will be removed when JfreeChart 1.0.14 will be released.
 * 
 * 
 * @author oleh
 * 
 */
public class FixedCategoryAxis extends CategoryAxis {

    private static final long serialVersionUID = -4061359422002464531L;

    public FixedCategoryAxis() {
	super();
    }

    public FixedCategoryAxis(final String name) {
	super(name);
    }

    @Override
    public AxisState draw(final Graphics2D g2, final double cursor, final Rectangle2D plotArea, final Rectangle2D dataArea, final RectangleEdge edge, final PlotRenderingInfo plotState) {
	// if the axis is not visible, don't draw it...
	if (!isVisible()) {
	    return new AxisState(cursor);
	}

	if (isAxisLineVisible()) {
	    drawAxisLine(g2, cursor, dataArea, edge);
	}
	AxisState state = new AxisState(cursor);
	if (isTickMarksVisible()) {
	    drawTickMarks(g2, cursor, dataArea, edge, state);
	}

	createAndAddEntity(cursor, state, dataArea, edge, plotState);
	// draw the category labels and axis label
	state = drawCategoryLabels(g2, plotArea, dataArea, edge, state, plotState);
	state = drawLabel(getLabel(), g2, plotArea, dataArea, edge, state);
	return state;
    }
}

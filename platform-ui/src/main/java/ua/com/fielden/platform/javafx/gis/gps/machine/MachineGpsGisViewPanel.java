package ua.com.fielden.platform.javafx.gis.gps.machine;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ListSelectionModel;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.javafx.gis.gps.GpsGisViewPanel;
import ua.com.fielden.platform.javafx.gis.gps.MessagePoint;
import ua.com.fielden.platform.javafx.gis.gps.MessagePointNodeMixin;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.pagination.PageHolder;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;

public class MachineGpsGisViewPanel<T extends AbstractEntity<?>> extends GpsGisViewPanel<T> {
    private static final long serialVersionUID = -7032805070573512539L;
    private final static int TAIL_SIZE_LIMIT = 5;

    public MachineGpsGisViewPanel(final MachineGpsGridAnalysisView<T> parentView, final EntityGridInspector egi, final ListSelectionModel listSelectionModel, final PageHolder pageHolder) {
	super(parentView, egi, listSelectionModel, pageHolder);
    }

    @Override
    protected List<MessagePoint> createPoints(final IPage<AbstractEntity<?>> entitiesPage) {
	clearEntityPoints();
	final List<MessagePoint> newPoints = new ArrayList<>();
	for (final AbstractEntity<?> machine : entitiesPage.data()) {
	    if (machine.get("lastMessage") != null) {
		final List<AbstractEntity> lastMessages = (List<AbstractEntity>) machine.get("lastMessages");
		for (int i = lastMessages.size() - 2 - TAIL_SIZE_LIMIT; i <= lastMessages.size() - 2; i++) { // without last message
		    if (i >= 0) {
			final MessagePoint mp = MessagePoint.createMessagePointFromMachine(machine, lastMessages.get(i));
			newPoints.add(mp);
			extendEntityPointsBy(machine, mp);
		    }
		}
		final MessagePoint mp = MessagePoint.createMessagePointFromMachine(machine);
		newPoints.add(mp);
		extendEntityPointsBy(machine, mp);
	    }
	}
	return newPoints;
    }

    @Override
    protected boolean drawLines(final MessagePoint start, final MessagePoint end) {
	return false;
    }

    @Override
    public javafx.scene.paint.Color getColor(final MessagePoint start) {
	//	    final List<Message> lastMessages = (List<Message>) start.getMachine().get("lastMessages");
	//	    final int index = lastMessages.indexOf(start.getMessage());
	//	    final int indexFromTail = lastMessages.size() - index;
	//	    final double fraction = (1.0 * indexFromTail) / TAIL_SIZE_LIMIT;
	// TODO
	// TODO
	// TODO
	// TODO
	// TODO
	// TODO
	/*.interpolate(javafx.scene.paint.Color.WHITE, fraction)*/
	// start.getSpeed() == 0 ? javafx.scene.paint.Color.RED : super.getColor(start);

	/* super.getColor(start) -- speed based color? */
	return MessagePointNodeMixin.teltonikaColor(start);
    }

    @Override
    protected double initialHalfSizeFactor() {
	return 3.0;
    }

    @Override
    protected double pixelThreashould() {
	return 0.0; // no threshold -- show all nodes regardless of the proximity in pixels
    }

    @Override
    protected AbstractEntity<?> entityToSelect(final MessagePoint point) {
	return point.getMachine();
    }
}
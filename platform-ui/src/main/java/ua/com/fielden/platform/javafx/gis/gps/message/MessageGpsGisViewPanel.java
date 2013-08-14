package ua.com.fielden.platform.javafx.gis.gps.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ListSelectionModel;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.gis.MapUtils;
import ua.com.fielden.platform.javafx.gis.gps.GpsGisViewPanel;
import ua.com.fielden.platform.javafx.gis.gps.MessagePoint;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.pagination.PageHolder;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.utils.Pair;

public class MessageGpsGisViewPanel<T extends AbstractEntity<?>> extends GpsGisViewPanel<T> {
    private static final long serialVersionUID = -7032805070573512539L;

    public MessageGpsGisViewPanel(final MessageGpsGridAnalysisView<T> parentView, final EntityGridInspector egi, final ListSelectionModel listSelectionModel, final PageHolder pageHolder) {
	super(parentView, egi, listSelectionModel, pageHolder);
    }

    @Override
    protected Pair<List<MessagePoint>, Map<Long, List<MessagePoint>>> createPoints(final IPage<AbstractEntity<?>> entitiesPage) {
	final List<MessagePoint> newPoints = new ArrayList<>();
	final Map<Long, List<MessagePoint>> newEntityPoints = new HashMap<Long, List<MessagePoint>>();
	for (final AbstractEntity<?> message : entitiesPage.data()) {
	    final MessagePoint mp = MessagePoint.createMessagePointFromMessage(message);
	    newPoints.add(mp);
	    extendEntityPointsBy(newEntityPoints, message, mp);
	}
	return new Pair<>(newPoints, newEntityPoints);
    }

    private double distance(final MessagePoint start, final MessagePoint end) {
	return MapUtils.calcDistance(start.getLongitude(), start.getLatitude(), end.getLongitude(), end.getLatitude());
    }

    @Override
    protected boolean drawLines(final MessagePoint start, final MessagePoint end) {
	return start.getMachine().equals(end.getMachine()) && // draw line if it is same machine
		distance(start, end) < 200; // draw line if a distance between points is < 200 meters
    }

    @Override
    public javafx.scene.paint.Color getColor(final MessagePoint start) {
	/* TODO provide MACHINE dependent logic? super.getColor(start) -- speed based color? */
	/* MessagePointNodeMixin.teltonikaColor(start); */
	return start.getSpeed() == 0 ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.BLUE;
    }

    @Override
    protected double initialHalfSizeFactor() {
	return 1.0;
    }

    @Override
    protected AbstractEntity<?> entityToSelect(final MessagePoint point) {
	return point.getMessage();
    }
}

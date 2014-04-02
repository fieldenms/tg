package ua.com.fielden.platform.javafx.gis.gps.machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ListSelectionModel;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.javafx.gis.gps.GpsGisViewPanel;
import ua.com.fielden.platform.javafx.gis.gps.MessagePoint;
import ua.com.fielden.platform.javafx.gis.gps.MessagePointNodeMixin;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.pagination.PageHolder;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.utils.Pair;

public class MachineGpsGisViewPanel<T extends AbstractEntity<?>> extends GpsGisViewPanel<T> {
    private static final long serialVersionUID = -7032805070573512539L;

    public MachineGpsGisViewPanel(final MachineGpsGridAnalysisView<T> parentView, final EntityGridInspector egi, final ListSelectionModel listSelectionModel, final PageHolder pageHolder) {
        super(parentView, egi, listSelectionModel, pageHolder);
    }

    @Override
    protected Pair<List<MessagePoint>, Map<Long, List<MessagePoint>>> createPoints(final IPage<AbstractEntity<?>> entitiesPage) {
        final List<MessagePoint> newPoints = new ArrayList<>();
        final Map<Long, List<MessagePoint>> newEntityPoints = new HashMap<Long, List<MessagePoint>>();
        for (final AbstractEntity<?> machine : entitiesPage.data()) {
            if (machine.get("lastMessage") != null) {
                final List<AbstractEntity> lastMessages = (List<AbstractEntity>) machine.get("lastMessages");
                for (int i = 0; i < lastMessages.size() - 1; i++) { // without last message
                    final MessagePoint mp = MessagePoint.createMessagePointFromMachine(machine, lastMessages.get(i));
                    newPoints.add(mp);
                    extendEntityPointsBy(newEntityPoints, machine, mp);
                }
                final MessagePoint mp = MessagePoint.createMessagePointFromMachine(machine);
                newPoints.add(mp);
                extendEntityPointsBy(newEntityPoints, machine, mp);
            }
        }
        return new Pair<>(newPoints, newEntityPoints);
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
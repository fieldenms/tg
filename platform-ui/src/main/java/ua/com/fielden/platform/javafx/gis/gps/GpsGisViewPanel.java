package ua.com.fielden.platform.javafx.gis.gps;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.web.WebEngine;

import javax.swing.ListSelectionModel;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.javafx.gis.GisViewPanel;
import ua.com.fielden.platform.pagination.PageHolder;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;

/**
 * An abstract base class for all GPS-message related {@link GisViewPanel}s.
 *
 * @author TG Team
 *
 * @param <T>
 */
public abstract class GpsGisViewPanel<T extends AbstractEntity<?>> extends GisViewPanel<T, MessagePoint> {
    private static final long serialVersionUID = -7032805070573512539L;

    private final GpsGridAnalysisView<T, ?> parentView;
    protected final Map<Long, List<MessagePoint>> entityPoints;
    private final Map<MessagePoint, IMessagePointNode> components = new HashMap<>();
    private int zoom;
    private final List<IMessagePointNode> nodesWithCallouts = new ArrayList<>();
    private boolean inside;

    public GpsGisViewPanel(final GpsGridAnalysisView<T, ?> parentView, final EntityGridInspector egi, final ListSelectionModel listSelectionModel, final PageHolder pageHolder) {
	super(parentView, egi, listSelectionModel, pageHolder);
	this.parentView = parentView;
	this.entityPoints = new HashMap<>();
    }

    @Override
    protected final void findAndSelectPoint(final AbstractEntity<?> selectedEntity, final AbstractEntity<?> unselectedEntity) {
	if (unselectedEntity != null) {
	    final MessagePoint unselectedEntityPoint = getCorrespondingPoint(unselectedEntity);
	    if (unselectedEntityPoint != null) {
		ensureThatComponentExists(unselectedEntityPoint).unselect();
	    }
	}

	if (selectedEntity != null) {
	    final MessagePoint selectedEntityPoint = getCorrespondingPoint(selectedEntity);
	    if (selectedEntityPoint != null) {
		// TODO at this stage centering is returned... but possibly we should fit a new callout to bounds?
		centerBy(selectedEntityPoint.getLongitude(), selectedEntityPoint.getLatitude());
		final IMessagePointNode selectedComponent = ensureThatComponentExists(selectedEntityPoint);
		selectedComponent.select(showCallout(selectedComponent));
	    }
	}
    }

    protected void extendEntityPointsBy(final AbstractEntity entity, final MessagePoint messagePoint) {
	final List<MessagePoint> list = entityPoints.get(entity.getId());
	if (list == null) {
	    entityPoints.put(entity.getId(), new ArrayList<MessagePoint>());
	}
	entityPoints.get(entity.getId()).add(messagePoint);
    }

    protected void clearEntityPoints() {
	entityPoints.clear();
    }

    public MessagePoint getCorrespondingPoint(final AbstractEntity entity) {
	if (entity == null) {
	    return null;
	} else if (entity.getId() == null) {
	    throw new IllegalArgumentException("An id for entity [" + entity + "] should exist.");
	}
	final List<MessagePoint> list = entityPoints.get(entity.getId());
	return list == null ? null : list.get(list.size() - 1); // get the last point in the list
    }

    protected IMessagePointNode ensureThatComponentExists(final MessagePoint point) {
	if (components.get(point) == null) {
	    prevXY = null;

	    inside = true; // turn off adaptive adding
	    addPoint(getWebEngine(), point); // this is necessary when a node is not even created (as the scene is lazy now)
	    inside = false;
	}
	if (components.get(point) == null) {
	    throw new IllegalStateException("components.get(point) is null! It is illegal situation!");
	}
	return components.get(point);
    }

    @Override
    protected boolean inside(final Point2D xY) {
	return inside ? true : super.inside(xY);
    }

    protected boolean showCallout(final IMessagePointNode mpn) {
	return true;
	//	    if (this.nodesWithCallouts.isEmpty()) {
	//		return true;
	//	    } else {
	//		for (final IMessagePointNode nodeWithCallout : this.nodesWithCallouts) {
	//		    if (nodeWithCallout == mpn) {
	//			return true;
	//		    }
	//		}
	//		return false;
	//	    }
    }

    @Override
    protected boolean shouldFitToBounds() {
	return this.parentView.getModel().getFitToBounds();
    }

    @Override
    protected void afterMapLoaded() {
	getWebEngine().executeScript("document.goToLocation(\"Lviv\")");
	// getWebEngine().executeScript("document.setMapTypeSatellite()");
	activateView(GisView.ROAD);
    }

    @Override
    protected boolean drawSpeedValues(final int zoom) {
	return false;
    }

    @Override
    public String getTooltip(final MessagePoint messagePoint) {
	final StringBuilder tooltipText = new StringBuilder();
	for (final String resultProp : this.parentView.getModel().getCdtme().getSecondTick().checkedProperties(this.parentView.getModel().getEntityType())) {
	    final String property = StringUtils.isEmpty(resultProp) ? AbstractEntity.KEY : resultProp;
	    final Class<?> enhancedType = this.parentView.getModel().getCdtme().getEnhancer().getManagedType(this.parentView.getModel().getEntityType());
	    if (!Finder.findFieldByName(enhancedType, property).isAnnotationPresent(Calculated.class)) {
		// TODO
		// TODO
		// TODO can be calc -- except Calc AGGREGATION_EXPRESSION!
		// TODO
		// TODO
		// TODO
		tooltipText.append("" + TitlesDescsGetter.getTitleAndDesc(property, enhancedType).getKey() + ": " + entityToSelect(messagePoint).get(property) + "\n");
	    }
	}
	return /*str + "\n" + */tooltipText.toString();
    }

    @Override
    protected void removeOldAndAddNew(final WebEngine webEngine, final int zoom) {
	this.zoom = zoom;
	this.nodesWithCallouts.clear();
	// components.clear();
	super.removeOldAndAddNew(webEngine, zoom);

	// add callouts to upper "layer" of nodes
	for (final IMessagePointNode nodeWithCallouts : nodesWithCallouts) {
	    nodeWithCallouts.addExistingCalloutToScene(path());
	}
    }

    protected abstract double initialHalfSizeFactor();

    protected IMessagePointNode createMessagePointNode(final MessagePoint messagePoint) {
	if (messagePoint.getSpeed() <= 0) {
	    return new MessagePointBead(this, messagePoint, initialHalfSizeFactor() * 2, 15, getScene(), path());
	} else {
	    return new MessagePointArrow(this, messagePoint, initialHalfSizeFactor() * 1, 15, getScene(), path());
	}
    }

    @Override
    protected Point2D addPoint(final WebEngine webEngine, final MessagePoint messagePoint) {
	final Point2D xY = super.addPoint(webEngine, messagePoint);

	if (inside(xY) && dist(prevXY, xY) >= pixelThreashould()) {
	    countOfProcessed++;
	    final IMessagePointNode cachedMessagePointNode = components.get(messagePoint);
	    if (cachedMessagePointNode != null) {
		cachedMessagePointNode.updateAndAdd(xY, path(), zoom);

		if (cachedMessagePointNode.hasCallout()) {
		    this.nodesWithCallouts.add(cachedMessagePointNode);
		}
	    } else {
		final IMessagePointNode newMessagePointNode = createMessagePointNode(messagePoint);
		newMessagePointNode.updateAndAdd(xY, path(), zoom);
		components.put(messagePoint, newMessagePointNode);

		if (newMessagePointNode.hasCallout()) {
		    this.nodesWithCallouts.add(newMessagePointNode);
		}
	    }
	    prevXY = xY;
	}
	return xY;
    }
}

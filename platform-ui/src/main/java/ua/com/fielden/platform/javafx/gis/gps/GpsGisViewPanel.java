package ua.com.fielden.platform.javafx.gis.gps;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.Node;
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
import ua.com.fielden.platform.utils.Pair;

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
    private final Map<MessagePoint, IMessagePointNode> components = new HashMap<>();
    private Long entityWithCallout;
    private boolean calloutHasBeenTurnedOff = false;
    private boolean inside;
    private boolean dist;
    private MessagePoint selectedPoint;

    public GpsGisViewPanel(final GpsGridAnalysisView<T, ?> parentView, final EntityGridInspector egi, final ListSelectionModel listSelectionModel, final PageHolder pageHolder) {
	super(parentView, egi, listSelectionModel, pageHolder);
	this.parentView = parentView;
    }

    protected abstract double initialHalfSizeFactor();

    @Override
    protected void freeResources() {
	components.clear();

        super.freeResources();
    }

    @Override
    protected final void findAndSelectPoint(final AbstractEntity<?> selectedEntity, final AbstractEntity<?> unselectedEntity, final boolean forceCalloutChange) {
	if (forceCalloutChange) {
	    closeCallout();
	}

	if (unselectedEntity != null) {
	    final MessagePoint unselectedEntityPoint = getCorrespondingPoint(unselectedEntity);
	    if (unselectedEntityPoint != null) {
		ensureThatComponentExists(unselectedEntityPoint).unselect();
	    }
	}

	if (selectedEntity != null) {
	    final MessagePoint selectedEntityPoint = getCorrespondingPoint(selectedEntity);
	    if (selectedEntityPoint != null) {
		// at this stage centering is returned... but possibly we should fit a new callout to bounds?
		centerBy(selectedEntityPoint.getLongitude(), selectedEntityPoint.getLatitude());

		ensureThatComponentExists(selectedEntityPoint).select();
		openCallout(selectedEntityPoint);

		removeOldAndAddNew(getWebEngine(), zoom(getWebEngine()));
	    }
	}
    }

    protected IMessagePointNode ensureThatComponentExists(final MessagePoint point) {
	if (components.get(point) == null) {
	    inside = true; // turn off adaptive adding
	    dist = true; // turn off adaptive adding
	    addPoint(getWebEngine(), point); // this is necessary when a node is not even created (as the scene is lazy now)
	    inside = false;
	    dist = false;
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

    @Override
    protected boolean isPixelBusy(final Pair<Integer, Integer> pixel) {
	return dist ? false : super.isPixelBusy(pixel);
    }

    @Override
    protected boolean shouldFitToBounds() {
	return this.parentView.getModel().getFitToBounds();
    }

    @Override
    protected void afterMapLoaded() {
	executeScript("document.goToLocation(\"Lviv\")");
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
	// System.err.println("\tentityWithCallout == " + entityWithCallout + " calloutHasBeenTurnedOff == " + calloutHasBeenTurnedOff);
	selectedPoint = null;
	super.removeOldAndAddNew(webEngine, zoom);

	if (selectedPoint != null) {
	    dist = true; // turn off adaptive adding
	    addPoint(webEngine, selectedPoint);
	    dist = false;
	}
	selectedPoint = null;

	// add callouts to upper "layer" of nodes
	if (entityWithCallout != null) {
	    updateCallout(webEngine);
	}
    }

    protected void updateCallout(final WebEngine webEngine) {
	final MessagePoint pointOfEntityWithCallout = getCorrespondingPoint(entityWithCallout);
	if (pointOfEntityWithCallout != null) {
	    ensureThatComponentExists(pointOfEntityWithCallout).closeCallout();

	    dist = true; // turn off adaptive adding
	    final Point2D xY = addPoint(webEngine, pointOfEntityWithCallout);
	    dist = false;

	    if (!calloutHasBeenTurnedOff && inside(xY)) {
		ensureThatComponentExists(pointOfEntityWithCallout).createAndAddCallout(path());
	    }
	}
    }

    @Override
    public void closeCallout() {
	if (entityWithCallout != null) {
	    final MessagePoint pointOfEntityWithCallout = getCorrespondingPoint(entityWithCallout);
	    if (pointOfEntityWithCallout != null) {
		ensureThatComponentExists(pointOfEntityWithCallout).closeCallout();
		entityWithCallout = null;
		calloutHasBeenTurnedOff = false;
	    }
	}
    }

    @Override
    public void openCallout(final MessagePoint point) {
	final Long newEntityWithCallout = entityToSelect(point).getId();
	if (entityWithCallout != null) {
	    if (entityWithCallout.equals(newEntityWithCallout)) {
		updateCallout(getWebEngine());
		return;
	    } else {
		throw new IllegalStateException("Old callout for entity [" + entityWithCallout + "] should be removed at this stage when new callout has been arrived (" + newEntityWithCallout + ").");
	    }
	} else {
	    final MessagePoint pointOfNewEntityWithCallout = getCorrespondingPoint(newEntityWithCallout);
	    if (pointOfNewEntityWithCallout != null) {
		ensureThatComponentExists(pointOfNewEntityWithCallout).createAndAddCallout(path());
		entityWithCallout = newEntityWithCallout;
		// calloutHasBeenTurnedOff = false;
	    }
	}
    }

    @Override
    public void turnOffCallout() {
	if (entityWithCallout != null) {
	    final MessagePoint pointOfEntityWithCallout = getCorrespondingPoint(entityWithCallout);
	    if (pointOfEntityWithCallout != null) {
		ensureThatComponentExists(pointOfEntityWithCallout).closeCallout();
		calloutHasBeenTurnedOff = true;
	    }
	}
    }

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

	final IMessagePointNode cachedMessagePointNode = components.get(messagePoint);
	if (cachedMessagePointNode != null && cachedMessagePointNode.selected()) {
	    selectedPoint = messagePoint;
	}

	final Pair<Integer, Integer> pixel = getPixel(xY.getX(), xY.getY());

	if (inside(xY) && !isPixelBusy(pixel)) {
	    countOfProcessed++;
	    if (cachedMessagePointNode != null) {
		oldCountOfProcessed++;
		cachedMessagePointNode.makeVisibleAndUpdate(xY, path(), getZoom());
		putPixelNode(pixel, (Node) cachedMessagePointNode);
	    } else {
		newCountOfProcessed++;
		final IMessagePointNode newMessagePointNode = createMessagePointNode(messagePoint);
		newMessagePointNode.add(path());
		newMessagePointNode.makeVisibleAndUpdate(xY, path(), getZoom());
		components.put(messagePoint, newMessagePointNode);
		putPixelNode(pixel, (Node) newMessagePointNode);
	    }
	} else {
	    if (cachedMessagePointNode != null) {
		cachedMessagePointNode.makeInvisible();
	    }
	}
	return xY;
    }
}

package ua.com.fielden.platform.web.gis.gps;

import javax.swing.ListSelectionModel;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.javafx.gis.GisViewPanel;
import ua.com.fielden.platform.javafx.gis.gps.MessagePoint;
import ua.com.fielden.platform.pagination.PageHolder;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.web.gis.GisViewPanel2;

/**
 * An abstract base class for all GPS-message related {@link GisViewPanel}s.
 *
 * @author TG Team
 *
 * @param <T>
 */
public abstract class GpsGisViewPanel2<T extends AbstractEntity<?>> extends GisViewPanel2<T, MessagePoint> {
    private static final long serialVersionUID = -7032805070573512539L;
    private static final Logger logger = Logger.getLogger(GpsGisViewPanel2.class);

    private final GpsGridAnalysisView2<T, ?> parentView;

    public GpsGisViewPanel2(final GpsGridAnalysisView2<T, ?> parentView, final EntityGridInspector egi, final ListSelectionModel listSelectionModel, final PageHolder pageHolder) {
        super(parentView, egi, listSelectionModel, pageHolder);
        this.parentView = parentView;
    }

    protected abstract double initialHalfSizeFactor();

    @Override
    protected void freeResources() {
        super.freeResources();
    }

    @Override
    protected boolean shouldFitToBounds() {
        return this.parentView.getModel().getFitToBounds();
    }

    @Override
    protected void afterMapLoaded() {
        // executeScript("document.goToLocation(\"Lviv\")");
    }

//    @Override
//    public String getTooltip(final MessagePoint messagePoint) {
//        final StringBuilder tooltipText = new StringBuilder();
//        for (final String resultProp : this.parentView.getModel().getCdtme().getSecondTick().checkedProperties(this.parentView.getModel().getEntityType())) {
//            final String property = StringUtils.isEmpty(resultProp) ? AbstractEntity.KEY : resultProp;
//            final Class<?> enhancedType = this.parentView.getModel().getCdtme().getEnhancer().getManagedType(this.parentView.getModel().getEntityType());
//            if (!AnnotationReflector.isAnnotationPresent(Finder.findFieldByName(enhancedType, property), Calculated.class)) {
//                // TODO
//                // TODO
//                // TODO can be calc -- except Calc AGGREGATION_EXPRESSION!
//                // TODO
//                // TODO
//                // TODO
//                tooltipText.append("" + TitlesDescsGetter.getTitleAndDesc(property, enhancedType).getKey() + ": " + entityToSelect(messagePoint).get(property) + "\n");
//            }
//        }
//        return /*str + "\n" + */tooltipText.toString();
//    }
}

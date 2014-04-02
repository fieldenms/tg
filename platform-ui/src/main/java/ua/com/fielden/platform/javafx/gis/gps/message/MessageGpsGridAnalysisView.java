package ua.com.fielden.platform.javafx.gis.gps.message;

import java.awt.Color;

import javax.swing.ListSelectionModel;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.javafx.gis.gps.GpsGridAnalysisModel;
import ua.com.fielden.platform.javafx.gis.gps.GpsGridAnalysisView;
import ua.com.fielden.platform.javafx.gis.gps.GpsGridConfigurationView;
import ua.com.fielden.platform.javafx.gis.gps.MessagePoint;
import ua.com.fielden.platform.pagination.PageHolder;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.egi.coloring.IColouringScheme;
import ua.com.fielden.platform.swing.review.report.analysis.chart.CategoryChartFactory;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;

/**
 * {@link GridAnalysisView} for Vehicle main details with EGI and GIS views.
 * 
 * @author TG Team
 * 
 */
public abstract class MessageGpsGridAnalysisView<T extends AbstractEntity<?>> extends GpsGridAnalysisView<T, MessageGpsGisViewPanel<T>> {
    private static final long serialVersionUID = 553731585658593055L;

    public MessageGpsGridAnalysisView(final GpsGridAnalysisModel<T> model, final GpsGridConfigurationView<T> owner) {
        super(model, owner);
    }

    @Override
    protected final MessageGpsGisViewPanel<T> createGisViewPanel(final EntityGridInspector egi, final ListSelectionModel listSelectionModel, final PageHolder pageHolder) {
        return new MessageGpsGisViewPanel<T>(this, egi, listSelectionModel, pageHolder);
    }

    @Override
    protected IColouringScheme<AbstractEntity> createRowColoringScheme() {
        return new IColouringScheme<AbstractEntity>() {
            @Override
            public Color getColor(final AbstractEntity entity) {
                final MessagePoint p = getGisViewPanel().getCorrespondingPoint(entity);
                if (p == null) {
                    return Color.WHITE;
                } else {
                    final javafx.scene.paint.Color messageColor = getGisViewPanel().getColor(p);
                    return CategoryChartFactory.getAwtColor(messageColor.equals(javafx.scene.paint.Color.BLUE) ? javafx.scene.paint.Color.GREEN : messageColor);
                }
            }
        };
    }
}

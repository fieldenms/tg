package ua.com.fielden.platform.web.gis.gps.message;

import java.awt.Color;

import javax.swing.ListSelectionModel;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.PageHolder;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.egi.coloring.IColouringScheme;
import ua.com.fielden.platform.swing.review.report.analysis.chart.CategoryChartFactory;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;
import ua.com.fielden.platform.web.gis.gps.GpsGridAnalysisModel2;
import ua.com.fielden.platform.web.gis.gps.GpsGridAnalysisView2;
import ua.com.fielden.platform.web.gis.gps.GpsGridConfigurationView2;

/**
 * {@link GridAnalysisView} for Vehicle main details with EGI and GIS views.
 *
 * @author TG Team
 *
 */
public abstract class MessageGpsGridAnalysisView2<T extends AbstractEntity<?>> extends GpsGridAnalysisView2<T, MessageGpsGisViewPanel2<T>> {
    private static final long serialVersionUID = 553731585658593055L;

    public MessageGpsGridAnalysisView2(final GpsGridAnalysisModel2<T> model, final GpsGridConfigurationView2<T> owner) {
        super(model, owner);
    }

    @Override
    protected final MessageGpsGisViewPanel2<T> createGisViewPanel(final EntityGridInspector egi, final ListSelectionModel listSelectionModel, final PageHolder pageHolder) {
        return new MessageGpsGisViewPanel2<T>(this, egi, listSelectionModel, pageHolder);
    }

    @Override
    protected IColouringScheme<AbstractEntity> createRowColoringScheme() {
        return new IColouringScheme<AbstractEntity>() {
            @Override
            public Color getColor(final AbstractEntity entity) {

                if (entity.get("vectorSpeed") == null) {
                    return Color.WHITE;
                } else if (entity.get("vectorSpeed").equals(0)) {
                    return CategoryChartFactory.getAwtColor(javafx.scene.paint.Color.RED);
                } else {
                    return CategoryChartFactory.getAwtColor(javafx.scene.paint.Color.GREEN);
                }
                // final javafx.scene.paint.Color messageColor = getGisViewPanel().getColor(p);
                // return CategoryChartFactory.getAwtColor(messageColor.equals(javafx.scene.paint.Color.BLUE) ? javafx.scene.paint.Color.GREEN : messageColor);
            }
        };
    }
}

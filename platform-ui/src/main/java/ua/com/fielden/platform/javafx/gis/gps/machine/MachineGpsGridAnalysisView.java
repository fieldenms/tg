package ua.com.fielden.platform.javafx.gis.gps.machine;

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
 * {@link GridAnalysisView} for {@link Machine} main details with EGI and GIS views.
 * 
 * @author TG Team
 * 
 */
public class MachineGpsGridAnalysisView<T extends AbstractEntity<?>> extends GpsGridAnalysisView<T, MachineGpsGisViewPanel<T>> {
    private static final long serialVersionUID = 553731585658593055L;

    public MachineGpsGridAnalysisView(final GpsGridAnalysisModel<T> model, final GpsGridConfigurationView<T> owner) {
        super(model, owner);
    }

    @Override
    protected final MachineGpsGisViewPanel<T> createGisViewPanel(final EntityGridInspector egi, final ListSelectionModel listSelectionModel, final PageHolder pageHolder) {
        return new MachineGpsGisViewPanel<T>(this, egi, listSelectionModel, pageHolder);
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
                    return CategoryChartFactory.getAwtColor(getGisViewPanel().getColor(p));
                }
            }
        };
    }
}

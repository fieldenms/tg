package ua.com.fielden.platform.web.gis.gps.stop;

import java.awt.Color;
import java.util.Set;

import javax.swing.ListSelectionModel;

import tg.tablecode.Message;
import tg.tablecode.Stop;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.PageHolder;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.egi.coloring.IColouringScheme;
import ua.com.fielden.platform.swing.review.report.analysis.chart.CategoryChartFactory;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;
import ua.com.fielden.platform.web.gis.gps.GpsGridAnalysisView2;

/**
 * {@link GridAnalysisView} for {@link Message} main details with EGI and GIS views.
 *
 * @author TG Team
 *
 */
public class StopGridAnalysisView2 extends GpsGridAnalysisView2<Stop, StopGisViewPanel2> {
    private static final long serialVersionUID = 1281406501816511413L;

    public StopGridAnalysisView2(final StopGridAnalysisModel2 model, final StopGridConfigurationView2 owner) {
        super(model, owner);
    }

    @Override
    protected IColouringScheme<AbstractEntity> createRowColoringScheme() {
        return new IColouringScheme<AbstractEntity>() {
            @Override
            public Color getColor(final AbstractEntity entity) {
                if (entity.get("messages") == null || ((Set<Message>) entity.get("messages")).isEmpty()) {
                    return Color.WHITE;
                }

                //                if (entity.get("vectorSpeed") == null) {
                return CategoryChartFactory.getAwtColor(javafx.scene.paint.Color.rgb(255, 235, 156)); // javafx.scene.paint.Color.ORANGERED
                //                } else if (entity.get("vectorSpeed").equals(0)) {
                //                    return CategoryChartFactory.getAwtColor(javafx.scene.paint.Color.RED);
                //                } else {
                //                    return CategoryChartFactory.getAwtColor(javafx.scene.paint.Color.GREEN);
                //                }
                //                // final javafx.scene.paint.Color messageColor = getGisViewPanel().getColor(p);
                //                // return CategoryChartFactory.getAwtColor(messageColor.equals(javafx.scene.paint.Color.BLUE) ? javafx.scene.paint.Color.GREEN : messageColor);
            }
        };
    }

    @Override
    protected final StopGisViewPanel2 createGisViewPanel(final EntityGridInspector egi, final ListSelectionModel listSelectionModel, final PageHolder pageHolder) {
        return new StopGisViewPanel2(this, egi, listSelectionModel, pageHolder);
    }
}
package ua.com.fielden.platform.javafx.gis.gps;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.PageHolder;
import ua.com.fielden.platform.swing.egi.EgiPanel;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.egi.coloring.IColouringScheme;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;

/**
 * {@link GridAnalysisView} for Vehicle main details with EGI and GIS views.
 *
 * @author TG Team
 *
 */
public abstract class GpsGridAnalysisView<T extends AbstractEntity, GVPTYPE extends GpsGisViewPanel<T>> extends GridAnalysisView<T, ICentreDomainTreeManagerAndEnhancer> {
    private static final long serialVersionUID = 553731585658593055L;

    private final JSplitPane tableAndGisViewSplitter;
    private final GVPTYPE gisViewPanel;

    public GpsGridAnalysisView(final GpsGridAnalysisModel<T> model, final GpsGridConfigurationView<T> owner) {
	super(model, owner);

	gisViewPanel = createGisViewPanel(getEgiPanel().getEgi(), getEgiPanel().getEgi().getSelectionModel(), getModel().getPageHolder()); // new MessageGisViewPanel<T>(this, getEgiPanel().getEgi(), getEgiPanel().getEgi().getSelectionModel(), getModel().getPageHolder());
	tableAndGisViewSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getEgiPanel(), gisViewPanel);
	tableAndGisViewSplitter.setOneTouchExpandable(true);
	tableAndGisViewSplitter.setDividerLocation(0.5);
	// tableAndGisViewSplitter.setResizeWeight(0.5);
	layoutView();
    }

    protected GVPTYPE getGisViewPanel() {
	return gisViewPanel;
    }

    protected abstract GVPTYPE createGisViewPanel(/*final AbstractMessageGridAnalysisView<T> parentView, */ final EntityGridInspector egi, final ListSelectionModel listSelectionModel, final PageHolder pageHolder);

    @Override
    public GpsGridAnalysisModel<T> getModel() {
        return (GpsGridAnalysisModel<T>) super.getModel();
    }

    @Override
    protected EgiPanel createEgiPanel() {
        return new EgiPanel(getModel().getCriteria().getEntityClass(), getModel().getCriteria().getCentreDomainTreeMangerAndEnhancer(), createRowColoringScheme());
    }

//    private IColouringScheme<AbstractEntity> createRowColoringScheme() {
//	return new IColouringScheme<AbstractEntity>() {
//	    @Override
//	    public Color getColor(final AbstractEntity entity) {
//		for (final MessagePoint p : gisViewPanel.points()) {
//		    if (p.getMessage().getId().equals(entity.getId())) {
//			final javafx.scene.paint.Color messageColor = gisViewPanel.getColor(p);
//			return CategoryChartFactory.getAwtColor(messageColor.equals(javafx.scene.paint.Color.BLUE) ? javafx.scene.paint.Color.GREEN : messageColor);
//			// TODO return CategoryChartFactory.getAwtColor(messageColor);
//		    }
//		}
//		return Color.WHITE;
//	    }
//	};
//    }

    protected abstract IColouringScheme<AbstractEntity> createRowColoringScheme();

    @Override
    protected int getPageSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public GpsGridConfigurationView<T> getOwner() {
        return (GpsGridConfigurationView<T>) super.getOwner();
    }

    @Override
    protected void layoutView() {
	if (gisViewPanel != null) {
	    final List<JComponent> components = new ArrayList<JComponent>();
	    final StringBuffer rowConstraints = new StringBuffer("");

	    //Creates entity centre's tool bar.
	    rowConstraints.append(AbstractEntityCentre.addToComponents(components, "[fill]", getToolBar()));
	    rowConstraints.append(AbstractEntityCentre.addToComponents(components, "[fill, grow]", getTableAndGisViewSplitter()));

	    setLayout(new MigLayout("fill, insets 0", "[fill, grow]", isEmpty(rowConstraints.toString()) ? "[fill, grow]" : rowConstraints.toString()));
	    for (int componentIndex = 0; componentIndex < components.size() - 1; componentIndex++) {
		add(components.get(componentIndex), "wrap");
	    }
	    add(components.get(components.size() - 1));
	}
    }

    public JSplitPane getTableAndGisViewSplitter() {
	return tableAndGisViewSplitter;
    }
}

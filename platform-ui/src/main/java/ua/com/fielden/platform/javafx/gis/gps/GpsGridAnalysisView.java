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
 * {@link GridAnalysisView} with EGI and GPS GIS views.
 *
 * @author TG Team
 *
 */
public abstract class GpsGridAnalysisView<T extends AbstractEntity<?>, GVPTYPE extends GpsGisViewPanel<T>> extends GridAnalysisView<T, ICentreDomainTreeManagerAndEnhancer> {
    private static final long serialVersionUID = 553731585658593055L;

    private final JSplitPane tableAndGisViewSplitter;
    private final GVPTYPE gisViewPanel;

    public GpsGridAnalysisView(final GpsGridAnalysisModel<T> model, final GpsGridConfigurationView<T> owner) {
	super(model, owner);

	gisViewPanel = createGisViewPanel(getEgiPanel().getEgi(), getEgiPanel().getEgi().getSelectionModel(), getModel().getPageHolder());
	tableAndGisViewSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getEgiPanel(), gisViewPanel);
	tableAndGisViewSplitter.setOneTouchExpandable(true);
	tableAndGisViewSplitter.setDividerLocation(0.5); // FIXME this does not work properly!  // tableAndGisViewSplitter.setResizeWeight(0.5);

	layoutView();
    }

    protected GVPTYPE getGisViewPanel() {
	return gisViewPanel;
    }

    /**
     * Creates gis view panel.
     *
     * @param egi
     * @param listSelectionModel
     * @param pageHolder
     * @return
     */
    protected abstract GVPTYPE createGisViewPanel(final EntityGridInspector egi, final ListSelectionModel listSelectionModel, final PageHolder pageHolder);

    /**
     * Provides a colouring scheme for egi table based on specific nature of Gps Gis centre.
     *
     * @return
     */
    protected abstract IColouringScheme<AbstractEntity> createRowColoringScheme();

    @Override
    public GpsGridAnalysisModel<T> getModel() {
        return (GpsGridAnalysisModel<T>) super.getModel();
    }

    @Override
    protected EgiPanel createEgiPanel() {
        return new EgiPanel(getModel().getCriteria().getEntityClass(), getModel().getCriteria().getCentreDomainTreeMangerAndEnhancer(), createRowColoringScheme());
    }


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

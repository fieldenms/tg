package ua.com.fielden.platform.javafx.gis.gps;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.IToolbarCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;

/**
 * {@link GridConfigurationView} for Message main details.
 *
 * @author TG Team
 *
 */
public abstract class GpsGridConfigurationView<T extends AbstractEntity<?>> extends GridConfigurationView<T, ICentreDomainTreeManagerAndEnhancer> {
    private static final long serialVersionUID = 1507085016131840748L;

    public GpsGridConfigurationView(final GpsGridConfigurationModel<T> model, final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner, final BlockingIndefiniteProgressLayer progressLayer, final IToolbarCustomiser<GridAnalysisView<T, ICentreDomainTreeManagerAndEnhancer>> toolbarCustomiser) {
	//TODO the details customiser might be set for this type of analysis.
	super(model, owner, toolbarCustomiser, null, progressLayer);
    }

    @Override
    protected abstract GridAnalysisView<T, ICentreDomainTreeManagerAndEnhancer> createConfigurableView();

    @Override
    public GpsGridConfigurationModel<T> getModel() {
	return (GpsGridConfigurationModel<T>) super.getModel();
    }
}

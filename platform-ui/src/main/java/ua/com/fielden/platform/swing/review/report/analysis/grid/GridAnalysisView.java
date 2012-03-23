package ua.com.fielden.platform.swing.review.report.analysis.grid;

import javax.swing.Action;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.egi.EgiPanel;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;

public class GridAnalysisView<T extends AbstractEntity, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractAnalysisReview<T, CDTME, IAbstractAnalysisDomainTreeManager, IPage<T>> {

    private static final long serialVersionUID = 8538099803371092525L;

    private final EgiPanel<T> egiPanel;

    public GridAnalysisView(final GridAnalysisModel<T, CDTME> model, final BlockingIndefiniteProgressLayer progressLayer, final AbstractEntityCentre<T, CDTME> owner) {
	super(model, progressLayer, owner);
	this.egiPanel = new EgiPanel<T>(getModel().getGridModel(), false);
	layoutView();
    }

    public EgiPanel<T> getEgiPanel() {
	return egiPanel;
    }

    protected void layoutView() {
	setLayout(new MigLayout("fill, insets 0","[fill, grow]","[fill, grow]"));
	add(this.egiPanel);
    }

    @Override
    public GridAnalysisModel<T, CDTME> getModel() {
	return (GridAnalysisModel<T, CDTME>) super.getModel();
    }

    @Override
    protected Action createConfigureAction() {
	return null;
    }

    @Override
    protected void enableRelatedActions(final boolean enable, final boolean navigate) {
	// TODO Auto-generated method stub

    }
}

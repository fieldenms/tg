package ua.com.fielden.platform.swing.review.report.analysis.grid;

import javax.swing.Action;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.egi.EgiPanel;
import ua.com.fielden.platform.swing.pagination.model.development.PageHolder;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;

public class GridAnalysisView<T extends AbstractEntity> extends AbstractAnalysisReview<T, IAbstractAnalysisDomainTreeManager> {

    private static final long serialVersionUID = 8538099803371092525L;

    private final EgiPanel<T> egiPanel;

    public GridAnalysisView(final GridAnalysisModel<T> model, final BlockingIndefiniteProgressLayer progressLayer, final AbstractEntityCentre<T> owner, final PageHolder pageHolder) {
	super(model, progressLayer, owner, pageHolder);
	this.egiPanel = new EgiPanel<T>(getModel().getGridModel(), false);
	layoutView();
    }

    protected void layoutView() {
	setLayout(new MigLayout("fill, insets 0","[fill, grow]","[fill, grow]"));
	add(this.egiPanel);
    }

    @Override
    public GridAnalysisModel<T> getModel() {
	return (GridAnalysisModel<T>) super.getModel();
    }

    @Override
    protected Action createConfigureAction() {
	return null;
    }

    @Override
    protected Action createRemoveAction() {
	return null;
    }

    @Override
    protected Action createSaveAction() {
	return null;
    }

    @Override
    protected Action createSaveAsAction() {
	return null;
    }

    @Override
    protected Action createSaveAsDefaultAction() {
	return null;
    }

}

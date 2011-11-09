package ua.com.fielden.platform.swing.review.report.analysis.grid;

import javax.swing.Action;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.egi.EgiPanel;
import ua.com.fielden.platform.swing.review.development.AbstractEntityReview;

public class GridAnalysisView<T extends AbstractEntity> extends AbstractEntityReview<T, ICentreDomainTreeManager> {

    private static final long serialVersionUID = 8538099803371092525L;

    public GridAnalysisView(final GridAnalysisModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
    }

    @Override
    protected void initView() {
	setLayout(new MigLayout("fill, insets 0","[fill, grow]","[fill, grow]"));
	add(new EgiPanel<T>(getModel().getGridModel(), false));
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

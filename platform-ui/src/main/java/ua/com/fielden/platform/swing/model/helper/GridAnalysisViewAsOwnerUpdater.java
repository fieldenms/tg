package ua.com.fielden.platform.swing.model.helper;

import java.awt.event.ActionEvent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.model.IUmViewOwner;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;

/**
 *
 * Custom view owner that is used to refresh a corresponding entity centre when entity is saved on master.
 *
 * @author TG Team
 */
public class GridAnalysisViewAsOwnerUpdater implements IUmViewOwner {

    private final GridAnalysisView<?, ?> analysisView;

    public GridAnalysisViewAsOwnerUpdater(final GridAnalysisView<?, ?> analysisView) {
	this.analysisView = analysisView;
    }

    @Override
    public <T extends AbstractEntity<?>> void notifyEntityChange(final T entity) {
	new BlockingLayerCommand<Void>("wrapper", analysisView.getBlockingLayer()) {
	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		setMessage("Updating...");
		analysisView.getModel().reExecuteAnalysisQuery();
		setMessage("Completed");
		return null;
	    }
	}.actionPerformed(null);
    }

}

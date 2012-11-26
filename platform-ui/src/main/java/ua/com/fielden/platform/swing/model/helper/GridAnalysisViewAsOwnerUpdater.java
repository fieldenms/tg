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
    private final Runnable runnable;

    public GridAnalysisViewAsOwnerUpdater(final GridAnalysisView<?, ?> analysisView) {
	this.analysisView = analysisView;
	this.runnable = null;
    }

    /**
     * Similar as the above constructor, but in addition accepts {@link Runnable} implementing custom logic that gets executed once the analysis view is updated.
     *
     * @param analysisView
     * @param runnable
     */
    public GridAnalysisViewAsOwnerUpdater(final GridAnalysisView<?, ?> analysisView, final Runnable runnable) {
	this.analysisView = analysisView;
	this.runnable = runnable;
    }

    @Override
    public <T extends AbstractEntity<?>> void notifyEntityChange(final T entity) {
	new BlockingLayerCommand<Void>("wrapper", analysisView.getBlockingLayer()) {
	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		setMessage("Updating...");
		analysisView.getModel().reExecuteAnalysisQuery();
		if (runnable != null) {
		    setMessage("Executing post action...");
		    runnable.run();
		}
		setMessage("Completed");
		return null;
	    }
	}.actionPerformed(null);
    }

}

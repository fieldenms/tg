package ua.com.fielden.platform.swing.review.report.centre;

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationView;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView;
import ua.com.fielden.platform.swing.review.report.events.LoadEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ILoadListener;

public abstract class AbstractSingleAnalysisEntityCentre<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractEntityCentre<T, CDTME> {

    private static final long serialVersionUID = -7393061848126429375L;

    private int analysisCounter;

    private GridConfigurationView<T, CDTME> gridConfigurationView;

    private boolean wasHierarchyChanged;
    private boolean wasAnalysisLoaded;

    public AbstractSingleAnalysisEntityCentre(final AbstractEntityCentreModel<T, CDTME> model, final AbstractConfigurationView<? extends AbstractEntityCentre<T, CDTME>, ?> owner) {
	super(model, owner);
	this.analysisCounter = 0;
	this.gridConfigurationView = null;
	this.wasHierarchyChanged = false;
	this.wasAnalysisLoaded = false;
	addHierarchyListener(createComponentWasShown());
    }

    @Override
    public List<AbstractAnalysisConfigurationView<T, CDTME, ?, ?, ?>> getVisibleAnalysisList() {
	final List<AbstractAnalysisConfigurationView<T, CDTME, ?, ?, ?>> analysisList = new ArrayList<AbstractAnalysisConfigurationView<T,CDTME,?,?,?>>();
	analysisList.add(gridConfigurationView);
	return analysisList;
    }

    /**
     * Adds the {@link ILoadListener} to the specified {@link GridConfigurationView}. That "load listener" determines when the specified component was loaded.
     * Also if this component wasn't loaded yet it fires load event for this {@link AbstractConfigurationView} instance.
     * 
     * @param component
     */
    private void addLoadListenerToAnalysis(final GridConfigurationView<T, CDTME> configView) {
	configView.addLoadListener(new ILoadListener() {

	    @Override
	    public void viewWasLoaded(final LoadEvent event) {
		synchronized (AbstractSingleAnalysisEntityCentre.this) {
		    // should child load event be handled?
		    if (!wasAnalysisLoaded) {
			// yes, so this one is first, lets handle it and set flag
			// to indicate that we won't handle any more
			// child load events.
			wasAnalysisLoaded = true;

			//The child was loaded so lets see whether this component was resized if that is true then fire
			//event that this was loaded.
			if(wasHierarchyChanged){
			    fireLoadEvent(new LoadEvent(AbstractSingleAnalysisEntityCentre.this));
			}
			// after this handler end its execution, lets remove it
			// from component because it is already not-useful
			final ILoadListener refToThis = this;
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
				configView.removeLoadListener(refToThis);
			    }
			});
		    }
		}
	    }
	});

    }

    @Override
    public JComponent getReviewPanel() {
	return getReviewProgressLayer();
    }

    final void createReview() {
	final BlockingIndefiniteProgressLayer reviewProgressLayer = getReviewProgressLayer();
	final GridConfigurationModel<T, CDTME> configModel = new GridConfigurationModel<T, CDTME>(getModel().getCriteria());
	gridConfigurationView = new GridConfigurationView<T, CDTME>(configModel, this, reviewProgressLayer);
	addLoadListenerToAnalysis(gridConfigurationView);
	reviewProgressLayer.setView(gridConfigurationView);
	setCurrentAnalysisConfigurationView(gridConfigurationView);
	analysisCounter++;
	gridConfigurationView.open();
    }

    /**
     * Creates the {@link HierarchyListener} that determines when the component was shown and it's size was determined.
     * Also if child component was also loaded then it fires the load event.
     * 
     * @return
     */
    private HierarchyListener createComponentWasShown() {
	return new HierarchyListener() {

	    @Override
	    public void hierarchyChanged(final HierarchyEvent e) {
		synchronized (AbstractSingleAnalysisEntityCentre.this) {
		    // should hierarchy change event be handled?
		    if (((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) == HierarchyEvent.SHOWING_CHANGED)
			    && !wasHierarchyChanged) {
			// yes, so this one is first, lets handle it and set flag
			// to indicate that we won't handle any more
			// hierarchy changed events
			wasHierarchyChanged = true;

			//The component was resized so lets see whether analysis was loaded if that is true then fire
			//event that this component was loaded.
			if(wasAnalysisLoaded){
			    fireLoadEvent(new LoadEvent(AbstractSingleAnalysisEntityCentre.this));
			}
			// after this handler end its execution, lets remove it
			// from component because it is already not-useful
			final HierarchyListener refToThis = this;
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
				removeHierarchyListener(refToThis);
			    }
			});
		    }
		}
	    }
	};
    }

}

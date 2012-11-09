package ua.com.fielden.platform.swing.review.report.centre;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyListener;

import javax.swing.JComponent;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationView;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView;
import ua.com.fielden.platform.swing.review.report.events.LoadEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.ILoadListener;

public abstract class AbstractSingleAnalysisEntityCentre<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractEntityCentre<T, CDTME> {

    private static final long serialVersionUID = -7393061848126429375L;

    private GridConfigurationView<T, CDTME> gridConfigurationView;

    private boolean wasResized;
    private boolean wasAnalysisLoaded;

    public AbstractSingleAnalysisEntityCentre(final AbstractEntityCentreModel<T, CDTME> model, final AbstractConfigurationView<? extends AbstractEntityCentre<T, CDTME>, ?> owner) {
	super(model, owner);
	this.gridConfigurationView = null;
	this.wasResized = false;
	this.wasAnalysisLoaded = false;
	createReview();
	addComponentListener(createComponentWasResized());
    }

    @Override
    public JComponent getReviewPanel() {
	return getReviewProgressLayer();
    }

    @Override
    public void selectAnalysis(final String name) {
	throw new UnsupportedOperationException("This entity centre has only one analysis that is always selected. No need to select it twice!");
    }

    @Override
    public boolean isLoaded() {
        return wasResized && wasAnalysisLoaded;
    }

    @Override
    public void close() {
	wasResized = false;
	wasAnalysisLoaded = false;
	setSize(new Dimension(0, 0));
	super.close();
    }

    /**
     * Returns the single analysis view.
     *
     * @return
     */
    public GridConfigurationView<T, CDTME> getSingleAnalysis(){
	return gridConfigurationView;
    }

    /**
     * Creates single grid analysis view.
     *
     * @return
     */
    protected GridConfigurationView<T, CDTME> createGridAnalysis(){
	final GridConfigurationModel<T, CDTME> configModel = new GridConfigurationModel<T, CDTME>(getModel().getCriteria());
	return GridConfigurationView.createMainDetailsWithDefaultCustomiser(configModel, this, getReviewProgressLayer());
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
        		if(wasResized){
        		    fireLoadEvent(new LoadEvent(AbstractSingleAnalysisEntityCentre.this));
        		}
        	    }
        	}
            }
        });

    }

    /**
     * Creates the main centre's review panel.
     *
     */
    private void createReview() {
	final BlockingIndefiniteProgressLayer reviewProgressLayer = getReviewProgressLayer();
	gridConfigurationView = createGridAnalysis();
	addLoadListenerToAnalysis(gridConfigurationView);
	reviewProgressLayer.setView(gridConfigurationView);
	setCurrentAnalysisConfigurationView(gridConfigurationView);
	gridConfigurationView.open();
    }

    /**
     * Creates the {@link HierarchyListener} that determines when the component was shown and it's size was determined.
     * Also if child component was also loaded then it fires the load event.
     *
     * @return
     */
    private ComponentListener createComponentWasResized() {
	return new ComponentAdapter() {

	    @Override
	    public void componentResized(final ComponentEvent e) {
		synchronized (AbstractSingleAnalysisEntityCentre.this) {
		    // should size change event be handled?
		    if (!wasResized) {
			// yes, so this one is first, lets handle it and set flag
			// to indicate that we won't handle any more
			// size changed events
			wasResized = true;

			//The component was resized so lets see whether analysis was loaded if that is true then fire
			//event that this component was loaded.
			if(wasAnalysisLoaded){
			    fireLoadEvent(new LoadEvent(AbstractSingleAnalysisEntityCentre.this));
			}
		    }
		}
	    }
	};
    }

}

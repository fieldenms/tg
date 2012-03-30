package ua.com.fielden.platform.swing.review.report.configuration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JOptionPane;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.dialogs.DialogWithDetails;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.events.ReviewEvent;
import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;
import ua.com.fielden.platform.swing.review.report.events.WizardEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.IReview;
import ua.com.fielden.platform.swing.review.report.interfaces.IReviewEventListener;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectable;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectionEventListener;
import ua.com.fielden.platform.swing.review.report.interfaces.IWizard;
import ua.com.fielden.platform.swing.review.report.interfaces.IWizardEventListener;
import ua.com.fielden.platform.swing.view.BasePanel;

/**
 * The holder for wizard and view panels. Provides functionality that allows one to switch view between report and wizard modes.
 * 
 * @author TG Team
 *
 * @param <VT>
 * @param <WT>
 */
public abstract class AbstractConfigurationView<VT extends BasePanel & IReview, WT extends BasePanel & IWizard> extends BasePanel implements ISelectable{

    private static final long serialVersionUID = 362789325125491283L;

    private final AbstractConfigurationModel model;
    private final BlockingIndefiniteProgressLayer progressLayer;

    /**
     * Holds the previous wizard and view of the report.
     */
    private WT previousWizard = null;
    private VT previousView = null;

    /**
     * Initiates this {@link AbstractConfigurationView} with associated {@link AbstractConfigurationModel}.
     * 
     * @param model
     */
    public AbstractConfigurationView(final AbstractConfigurationModel model, final BlockingIndefiniteProgressLayer progressLayer){
	super(new MigLayout("fill, insets 0", "[fill, grow]","[fill, grow]"));
	this.model = model;
	this.progressLayer = progressLayer;
	model.addPropertyChangeListener(createModeChangeListener());
    }

    /**
     * Returns the previous configurable review. If this configuration panel is in the report mode then this method returns currently visible entity review.
     * 
     * @return
     */
    public VT getPreviousView() {
	return previousView;
    }

    /**
     * Returns the previous wizard view. If this configuration panel is in the wizard mode then this method returns currently visible wizard.
     * 
     * @return
     */
    public WT getPreviousWizard() {
	return previousWizard;
    }

    /**
     * Returns the associated {@link AbstractConfigurationModel}.
     * 
     * @return
     */
    public AbstractConfigurationModel getModel() {
	return model;
    }

    @Override
    public String getInfo() {
        return "Abstract configuration panel";
    }

    @Override
    public void addSelectionEventListener(final ISelectionEventListener l) {
	listenerList.add(ISelectionEventListener.class, l);
    }

    @Override
    public void removeSelectionEventListener(final ISelectionEventListener l) {
	listenerList.remove(ISelectionEventListener.class, l);
    }

    /**
     * Selects this {@link AbstractConfigurationModel} and fires {@link SelectionEvent}.
     */
    public void select(){
	fireSelectionEvent(new SelectionEvent(this));
    }

    /**
     * Returns the progress layer for the associated {@link AbstractConfigurationView}.
     * 
     * @return
     */
    public final BlockingIndefiniteProgressLayer getProgressLayer() {
        return progressLayer;
    }

    /**
     * Opens this {@link AbstractConfigurationView}. First it tries to open this in {@link ReportMode#REPORT} mode, if it fails, then it opens in {@link ReportMode#WIZARD} mode.
     */
    public final void open(){

	final Result res = getModel().canSetMode(ReportMode.REPORT);
	if(res.isSuccessful()){
	    try{
		getModel().setMode(ReportMode.REPORT);
		return;
	    }catch(final Exception e){
		new DialogWithDetails(null, "Exception while opening report view", e).setVisible(true);
	    }
	}
	try{
	    getModel().setMode(ReportMode.WIZARD);
	    return;
	}catch(final Exception e){
	    new DialogWithDetails(null, "Exception while opening wizard view", e).setVisible(true);

	}
	try {
	    getModel().setMode(ReportMode.NOT_SPECIFIED);
	} catch (final Exception e) {
	    new DialogWithDetails(null, "Exception while opening wizard view", e).setVisible(true);
	}
    }

    /**
     * Notifies all registered {@link ISelectionEventListener} that this configuration model was selected.
     * 
     * @param event
     */
    protected final void fireSelectionEvent(final SelectionEvent event){
        for(final ISelectionEventListener listener : listenerList.getListeners(ISelectionEventListener.class)){
            listener.viewWasSelected(event);
        }
    }

    /**
     * Override this to provide custom report view.
     * 
     * @param configurableView - view to configure.
     * @return
     */
    protected VT initConfigurableView(final VT configurableView){
        if(configurableView != null){
            configurableView.addReviewEventListener(createReviewListener());
        }
        return configurableView;
    }

    /**
     * Override this to provide custom wizard to configure report.
     * 
     * @param wizardView - wizard view to configure
     * @return
     */
    protected WT initWizardView(final WT wizardView){
        if(wizardView != null){
            wizardView.addWizardEventListener(createWizardListener());
        }
        return wizardView;
    }

    /**
     * Creates listener that listens mode changed event.
     * 
     * @return
     */
    private PropertyChangeListener createModeChangeListener(){
	return new PropertyChangeListener() {

	    @Override
	    public void propertyChange(final PropertyChangeEvent evt) {
		if("mode".equals(evt.getPropertyName())){
		    final ReportMode mode = (ReportMode)evt.getNewValue();
		    switch(mode){
		    case WIZARD:
			previousWizard = initWizardView(null);
			setView(previousWizard);
			break;
		    case REPORT:
			previousView = initConfigurableView(null);
			setView(previousView);
			break;
		    case NOT_SPECIFIED:
			setView(null);
			break;
		    }
		}

	    }

	};
    }



    /**
     * Creates the {@link IReviewEventListener} for entity review, that listens the review specific actions.
     *
     * @return
     */
    private IReviewEventListener createReviewListener() {
	return new IReviewEventListener() {

	    @Override
	    public boolean configureActionPerformed(final ReviewEvent e) {
		switch(e.getReviewAction()){
		case PRE_CONFIGURE:
		    final Result res = getModel().canSetMode(ReportMode.WIZARD);
		    if(res.isSuccessful()){
			return true;
		    } else {
			new DialogWithDetails(null, "Exception while opening wizard view", res.getEx()).setVisible(true);
			return false;
		    }
		case POST_CONFIGURE:
		    try {
			getModel().setMode(ReportMode.WIZARD);
		    } catch (final Exception e1) {
			e1.printStackTrace();
		    }
		    break;
		}
		return true;
	    }
	};
    }

    /**
     * Creates the {@link IWizardEventListener} for entity review wizard, that listens the wizard specific actions.
     * 
     * @return
     */
    private IWizardEventListener createWizardListener() {
	return new IWizardEventListener() {
	    @Override
	    public boolean wizardActionPerformed(final WizardEvent e) {
		switch (e.getWizardAction()) {
		case PRE_BUILD:
		    final Result setModeRes = getModel().canSetMode(ReportMode.REPORT);
		    if(!setModeRes.isSuccessful()){
			JOptionPane.showMessageDialog(AbstractConfigurationView.this, setModeRes.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
			return false;
		    }
		    break;
		case POST_CANCEL:
		case POST_BUILD:
		    try {
			getModel().setMode(ReportMode.REPORT);
		    } catch (final Exception e1) {
			e1.printStackTrace();
		    }
		    break;
		}
		return true;
	    }
	};
    }

    /**
     * Set the current view for this panel: wizard or configurable review.
     * 
     * @param component
     */
    private <ST extends BasePanel & ISelectable> void setView(final ST component){
	removeAll();
	if(component != null){
	    add(component);
	    component.select();
	}
	invalidate();
	validate();
	repaint();
    }

}

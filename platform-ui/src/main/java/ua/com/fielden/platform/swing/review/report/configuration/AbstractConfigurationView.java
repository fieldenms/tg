package ua.com.fielden.platform.swing.review.report.configuration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.dialogs.DialogWithDetails;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationModel.CanNotSetModeException;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationModel.UndefinedFormatException;
import ua.com.fielden.platform.swing.review.report.interfaces.IReview;
import ua.com.fielden.platform.swing.review.report.interfaces.IWizard;
import ua.com.fielden.platform.swing.view.BasePanel;

/**
 * The holder for wizard and view panels. Provides functionality that allows one to switch view between report and wizard modes.
 * 
 * @author TG Team
 *
 * @param <VT>
 * @param <WT>
 */
public abstract class AbstractConfigurationView<VT extends BasePanel & IReview, WT extends BasePanel & IWizard> extends BasePanel {

    private static final long serialVersionUID = 362789325125491283L;

    private final AbstractConfigurationModel model;
    private final BlockingIndefiniteProgressLayer progressLayer;

    /**
     * Initiates this {@link AbstractConfigurationView} with associated {@link AbstractConfigurationModel}.
     * 
     * @param model
     */
    public AbstractConfigurationView(final AbstractConfigurationModel model, final BlockingIndefiniteProgressLayer progressLayer){
	super(new MigLayout("fill, insets 0", "[fill, grow]"));
	this.model = model;
	this.progressLayer = progressLayer;
	model.addPropertyChangeListener(createModeChangeListener());
    }

    /**
     * Returns the associated {@link AbstractConfigurationModel}.
     * 
     * @return
     */
    public AbstractConfigurationModel getModel() {
	return model;
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
	try{
	    getModel().setMode(ReportMode.REPORT);
	    return;
	}catch(final UndefinedFormatException e){
	    //TODO this is optional. This type of exception might be removed later.
	    JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	}catch (final CanNotSetModeException e){
	    //Doesn't do anything: opened for the first time.
	}catch(final Exception e){
	    new DialogWithDetails(null, "Exception while opening report view", e).setVisible(true);
	}
	try{
	    getModel().setMode(ReportMode.WIZARD);
	}catch(final Exception e){
	    new DialogWithDetails(null, "Exception while opening wizard view", e).setVisible(true);
	}
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
			setView(createWizardView());
			break;
		    case REPORT:
			setView(createConfigurableView());
			break;
		    }
		}

	    }
	};
    }

    /**
     * Override this to provide custom report view.
     * 
     * @return
     */
    protected abstract VT createConfigurableView();

    /**
     * Override this to provide custom wizard to configure report.
     * 
     * @return
     */
    protected abstract WT createWizardView();

    /**
     * Set the current view for this panel: wizard or configurable review.
     * 
     * @param component
     */
    private void setView(final JComponent component){
	removeAll();
	add(component);
	invalidate();
	validate();
	repaint();
    }

    @Override
    public String getInfo() {
	return "Abstract configuration panel";
    }

}

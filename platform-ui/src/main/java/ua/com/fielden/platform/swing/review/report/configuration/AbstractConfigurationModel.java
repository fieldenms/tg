package ua.com.fielden.platform.swing.review.report.configuration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventListener;

import javax.swing.event.EventListenerList;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.events.WizardCancelledEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.IWizardCancelledEventListener;

public abstract class AbstractConfigurationModel {

    /**
     * Holds all registered {@link EventListener}s.
     */
    private final EventListenerList listenerList;

    /**
     * Determines the current report's mode. There are two possible report modes: WIZARD, REPORT.
     */
    private ReportMode mode;

    /**
     * Default constructor. Initiates with undefined {@link ReportMode}.
     */
    public AbstractConfigurationModel(){
	this.mode = null;
	listenerList = new EventListenerList();
    }

    /**
     * Returns
     * 
     * @return
     */
    public final ReportMode getMode() {
	return mode;
    }

    /**
     * This cancels the modifications made in the wizard and set the REPORT mode if it's possible.
     * 
     * @param mode
     */
    final void cancelWizardModification() throws Exception {
	if(ReportMode.REPORT.equals(mode)){
	    return;
	}
	final Result setModeResult = canSetMode(ReportMode.REPORT);
	if(setModeResult.isSuccessful()){
	    this.mode = ReportMode.REPORT;
	    fireWizardModificationCancelledEvent(new WizardCancelledEvent(this));
	}else{
	    throw setModeResult.getEx();
	}
    }

    /**
     * Set the specified mode for this {@link AbstractConfigurationModel}.
     * 
     * @param mode
     */
    final void setMode(final ReportMode mode) throws Exception {
	if(mode == null || mode.equals(this.mode)){
	    return;
	}
	final Result setModeResult = canSetMode(mode);
	if(setModeResult.isSuccessful()){
	    final Object oldValue = this.mode;
	    this.mode = mode;
	    firePropertyChangeEvent(new PropertyChangeEvent(this, "mode", oldValue, this.mode));
	}else{
	    throw setModeResult.getEx();
	}
    }

    /**
     * See {@link EventListenerList#add(Class, java.util.EventListener)}.
     * 
     * @param l
     */
    public void addWizardCancelledEventListener(final IWizardCancelledEventListener l){
	this.listenerList.add(IWizardCancelledEventListener.class, l);
    }

    /**
     * See {@link EventListenerList#remove(Class, java.util.EventListener)}.
     * 
     * @param l
     */
    public void removeWizardCancelledEventListener(final IWizardCancelledEventListener l){
	this.listenerList.remove(IWizardCancelledEventListener.class, l);
    }

    /**
     * See {@link EventListenerList#add(Class, java.util.EventListener)}.
     * 
     * @param l
     */
    public void addPropertyChangeListener(final PropertyChangeListener l){
	this.listenerList.add(PropertyChangeListener.class, l);
    }

    /**
     * See {@link EventListenerList#remove(Class, java.util.EventListener)}.
     * 
     * @param l
     */
    public void removePropertyChangeListener(final PropertyChangeListener l){
	this.listenerList.remove(PropertyChangeListener.class, l);
    }

    /**
     * Notifies all {@link PropertyChangeListener}s that specific property has changed it's value.
     * 
     * @param event
     */
    protected final void firePropertyChangeEvent(final PropertyChangeEvent event) {
	for(final PropertyChangeListener listener : listenerList.getListeners(PropertyChangeListener.class)){
	    listener.propertyChange(event);
	}
    }

    /**
     * Notifies all the {@link IWizardCancelledEventListener}s that the wizard cancelled it's changes.
     * 
     * @param event
     */
    protected final void fireWizardModificationCancelledEvent(final WizardCancelledEvent event){
	for(final IWizardCancelledEventListener listener : listenerList.getListeners(IWizardCancelledEventListener.class)){
	    listener.wizardCancelled(event);
	}
    }

    /**
     * Return the {@link Result} that indicates whether specified mode can be set or not.
     * 
     * @param mode
     * @return
     */
    abstract protected Result canSetMode(final ReportMode mode);

    /**
     * {@link RuntimeException} that indicates that entity centre has changed it's format therefore can not be restored.
     */
    public static class UndefinedFormatException extends Exception{

	private static final long serialVersionUID = -3336587780299617875L;

	/**
	 * Default constructor.
	 */
	public UndefinedFormatException(){
	}

	/**
	 * Initiates this {@link UndefinedFormatException} instance with specific message.
	 * 
	 * @param message
	 */
	public UndefinedFormatException(final String message){
	    super(message);
	}
    }

    /**
     * {@link RuntimeException} that indicates that specific {@link ReportMode} can not be set.
     * 
     * @author TG Team
     *
     */
    public static class CanNotSetModeException extends Exception{

	private static final long serialVersionUID = -1405371553894125638L;

	/**
	 * Default constructor.
	 */
	public CanNotSetModeException(){
	}

	/**
	 * Initiates this {@link CanNotSetModeException} instance with specific message.
	 * 
	 * @param message
	 */
	public CanNotSetModeException(final String message){
	    super(message);
	}

    }
}

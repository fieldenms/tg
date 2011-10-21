package ua.com.fielden.platform.swing.review.report.centre;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.EventListenerList;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.report.ReportMode;

public abstract class AbstractConfigurationModel {

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
     * Set the specified mode for this {@link AbstractConfigurationModel}.
     * 
     * @param mode
     */
    final void setMode(final ReportMode mode) throws Exception{
	if(mode == null || mode.equals(this.mode)){
	    return;
	}
	final Result setModeResult = canSetMode(mode);
	if(setModeResult.isSuccessful()){
	    final Object oldValue = this.mode;
	    this.mode = mode;
	    processPropertyChangeEvent(new PropertyChangeEvent(this, "mode", oldValue, this.mode));
	}else{
	    throw setModeResult.getEx();
	}
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
    protected final void processPropertyChangeEvent(final PropertyChangeEvent event) {
	// Guaranteed to return a non-null array
	final Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==PropertyChangeListener.class) {
		((PropertyChangeListener)listeners[i+1]).propertyChange(event);
	    }
	}

    }

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

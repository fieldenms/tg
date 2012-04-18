package ua.com.fielden.platform.swing.review.report.configuration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventListener;

import javax.swing.event.EventListenerList;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.report.ReportMode;

public abstract class AbstractConfigurationModel {

    /**
     * Holds all registered {@link EventListener}s.
     */
    protected final EventListenerList listenerList;

    /**
     * Determines the current report's mode. There are two possible report modes: WIZARD, REPORT.
     */
    private ReportMode mode;

    /**
     * Default constructor. Initiates with undefined {@link ReportMode}.
     */
    public AbstractConfigurationModel(){
	this.mode = ReportMode.NOT_SPECIFIED;
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
     * Set the specified mode for this {@link AbstractConfigurationModel}. Use {@link #canSetMode(ReportMode)} to determine whether specific mode can be set.
     * 
     * @param mode
     */
    public final void setMode(final ReportMode mode){
	if(mode == null){
	    throw new IllegalArgumentException("The null mode can not be set");
	}
	final Object oldValue = this.mode;
	this.mode = mode;
	firePropertyChangeEvent(new PropertyChangeEvent(this, "mode", oldValue, this.mode));
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
    private void firePropertyChangeEvent(final PropertyChangeEvent event) {
	for(final PropertyChangeListener listener : listenerList.getListeners(PropertyChangeListener.class)){
	    listener.propertyChange(event);
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

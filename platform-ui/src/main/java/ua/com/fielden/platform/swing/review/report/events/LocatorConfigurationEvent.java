package ua.com.fielden.platform.swing.review.report.events;

import java.util.EventObject;

import ua.com.fielden.platform.swing.review.report.centre.configuration.LocatorConfigurationView;

public class LocatorConfigurationEvent extends EventObject{

    private static final long serialVersionUID = -5129768169248668026L;

    public enum LocatorConfigurationAction{
	PRE_SAVE, SAVE, POST_SAVE, SAVE_FAILED,
	PRE_SAVE_AS_DEFAULT, SAVE_AS_DEFAULT, POST_SAVE_AS_DEFAULT, SAVE_AS_DEFAULT_FAILED,
	PRE_LOAD_DEFAULT, LOAD_DEFAULT, POST_LOAD_DEFAULT, LOAD_DEFAULT_FAILED;
    }

    private final LocatorConfigurationAction eventAction;

    public LocatorConfigurationEvent(final LocatorConfigurationView<?, ?> source, final LocatorConfigurationAction eventAction) {
	super(source);
	this.eventAction = eventAction;
    }

    @Override
    public LocatorConfigurationView<?, ?> getSource() {
	return (LocatorConfigurationView<?, ?>)super.getSource();
    }

    public LocatorConfigurationAction getEventAction() {
	return eventAction;
    }
}

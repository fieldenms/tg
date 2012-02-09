package ua.com.fielden.platform.swing.review.report.events;

import java.util.EventObject;

import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationModel;

public class AnalysisConfigurationEvent extends EventObject{

    private static final long serialVersionUID = -5129768169248668026L;

    public enum AnalysisConfigurationAction{
	PRE_SAVE, SAVE, POST_SAVE, SAVE_FAILED,
	PRE_REMOVE, REMOVE, POST_REMOVE, REMOVE_FAILED;
    }

    private final AnalysisConfigurationAction eventAction;

    public AnalysisConfigurationEvent(final AbstractAnalysisConfigurationModel<?, ?> source, final AnalysisConfigurationAction eventAction) {
	super(source);
	this.eventAction = eventAction;
    }

    @Override
    public AbstractAnalysisConfigurationModel<?, ?> getSource() {
	return (AbstractAnalysisConfigurationModel<?, ?>)super.getSource();
    }

    public AnalysisConfigurationAction getEventAction() {
	return eventAction;
    }
}

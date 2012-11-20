package ua.com.fielden.platform.swing.review.report.events;

import java.util.EventObject;

import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;

public class AnalysisConfigurationEvent extends EventObject{

    private static final long serialVersionUID = -5129768169248668026L;

    public enum AnalysisConfigurationAction{
	PRE_SAVE, SAVE, POST_SAVE, SAVE_FAILED,
	PRE_REMOVE, REMOVE, POST_REMOVE, REMOVE_FAILED;
    }

    private final AnalysisConfigurationAction eventAction;

    public AnalysisConfigurationEvent(final AbstractAnalysisConfigurationView<?, ?, ?, ?> source, final AnalysisConfigurationAction eventAction) {
	super(source);
	this.eventAction = eventAction;
    }

    @Override
    public AbstractAnalysisConfigurationView<?, ?, ?, ?> getSource() {
	return (AbstractAnalysisConfigurationView<?, ?, ?, ?>)super.getSource();
    }

    public AnalysisConfigurationAction getEventAction() {
	return eventAction;
    }
}

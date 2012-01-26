package ua.com.fielden.platform.swing.review.report.analysis.configuration;

import ua.com.fielden.platform.swing.pagination.model.development.PageHolder;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationModel;

public abstract class AbstractAnalysisConfigurationModel extends AbstractConfigurationModel {

    /**
     * The page holder for this analysis.
     */
    private final PageHolder pageHolder;

    public AbstractAnalysisConfigurationModel(){
	this.pageHolder = new PageHolder();
    }

    /**
     * Returns the {@link PageHolder} instance for this analysis configuration view.
     * 
     * @return
     */
    public PageHolder getPageHolder() {
	return pageHolder;
    }
}

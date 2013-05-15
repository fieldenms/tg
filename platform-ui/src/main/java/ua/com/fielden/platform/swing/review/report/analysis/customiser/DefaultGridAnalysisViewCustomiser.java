package ua.com.fielden.platform.swing.review.report.analysis.customiser;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;

/**
 * The grid analysis view customiser that would be used to customise analysis view if another analysis view customiser wasn't specified.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <CDTME>
 */
public class DefaultGridAnalysisViewCustomiser<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> implements IAnalysisViewCustomiser<GridAnalysisView<T, CDTME>> {

    @Override
    public void customiseView(final GridAnalysisView<T, CDTME> analysisView) {

    }

}

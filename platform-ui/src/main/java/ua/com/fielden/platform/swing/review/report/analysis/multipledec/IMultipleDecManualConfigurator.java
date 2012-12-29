package ua.com.fielden.platform.swing.review.report.analysis.multipledec;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IMultipleDecDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract that allows one to configure multiple dec analysis.
 *
 * @author TG Team
 *
 */
public interface IMultipleDecManualConfigurator<T extends AbstractEntity<?>> {

    /**
     * Configures multiple dec analysis.
     *
     * @param cdtme
     * @param multipleDec
     * @param root
     */
    void configureMultipleDecAnalysis(ICentreDomainTreeManagerAndEnhancer cdtme, IMultipleDecDomainTreeManager multipleDec, Class<T> root);
}

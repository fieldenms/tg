package ua.com.fielden.platform.report.query.generation;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.utils.IDates;

public class ManualGridAnalysisQueryGenerator<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends GridAnalysisQueryGenerator<T, CDTME> {

    private final String linkProperty;
    private final Object linkPropertyValue;

    public ManualGridAnalysisQueryGenerator(final Class<T> root, final CDTME cdtme, final String linkProperty, final Object linkPropertyValue, final IDates dates) {
        super(root, cdtme, dates);
        this.linkProperty = linkProperty;
        this.linkPropertyValue = linkPropertyValue;
    }

    @Override
    public ICompleted<T> createQuery() {
        return where(super.createQuery()).prop(property(linkProperty)).//
        /*  */eq().val(linkPropertyValue);
    }
}

package ua.com.fielden.platform.report.query.generation;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

public class ManualGridAnalysisQueryGenerator<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends GridAnalysisQueryGenerator<T, CDTME> {

    private final String linkProperty;
    private final Object linkPropertyValue;

    public ManualGridAnalysisQueryGenerator(final Class<T> root, final CDTME cdtme, final String linkProperty, final Object linkPropertyValue) {
	super(root, cdtme);
	this.linkProperty = linkProperty;
	this.linkPropertyValue = linkPropertyValue;
    }

    @Override
    public EntityResultQueryModel<T> createQueryModel() {
	return where(createBaseQueryModel()).prop(property(linkProperty)).//
        /*  */eq().val(linkPropertyValue).model();
    }
}

package ua.com.fielden.platform.report.query.generation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteriaUtils;
import ua.com.fielden.platform.utils.EntityUtils;

public class ReportQueryGenerationUtils {

    /**
     * Converts existing properties model (which has separate properties for from/to, is/isNot and so on)
     * into new properties model (which has single abstraction for one criterion).
     *
     * @param properties
     * @return
     */
    public static <T extends AbstractEntity<?>> List<QueryProperty> createQueryProperties(final Class<T> root, final ICentreDomainTreeManagerAndEnhancer cdtme) {
        final List<QueryProperty> queryProperties = new ArrayList<QueryProperty>();
        for (final String actualProperty : cdtme.getFirstTick().checkedProperties(root)) {
            if (!AbstractDomainTree.isPlaceholder(actualProperty)) {
        	queryProperties.add(createQueryProperty(actualProperty, root, cdtme));
            }
        }
        return queryProperties;
    }

    /**
     * Converts existing properties model (which has separate properties for from/to, is/isNot and so on)
     * into new properties model (which has single abstraction for one criterion).
     *
     * @param properties
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T extends AbstractEntity<?>> QueryProperty createQueryProperty(final String actualProperty, final Class<T> root, final ICentreDomainTreeManagerAndEnhancer cdtme) {
	final IAddToCriteriaTickManager tickManager = cdtme.getFirstTick();
	final Class<T> managedType = (Class<T>)cdtme.getEnhancer().getManagedType(root);
	final QueryProperty queryProperty = EntityQueryCriteriaUtils.createNotInitialisedQueryProperty(managedType, actualProperty);

	queryProperty.setValue(tickManager.getValue(root, actualProperty));
	if (AbstractDomainTree.isDoubleCriterionOrBoolean(managedType, actualProperty)) {
	    queryProperty.setValue2(tickManager.getValue2(root, actualProperty));
	}
	if (AbstractDomainTree.isDoubleCriterion(managedType, actualProperty)) {
	    queryProperty.setExclusive(tickManager.getExclusive(root, actualProperty));
	    queryProperty.setExclusive2(tickManager.getExclusive2(root, actualProperty));
	}
	final Class<?> propertyType = StringUtils.isEmpty(actualProperty) ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, actualProperty);
	if (EntityUtils.isDate(propertyType)) {
	    queryProperty.setDatePrefix(tickManager.getDatePrefix(root, actualProperty));
	    queryProperty.setDateMnemonic(tickManager.getDateMnemonic(root, actualProperty));
	    queryProperty.setAndBefore(tickManager.getAndBefore(root, actualProperty));
	}
	queryProperty.setOrNull(tickManager.getOrNull(root, actualProperty));
	queryProperty.setNot(tickManager.getNot(root, actualProperty));
	return queryProperty;
    }
}

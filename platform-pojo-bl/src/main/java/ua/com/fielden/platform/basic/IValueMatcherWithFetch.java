package ua.com.fielden.platform.basic;

import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.utils.EntityUtils.getPathsToLeafPropertiesOfEntityWithCompositeKey;
import static ua.com.fielden.platform.utils.EntityUtils.hasDescProperty;
import static ua.com.fielden.platform.utils.EntityUtils.isCompositeEntity;

import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * A contract for value matcher with custom fetch strategy.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IValueMatcherWithFetch<T extends AbstractEntity<?>> extends IValueMatcher<T> {

    /**
     * Return the provided custom fetch strategy for entity retrieval.
     *
     */
    fetch<T> getFetch();

    /**
     * Sets a custom fetch strategy for entity retrieval.
     */
    void setFetch(final fetch<T> fetchModel);

    /**
     * The same as {@link #findMatches(String)}, but uses a the provided custom fetch strategy when retrieving entities.
     *
     * @param value
     * @return
     */
    List<T> findMatchesWithModel(final String value);
    
    
    default OrderingModel createKeyBeforeDescOrderingModel (Class<? extends AbstractEntity<?>> entityType, final String searchString) {
    	return orderBy().caseWhen().condition(createSearchByKeyCriteriaModel(entityType, searchString)).then().val(0).otherwise().val(1).endAsInt().asc().model();
    }
    
    static ConditionModel createSearchByKeyCriteriaModel(Class<? extends AbstractEntity<?>> entityType, final String searchString) {
    	
        if ("%".equals(searchString)) {
        	return cond().val(1).eq().val(1).model();
        }
    	
        ConditionModel keyCriteria = cond().prop(KEY).iLike().val(searchString).model(); 				
        		
        if (isCompositeEntity((Class<? extends AbstractEntity<?>>) entityType) ) {
        	for (String propName : getPathsToLeafPropertiesOfEntityWithCompositeKey(null, (Class<? extends AbstractEntity<DynamicEntityKey>>) entityType)) {
        		keyCriteria = cond().condition(keyCriteria).or().prop(propName).iLike().val(searchString).model();
			}
        }
    	
        return hasDescProperty(entityType) ? cond().condition(keyCriteria).or().prop(DESC).iLike().val("%" + searchString).model() : keyCriteria;
    }
}
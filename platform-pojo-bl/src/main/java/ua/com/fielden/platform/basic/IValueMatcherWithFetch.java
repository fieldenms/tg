package ua.com.fielden.platform.basic;

import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;

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
    
    
    default ConditionModel createSearchByKeyAndDescCondition(final String searchString) {
    	return cond().prop(KEY).iLike().val(searchString).or().prop(DESC).iLike().val(searchString).model();
    }
    
    default OrderingModel createKeyBeforeDescOrderingModel (final String searchString) {
    	return orderBy().caseWhen().prop(KEY).iLike().val(searchString).then().val(0).otherwise().val(1).endAsInt().asc().model();
    }
}

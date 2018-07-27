package ua.com.fielden.platform.basic;

import static ua.com.fielden.platform.basic.ValueMatcherUtils.createRelaxedSearchByKeyCriteriaModel;
import static ua.com.fielden.platform.basic.ValueMatcherUtils.createStrictSearchByKeyCriteriaModel;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.utils.EntityUtils.hasDescProperty;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

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

    default ExpressionModel makeSearchResultOrderingPriority(Class<? extends AbstractEntity<?>> entityType, final String searchString) {
        return hasDescProperty(entityType)
                ? expr().caseWhen().condition(createStrictSearchByKeyCriteriaModel(entityType, searchString)).then().val(0).when().condition(createRelaxedSearchByKeyCriteriaModel(searchString)).then().val(1).otherwise().val(2).endAsInt().model()
                : expr().caseWhen().condition(createStrictSearchByKeyCriteriaModel(entityType, searchString)).then().val(0).otherwise().val(1).endAsInt().model();
    }
}
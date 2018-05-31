package ua.com.fielden.platform.web.test.matchers;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;

import com.google.inject.Inject;

import ua.com.fielden.platform.basic.autocompleter.AbstractSearchEntityByKeyWithContext;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.sample.domain.ITgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;

/**
 * A demo context-dependent matcher that adds condition to the search query in
 * case some predicated holds for the provided context.
 *
 * @author TG Team
 *
 */
public class ContextMatcher extends
		AbstractSearchEntityByKeyWithContext<TgPersistentEntityWithProperties, TgPersistentEntityWithProperties> {

	@Inject
	public ContextMatcher(final ITgPersistentEntityWithProperties dao) {
		super(dao);
	}

	@Override
	protected ConditionModel makeSearchCriteriaModel(final TgPersistentEntityWithProperties context, final String searchString) {

		final ConditionModel originalSearchCriteria = super.makeSearchCriteriaModel(context, searchString);

		if (context.getIntegerProp() != null && context.getIntegerProp() > 50) {
			return cond().condition(originalSearchCriteria).and().prop("integerProp").ge().val(50).model();
		}
		return originalSearchCriteria;
	}
}
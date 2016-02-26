package ua.com.fielden.platform.web.test.matchers;

import ua.com.fielden.platform.basic.autocompleter.AbstractSearchEntityByKeyWithContext;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.ITgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;

import com.google.inject.Inject;

/**
 * A demo context-dependent matcher that adds condition to the search query in case some predicated holds for the provided context.
 *
 * @author TG Team
 *
 */
public class ContextMatcher extends AbstractSearchEntityByKeyWithContext<TgPersistentEntityWithProperties, TgPersistentEntityWithProperties> {

    @Inject
    public ContextMatcher(final ITgPersistentEntityWithProperties dao) {
        super(dao);
    }

    @Override
    protected EntityResultQueryModel<TgPersistentEntityWithProperties> completeEqlBasedOnContext(
            final TgPersistentEntityWithProperties context,
            final String searchString,
            final ICompoundCondition0<TgPersistentEntityWithProperties> incompleteEql) {
        if (context.getIntegerProp() != null && context.getIntegerProp() > 50) {
            return incompleteEql.and().prop("integerProp").ge().val(50).model();
        }
        return incompleteEql.model();
    }

}

package ua.com.fielden.platform.web.test.matchers;

import ua.com.fielden.platform.basic.IValueMatcherByDesc;
import ua.com.fielden.platform.basic.autocompleter.AbstractSearchEntityByKeyWithContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.ITgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;

import com.google.inject.Inject;

/**
 * A demo matcher that searches not only by key, but also by desc propert.
 *
 * @author TG Team
 *
 */
public class SearchAlsoByDescMatcher extends AbstractSearchEntityByKeyWithContext<TgPersistentEntityWithProperties, TgPersistentEntityWithProperties>
    implements IValueMatcherByDesc<TgPersistentEntityWithProperties>{

    @Inject
    public SearchAlsoByDescMatcher(final ITgPersistentEntityWithProperties dao) {
        super(dao);
    }

    @Override
    protected EntityResultQueryModel<TgPersistentEntityWithProperties> completeEqlBasedOnContext(
            final TgPersistentEntityWithProperties context,
            final String searchString,
            final ICompoundCondition0<TgPersistentEntityWithProperties> incompleteEql) {
        return incompleteEql.or().prop(AbstractEntity.DESC).iLike().val(searchString).model();
    }

}

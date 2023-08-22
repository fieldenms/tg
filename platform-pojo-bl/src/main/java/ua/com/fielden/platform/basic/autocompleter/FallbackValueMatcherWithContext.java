package ua.com.fielden.platform.basic.autocompleter;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.query.model.ConditionModel;

/**
 * This is a fall back implementation for {@link IValueMatcherWithContext}, which does not use a context. It simply performs the search by key and description, if applicable.
 * <p>
 * Also, in case of matching activatable entity values, only <code>active</code> ones are matched.
 *
 * @author TG Team
 *
 * @param <CONTEXT>
 * @param <T>
 */
public class FallbackValueMatcherWithContext<CONTEXT extends AbstractEntity<?>, T extends AbstractEntity<?>> extends AbstractSearchEntityByKeyWithContext<CONTEXT, T> {

    public final boolean activeOnlyByDefault;
    private boolean activeOnly;

    public FallbackValueMatcherWithContext(final IEntityDao<T> co, final boolean activeOnlyByDefault) {
        super(co);
        final Class<T> entityType = co.getEntityType();
        this.activeOnlyByDefault = activeOnlyByDefault;
        this.activeOnly = activeOnlyByDefault;
        if (activeOnlyByDefault && !ActivatableAbstractEntity.class.isAssignableFrom(entityType)) {
            final String entityTitle = getEntityTitleAndDesc(entityType).getKey();
            throw new EntityException(format("Activatable type is expected. Entity [%s] is not activatable.", entityTitle));
        }

    }

    @Override
    public Integer getPageSize() {
        return IEntityDao.DEFAULT_PAGE_CAPACITY;
    }

    @Override
    protected ConditionModel makeSearchCriteriaModel(final CONTEXT context, final String searchString) {

        final ConditionModel originalSearchCriteria = super.makeSearchCriteriaModel(context, searchString);
        return activeOnly ? cond().condition(originalSearchCriteria).and().prop(ACTIVE).eq().val(true).model() : originalSearchCriteria;
    }

    public boolean isActiveOnly() {
        return activeOnly;
    }

    public void setActiveOnly(final boolean activeOnly) {
        this.activeOnly = activeOnly;
    }

}
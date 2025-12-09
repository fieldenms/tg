package ua.com.fielden.platform.basic.autocompleter;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.query.model.ConditionModel;

import static java.lang.String.format;
import static ua.com.fielden.platform.basic.ValueMatcherUtils.createActiveOnlyCondition;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.utils.EntityUtils.isActivatableEntityOrUnionType;

/// This is a fallback implementation of [IValueMatcherWithContext], which does not use a context.
/// It simply performs the search by key and description, if applicable.
///
/// Also, when matching activatable entity values, only active ones are matched.
///
public class FallbackValueMatcherWithContext<CONTEXT extends AbstractEntity<?>, T extends AbstractEntity<?>> extends AbstractSearchEntityByKeyWithContext<CONTEXT, T> {

    /// The default setting to configure the matching logic for including/excluding inactive activatable entity values.
    /// It should be set to `true` only for activatable entities.
    /// Users have the ability to control inclusion/exclusion of inactive values by means of setting value for attribute `activeOnly`, which is exposed via UI.
    /// Refer to `EntityAutocompletionResource.post` for more details.
    ///
    public final boolean activeOnlyByDefault;
    private boolean activeOnly;

    public FallbackValueMatcherWithContext(final IEntityDao<T> co, final boolean activeOnlyByDefault) {
        super(co);
        final Class<T> entityType = co.getEntityType();
        this.activeOnlyByDefault = activeOnlyByDefault;
        this.activeOnly = activeOnlyByDefault;
        if (activeOnlyByDefault && !isActivatableEntityOrUnionType(entityType)) {
            throw new EntityException(format("Expected an activatable entity type or a union type with an activatable member, but received [%s] instead.".formatted(entityType.getSimpleName())));
        }
    }

    @Override
    public Integer getPageSize() {
        return IEntityDao.DEFAULT_PAGE_CAPACITY;
    }

    /// Creates a standard condition for querying Entity Master entity editor values.
    /// Takes into account the "active only" option.
    ///
    /// This method may be overridden to provide a different condition model for search criteria.
    ///
    @Override
    protected ConditionModel makeSearchCriteriaModel(final CONTEXT context, final String searchString) {
        final var originalSearchCriteria = super.makeSearchCriteriaModel(context, searchString);
        return activeOnly ? cond().condition(originalSearchCriteria).and().condition(createActiveOnlyCondition(getEntityType()).model()).model() : originalSearchCriteria;
    }

    public boolean isActiveOnly() {
        return activeOnly;
    }

    public void setActiveOnly(final boolean activeOnly) {
        this.activeOnly = activeOnly;
    }

}

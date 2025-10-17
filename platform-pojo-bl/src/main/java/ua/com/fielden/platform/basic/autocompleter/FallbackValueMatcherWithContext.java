package ua.com.fielden.platform.basic.autocompleter;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.query.model.ConditionModel;

import static java.lang.String.format;
import static ua.com.fielden.platform.basic.ValueMatcherUtils.createActiveOnlyCondition;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.utils.EntityUtils.isActivatableEntityOrUnionType;

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

    /**
     * The default setting to configure the matching logic for including/excluding inactive activatable entity values.
     * It should be set to {@code true} only for activatable entities.
     * Users have the ability to control inclusion/exclusion of inactive values by means of setting value for attribute {@code activeOnly}, which is exposed via UI.
     * Refer {@code EntityAutocompletionResource.post} for more details.
     */
    public final boolean activeOnlyByDefault;
    private boolean activeOnly;

    public FallbackValueMatcherWithContext(final IEntityDao<T> co, final boolean activeOnlyByDefault) {
        super(co);
        final Class<T> entityType = co.getEntityType();
        this.activeOnlyByDefault = activeOnlyByDefault;
        this.activeOnly = activeOnlyByDefault;
        if (activeOnlyByDefault && !isActivatableEntityOrUnionType(entityType)) {
            final String entityTitle = getEntityTitleAndDesc(entityType).getKey();
            throw new EntityException(format("Activatable entity type is expected (or union with activatable subtype). Entity [%s] does not conform to this condition.", entityTitle));
        }
    }

    @Override
    public Integer getPageSize() {
        return IEntityDao.DEFAULT_PAGE_CAPACITY;
    }

    /// Makes standard condition for querying Entity Master entity editor values.
    /// Takes into account 'active only' option.
    ///
    ///  This method may be overridden to create a different EQL condition model for search criteria.
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
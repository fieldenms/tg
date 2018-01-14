package ua.com.fielden.platform.basic.autocompleter;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * This is a fall back implementation for {@link IValueMatcherWithCentreContext}, which does not do anything with the provided context.
 * It simply performs the search by key operation.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class FallbackValueMatcherWithCentreContext<T extends AbstractEntity<?>> extends AbstractSearchEntityByKeyWithCentreContext<T> {

    private final Class<T> entityType;

    public FallbackValueMatcherWithCentreContext(final IEntityDao<T> co) {
        super(co);

        entityType = co.getEntityType();
    }

    @Override
    public Integer getPageSize() {
        return 10;
    }

    @Override
    protected ICompoundCondition0<T> startEqlBasedOnContext(
            final CentreContext<T, ?> context,
            final String searchString) {
        if (EntityUtils.hasDescProperty(entityType)) {
            return select(entityType).where()
                    .begin()
                    .prop(KEY).iLike().val(searchString).or()
                    .upperCase().prop(AbstractEntity.DESC).iLike().val("%" + searchString)
                    .end();
        } else {
            return select(entityType).where().prop(KEY).iLike().val(searchString);
        }
    }

}

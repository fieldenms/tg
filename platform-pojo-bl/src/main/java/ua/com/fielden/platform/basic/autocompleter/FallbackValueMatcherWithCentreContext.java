package ua.com.fielden.platform.basic.autocompleter;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * This is a fall back implementation for {@link IValueMatcherWithCentreContext}, which does not do anything with the provided context. It simply performs the search by key
 * operation.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class FallbackValueMatcherWithCentreContext<T extends AbstractEntity<?>> extends AbstractSearchEntityByKeyWithCentreContext<T> {

    public FallbackValueMatcherWithCentreContext(final IEntityDao<T> co) {
        super(co);
    }

    @Override
    public Integer getPageSize() {
        return IEntityDao.DEFAULT_PAGE_CAPACITY;
    }
}
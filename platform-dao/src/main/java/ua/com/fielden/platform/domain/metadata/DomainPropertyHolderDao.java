package ua.com.fielden.platform.domain.metadata;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO for {@link DomainPropertyHolder}.
 * 
 * @author TG Team
 */
@EntityType(DomainPropertyHolder.class)
public class DomainPropertyHolderDao extends CommonEntityDao<DomainPropertyHolder> implements DomainPropertyHolderCo {

    @Inject
    protected DomainPropertyHolderDao(final IFilter filter) {
        super(filter);
    }
}

package ua.com.fielden.platform.domain.metadata;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO for {@link DomainType}.
 * 
 * @author TG Team
 */
@EntityType(DomainType.class)
public class DomainTypeDao extends CommonEntityDao<DomainType> implements DomainTypeCo {

    @Inject
    protected DomainTypeDao(final IFilter filter) {
        super(filter);
    }
}

package ua.com.fielden.platform.domain.metadata;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO for {@link DomainProperty}.
 * 
 * @author TG Team
 */
@EntityType(DomainProperty.class)
public class DomainPropertyDao extends CommonEntityDao<DomainProperty> implements DomainPropertyCo {

    @Inject
    protected DomainPropertyDao(final IFilter filter) {
        super(filter);
    }
}

package ua.com.fielden.platform.domain.metadata;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO for {@link DomainExplorerInsertionPoint}.
 *
 * @author TG Team
 */
@EntityType(DomainExplorerInsertionPoint.class)
public class DomainExplorerInsertionPointDao extends CommonEntityDao<DomainExplorerInsertionPoint> implements DomainExplorerInsertionPointCo {

    @Inject
    protected DomainExplorerInsertionPointDao(final IFilter filter) {
        super(filter);
    }

}

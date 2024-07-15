package ua.com.fielden.platform.domain.metadata;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO for {@link DomainExplorer}.
 *
 * @author TG Team
 */
@EntityType(DomainExplorer.class)
public class DomainExplorerDao extends CommonEntityDao<DomainExplorer> implements DomainExplorerCo {

    @Inject
    protected DomainExplorerDao(final IFilter filter) {
        super(filter);
    }

}

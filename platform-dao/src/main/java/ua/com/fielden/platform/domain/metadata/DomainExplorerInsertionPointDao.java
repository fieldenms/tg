package ua.com.fielden.platform.domain.metadata;

import java.util.List;

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

    @Override
        public DomainExplorerInsertionPoint save(final DomainExplorerInsertionPoint entity) {
            if (entity.getDomainTypeName() == null) {
                entity.setLoadedHierarchy(loadTypes(entity));
            } else {
                entity.setLoadedHierarchy(loadProperties(entity));
            }
            return super.save(entity);
    }

    private List<Long> loadProperties(final DomainExplorerInsertionPoint entity) {
        // TODO Auto-generated method stub
        return null;
    }

    private List<Long> loadTypes(final DomainExplorerInsertionPoint entity) {
        // TODO Auto-generated method stub
        return null;
    }
}

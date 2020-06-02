package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * The DAO implementation for  {@link IReferenceLevelHierarchyEntry}
 *
 * @author TG Team
 *
 */
@EntityType(ReferenceLevelHierarchyEntry.class)
public class ReferenceLevelHierarchyEntryDao extends CommonEntityDao<ReferenceLevelHierarchyEntry> implements IReferenceLevelHierarchyEntry {

    @Inject
    protected ReferenceLevelHierarchyEntryDao(final IFilter filter) {
        super(filter);
    }

}

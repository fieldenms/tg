package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(ReferencedByLevelHierarchyEntry.class)
public class ReferencedByLevelHierarchyEntryDao extends CommonEntityDao<ReferencedByLevelHierarchyEntry> implements IReferencedByLevelHierarchyEntry {

    @Inject
    protected ReferencedByLevelHierarchyEntryDao(final IFilter filter) {
        super(filter);
    }

}

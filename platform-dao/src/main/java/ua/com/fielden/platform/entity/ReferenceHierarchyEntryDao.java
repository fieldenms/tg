package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for {@link IReferenceHierarchyEntry}
 *
 * @author TG Team
 *
 */
@EntityType(ReferenceHierarchyEntry.class)
public class ReferenceHierarchyEntryDao extends CommonEntityDao<ReferenceHierarchyEntry> implements IReferenceHierarchyEntry {

    @Inject
    protected ReferenceHierarchyEntryDao(final IFilter filter) {
        super(filter);
    }

}

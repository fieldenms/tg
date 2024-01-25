package ua.com.fielden.platform.ref_hierarchy;

import ua.com.fielden.platform.dao.IEntityDao;

/**
 * Companion object for entity {@link ReferenceHierarchy}.
 *
 * @author TG Team
 *
 */
public interface IReferenceHierarchy extends IEntityDao<ReferenceHierarchy> {

    String ERR_ENTITY_HAS_NO_REFERENCES = "This entity has no references.";

}
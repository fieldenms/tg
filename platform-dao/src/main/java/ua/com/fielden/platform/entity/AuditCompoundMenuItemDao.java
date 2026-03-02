package ua.com.fielden.platform.entity;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.web.interfaces.IAuditMenuItemInitializer;

/// DAO implementation for the {@link AuditCompoundMenuItemCo} companion object.
///
@EntityType(AuditCompoundMenuItem.class)
public class AuditCompoundMenuItemDao extends CommonEntityDao<AuditCompoundMenuItem> implements AuditCompoundMenuItemCo {

    private final IAuditMenuItemInitializer menuItemInitializer;

    @Inject
    AuditCompoundMenuItemDao(final IAuditMenuItemInitializer menuItemInitializer) {
        this.menuItemInitializer = menuItemInitializer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AuditCompoundMenuItem save(final AuditCompoundMenuItem entity) {
        final var auditType = (Class<? extends AbstractEntity<?>>) ClassesRetriever.findClass(entity.getKey().getEntityType());
        return super.save(menuItemInitializer.init(auditType, entity));
    }

}

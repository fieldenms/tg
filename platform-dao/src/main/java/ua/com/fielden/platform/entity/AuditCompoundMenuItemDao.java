package ua.com.fielden.platform.entity;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.web.interfaces.IAuditMenuItemInitializer;

/// DAO implementation for companion object {@link AuditCompoundMenuItemCo}.
///
@EntityType(AuditCompoundMenuItem.class)
public class AuditCompoundMenuItemDao extends CommonEntityDao<AuditCompoundMenuItem> implements AuditCompoundMenuItemCo{

    private final IAuditMenuItemInitializer menuItemInitializer;

    @Inject
    public AuditCompoundMenuItemDao(final IAuditMenuItemInitializer menuItemInitializer) {
        this.menuItemInitializer = menuItemInitializer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AuditCompoundMenuItem save(final AuditCompoundMenuItem entity) {
        var auditType = (Class<? extends AbstractEntity<?>>)ClassesRetriever.findClass(entity.getKey().getEntityType());
        return super.save(menuItemInitializer.init(auditType, entity));
    }

}

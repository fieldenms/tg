package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.compound_master_menu.AuditCompoundMenuItem_CanAccess_Token;

/// DAO implementation for companion object {@link AuditCompoundMenuItemCo}.
///
@EntityType(AuditCompoundMenuItem.class)
public class AuditCompoundMenuItemDao extends CommonEntityDao<AuditCompoundMenuItem> implements AuditCompoundMenuItemCo{

    @Override
    @Authorise(AuditCompoundMenuItem_CanAccess_Token.class)
    public AuditCompoundMenuItem save(final AuditCompoundMenuItem entity) {
        return super.save(entity);
    }

}

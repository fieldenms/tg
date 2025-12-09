package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.dao.IEntityDao;

public interface CopyUserRoleActionCo extends IEntityDao<CopyUserRoleAction> {

    String ERR_EMPTY_SELECTION = "Please select at least one %s and try again.".formatted(UserRole.ENTITY_TITLE);

}

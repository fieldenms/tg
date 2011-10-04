package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.validation.annotation.DefaultController;

/**
 * Represents a concept of a user role. Multiple users may have the same role. At this stage user role has only key and description.
 * <p>
 * It is also envisaged that multiple roles can be associated with one user. This would provide a flexible facility for configuring user permissions.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("Role Title")
@DescTitle("Description")
@MapEntityTo("USER_ROLE")
@DefaultController(IUserRoleDao.class)
public class UserRole extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    protected UserRole() {
    }

    public UserRole(final String key, final String desc) {
	super(null, key, desc);
    }
}

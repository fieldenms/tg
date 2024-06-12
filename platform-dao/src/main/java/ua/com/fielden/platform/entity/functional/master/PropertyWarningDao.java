package ua.com.fielden.platform.entity.functional.master;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.user.SecurityTokenInfo;

/**
 * DAO implementation for companion object {@link PropertyWarningCo}.
 *
 * @author Developers
 */
@EntityType(SecurityTokenInfo.class)
public class PropertyWarningDao extends CommonEntityDao<PropertyWarning> implements PropertyWarningCo {

}

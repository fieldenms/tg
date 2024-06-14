package ua.com.fielden.platform.security.user;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.user.SecurityTokenInfoCo;
import ua.com.fielden.platform.security.user.SecurityTokenInfo;

/** 
 * DAO implementation for companion object {@link SecurityTokenInfoCo}.
 * 
 * @author TG Team
 *
 */
@EntityType(SecurityTokenInfo.class)
public class SecurityTokenInfoDao extends CommonEntityDao<SecurityTokenInfo> implements SecurityTokenInfoCo {

    @Inject
    public SecurityTokenInfoDao(final IFilter filter) {
        super(filter);
    }

}
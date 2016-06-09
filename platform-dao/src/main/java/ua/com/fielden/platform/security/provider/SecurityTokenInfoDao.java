package ua.com.fielden.platform.security.provider;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.user.ISecurityTokenInfo;
import ua.com.fielden.platform.security.user.SecurityTokenInfo;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/** 
 * DAO implementation for companion object {@link ISecurityTokenInfo}.
 * 
 * @author Developers
 *
 */
@EntityType(SecurityTokenInfo.class)
public class SecurityTokenInfoDao extends CommonEntityDao<SecurityTokenInfo> implements ISecurityTokenInfo {
    @Inject
    public SecurityTokenInfoDao(final IFilter filter) {
        super(filter);
    }

}
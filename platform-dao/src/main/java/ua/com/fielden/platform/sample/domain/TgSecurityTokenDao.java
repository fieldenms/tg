package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/** 
 * DAO implementation for companion object {@link ITgSecurityToken}.
 * 
 * @author Developers
 *
 */
@EntityType(TgSecurityToken.class)
public class TgSecurityTokenDao extends CommonEntityDao<TgSecurityToken> implements ITgSecurityToken {
    @Inject
    public TgSecurityTokenDao(final IFilter filter) {
        super(filter);
    }

}
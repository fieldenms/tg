package ua.com.fielden.platform.entity;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.functional.PersistentEntityInfo_CanExecute_Token;

/**
 * DAO implementation for companion object {@link PersistentEntityInfoCo}.
 *
 * @author TG Team
 */
@EntityType(PersistentEntityInfo.class)
public class PersistentEntityInfoDao extends CommonEntityDao<PersistentEntityInfo> implements PersistentEntityInfoCo {

    @Inject
    public PersistentEntityInfoDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    @Authorise(PersistentEntityInfo_CanExecute_Token.class)
    public PersistentEntityInfo save(final PersistentEntityInfo entity) {
        return super.save(entity);
    }

}

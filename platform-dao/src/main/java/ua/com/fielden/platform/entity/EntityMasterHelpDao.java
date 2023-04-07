package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.persistent.EntityMasterHelp_CanSave_Token;

/**
 * DAO implementation for companion object {@link EntityMasterHelpCo}.
 *
 * @author Developers
 *
 */
@EntityType(EntityMasterHelp.class)
public class EntityMasterHelpDao extends CommonEntityDao<EntityMasterHelp> implements EntityMasterHelpCo {

    @Inject
    public EntityMasterHelpDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    @Authorise(EntityMasterHelp_CanSave_Token.class)
    public EntityMasterHelp save(final EntityMasterHelp entity) {
        return super.save(entity);
    }

    @Override
    protected IFetchProvider<EntityMasterHelp> createFetchProvider() {
        return FETCH_PROVIDER;
    }
}
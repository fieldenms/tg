package ua.com.fielden.platform.security.user;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAndInstrument;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ICollectionModificationController;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.web.centre.CentreContext;

public class UserRoleTokensUpdaterController implements ICollectionModificationController<UserRole, UserRoleTokensUpdater, String> {
    private final EntityFactory factory;
    private final SecurityTokenProvider securityTokenProvider;
    private final IEntityDao<UserRole> coUserRole;
    private final IEntityDao<UserRoleTokensUpdater> coUserRoleTokensUpdater;
    
    public UserRoleTokensUpdaterController(final EntityFactory factory, final IApplicationSettings applicationSettings, final IEntityDao<UserRole> coUserRole, final IEntityDao<UserRoleTokensUpdater> coUserRoleTokensUpdater) {
        this.factory = factory;
        this.securityTokenProvider = new SecurityTokenProvider(applicationSettings.pathToSecurityTokens(), applicationSettings.securityTokensPackageName());
        this.coUserRole = coUserRole;
        this.coUserRoleTokensUpdater = coUserRoleTokensUpdater;
    }
    
    @Override
    public AbstractEntity<?> getMasterEntityFromContext(final CentreContext<?, ?> context) {
        // this producer is suitable for property actions on User master and for actions on User centre
        return context.getMasterEntity() == null ? context.getCurrEntity() : context.getMasterEntity();
    }
    
    @Override
    public UserRole refetchMasterEntity(final AbstractEntity<?> masterEntityFromContext) {
        return coUserRole.findById(masterEntityFromContext.getId(), coUserRole.getFetchProvider().with("tokens").fetchModel());
    }
    
    @Override
    public UserRoleTokensUpdater refetchActionEntity(final Long masterEntityId) {
        final UserRoleTokensUpdater refetchedAction = coUserRoleTokensUpdater.getEntity(
            from(select(UserRoleTokensUpdater.class).where().prop(AbstractEntity.KEY).eq().val(masterEntityId).model())
            .with(fetchAndInstrument(UserRoleTokensUpdater.class).with(AbstractEntity.KEY))
            .model()
        );
        if (refetchedAction != null) {
            refetchedAction.setTokens(UserRoleTokensUpdaterProducer.loadAvailableTokens(securityTokenProvider, factory));
        }
        return refetchedAction;
    }
    
}

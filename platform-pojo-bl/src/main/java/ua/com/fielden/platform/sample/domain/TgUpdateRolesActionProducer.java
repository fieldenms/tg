package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;

/**
 * A producer for new instances of entity {@link TgUpdateRolesAction}.
 *
 * @author TG Team
 *
 */
public class TgUpdateRolesActionProducer extends DefaultEntityProducerWithContext<TgUpdateRolesAction, TgUpdateRolesAction> implements IEntityProducer<TgUpdateRolesAction> {
    private final Logger logger = Logger.getLogger(getClass());
    private final IUserRoleDao coUserRole;
    private final ITgUpdateRolesAction companion;
    
    @Inject
    public TgUpdateRolesActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder, final IUserRoleDao coUserRole, final ITgUpdateRolesAction companion) {
        super(factory, TgUpdateRolesAction.class, companionFinder);
        this.coUserRole = coUserRole;
        this.companion = companion;
    }

    @Override
    protected TgUpdateRolesAction provideDefaultValues(final TgUpdateRolesAction entity) {
        if (getCentreContext() != null) {
            entity.setContext(getCentreContext());

            final User user = (User) entity.getContext().getMasterEntity();
            if (user.isDirty()) {
                throw Result.failure("This action is applicable only to a saved entity! Please save entity and try again!");
            }
            
            // IMPORTANT: it is necessary to reset state for "key" property after its change.
            //   This is necessary to make the property marked as 'not changed from original' (origVal == val == 'DEMO') to be able not to re-apply afterwards
            //   the initial value against "key" property
            entity.setKey(user);
            entity.getProperty(AbstractEntity.KEY).resetState();
            
            final TgUpdateRolesAction persistedEntity = retrieveActionFor(user, companion);
            
            // IMPORTANT: it is necessary not to reset state for "surrogateVersion" property after its change.
            //   This is necessary to leave the property marked as 'changed from original' (origVal == null) to be able to apply afterwards
            //   the initial value against '"surrogateVersion", that was possibly changed by another user'
            entity.setSurrogateVersion(surrogateVersion(persistedEntity));
            logger.error("surrogate version after modification == " + entity.getSurrogateVersion());

            final List<UserRole> allAvailableRoles = coUserRole.findAll();
            final Set<UserRole> roles = new LinkedHashSet<>(allAvailableRoles);
            entity.setRoles(roles);
            
            final Set<Long> chosenRoleIds = new LinkedHashSet<>();
            for (final UserAndRoleAssociation association: user.getRoles()) {
                chosenRoleIds.add(association.getUserRole().getId());
            }
            entity.setChosenIds(chosenRoleIds);
        }
        
        return entity;
    }
    
    public static <T extends AbstractEntity<?>> Long surrogateVersion(final T persistedEntity) {
        return persistedEntity == null ? 99L : (persistedEntity.getVersion() + 100L);
    }
    
    public static TgUpdateRolesAction retrieveActionFor(final User user, final ITgUpdateRolesAction coTgUpdateRolesAction) {
        return coTgUpdateRolesAction.getEntity(from(select(TgUpdateRolesAction.class).where().prop(AbstractEntity.KEY).eq().val(user).model()).with(fetch(TgUpdateRolesAction.class).with("key")).model());
    }

}
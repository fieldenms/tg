package ua.com.fielden.platform.security.user;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.AbstractFunctionalEntityProducerForCollectionModification;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * A producer for new instances of entity {@link UserRolesUpdater}.
 *
 * @author TG Team
 *
 */
public class UserRolesUpdaterProducer extends AbstractFunctionalEntityProducerForCollectionModification<User, UserRolesUpdater> implements IEntityProducer<UserRolesUpdater> {
    private final IUserRoleDao coUserRole;
    private final IUser coUser;
    
    @Inject
    public UserRolesUpdaterProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder, final IUserRoleDao coUserRole, final IUser coUser) {
        super(factory, UserRolesUpdater.class, companionFinder);
        this.coUserRole = coUserRole;
        this.coUser = coUser;
    }
    
    @Override
    protected UserRolesUpdater provideCurrentlyAssociatedValues(final UserRolesUpdater entity, final User masterEntity) {
        final List<UserRole> allAvailableRoles = coUserRole.findAll();
        final Set<UserRole> roles = new LinkedHashSet<>(allAvailableRoles);
        entity.setRoles(roles);
        entity.getProperty("roles").resetState();
        
        final Set<Long> chosenRoleIds = new LinkedHashSet<>();
        for (final UserAndRoleAssociation association: masterEntity.getRoles()) {
            chosenRoleIds.add(association.getUserRole().getId());
        }
        entity.setChosenIds(chosenRoleIds);
        return entity;
    }
    
    @Override
    protected AbstractEntity<?> getMasterEntityFromContext(final CentreContext<?, ?> context) {
        // this producer is suitable for property actions on User master and for actions on User centre
        return context.getMasterEntity() == null ? context.getCurrEntity() : context.getMasterEntity();
    }

    @Override
    protected fetch<User> fetchModelForMasterEntity() {
        return coUser.getFetchProvider().fetchModel();
    }
}
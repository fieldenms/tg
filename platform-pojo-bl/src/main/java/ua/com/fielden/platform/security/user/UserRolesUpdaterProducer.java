package ua.com.fielden.platform.security.user;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.dao.IUserRole;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModificationProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.user.UserReviewToken;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * A producer for new instances of entity {@link UserRolesUpdater}.
 *
 * @author TG Team
 *
 */
public class UserRolesUpdaterProducer extends AbstractFunctionalEntityForCollectionModificationProducer<User, UserRolesUpdater> implements IEntityProducer<UserRolesUpdater> {
    private final IUserRole coUserRole;
    private final IUser coUser;
    
    @Inject
    public UserRolesUpdaterProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder, final IUserRole coUserRole, final IUser coUser) {
        super(factory, UserRolesUpdater.class, companionFinder);
        this.coUserRole = coUserRole;
        this.coUser = coUser;
    }
    
    @Override
    @Authorise(UserReviewToken.class)
    protected UserRolesUpdater provideCurrentlyAssociatedValues(final UserRolesUpdater entity, final User masterEntity) {
        final Set<Long> chosenRoleIds = new LinkedHashSet<>(masterEntity.getRoles().stream().map(item -> item.getUserRole().getId()).collect(Collectors.toList()));
        
        final List<UserRole> allAvailableRoles = coUserRole.findAll();
        final Map<Boolean, List<UserRole>> chosenAndNot = allAvailableRoles.stream().collect(Collectors.partitioningBy(role -> chosenRoleIds.contains(role.getId())));
        
        // show chosen items first
        final Set<UserRole> roles = new LinkedHashSet<>();
        roles.addAll(chosenAndNot.get(true));
        roles.addAll(chosenAndNot.get(false));
        entity.setRoles(roles);
        
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
        return coUser.getFetchProvider().with("roles").fetchModel();
    }
}
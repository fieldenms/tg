package ua.com.fielden.platform.menu;

import static ua.com.fielden.platform.entity.CollectionModificationUtils.toMapById;
import static ua.com.fielden.platform.entity.CollectionModificationUtils.validateAction;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.tuples.T2;

/**
 * DAO implementation for {@link UserMenuVisibilityAssociatorCo}.
 *
 * @author TG Team
 *
 */
@EntityType(UserMenuVisibilityAssociator.class)
public class UserMenuVisibilityAssociatorDao extends CommonEntityDao<UserMenuVisibilityAssociator> implements UserMenuVisibilityAssociatorCo {

    private final IUserProvider userProvider;

    @Inject
    protected UserMenuVisibilityAssociatorDao(final IUserProvider userProvider) {
        this.userProvider = userProvider;
    }

    @Override
    @SessionRequired
    public UserMenuVisibilityAssociator save(final UserMenuVisibilityAssociator entity) {
        if (userProvider.getUser().isBase()) {
            final T2<UserMenuVisibilityAssociator, WebMenuItemInvisibility> actionAndUserBeingUpdated = validateAction(entity, this, Long.class, new UserMenuVisibilityAssociatorController(co(User.class), userProvider));
            final UserMenuVisibilityAssociator actionToSave = actionAndUserBeingUpdated._1;
            final Map<Object, User> availableUsers = toMapById(actionToSave.getUsers());

            final Set<WebMenuItemInvisibility> addedAssociations = new LinkedHashSet<>();
            for (final Long addedId : actionToSave.getAddedIds()) {
                addedAssociations.add(co$(WebMenuItemInvisibility.class).new_().setOwner(availableUsers.get(addedId)).setMenuItemUri(actionAndUserBeingUpdated._2.getMenuItemUri()));
            }

            final Set<WebMenuItemInvisibility> removedAssociations = new LinkedHashSet<>();
            for (final Long removedId : actionToSave.getRemovedIds()) {
                removedAssociations.add(co$(WebMenuItemInvisibility.class).new_().setOwner(availableUsers.get(removedId)).setMenuItemUri(actionAndUserBeingUpdated._2.getMenuItemUri()));
            }

            final UserMenuInvisibilityAssociationBatchActionCo co$ = co$(UserMenuInvisibilityAssociationBatchAction.class);
            co$.save(co$.new_().setSaveEntities(removedAssociations).setRemoveEntities(addedAssociations));
        }
        return super.save(entity);
    }

}

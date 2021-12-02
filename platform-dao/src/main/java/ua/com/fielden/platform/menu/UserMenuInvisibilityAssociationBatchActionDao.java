package ua.com.fielden.platform.menu;

import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link UserMenuInvisibilityAssociationBatchActionCo}.
 *
 * @author TG Team
 *
 */
@EntityType(UserMenuInvisibilityAssociationBatchAction.class)
public class UserMenuInvisibilityAssociationBatchActionDao extends CommonEntityDao<UserMenuInvisibilityAssociationBatchAction> implements UserMenuInvisibilityAssociationBatchActionCo {

    private final WebMenuItemInvisibilityCo coMenuItemInvisibility;

    @Inject
    protected UserMenuInvisibilityAssociationBatchActionDao(final IFilter filter, final WebMenuItemInvisibilityCo coMenuItemInvisibility) {
        super(filter);
        this.coMenuItemInvisibility = coMenuItemInvisibility;
    }

    /**
     * Saves and removes association specified in batch action.
     */
    @Override
    @SessionRequired
    public UserMenuInvisibilityAssociationBatchAction save(final UserMenuInvisibilityAssociationBatchAction entity) {
        processSaveAction(entity.getSaveEntities());
        coMenuItemInvisibility.deleteAssociation(entity.getRemoveEntities());
        return entity;
    }

    /**
     * Saves the set of given associations.
     *
     * @param associations
     */
    private void processSaveAction(final Set<WebMenuItemInvisibility> associations) {
        for (final WebMenuItemInvisibility association : associations) {
            coMenuItemInvisibility.save(association);
        }
    }
}

package ua.com.fielden.platform.menu;

import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(UserMenuInvisibilityAssociationBatchAction.class)
public class UserMenuInvisibilityAssociationBatchActionDao extends CommonEntityDao<UserMenuInvisibilityAssociationBatchAction> implements UserMenuInvisibilityAssociationBatchActionCo {

    private final IWebMenuItemInvisibility coMenuItemInvisibility;

    @Inject
    protected UserMenuInvisibilityAssociationBatchActionDao(final IFilter filter, final IWebMenuItemInvisibility coMenuItemInvisibility) {
        super(filter);
        this.coMenuItemInvisibility = coMenuItemInvisibility;
    }

    @Override
    @SessionRequired
    public UserMenuInvisibilityAssociationBatchAction save(final UserMenuInvisibilityAssociationBatchAction entity) {
        processSaveAction(entity.getSaveEntities());
        coMenuItemInvisibility.removeAssociation(entity.getRemoveEntities());
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

package ua.com.fielden.platform.sample.domain;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.AbstractFunctionalEntityProducerForCollectionModification;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.IUserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.security.UserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/** 
 * DAO implementation for companion object {@link ITgUpdateRolesAction}.
 * 
 * @author Developers
 *
 */
@EntityType(TgUpdateRolesAction.class)
public class TgUpdateRolesActionDao extends CommonEntityDao<TgUpdateRolesAction> implements ITgUpdateRolesAction {
    private final Logger logger = Logger.getLogger(getClass());
    private final IUserAndRoleAssociationBatchAction coUserAndRoleAssociationBatchAction;
    private final EntityFactory factory;
    
    @Inject
    public TgUpdateRolesActionDao(final IFilter filter, final IUserAndRoleAssociationBatchAction coUserAndRoleAssociationBatchAction, final EntityFactory factory) {
        super(filter);
        this.coUserAndRoleAssociationBatchAction = coUserAndRoleAssociationBatchAction;
        this.factory = factory;
    }
    
    @Override
    @SessionRequired
    public TgUpdateRolesAction save(final TgUpdateRolesAction action) {
        final TgUpdateRolesAction actionToSave = AbstractFunctionalEntityProducerForCollectionModification.validateAction(action, a -> a.getRoles(), this, factory, Long.class);
        
        // after all validations have passed -- the association changes could be saved:
        final User userBeingUpdated = action.getKey();
        final Map<Object, UserRole> availableRoles = AbstractFunctionalEntityProducerForCollectionModification.mapById(action.getRoles(), Long.class);
        
        final Set<UserAndRoleAssociation> addedAssociations = new LinkedHashSet<>();
        for (final Long addedId : action.getAddedIds()) {
            addedAssociations.add(new UserAndRoleAssociation(userBeingUpdated, availableRoles.get(addedId)));
        }

        final Set<UserAndRoleAssociation> removedAssociations = new LinkedHashSet<>();
        for (final Long removedId : action.getRemovedIds()) {
            removedAssociations.add(new UserAndRoleAssociation(userBeingUpdated, availableRoles.get(removedId)));
        }

        final UserAndRoleAssociationBatchAction batchAction = new UserAndRoleAssociationBatchAction();
        batchAction.setSaveEntities(addedAssociations);
        batchAction.setRemoveEntities(removedAssociations);
        coUserAndRoleAssociationBatchAction.save(batchAction);
        
        // after the association changes were successfully saved, the action should also be saved:
        final TgUpdateRolesAction saved = super.save(actionToSave);
        logger.error("saved.getVersion() = " + saved.getVersion());
        logger.error("saved.getSurrogateVersion() = " + saved.getSurrogateVersion());
        return saved;
    }
}
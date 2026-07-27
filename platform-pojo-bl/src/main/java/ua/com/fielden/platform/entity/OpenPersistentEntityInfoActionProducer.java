package ua.com.fielden.platform.entity;

import com.google.inject.Inject;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.functional.PersistentEntityInfo_CanExecute_Token;

/// A producer for new instances of the {@link OpenPersistentEntityInfoAction} entity.
///
public class OpenPersistentEntityInfoActionProducer extends AbstractProducerForOpenEntityMasterAction<PersistentEntityInfo, OpenPersistentEntityInfoAction> {

    @Inject
    OpenPersistentEntityInfoActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, PersistentEntityInfo.class, OpenPersistentEntityInfoAction.class, companionFinder);
    }

    @Override
    @Authorise(PersistentEntityInfo_CanExecute_Token.class)
    protected OpenPersistentEntityInfoAction provideDefaultValues(final OpenPersistentEntityInfoAction openAction) {
        if (currentEntityNotEmpty()) {
            final PersistentEntityInfoCo co = co(PersistentEntityInfo.class);
            openAction.setKey(co.initialise(currentEntity(), co.new_()));
            openAction.setSectionTitle(openAction.getKey().getEntityTitle());
            return openAction;
        }
        // This happens when the entity master gets closed.
        else {
            return super.provideDefaultValues(openAction);
        }
    }
}

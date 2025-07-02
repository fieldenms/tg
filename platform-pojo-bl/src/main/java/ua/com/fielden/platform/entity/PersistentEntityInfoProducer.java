package ua.com.fielden.platform.entity;

import com.google.inject.Inject;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.functional.PersistentEntityInfo_CanExecute_Token;

/// Producer of {@link PersistentEntityInfo} that retrieve the versioning information for an entity that is passed as the "current entity" in the context.
///
public class PersistentEntityInfoProducer extends DefaultEntityProducerWithContext<PersistentEntityInfo> {

    @Inject
    public PersistentEntityInfoProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, PersistentEntityInfo.class, companionFinder);
    }

    @Override
    @Authorise(PersistentEntityInfo_CanExecute_Token.class)
    protected PersistentEntityInfo provideDefaultValues(final PersistentEntityInfo entity) {
        if (currentEntityNotEmpty()) {
            final AbstractEntity<?> currEntity = currentEntity();
            final PersistentEntityInfoCo infoEntityCo = co(PersistentEntityInfo.class);
            return infoEntityCo.initEntityWith(currEntity, entity);
        }
        // This happens when the entity master gets closed.
        else {
            return super.provideDefaultValues(entity);
        }
    }
}

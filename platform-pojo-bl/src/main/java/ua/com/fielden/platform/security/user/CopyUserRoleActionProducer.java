package ua.com.fielden.platform.security.user;

import com.google.inject.Inject;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

import static ua.com.fielden.platform.error.Result.failure;

public class CopyUserRoleActionProducer extends DefaultEntityProducerWithContext<CopyUserRoleAction> {

    @Inject
    protected CopyUserRoleActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, CopyUserRoleAction.class, companionFinder);
    }

    @Override
    protected CopyUserRoleAction provideDefaultValues(final CopyUserRoleAction entity) {
        if (contextNotEmpty()) {
            if (selectedEntitiesEmpty()) {
                throw failure("Please select at least one %s and try again.".formatted(UserRole.ENTITY_TITLE));
            }

            entity.setSelectedIds(selectedEntityIds());
            entity.setRoleActive(true);
        }

        return super.provideDefaultValues(entity);
    }

}

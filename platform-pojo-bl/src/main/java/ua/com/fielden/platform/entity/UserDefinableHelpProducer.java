package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * Producer for {@link UserDefinableHelp} entity
 *
 * @author TG Team
 *
 */
public class UserDefinableHelpProducer extends DefaultEntityProducerWithContext<UserDefinableHelp> {

    @Inject
    public UserDefinableHelpProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, UserDefinableHelp.class, companionFinder);
    }

    @Override
    protected UserDefinableHelp provideDefaultValues(final UserDefinableHelp entity) {
        if (masterEntityInstanceOf(OpenEntityMasterHelpAction.class)) {
            final OpenEntityMasterHelpAction masterEntity = masterEntity(OpenEntityMasterHelpAction.class);

            final UserDefinableHelpCo entityMasterHelpCo = co$(UserDefinableHelp.class);
            final UserDefinableHelp persistedEntity = entityMasterHelpCo.findByKeyAndFetch(entityMasterHelpCo.getFetchProvider().fetchModel(), masterEntity.getEntityType());

            if (persistedEntity != null) {
                return persistedEntity;
            } else {
                entity.setReferenceElement(masterEntity.getEntityType());
            }
        }
        return entity;
    }
}

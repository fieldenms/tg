package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * Producer of {@link OpenEntityMasterHelpAction} entity to create or edit help hyperlink for entity master.
 *
 * @author TG Team
 *
 */
public class OpenEntityMasterHelpActionProducer extends DefaultEntityProducerWithContext<OpenEntityMasterHelpAction> {

    public static final String ERR_HELP_MISSING = "Help doesn't exist";

    @Inject
    public OpenEntityMasterHelpActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, OpenEntityMasterHelpAction.class, companionFinder);
    }

    @Override
        protected OpenEntityMasterHelpAction provideDefaultValues(final OpenEntityMasterHelpAction entity) {
            if (currentEntityInstanceOf(AbstractEntity.class)) {
                final AbstractEntity<?> currEntity = currentEntity(AbstractEntity.class);
                final Class<AbstractEntity<?>> entityType = getOriginalType(currEntity.getType());
                entity.setEntityType(entityType.getName());
                entity.setSkipUi(this.chosenPropertyEmpty());
                if (entity.isSkipUi()) {
                    final UserDefinableHelpCo entityMasterHelpCo = co(UserDefinableHelp.class);
                    final UserDefinableHelp persistedEntity = entityMasterHelpCo.findByKeyAndFetch(entityMasterHelpCo.getFetchProvider().fetchModel(), entity.getEntityType());
                    if (persistedEntity != null) {
                        entity.setHelp(persistedEntity.getHelp());
                    } else {
                        throw new EntityException(ERR_HELP_MISSING);
                    }
                }
            }

            return entity;
        }
}

package ua.com.fielden.platform.entity;

import static java.lang.String.format;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
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
                    final EntityMasterHelpCo entityMasterHelpCo = co$(EntityMasterHelp.class);
                    final EntityMasterHelp persistedEntity = entityMasterHelpCo.findByKeyAndFetch(entityMasterHelpCo.getFetchProvider().fetchModel(), entity.getEntityType());
                    if (persistedEntity != null) {
                        entity.setHelp(persistedEntity.getHelp());
                    } else {
                        throw new EntityException(format("The help doesn't exists for %s entity", getEntityTitleAndDesc(entityType).getKey()));
                    }
                }
            }

            return entity;
        }
}

package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * Producer for {@link UserDefinableHelp} entity
 *
 * @author TG Team
 *
 */
public class UserDefinableHelpProducer extends DefaultEntityProducerWithContext<UserDefinableHelp> {

    public static final String ERR_HELP_MISSING = "Help doesn't exist";

    @Inject
    public UserDefinableHelpProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, UserDefinableHelp.class, companionFinder);
    }

    @Override
    protected UserDefinableHelp provideDefaultValues(final UserDefinableHelp entity) {
        final String refElement = getReferenceElement();
        final UserDefinableHelpCo entityMasterHelpCo = co$(UserDefinableHelp.class);
        final UserDefinableHelp persistedEntity = entityMasterHelpCo.findByKeyAndFetch(entityMasterHelpCo.getFetchProvider().fetchModel(), refElement);
        final boolean skipUi = this.chosenPropertyEmpty();
        if (persistedEntity != null) {
            if (skipUi) {
                entity.setReferenceElement(persistedEntity.getReferenceElement());
                entity.setHelp(persistedEntity.getHelp());
                entity.setSkipUi(true);
                return entity;
            }
            return persistedEntity;
        } else {
            if (skipUi) {
                throw new EntityException(ERR_HELP_MISSING);
            } else {
                entity.setReferenceElement(refElement);
                return entity;
            }
        }
    }

    private String getReferenceElement() {
        if (currentEntityInstanceOf(AbstractEntity.class)) {
            final Class<AbstractEntity<?>> entityType = getOriginalType(currentEntity(AbstractEntity.class).getType());
            return entityType.getName();
        } else {
            return getContext().getCustomObject().get("@@miType").toString();
        }
    }
}

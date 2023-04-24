package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.error.Result.failureEx;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Producer for {@link UserDefinableHelp} entity
 *
 * @author TG Team
 *
 */
public class UserDefinableHelpProducer extends DefaultEntityProducerWithContext<UserDefinableHelp> {

    public static final String ERR_HELP_MISSING = "There is no help to open.";

    @Inject
    public UserDefinableHelpProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, UserDefinableHelp.class, companionFinder);
    }

    @Override
    protected UserDefinableHelp provideDefaultValues(final UserDefinableHelp entity) {
        if (currentEntityInstanceOf(AbstractEntity.class)) {
            final AbstractEntity<?> currEntity = currentEntity(AbstractEntity.class);
            final Class<AbstractEntity<?>> entityType = getOriginalType(currEntity.getType());

            // let's restrict nested calls to UserDefinableHelp Master invocations related to UserDefinableHelp itself
            if (UserDefinableHelp.class.isAssignableFrom(entityType) && EntityUtils.equalsEx(((UserDefinableHelp) currEntity).getReferenceElement(), UserDefinableHelp.class.getName())) {
                throw failureEx("Nested help links are not supported.", "You are currently attempting to add a help link to a master, which represents a help link for a Help Master (i.e., this is a nested call).<br>Simply finish your changes to a help link, if any, and close the curent Help Master dialog.");
            }

            final UserDefinableHelpCo entityMasterHelpCo = co$(UserDefinableHelp.class);
            final UserDefinableHelp persistedEntity = entityMasterHelpCo.findByKeyAndFetch(entityMasterHelpCo.getFetchProvider().fetchModel(), entityType.getName());
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
                    entity.setReferenceElement(entityType.getName());
                    return entity;
                }
            }
        }
        return entity;
    }

}
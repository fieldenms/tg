package ua.com.fielden.platform.entity;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.error.Result.failureEx;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;

import java.util.Optional;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Producer for {@link UserDefinableHelp} entity
 *
 * @author TG Team
 *
 */
public class UserDefinableHelpProducer extends DefaultEntityProducerWithContext<UserDefinableHelp> {

    private final String defaultHelpUri;

    @Inject
    public UserDefinableHelpProducer(final @Named("help.defaultUri") String defaultHelpUri, final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, UserDefinableHelp.class, companionFinder);
        this.defaultHelpUri = defaultHelpUri;
    }

    @Override
    protected UserDefinableHelp provideDefaultValues(final UserDefinableHelp entity) {
       return getReferenceElement().map(refElement -> {

            // let's restrict nested calls to UserDefinableHelp Master invocations related to UserDefinableHelp itself
            if (currentEntityNotEmpty() && UserDefinableHelp.class.isAssignableFrom(getOriginalType(currentEntity(AbstractEntity.class).getType())) && EntityUtils.equalsEx(currentEntity(UserDefinableHelp.class).getReferenceElement(), UserDefinableHelp.class.getName())) {
                throw failureEx("Nested help links are not supported.", "You are currently attempting to add a help link to a master, which represents a help link for a Help Master (i.e., this is a nested call).<br>Simply finish your changes to a help link, if any, and close the curent Help Master dialog.");
            }

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
               entity.setHelp(new Hyperlink(defaultHelpUri));
               entity.setReferenceElement(refElement);
               entity.setSkipUi(skipUi);
               return entityMasterHelpCo.save(entity);
           }
       }).orElse(entity);
    }

    private Optional<String> getReferenceElement() {
        if (currentEntityInstanceOf(AbstractEntity.class)) {
            final Class<AbstractEntity<?>> entityType = getOriginalType(currentEntity(AbstractEntity.class).getType());
            return of(entityType.getName());
        } else if (getContext() != null){
            return of(getContext().getCustomObject().get("@@miType").toString());
        }
        return empty();
    }

}
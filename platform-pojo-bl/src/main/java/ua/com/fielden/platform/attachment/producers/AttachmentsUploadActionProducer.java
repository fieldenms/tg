package ua.com.fielden.platform.attachment.producers;

import static java.lang.String.format;
import static ua.com.fielden.platform.error.Result.failure;

import com.google.inject.Inject;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.AttachmentsUploadAction;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCompoundMenuItem;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;

/**
 * Producer of {@link AttachmentsUploadAction} that populates its master entity if that entity is present in the context.
 *
 * @author TG Team
 *
 */
public class AttachmentsUploadActionProducer extends DefaultEntityProducerWithContext<AttachmentsUploadAction> {

    @Inject
    public AttachmentsUploadActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, AttachmentsUploadAction.class, companionFinder);
    }

    @Override
    protected AttachmentsUploadAction provideDefaultValues(final AttachmentsUploadAction entity) {
        if (masterEntityNotEmpty() && masterEntityInstanceOf(AbstractEntity.class) &&
            // Entities that represent menu items should not be considered -- their key will be considered on the next branch
            !masterEntityInstanceOf(AbstractFunctionalEntityForCompoundMenuItem.class)) {
            // first assign the master entity
            final AbstractEntity<?> masterEntity = masterEntity();
            entity.setMasterEntity(masterEntity);

            // now let's analyse the chosen property
            // if chosen property is specified and it is of type Attachment then it is assumed that this property is to be modified as the result of the attachment uploading
            if (chosenPropertyNotEmpty()) {
                final MetaProperty<?> prop = masterEntity.getProperty(chosenProperty());
                if (Attachment.class.isAssignableFrom(prop.getType())) {
                    if (prop.isEditable()) {
                        entity.setChosenPropName(chosenProperty());
                    } else {
                        throw failure(format("%s is not editable.", prop.getTitle()));
                    }
                }
            }
        } else if (keyOfMasterEntityInstanceOf(AbstractEntity.class)) {
            entity.setMasterEntity(keyOfMasterEntity(AbstractEntity.class));
        } else if (selectedEntitiesOnlyOne()) {
            final AbstractEntity<?> selected = selectedEntities().get(0);
            entity.setMasterEntity(selected);
        }
        return entity;
    }
}
